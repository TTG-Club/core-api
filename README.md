# TTG Club Core API

Backend REST API for the TTG Club DnD 5E portal. The service stores and serves game reference data, user-related data, generated content, images, ratings, notifications, saved filters, and search metadata.

## Tech Stack

- Java 21
- Spring Boot 3.4
- Spring Web, Spring Data JPA, Spring Security, Validation, Actuator
- PostgreSQL
- Liquibase migrations
- QueryDSL
- MapStruct and Lombok
- JWT authentication
- Spring Mail
- S3-compatible object storage
- OpenAPI/Swagger UI and Scalar UI
- Maven Wrapper
- Docker multi-stage build

## Main API Areas

The public API is mostly grouped under `/api/v2`.

- Spells, species, classes, feats, backgrounds, bestiary, items, magic items, glossary, sources, articles, roadmap
- Full-text search and filter metadata
- Dictionaries and selectable reference values
- Ratings, notifications, personas, online counters
- Image upload/conversion and token borders
- Workshop and generator endpoints
- User profile endpoints under `/api/user`

API documentation is available after startup:

- Swagger UI: `/swagger-ui/index.html`
- Scalar UI: `/scalar-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Health check: `/actuator/health`

## Requirements

- JDK 21
- Maven is optional because the repository includes `mvnw` and `mvnw.cmd`
- PostgreSQL
- Access to an S3-compatible bucket if image storage is enabled
- SMTP credentials if mail delivery is enabled

## Configuration

Configuration is provided through Spring properties and environment variables. Do not commit real secrets, passwords, tokens, private endpoints, or production connection strings.

Required environment variables:

```text
APP_URL
JWT_SECRET
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_MAIL_PASSWORD
SPRING_CLOUD_AWS_S3_ENDPOINT
SPRING_CLOUD_AWS_S3_REGION
SPRING_CLOUD_AWS_S3_BUCKET
SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY
SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY
```

Profiles:

- `dev`: enables SQL logging
- `prod`: disables SQL logging

Keep local environment files outside commits, or use local-only files ignored by Git. If you need an example config, create a sanitized template with placeholder values only.

## Run Locally

Set the required environment variables, then start the app:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

To select a Spring profile:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

Liquibase runs on application startup and applies migrations from:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

## Build

```bash
./mvnw clean package
```

The application jar is built as:

```text
target/dnd5.jar
```

## Test

```bash
./mvnw test
```

Some tests use Testcontainers with PostgreSQL, so Docker must be available for those tests.

## Docker

Build the image:

```bash
docker build -t ttg-club-core-api .
```

Run the container with environment variables supplied by your deployment system:

```bash
docker run --rm -p 8080:8080 ttg-club-core-api
```

## Project Structure

```text
src/main/java/club/ttg/dnd5
  config/          Spring, security, cache, S3, OpenAPI, rate limit config
  domain/          Feature modules and REST controllers
  dto/             Shared DTOs, serializers, filters
  exception/       API exception handling
  security/        JWT and authentication helpers
  util/            Shared utilities

src/main/resources
  application*.properties
  db/changelog/    Liquibase migrations
  static/          Static API documentation assets

src/test
  java/            Unit and integration tests
  resources/       Test SQL/resources
```

## Security Notes

- Never place secret values in `README.md`, examples, screenshots, commits, or issue comments.
- Use placeholder values in documentation, for example `<set-in-environment>`.
- Rotate any credential that was ever committed or shared accidentally.
- Keep production secrets in a secret manager, CI/CD variables, or the deployment environment.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution requirements and CLA details.

## License

This project is licensed under the [Apache 2.0 License](LICENSE.md).
