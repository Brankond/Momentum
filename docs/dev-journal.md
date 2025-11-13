# Dev Journal

## 2025-11-13

- Initialized a Gradle multi-module backend (`gateway`, `libs/shared-kernel`, `wallet`, `transfer`, `notification`) with shared toolchain (Spring Boot 3.5.7, Java 21) and wrapper config.
- Built the shared kernel library containing `Money`, domain events, and `DomainException`, plus matching unit tests; wired AssertJ and JUnit dependencies.
- Scaffolded wallet service (Boot app, health endpoint, Postgres/Redis/RabbitMQ config, H2-backed test profile) and added placeholder modules for transfer/notification.
- Introduced GitHub Actions workflow (`.github/workflows/ci.yml`) running `./gradlew clean build`, and refreshed local infra images in `docker-compose.yml`.
- Added runnable skeletons and smoke tests for gateway, transfer, and notification services so `bootJar` tasks succeed.
- Documented shared-kernel public APIs with concise Javadoc to keep `withJavadocJar()` builds warning-free and refreshed contributor guidance around code documentation.
