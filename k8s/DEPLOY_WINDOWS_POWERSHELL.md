# Skytracker K8s 배포 가이드 - Windows PowerShell

Windows Intel 환경에서 Docker Desktop Kubernetes, kubectl, helm을 사용해 로컬 Kubernetes 클러스터에 배포하는 절차입니다.

> 이 문서는 PowerShell 기준입니다. 명령어 줄바꿈은 백틱 문자(`)를 사용합니다.
> 모든 명령은 프로젝트 루트 디렉터리에서 실행합니다.

## 사전 준비

- Windows Docker Desktop
- Docker Desktop Kubernetes 활성화
- kubectl
- helm
- PowerShell 또는 Windows Terminal

현재 kubectl context가 Docker Desktop인지 확인합니다.

```powershell
kubectl config current-context
kubectl get nodes
```

Docker Desktop Kubernetes를 사용할 경우 context는 보통 `docker-desktop`입니다.

```powershell
kubectl config use-context docker-desktop
```

## Helm Repo 등록

```powershell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add elastic https://helm.elastic.co
helm repo add strimzi https://strimzi.io/charts/
helm repo update
```

## 배포 순서

### 1. Namespace 생성

```powershell
kubectl apply -f k8s/namespaces.yaml
```

### 2. Secret 적용

`app-secret.yaml`은 gitignore 대상입니다. 새로 만들 때만 `app-secret.example.yaml`을 참고하고, 기존 로컬 `app-secret.yaml`은 덮어쓰지 않습니다.

MySQL과 Logstash가 `data` 네임스페이스의 `app-secret`을 참조하므로, 데이터 컴포넌트 배포 전에 Secret을 먼저 적용합니다.

```powershell
kubectl apply -f k8s/apps/app-secret.yaml
```

### 3. MySQL

```powershell
kubectl apply -f k8s/mysql/headless-service.yaml
kubectl apply -f k8s/mysql/statefulset.yaml
```

### 4. Redis - Bitnami Helm

```powershell
helm install redis bitnami/redis `
  -n data `
  -f k8s/redis/values-redis-ha.yaml
```

Sentinel 구성은 `master 1 + replica 3` 구조입니다.

### 5. Elasticsearch - ECK Operator

ECK Operator를 먼저 설치한 뒤 Elasticsearch, Kibana, Logstash를 배포합니다.

```powershell
# Operator 설치
helm install elastic-operator elastic/eck-operator `
  -n elastic-system `
  --create-namespace

# Operator Ready 대기
kubectl wait --for=condition=ready pod `
  -l app.kubernetes.io/name=elastic-operator `
  -n elastic-system `
  --timeout=120s

# ES 클러스터 배포
kubectl apply -f k8s/es/es.yaml -n data

# ES Ready 대기
kubectl wait elasticsearch/elastic `
  --for=jsonpath='{.status.health}'=green `
  -n data `
  --timeout=300s

# Kibana & Logstash
kubectl apply -f k8s/es/kibana.yaml -n data
kubectl apply -f k8s/es/logstash-configmap.yaml -n data
kubectl apply -f k8s/es/logstash-deployment.yaml -n data
```

ECK는 배포 시 Elasticsearch 비밀번호를 자동 생성합니다. `apps`, `data` 네임스페이스의 `app-secret`에 있는 `ES_PASSWORD`를 모두 동기화해야 합니다.

```powershell
# ECK 생성 비밀번호 조회 및 디코딩
$encodedEsPass = kubectl get secret elastic-es-elastic-user -n data -o jsonpath="{.data.elastic}"
$esPass = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($encodedEsPass))

# Secret patch에는 base64 인코딩 값이 들어가야 합니다.
$esPassBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($esPass))
$patch = @(
  @{
    op = "replace"
    path = "/data/ES_PASSWORD"
    value = $esPassBase64
  }
) | ConvertTo-Json -Compress

kubectl patch secret app-secret -n apps --type="json" -p $patch
kubectl patch secret app-secret -n data --type="json" -p $patch
```

### 6. Kafka - Strimzi

> Strimzi 0.45부터 Zookeeper 모드가 제거되었습니다. 이 프로젝트 manifest 기준으로는 0.44.0 버전을 사용합니다.

```powershell
# Strimzi Operator 설치
helm install strimzi-operator strimzi/strimzi-kafka-operator `
  -n kafka `
  --version 0.44.0

# Operator Ready 대기
kubectl wait --for=condition=ready pod `
  -l name=strimzi-cluster-operator `
  -n kafka `
  --timeout=120s

# Kafka 클러스터 배포
kubectl apply -f k8s/kafka/kafka-cluster.yaml -n kafka

# Kafka Pod Ready 확인
kubectl get pods -n kafka -w

# Topic 생성
kubectl apply -f k8s/kafka/kafka-topic.yaml -n kafka
```

`kubectl get pods -n kafka -w`는 계속 watch 상태로 유지됩니다. 다음 명령으로 넘어가려면 `Ctrl + C`로 종료합니다.

### 7. Apps 배포

MySQL, Redis, Elasticsearch, Kafka가 모두 Running 상태인지 확인한 뒤 애플리케이션을 배포합니다.

```powershell
# Secret을 수정한 경우에만 다시 적용합니다.
kubectl apply -f k8s/apps/app-secret.yaml

# Service & Deployments
kubectl apply -f k8s/apps/service.yaml
kubectl apply -f k8s/apps/api-server-deployment.yaml
kubectl apply -f k8s/apps/price-collector-deployment.yaml
kubectl apply -f k8s/apps/price-alert-deployment.yaml
kubectl apply -f k8s/apps/api-server-hpa.yaml
```

## 상태 확인

```powershell
# 전체 Pod 확인
kubectl get pods -A

# kube-system 제외해서 보기
kubectl get pods -A | Where-Object { $_ -notmatch "kube-system" }

# 네임스페이스별 확인
kubectl get pods -n data
kubectl get pods -n apps
kubectl get pods -n kafka

# ES 상태 확인
kubectl get elasticsearch -n data
```

## API 테스트 - Postman

api-server는 ClusterIP라 port-forward가 필요합니다.

```powershell
kubectl port-forward svc/api-server 8080:80 -n apps
```

Postman에서 다음 API를 호출합니다.

```text
POST http://localhost:8080/api/flights/search
```

```json
{
  "originLocationAirport": "ICN",
  "destinationLocationAirport": "NRT",
  "departureDate": "2026-05-01",
  "currencyCode": "KRW",
  "nonStop": false,
  "roundTrip": false,
  "travelClass": "ECONOMY",
  "adults": 1,
  "max": 5
}
```

## 자주 쓰는 PowerShell 확인 명령

```powershell
# Pod 로그 확인
kubectl logs -n apps deployment/api-server
kubectl logs -n apps deployment/price-alert
kubectl logs -n apps deployment/price-collector
kubectl logs -n data deployment/logstash

# 이벤트 확인
kubectl get events -n apps --sort-by='.lastTimestamp'
kubectl get events -n data --sort-by='.lastTimestamp'
kubectl get events -n kafka --sort-by='.lastTimestamp'

# Secret 키 목록 확인 - 값은 출력하지 않고 key 이름만 확인
(kubectl get secret app-secret -n apps -o json | ConvertFrom-Json).data.PSObject.Properties.Name
(kubectl get secret app-secret -n data -o json | ConvertFrom-Json).data.PSObject.Properties.Name
```

## 트러블슈팅

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| `kubectl`이 클러스터를 못 찾음 | Docker Desktop Kubernetes 비활성화 또는 context 불일치 | Docker Desktop에서 Kubernetes 활성화 후 `kubectl config use-context docker-desktop` |
| `mysql-0` CreateContainerConfigError | `data` 네임스페이스의 `app-secret` 누락 또는 `DB_PASSWORD` 누락 | `kubectl apply -f k8s/apps/app-secret.yaml` 실행 |
| `api-server` 401 ES 에러 | ECK 비밀번호와 `apps/data` app-secret의 `ES_PASSWORD` 불일치 | PowerShell용 ES_PASSWORD 동기화 명령 실행 |
| Kafka CRD not found | Strimzi Operator 미설치 | Strimzi Operator 설치 후 Kafka manifest 재적용 |
| Kafka pod 미생성 | Strimzi 버전 불일치 | `--version 0.44.0`으로 설치 |
| `logstash` CrashLoopBackOff | Kafka 또는 Elasticsearch가 준비되지 않음 | Kafka, ES 상태 확인 후 Logstash 재시작 |

Logstash만 재시작하려면 다음 명령을 사용합니다.

```powershell
kubectl rollout restart deployment/logstash -n data
```
