# reciprocity-ledger backend

Spring Boot backend service for the reciprocity ledger project.

## Tech stack

- Spring Boot 2.7.18
- Java 11
- Maven
- PostgreSQL
- MyBatis

## Database config

Default datasource values are read from these environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Default fallback URL:

`jdbc:postgresql://localhost:5432/reciprocity_ledger`

## Run

```bash
mvn spring-boot:run
```

## Build

```bash
mvn clean package
```

## Default endpoints

- `GET /api/health`
- `GET /actuator/health`
