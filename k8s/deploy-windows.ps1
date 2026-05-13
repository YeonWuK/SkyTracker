param(
    [ValidateSet("all", "images", "repos", "namespaces", "secrets", "mysql", "redis", "elastic", "sync-es-password", "kafka", "apps", "status")]
    [string]$Step = "all"
)

$ErrorActionPreference = "Stop"

$K8sDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $K8sDir

function Invoke-Step {
    param(
        [string]$Title,
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host "==> $Title" -ForegroundColor Cyan
    & $Command
}

function Pull-Images {
    Invoke-Step "Docker image 다운로드" {
        $images = @(
            "mysql:8.0",
            "yeonwoo02/skytracker-app:latest",
            "yeonwoo02/skytracker-price-alert:latest",
            "yeonwoo02/skytracker-price-collector:latest",
            "docker.elastic.co/elasticsearch/elasticsearch:8.13.4",
            "docker.elastic.co/kibana/kibana:8.13.4",
            "docker.elastic.co/logstash/logstash:8.13.4",
            "docker.elastic.co/eck/eck-operator:2.13.0",
            "quay.io/strimzi/operator:0.44.0",
            "quay.io/strimzi/kafka:0.44.0-kafka-3.7.0"
        )

        foreach ($image in $images) {
            Write-Host "docker pull $image"
            docker pull $image
        }
    }
}

function Add-HelmRepos {
    Invoke-Step "Helm repo 등록" {
        helm repo add bitnami https://charts.bitnami.com/bitnami
        helm repo add elastic https://helm.elastic.co
        helm repo add strimzi https://strimzi.io/charts/
        helm repo update
    }
}

function Deploy-Namespaces {
    Invoke-Step "Namespace 생성" {
        kubectl apply -f namespaces.yaml
    }
}

function Deploy-Secrets {
    Invoke-Step "Secret 적용" {
        if (-not (Test-Path "apps/app-secret.yaml")) {
            throw "apps/app-secret.yaml 파일이 없습니다. app-secret.example.yaml을 참고해 먼저 생성하세요."
        }

        kubectl apply -f apps/app-secret.yaml
    }
}

function Deploy-MySql {
    Invoke-Step "MySQL 배포" {
        kubectl apply -f mysql/headless-service.yaml
        kubectl apply -f mysql/statefulset.yaml
    }
}

function Deploy-Redis {
    Invoke-Step "Redis StorageClass 적용 및 Helm 설치" {
        kubectl apply -f redis/sc.yaml
        helm upgrade --install redis bitnami/redis -n data -f redis/values-redis-ha.yaml
    }
}

function Deploy-Elastic {
    Invoke-Step "ECK Operator 설치" {
        helm upgrade --install elastic-operator elastic/eck-operator -n elastic-system --create-namespace --version 2.13.0
        kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=elastic-operator -n elastic-system --timeout=120s
    }

    Invoke-Step "Elasticsearch, Kibana, Logstash 배포" {
        kubectl apply -f es/es.yaml -n data
        kubectl wait elasticsearch/elastic --for=jsonpath='{.status.health}'=green -n data --timeout=300s
        kubectl apply -f es/kibana.yaml -n data
        kubectl apply -f es/logstash-configmap.yaml -n data
        kubectl apply -f es/logstash-deployment.yaml -n data
    }
}

function Sync-EsPassword {
    Invoke-Step "ECK ES_PASSWORD를 apps/data app-secret에 동기화" {
        $encodedEsPass = kubectl get secret elastic-es-elastic-user -n data -o jsonpath="{.data.elastic}"
        if ([string]::IsNullOrWhiteSpace($encodedEsPass)) {
            throw "elastic-es-elastic-user Secret에서 비밀번호를 가져오지 못했습니다."
        }

        $esPass = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($encodedEsPass))
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

        kubectl rollout restart deployment/logstash -n data
    }
}

function Deploy-Kafka {
    Invoke-Step "Strimzi Operator 설치" {
        helm upgrade --install strimzi-operator strimzi/strimzi-kafka-operator -n kafka --version 0.44.0
        kubectl wait --for=condition=ready pod -l name=strimzi-cluster-operator -n kafka --timeout=120s
    }

    Invoke-Step "Kafka 클러스터 및 Topic 생성" {
        kubectl apply -f kafka/kafka-cluster.yaml -n kafka
        kubectl apply -f kafka/kafka-topic.yaml -n kafka
    }
}

function Deploy-Apps {
    Invoke-Step "Apps Service 및 Deployment 배포" {
        kubectl apply -f apps/app-secret.yaml
        kubectl apply -f apps/service.yaml
        kubectl apply -f apps/api-server-deployment.yaml
        kubectl apply -f apps/price-collector-deployment.yaml
        kubectl apply -f apps/price-alert-deployment.yaml
        kubectl apply -f apps/api-server-hpa.yaml
    }
}

function Show-Status {
    Invoke-Step "상태 확인" {
        kubectl get pods -A
        kubectl get elasticsearch -n data
    }
}

switch ($Step) {
    "images" { Pull-Images }
    "repos" { Add-HelmRepos }
    "namespaces" { Deploy-Namespaces }
    "secrets" { Deploy-Secrets }
    "mysql" { Deploy-MySql }
    "redis" { Deploy-Redis }
    "elastic" { Deploy-Elastic }
    "sync-es-password" { Sync-EsPassword }
    "kafka" { Deploy-Kafka }
    "apps" { Deploy-Apps }
    "status" { Show-Status }
    "all" {
        Pull-Images
        Add-HelmRepos
        Deploy-Namespaces
        Deploy-Secrets
        Deploy-MySql
        Deploy-Redis
        Deploy-Elastic
        Sync-EsPassword
        Deploy-Kafka
        Deploy-Apps
        Show-Status
    }
}
