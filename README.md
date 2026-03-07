⸻

✈️ SkyTracker

Real-time Flight Price Tracking & Popular Route Analytics

SkyTracker는 항공권 가격 변동을 자동으로 추적하고 인기 노선을 분석하여 사용자에게 효율적인 항공권 정보를 제공하는 서비스입니다.

사용자가 반복적으로 검색하지 않아도 가격 변동을 모니터링하고 알림을 제공하며,
검색 데이터를 분석하여 인기 노선 트렌드 정보를 제공합니다.

또한 실제 검색 로그 분석을 기반으로 Pareto 법칙(80/20) 전략을 적용하여
상위 인기 노선 중심으로 데이터 캐싱 및 분석 시스템을 설계했습니다.

⸻


🛠 Tech Stack

Backend <br>
Java 17, Spring Boot, Spring Data JPA, QueryDSL, Kafka

Database & Cache<br>
MySQL, Redis Sentinel

Search & Logging<br>
Elasticsearch, Logstash, Kibana (ELK)

Infra & DevOps<br>
AWS EKS, Docker, Kubernetes, Nginx Ingress, GitHub Actions

Others<br>
OpenAI API (GPT), OAuth2

⸻

🏗 System Architecture

SkyTracker는 MSA (Microservice Architecture) 기반으로 설계되었습니다.

Architecture
<img width="1482" height="932" alt="architecture" src="https://github.com/user-attachments/assets/d28d0de9-651a-4c6a-8e40-04c9ea1ee08c" />
Architecture Overview
	•	Spring Boot 기반 3개의 마이크로서비스 구성 <br> 
	•	Kafka 기반 비동기 이벤트 처리 <br>
	•	Redis / MySQL / Elasticsearch 데이터 저장 구조 <br>
	•	Kubernetes 기반 클러스터 환경 <br>
	•	NGINX Ingress를 통한 트래픽 라우팅 <br>


⸻

📊 Search Data Aggregation

사용자의 검색 데이터를 분석하여 Hot Route Top10 인기 노선을 제공합니다.

구현 방식
	•	Elasticsearch 집계 쿼리 활용 <br>
	•	Spring Scheduler 기반 매일 00:00 인기 노선 집계 <br>
	•	집계 결과를 Redis 캐싱 <br>

결과적으로
	•	빠른 인기 노선 조회 <br>
	•	트렌드 기반 항공권 탐색 기능 제공 <br>

⸻

☸ Kubernetes Infrastructure (AWS EKS)

서비스 인프라는 AWS EKS 기반 Kubernetes 클러스터에서 운영됩니다.

직접 구축 및 운영 경험
	•	Kubernetes Deployment / Service 구성 <br>
	•	NGINX Ingress Controller 기반 트래픽 라우팅 <br>
	•	HPA(Horizontal Pod Autoscaler) 기반 자동 스케일링 <br>
	•	Self-Healing 구조 운영 <br>

또한 운영 과정에서
	•	Kubernetes Service Networking <br>
	•	Ingress Inbound / Outbound 설정 <br>
	•	AWS IAM 권한 정책 <br>

등 실제 인프라 문제를 직접 해결했습니다.

⸻


🗄 ERD
<img width="1928" height="1312" alt="erd" src="https://github.com/user-attachments/assets/5930de0b-509a-4441-a242-eb1317f20902" />



⸻

🚀 Engineering Highlights
	•	Kafka 기반 비동기 데이터 파이프라인 구축 <br>
	•	Elasticsearch 기반 검색 데이터 집계 시스템 구현 <br>
	•	AWS EKS 기반 Kubernetes 인프라 구축 및 운영 <br>
	•	Redis Sentinel 기반 고가용성 캐시 구조 구현 <br> 
	•	Redis Cache 전략을 통한 검색 응답 속도 10배 개선 <br>

⸻
