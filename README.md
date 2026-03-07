⸻

✈️ SkyTracker

Real-time Flight Price Tracking & Popular Route Analytics

SkyTracker는 항공권 가격 변동을 자동으로 추적하고 인기 노선을 분석하여 사용자에게 효율적인 항공권 정보를 제공하는 서비스입니다.

사용자가 반복적으로 검색하지 않아도 가격 변동을 모니터링하고 알림을 제공하며,
검색 데이터를 분석하여 인기 노선 트렌드 정보를 제공합니다.

또한 실제 검색 로그 분석을 기반으로 Pareto 법칙(80/20) 전략을 적용하여
상위 인기 노선 중심으로 데이터 캐싱 및 분석 시스템을 설계했습니다.

---

## 🛠 Tech Stack

### Backend & Data Pipeline
![Java](https://img.shields.io/badge/Java17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/SpringBoot3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge)

### Database & Search (ELK)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis_Sentinel-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Logstash](https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=logstash&logoColor=white)
![Kibana](https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white)

### Infra & DevOps
![AWS EKS](https://img.shields.io/badge/AWS_EKS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

---

🏗 System Architecture

SkyTracker는 MSA (Microservice Architecture) 기반으로 설계되었습니다.

Architecture
<img width="1482" height="932" alt="architecture" src="https://github.com/user-attachments/assets/d28d0de9-651a-4c6a-8e40-04c9ea1ee08c" />
Architecture Overview
- Spring Boot 기반 3개의 마이크로서비스로 구성
- Kafka 기반 비동기 이벤트 처리로 서비스 간 결합도 최소화
- Redis / MySQL / Elasticsearch 역할 분리 데이터 저장 구조
- Kubernetes 기반 클러스터 환경에서 운영
- NGINX Ingress Controller를 통한 L7 트래픽 라우팅


<br>
<br>

### 📂 Multi-Module Project Structure

```text
skytracker-root
├── sky-api-server       # 항공권 검색 및 사용자 API
├── sky-price-collector  # 외부 데이터 수집 및 Kafka 프로듀서
├── sky-alert-service    # 가격 추적 알림 및 메일 발송
├── sky-core             # 공통 도메인 및 비즈니스 로직
└── sky-common           # 유틸리티 및 설정 정보
```
<br>


--- 
📊 Data Engineering & Analytics
Search Data Aggregation
사용자의 검색 데이터를 분석하여 실시간 Hot Route Top 10 인기 노선을 제공합니다.

Pipeline: Logstash 수집 → Elasticsearch 집계 → Redis 캐싱 → 매일 00:00 갱신

Elasticsearch 집계 쿼리를 활용한 대규모 로그 데이터의 실시간 인기 노선 분석

Spring Scheduler를 활용하여 매일 정기적인 데이터 분석 자동화

분석 결과를 Redis에 캐싱하여 검색 트래픽 집중 시에도 초고속 응답 성능 보장

Monitoring & Logging (ELK)
Logstash: 분산된 3개 마이크로서비스의 로그를 통합 수집 및 정규화

Kibana: 항공권 최저가 변동 추이 및 사용자 검색 트래픽 실시간 시각화 대시보드 구축

<br>


--- 


☸ Infrastructure (AWS EKS)
서비스 인프라는 AWS EKS 기반 Kubernetes 클러스터에서 실제 서비스 수준으로 운영됩니다.

Deployment & Service: K8s 표준 객체를 활용한 안정적인 워크로드 배포

Traffic Control: NGINX Ingress Controller 기반 L7 라우팅 및 SSL/TLS 관리

Auto Scaling: **HPA(Horizontal Pod Autoscaler)**를 적용하여 트래픽 부하에 따라 Pod 자동 확장

Self-Healing: Kubernetes의 헬스체크 및 자동 복구 기능을 통한 무중단 운영

Infrastructure Issue Solving: AWS IAM 정책 설정 및 인/아웃바운드 네트워크 트러블슈팅 해결

<br>


--- 


🗄 ERD
<img width="100%" alt="erd" src="https://github.com/user-attachments/assets/5930de0b-509a-4441-a242-eb1317f20902" />


<br>


--- 


Engineering Highlights
✅ 성능 최적화: Redis Cache-Aside 전략 적용으로 검색 응답 속도 10배 개선 (4s → 0.3s)

✅ 비동기 처리: Kafka 기반 파이프라인 구축으로 수집 데이터 처리의 안정성 확보

✅ 인프라 고가용성: Redis Sentinel 기반 Failover 구조 구현으로 캐시 레이어 HA 달성

✅ 분석 자동화: Elasticsearch 기반 검색 데이터 집계 시스템 구현 및 트렌드 정보 제공

✅ CI/CD: GitHub Actions를 통한 EKS 클러스터 무중단 배포 자동화


<br>
