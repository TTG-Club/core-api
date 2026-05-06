# TTG Club Core API

Backend REST API для портала TTG Club DnD 5E. Сервис хранит и отдаёт справочные игровые данные, пользовательские данные, сгенерированный контент, изображения, рейтинги, уведомления, сохранённые фильтры и метаданные поиска.

## Стек

- Java 21
- Spring Boot 3.4
- Spring Web, Spring Data JPA, Spring Security, Validation, Actuator
- PostgreSQL
- Liquibase migrations
- QueryDSL
- MapStruct и Lombok
- JWT-аутентификация
- Spring Mail
- S3-совместимое объектное хранилище
- OpenAPI/Swagger UI и Scalar UI
- Maven Wrapper
- Docker multi-stage build

## Основные разделы API

Публичное API в основном сгруппировано под `/api/v2`.

- Заклинания, виды, классы, черты, предыстории, бестиарий, предметы, магические предметы, глоссарий, источники, статьи, roadmap
- Полнотекстовый поиск и метаданные фильтров
- Справочники и значения для списков выбора
- Рейтинги, уведомления, персоны, счётчики онлайна
- Загрузка/конвертация изображений и рамки токенов
- Workshop и генераторы
- Эндпоинты профиля пользователя под `/api/user`

Документация API доступна после запуска:

- Swagger UI: `/swagger-ui/index.html`
- Scalar UI: `/scalar-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Health check: `/actuator/health`

## Требования

- JDK 21
- Maven опционален: в репозитории есть `mvnw` и `mvnw.cmd`
- PostgreSQL
- Доступ к S3-совместимому bucket, если включено хранение изображений
- SMTP-доступ, если включена отправка писем

## Конфигурация

Конфигурация задаётся через Spring properties и переменные окружения. Не коммитьте реальные секреты, пароли, токены, приватные эндпоинты и production connection strings.

Необходимые переменные окружения:

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

Профили:

- `dev`: включает логирование SQL
- `prod`: отключает логирование SQL

Локальные env-файлы держите вне коммитов или используйте только файлы, игнорируемые Git. Если нужен пример конфигурации, создавайте только очищенный шаблон с placeholder-значениями.

## Локальный запуск

Задайте необходимые переменные окружения и запустите приложение:

```bash
./mvnw spring-boot:run
```

На Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Чтобы выбрать Spring-профиль:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

Liquibase запускается при старте приложения и применяет миграции из:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

## Сборка

```bash
./mvnw clean package
```

Итоговый jar собирается в:

```text
target/dnd5.jar
```

## Тесты

```bash
./mvnw test
```

Часть тестов использует Testcontainers с PostgreSQL, поэтому для них должен быть доступен Docker.

## Docker

Собрать image:

```bash
docker build -t ttg-club-core-api .
```

Запустить контейнер с переменными окружения, переданными вашей системой деплоя:

```bash
docker run --rm -p 8080:8080 ttg-club-core-api
```

## Структура проекта

```text
src/main/java/club/ttg/dnd5
  config/          Конфигурация Spring, security, cache, S3, OpenAPI, rate limit
  domain/          Функциональные модули и REST-контроллеры
  dto/             Общие DTO, сериализаторы, фильтры
  exception/       Обработка API-ошибок
  security/        JWT и вспомогательная логика аутентификации
  util/            Общие утилиты

src/main/resources
  application*.properties
  db/changelog/    Liquibase migrations
  static/          Статические ресурсы документации API

src/test
  java/            Unit и integration tests
  resources/       SQL/resources для тестов
```

## Безопасность

- Никогда не добавляйте секретные значения в `README.md`, примеры, скриншоты, коммиты или комментарии в задачах.
- В документации используйте placeholder-значения, например `<set-in-environment>`.
- Ротируйте любой credential, который случайно попал в коммит или был передан наружу.
- Production-секреты храните в secret manager, CI/CD variables или окружении деплоя.

## Участие в разработке

Требования к вкладу и CLA описаны в [CONTRIBUTING.md](CONTRIBUTING.md).

## Лицензия

Проект распространяется под лицензией [Apache 2.0](LICENSE.md).
