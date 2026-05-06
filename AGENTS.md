# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build all modules
./gradlew clean build

# Build a specific module
./gradlew :apps:api-server:build
./gradlew :apps:price-alert:build
./gradlew :apps:price-collector:build

# Run tests (all modules)
./gradlew test

# Run a single test class
./gradlew :apps:api-server:test --tests "com.skytracker.ClassName"

# Run local dev server (requires MySQL, Redis, Kafka, Elasticsearch running)
./gradlew :apps:api-server:bootRun
./gradlew :apps:price-alert:bootRun
./gradlew :apps:price-collector:bootRun

# Build Docker images
./gradlew bootBuildImage
```

## Architecture Overview

SkyTracker is a **Kafka-driven flight price tracking and alerting system** deployed on Kubernetes. It consists of three Spring Boot applications and two shared libraries organized as a Gradle multi-module project.

```
skytracker/
├── apps/
│   ├── api-server        # REST API + OAuth2 auth + scheduled price checks
│   ├── price-alert       # Kafka consumer → sends email alerts
│   └── price-collector   # Kafka consumer → stores prices in Redis
├── libs/
│   ├── common            # DTOs, exceptions, enums (no Spring)
│   └── core              # RedisClient, AmadeusFlightSearchService
├── adapters/
│   └── kafka             # Kafka producer config and messaging services
└── k8s/                  # Kubernetes manifests
```

### Data Flow

```
api-server (scheduled every 3h)
  → Amadeus API (fetch prices)
  → Produce: flight-ticket-update → Kafka
      ↓
price-collector (consumes flight-ticket-update)
  → Stores prices in Redis
      ↓
api-server (FlightAlertService) compares DB-stored alert thresholds
  → Produce: flight-alert → Kafka
      ↓
price-alert (consumes flight-alert)
  → Sends email notification via Spring Mail
```

### Key Technologies

| Stack | Detail |
|-------|--------|
| Java | 17 |
| Spring Boot | 3.5.3 (api-server), 3.5.4 (libs/consumers) |
| Database | MySQL + Spring Data JPA + QueryDSL 5.0.0 |
| Cache | Redis Sentinel (3-node, Bitnami Helm) |
| Search | Elasticsearch (ECK on K8s) |
| Messaging | Apache Kafka (Strimzi 0.44.0) |
| Auth | OAuth2 (Google, Kakao, Naver) + JWT |
| External APIs | Amadeus (flight data), OpenAI GPT-4o-mini |
| Logging | Logback → Logstash → Elasticsearch |

### Module Responsibilities

- **api-server**: REST endpoints, OAuth2/JWT security, JPA entities, QueryDSL queries, Elasticsearch indexing, scheduled price-check tasks (every 3h), expired alert cleanup (daily)
- **price-alert**: Stateless Kafka consumer on `flight-alert` topic; no DB, no REST; horizontally scalable
- **price-collector**: Stateless Kafka consumer on `flight-ticket-update` topic; writes to Redis only; batch listener with concurrency 3
- **libs/common**: Shared DTOs (`FlightSearchResponseDto`, alert DTOs), exceptions — no runtime Spring dependencies
- **libs/core**: `RedisClient` wrapper, `AmadeusFlightSearchService`; compiled as a plain JAR (not Boot JAR)
- **adapters/kafka**: Kafka producer configuration; compiled as a plain JAR

### Kubernetes Namespaces

- `data` — MySQL, Redis Sentinel, Elasticsearch
- `kafka` — Strimzi Kafka cluster and topics
- `apps` — api-server, price-collector, price-alert deployments + HPAs

### Environment Variables Required

All sensitive config is injected via Kubernetes Secrets/ConfigMaps. Key vars: `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_SECRET`, `AMADEUS_API_KEY`, `AMADEUS_API_SECRET`, `OPENAI_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`.
