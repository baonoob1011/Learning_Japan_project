<div align="center">

# Learning Japan App (Identity Access Management Service)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.9-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

A comprehensive Identity Access Management (IAM) service built to handle user authentication, authorization, roles, and integrations for the Learning Japan project. This system utilizes modern backend technologies including AWS, Kafka, Elasticsearch, and WebSockets to provide scalable real-time experiences and AI-powered learning components.

</div>

<br/>

## 🌟 Highlighted Features

| Icon | Feature | Description | Libraries / Integrations |
| :---: | :--- | :--- | :--- |
| 🔐 | **Security & Auth** | OAuth2, JWT Authentication, and RBAC (Role-Based Access Control). | `spring-security`, `nimbus-jose-jwt`, `oauth2` |
| ☁️ | **AWS Integrations** | Comprehensive AWS usage for storage, AI processing, and logging. | `bedrock`, `transcribe`, `translate`, `s3`, `cognito`, `polly`, `cloudwatch` |
| ⚡ | **Real-time Events** | WebSocket and SocketIO support for live user interactions. | `spring-websocket`, `netty-socketio` |
| 🔍 | **Search & Indexing** | Full-text and fast indexing capabilities. | `elasticsearch`, `opensearch-java` |
| 🔄 | **Async Processing** | Message queuing and batch jobs for decoupled services. | `spring-kafka`, `spring-batch` |
| 💳 | **Payments** | PayPal checkout integrations. | `paypal-checkout-sdk` |
| 🚀 | **Performance** | Caching, ORM mapping, and optimized database queries. | `spring-data-redis`, `spring-data-jpa`, `mapstruct` |
| 📊 | **Monitoring** | System health checks and metric collections. | `actuator`, `micrometer-prometheus` |

<br/>

## 🛠️ Technical Stack

| Category | Technologies |
| :--- | :--- |
| **Core Framework** | Java 21, Spring Boot 3.3.9, Spring Cloud |
| **Databases** | PostgreSQL, Redis, Elasticsearch / OpenSearch, H2 (Testing) |
| **Messaging** | Apache Kafka |
| **Cloud (AWS)** | Bedrock, Transcribe, Translate, Polly, S3, Cognito, CloudWatch |
| **Security** | Spring Security, OAuth2, JWT (Nimbus), BCrypt |
| **Web & APIs** | Spring Web, SpringDoc OpenAPI (Swagger), OpenFeign |
| **Real-time & Network** | WebSocket, Socket.IO (Netty) |
| **Utils & Tools** | Lombok, MapStruct, Jsoup, Apache Commons CSV, Kuromoji |
| **Testing** | JUnit 5, Mockito, Embedded Redis/Kafka, Spring Boot Test |

<br/>

## 🚀 Getting Started

| Step | Action | Command |
| :---: | :--- | :--- |
| 1️⃣ | **Clone Repository** | `git clone <repository_url>` |
| 2️⃣ | **Navigate to Project** | `cd Learning_Japan_project/learningApp` |
| 3️⃣ | **Configure Env** | Create a `.env` file from the provided `.env.example` to set up your DB and AWS credentials. |
| 4️⃣ | **Build the Project** | `./mvnw clean install` (Linux/Mac) <br/> `mvnw.cmd clean install` (Windows) |
| 5️⃣ | **Start via Docker** | `docker-compose up -d` (To start dependencies like PostgreSQL/Redis/Kafka) |
| 6️⃣ | **Run Application** | `./mvnw spring-boot:run` |

<br/>

## 🏗️ Architecture overview

| Directory | Purpose |
| :--- | :--- |
| `.mvn/` | Maven wrapper configuration files ensuring consistent build environments. |
| `src/main/java/` | Contains the core Java source code, domain models, controllers, services, and repositories. |
| `src/main/resources/` | Configuration files (`application.yml`), static assets, templates, and database migrations. |
| `src/test/` | Unit tests, integration tests, and mock configurations using JUnit and embedded services. |
| `Dockerfile` | Defines the containerization process for deploying the application. |
| `docker-compose.yml`| Orchestrates local infrastructure (Databases, Message Brokers, etc.) for local development. |
| `pom.xml` | Project Object Model specifying dependencies, plugins, and build profiles. |
