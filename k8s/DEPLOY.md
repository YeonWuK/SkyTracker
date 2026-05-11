# Skytracker K8s 배포 가이드

## 사전 준비

- Docker Desktop (Kubernetes 활성화)
- kubectl
- helm

### Helm Repo 등록

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add elastic https://helm.elastic.co
helm repo add strimzi https://strimzi.io/charts/
helm repo update
```

---

## 배포 순서

### 1. Namespace 생성

```bash
kubectl apply -f k8s/namespaces.yaml
```

---

### 2. MySQL

```bash
kubectl apply -f k8s/mysql/headless-service.yaml
kubectl apply -f k8s/mysql/statefulset.yaml
```

---

### 3. Redis (Bitnami Helm)

```bash
helm install redis bitnami/redis \
  -n data \
  -f k8s/redis/values-redis-ha.yaml
```

> Sentinel 구성 (master 1 + replica 3)

---

### 4. Elasticsearch (ECK Operator)

ECK Operator를 먼저 설치한 뒤 ES 클러스터를 배포합니다.

```bash
# Operator 설치
helm install elastic-operator elastic/eck-operator \
  -n elastic-system \
  --create-namespace

# Operator Ready 대기
kubectl wait --for=condition=ready pod \
  -l app.kubernetes.io/name=elastic-operator \
  -n elastic-system \
  --timeout=120s

# ES 클러스터 배포
kubectl apply -f k8s/es/es.yaml -n data

# ES Ready 대기 (green 될 때까지)
kubectl wait elasticsearch/elastic \
  --for=jsonpath='{.status.health}'=green \
  -n data \
  --timeout=300s

# Kibana & Logstash
kubectl apply -f k8s/es/kibana.yaml -n data
kubectl apply -f k8s/es/logstash-configmap.yaml -n data
kubectl apply -f k8s/es/logstash-deployment.yaml -n data
```

> **주의**: ECK는 배포 시마다 ES 비밀번호를 새로 생성합니다.
> `apps`, `data` 네임스페이스의 `app-secret`에 있는 `ES_PASSWORD`를 모두 동기화해야 합니다.

```bash
# ECK 생성 비밀번호 → app-secret 동기화
ES_PASS=$(kubectl get secret elastic-es-elastic-user -n data -o jsonpath='{.data.elastic}' | base64 -d)

kubectl patch secret app-secret -n apps \
  --type='json' \
  -p="[{\"op\":\"replace\",\"path\":\"/data/ES_PASSWORD\",\"value\":\"$(echo -n $ES_PASS | base64)\"}]"

kubectl patch secret app-secret -n data \
  --type='json' \
  -p="[{\"op\":\"replace\",\"path\":\"/data/ES_PASSWORD\",\"value\":\"$(echo -n $ES_PASS | base64)\"}]"
```

---

### 5. Kafka (Strimzi)

> **주의**: Strimzi 0.45부터 Zookeeper 모드 제거됨. **반드시 0.44.0 버전 사용**

```bash
# Strimzi Operator 설치 (0.44.0 고정)
helm install strimzi-operator strimzi/strimzi-kafka-operator \
  -n kafka \
  --version 0.44.0

# Operator Ready 대기
kubectl wait --for=condition=ready pod \
  -l name=strimzi-cluster-operator \
  -n kafka \
  --timeout=120s

# Kafka 클러스터 배포
kubectl apply -f k8s/kafka/kafka-cluster.yaml -n kafka

# Kafka Pod Ready 대기
kubectl get pods -n kafka -w

# Topic 생성
kubectl apply -f k8s/kafka/kafka-topic.yaml -n kafka
```

---

### 6. Apps 배포

MySQL, Redis, ES, Kafka 모두 Running 확인 후 배포합니다.

```bash
# app-secret.yaml은 gitignore 대상입니다.
# 새로 만들 때만 app-secret.example.yaml을 참고하고, 기존 로컬 app-secret.yaml은 덮어쓰지 않습니다.
kubectl apply -f k8s/apps/app-secret.yaml

# Service & Deployments
kubectl apply -f k8s/apps/service.yaml
kubectl apply -f k8s/apps/api-server-deployment.yaml
kubectl apply -f k8s/apps/price-collector-deployment.yaml
kubectl apply -f k8s/apps/price-alert-deployment.yaml
kubectl apply -f k8s/apps/api-server-hpa.yaml
```

---

## 상태 확인

```bash
# 전체 Pod (kube-system 제외)
kubectl get pods -A | grep -v kube-system

# 네임스페이스별
kubectl get pods -n data
kubectl get pods -n apps
kubectl get pods -n kafka

# ES 상태
kubectl get elasticsearch -n data
```

---

## API 테스트 (Postman)

api-server는 ClusterIP라 port-forward 필요합니다.

```bash
kubectl port-forward svc/api-server 8080:80 -n apps
```

**POST** `http://localhost:8080/api/flights/search`

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

---

## 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| `mysql-0` CreateContainerConfigError | `data` 네임스페이스의 `app-secret` 누락 또는 `DB_PASSWORD` 누락 | `kubectl apply -f k8s/apps/app-secret.yaml` 실행 |
| `api-server` 401 ES 에러 | ECK 비밀번호와 `apps/data` app-secret의 `ES_PASSWORD` 불일치 | ES_PASSWORD 동기화 명령 실행 |
| Kafka CRD not found | Strimzi Operator 미설치 | helm install strimzi-operator |
| Kafka pod 미생성 | Strimzi 버전이 0.45+ (Zookeeper 제거됨) | --version 0.44.0 으로 재설치 |
| `logstash` CrashLoopBackOff | Kafka 미실행 | Kafka 배포 후 자동 정상화 |
