# Shopstream Technical Deep Dive

## 1. Executive Summary
Shopstream is a multi-module Spring Boot microservices platform. The repository contains multiple domain services and a shared parent build, with a runtime topology centered around Eureka for discovery and several backing stores depending on service responsibility.

The codebase was in a partially migrated state when I worked on it. The core engineering problem was not a single defect but a chain of compatibility and environment issues across:

- Maven dependency alignment
- Spring Boot 3 migration
- Spring Cloud release compatibility
- Swagger/OpenAPI tooling compatibility
- Hibernate 6 behavior changes
- local infrastructure reproducibility
- service-specific runtime configuration

My goal was to restore a working local development path and recover critical services in a way that reduced future operational friction.

## 2. Initial State
The repository showed classic signs of framework drift:

- explicit dependency overrides conflicting with Spring Boot-managed versions
- Spring Cloud versions from older release lines mixed with Boot 3.3.x
- old `javax.*` APIs in services now running on Jakarta-based Spring Boot 3
- Spring Security 5 style DSL still present in modules
- Springfox Swagger 2 usage in services on modern Boot
- hardcoded or machine-specific database assumptions
- no single repeatable local infrastructure bootstrap

Because of this, services failed in multiple ways:

- compile-time missing classes
- runtime `NoSuchMethodError`
- classpath conflicts
- invalid bean wiring
- database startup failures
- service registration issues

## 3. Platform-Level Corrections
### 3.1 Parent Build Stabilization
The first system-wide correction was in the parent build. The repository had a manually pinned `spring-web` version that diverged from the Spring Boot BOM. That created framework incompatibility at runtime. I removed the conflicting version override and restored dependency management to Spring Boot.

This mattered because, in a multi-module platform, child services often appear to fail independently when the actual root cause is a mismatched foundational framework artifact.

### 3.2 Spring Cloud Alignment
Some services were still on an old Spring Cloud release line. For Boot 3.3.x, Spring Cloud needed to be aligned to a compatible generation. I updated modules to use the `2023.0.3` line where needed.

This change directly affected:

- Eureka server startup
- Eureka client annotations and auto-configuration behavior
- downstream load balancer and discovery integration behavior

### 3.3 Infrastructure Reproducibility
The project depended on multiple external services. Instead of debugging against an unknown host environment, I created a local Docker-backed infrastructure definition and initialization scripts. This provided a deterministic local stack:

- MySQL
- PostgreSQL
- MongoDB
- Kafka
- Zookeeper
- Elasticsearch

This reduced environmental ambiguity and allowed service failures to be isolated to application code rather than local machine setup.

## 4. Service Recovery Work
### 4.1 Discovery Service
`discovery-service` was a foundational dependency because other services expect Eureka registration. The module failed due to framework incompatibility. After dependency correction and Spring Cloud alignment, it started correctly and exposed the Eureka UI and `/eureka/apps` endpoint.

### 4.2 Order Service
`order-service` required configuration work more than deep code migration.

Key actions:

- normalized datasource settings to environment-driven properties
- added Docker profile configuration
- added H2 support for testing and local convenience
- disabled discovery in test contexts where required

Result:

- tests became runnable
- the service started successfully against Docker MySQL
- Eureka registration was verified

### 4.3 Promotion Service
`promotion-service` was a stronger example of Boot 3 migration work.

Main issues:

- `javax.persistence` and `javax.validation` imports
- outdated Spring Security configuration style
- missing Resilience4j annotation support on the classpath
- PostgreSQL startup failure caused by timezone handling during Liquibase initialization

Fixes applied:

- migrated `javax.*` imports to `jakarta.*`
- updated Spring Security configuration to use Boot 3 compatible APIs such as `authorizeHttpRequests` and `requestMatchers`
- added the required Resilience4j support library
- standardized runtime timezone handling to UTC in both Maven/dev runtime and container runtime paths

Result:

- module compiled cleanly
- service started successfully with the Docker profile

### 4.4 User Service
`user-service` had the most layered failure sequence and is a strong interview example of staged debugging.

Observed progression:

1. unresolved compile dependency on `order-service`
2. SLF4J conflict from manually pinned legacy API version
3. Springfox incompatibility leading to `javax.servlet` resolution failures
4. compile errors after Springfox removal because controller annotations still referenced Swagger 1.x classes
5. Hibernate 6 rejection of `MySQL5Dialect`
6. obsolete `@EnableEurekaClient` after Spring Cloud upgrade
7. security startup failure because `JwtTokenFilter` was not registered as a bean

Fix strategy:

- removed the invalid `order-service` dependency because no active code path required it
- removed the legacy SLF4J override to allow Boot-managed logging
- removed Springfox libraries and the old Swagger 2 configuration class
- preserved controller annotation compilation by adding the lightweight `swagger-annotations` dependency
- switched security whitelist paths to `springdoc`-compatible endpoints
- aligned Spring Cloud to `2023.0.3`
- updated Hibernate dialect from `MySQL5Dialect` to `MySQLDialect`
- removed `@EnableEurekaClient` from the application class
- registered `JwtTokenFilter` as a Spring bean

Result:

- the service started successfully on port `8088`
- Swagger UI loaded successfully
- Eureka showed `USER-SERVICE` as `UP`

## 5. Most Challenging Engineering Aspect
The hardest part of the project was the dependency chain between migration issues and environment issues.

In a modern Spring stack, one visible error often hides the next deeper incompatibility. A direct example is `user-service`, where fixing one failure repeatedly exposed a different layer:

- build graph issue
- logging classpath issue
- API migration issue
- documentation tooling issue
- persistence compatibility issue
- cloud integration issue
- bean registration issue

This required disciplined debugging:

- change one thing at a time
- rerun immediately after each meaningful fix
- distinguish compile-time defects from runtime defects
- separate framework migration problems from infrastructure problems

Another difficult aspect was that the project mixed multiple architectural concerns in the same local stack. To get reliable results, I had to reason simultaneously about:

- repository-wide dependency policy
- per-service application startup
- Dockerized infrastructure correctness
- service discovery health
- database driver and dialect behavior

## 6. Engineering Decisions and Rationale
### Prefer managed dependency versions
When a project is on Spring Boot 3, forcing framework versions manually often causes unstable behavior. Restoring BOM-based control reduced hidden mismatch risk.

### Favor environment-driven configuration
Profile-based and env-based properties made services portable and reduced reliance on machine-specific values.

### Remove incompatible legacy tooling rather than patch around it
Springfox is a poor fit for Boot 3. Removing it and moving toward `springdoc`-compatible behavior was lower-risk and more maintainable than trying to preserve old internals.

### Use Docker for local determinism
For a distributed system, predictable local infrastructure is essential. Standardizing infra made debugging far more efficient.

### Solve services in dependency order
Recovering `discovery-service` early created a stable anchor for validating downstream services.

## 7. Final Runtime State Achieved
At the end of this work, I verified:

- `discovery-service` healthy on `8761`
- `order-service` running and registered in Eureka
- `promotion-service` running on its configured port and serving
- `user-service` running on `8088`, Swagger available, and registered in Eureka

These outcomes demonstrate that the repository moved from partially broken and environment-sensitive to reproducible and demonstrably operational for core services.

## 8. Senior-Level Talking Points
For a senior engineering audience, I would frame this work as:

- a platform stabilization initiative in a partially migrated microservices codebase
- a dependency-governance and modernization exercise across Spring Boot, Spring Cloud, Hibernate, and Swagger tooling
- a reproducibility improvement through Docker-backed local infrastructure
- a service-by-service runtime recovery process driven by empirical verification rather than speculative refactoring

The most important engineering lesson from this project is that reliability in distributed systems begins with version coherence, reproducible infrastructure, and disciplined isolation of failures. Once those are restored, service-level issues become much more tractable.
