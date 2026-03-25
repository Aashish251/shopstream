# Shopstream Project Interview Document

## Candidate Project Narrative
This project is a multi-service e-commerce platform built with Spring Boot and organized as a Maven multi-module repository. It follows a microservices architecture in which each business domain is implemented as an independent service and connected through service discovery, inter-service communication, and shared infrastructure such as databases, messaging, and search technologies.

My contribution to this project focused on stabilizing the platform for local development, resolving framework compatibility issues, and restoring service startup reliability across multiple modules. When I began, the project was not consistently runnable because several services were partially migrated, dependency versions were misaligned, and environment assumptions were tightly coupled to specific machines. My work was to bring the system into a reproducible state and recover the most critical services so that the application stack could run end-to-end in a realistic development environment.

## Project Context
The repository contains multiple domain services, including:

- `discovery-service` for Eureka-based service registration and discovery
- `order-service` for order lifecycle management
- `product-service` for product and catalog operations
- `promotion-service` for promotion and discount rules
- `user-service` for authentication, authorization, and user management
- additional services such as shipping, payment, notification, search, favourite, rating, tax, and inventory

The platform uses a combination of infrastructure technologies, including MySQL, PostgreSQL, Kafka, MongoDB, Elasticsearch, and Eureka. Because the project spans several services and data stores, making it runnable required both code changes and infrastructure orchestration.

## My Role and Responsibilities
My role was effectively a platform stabilization and modernization role within the project. I was responsible for:

- analyzing why the codebase could not start reliably
- fixing dependency and framework compatibility issues
- standardizing local infrastructure with Docker-based services
- updating service configuration to use environment-driven properties
- migrating incompatible code to work with Spring Boot 3 and Spring Cloud 2023
- verifying service startup and registration through live runtime checks

This work required combining application-level debugging, build-system correction, and local environment design.

## Key Contributions
### 1. Dependency Alignment Across the Repository
One of the most important improvements was correcting version drift between Spring Boot, Spring Framework, and Spring Cloud components. The project had a mixture of managed and hard-pinned versions that were no longer compatible with each other. This caused failures such as missing methods at runtime and incompatible framework behavior.

I updated the parent dependency behavior so services could rely on Spring Boot-managed versions rather than conflicting overrides. I also aligned child modules to Spring Cloud versions compatible with Spring Boot 3.3.x. This created a stable baseline for the rest of the debugging work.

### 2. Local Infrastructure Standardization
The project depended on multiple external systems, but local startup was inconsistent because the setup was not standardized. To solve this, I created a local Docker-based infrastructure definition that provisions the required backing services in a repeatable way.

This included:

- MySQL for transactional services
- PostgreSQL for promotion-related persistence
- MongoDB for document-oriented storage
- Kafka and Zookeeper for event-driven messaging
- Elasticsearch for search capabilities

I also created initialization scripts for databases so the environment could be started in a clean and repeatable manner.

### 3. Service Configuration Modernization
Several services had hardcoded or machine-specific values for databases, ports, and external URLs. I updated configuration files to use profile-based and environment-based property resolution. This made the services much easier to run in both local and containerized development workflows.

The result was a more portable setup where developers can run services against local Docker infrastructure without editing source-controlled configuration every time.

### 4. Recovery of `discovery-service`
I repaired the Eureka server by fixing dependency compatibility in the discovery module. Once corrected, `discovery-service` started successfully and exposed the Eureka UI and registration endpoint. This was a foundational milestone because other services depend on service discovery to behave like a real microservice ecosystem.

### 5. Recovery of `order-service`
For `order-service`, I corrected database configuration, added profile-specific settings, and introduced H2-based testing support so the service could start and test in a controlled environment. I verified that the service could run and register with Eureka successfully.

### 6. Recovery of `promotion-service`
`promotion-service` required a deeper framework migration. The service contained multiple Spring Boot 2-era patterns that are incompatible with Spring Boot 3. I updated `javax.*` imports to `jakarta.*`, modernized security configuration, added missing Resilience4j support, and resolved a PostgreSQL timezone startup issue. After these changes, the service was able to compile and run correctly.

### 7. Recovery of `user-service`
`user-service` involved the most layered debugging path. I removed an invalid cross-module dependency, resolved logging conflicts, removed an obsolete Swagger/Springfox configuration path, aligned its Spring Cloud version, updated Hibernate dialect settings, removed unnecessary Eureka annotations, and fixed JWT filter registration. After these changes, the service started successfully, exposed Swagger UI, and registered in Eureka as healthy.

## Technical Challenges
The most challenging part of this work was that the issues were not isolated. Each service had layered problems where one fix only revealed the next blocker. For example, in `user-service`, the path to a successful startup included:

- unresolved module dependency issues
- incompatible SLF4J version overrides
- Springfox incompatibility with Spring Boot 3
- Hibernate dialect changes in Hibernate 6
- Spring Cloud annotation changes
- missing bean registration in security configuration

This required iterative debugging with careful control over changes so that each new failure could be isolated and resolved without destabilizing other modules.

Another challenge was the interaction between infrastructure and application code. Some failures were caused by code-level migration issues, while others were caused by environment assumptions such as database credentials, driver behavior, or timezone handling. Solving the project required treating it as an integrated platform rather than a collection of unrelated services.

## Outcome
As a result of my work:

- `discovery-service` was restored and running correctly
- `order-service` was running and registered in Eureka
- `promotion-service` was running successfully on its configured port
- `user-service` was running successfully, served Swagger UI, and registered in Eureka
- local infrastructure became reproducible through Docker
- multiple services were made more portable through environment-based configuration

This substantially improved the usability of the repository for development, testing, and demonstration purposes.

## Business and Engineering Value
From an engineering perspective, this work reduced setup friction, improved reproducibility, and lowered the risk of environment-specific failures. From a team productivity perspective, it made the project significantly easier for other developers to run, understand, and continue building on. In a professional setting, this type of work is valuable because it enables faster onboarding, more predictable local testing, and a stronger path toward CI/CD reliability.

## Interview Closing Statement
If I were presenting this work in an interview, I would describe it as a platform recovery and modernization effort inside a microservices-based e-commerce system. My contribution was not limited to fixing one bug or one service. I stabilized the system across build configuration, runtime infrastructure, service compatibility, and application startup behavior. The result was a project that moved from partially broken and environment-sensitive to reproducible, understandable, and demonstrably runnable.
