# reciprocity-ledger backend

Spring Boot backend service for the reciprocity ledger project.

## Tech stack

- Spring Boot 3.5.11
- Java 17
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


