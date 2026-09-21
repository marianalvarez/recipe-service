# Recipe Management API

RESTful recipe service built with Spring Boot 3.5, Java 17, Liquibase, and PostgreSQL.

## Prerequisites

- Java 17
- Maven 3.8+
- PostgreSQL running locally on port `5432`

## Database setup

```sql
CREATE DATABASE recipedb;
```

Default connection in `src/main/resources/application.yml`:

- URL: `jdbc:postgresql://localhost:5432/recipedb`
- Username: `postgres`
- Password: `postgrespassword`

Change those values if your local Postgres differs.

On startup, Liquibase creates tables, search indexes, and sample data:

| Id | Name | Vegetarian |
|---|---|---|
| 1 | Vegetarian Pasta Primavera | yes |
| 2 | Chicken Adobo | no |

## Run

```bash
mvn clean install
mvn spring-boot:run
```

The API listens on [http://localhost:8080](http://localhost:8080).

| Resource | URL |
|---|---|
| OpenAPI spec | [http://localhost:8080/openapi.yaml](http://localhost:8080/openapi.yaml) |
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |

## API overview

Base path: `/api/v1/recipes`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/recipes` | Create a recipe |
| `GET` | `/api/v1/recipes` | List recipes (paged) |
| `GET` | `/api/v1/recipes/search` | Search recipes (paged) |
| `GET` | `/api/v1/recipes/{id}` | Get recipe by id |
| `PUT` | `/api/v1/recipes/{id}` | Replace a recipe by id |
| `DELETE` | `/api/v1/recipes/{id}` | Delete recipe by id |

### Pagination

List and search return:

```json
{
  "recipes": [ ],
  "totalCount": 2,
  "page": 2
}
```

- `page=1` is the first page
- Query params: `page` (default 1), `size` (default 20, max 100), `sort` (default `id,asc`).

Example: `/api/v1/recipes?page=1&size=20`

### Search

All provided filters are combined with **AND**. Omit a parameter to skip that filter.

| Parameter | Behavior |
|---|---|
| `vegetarian` | Exact match (`true` / `false`) |
| `servings` | Exact servings count |
| `includeIngredients` | Recipe must contain **every** listed name (case-insensitive, exact) |
| `excludeIngredients` | Recipe must contain **none** of the listed names (case-insensitive, exact) |
| `instructionSearch` | Case-insensitive substring match on instruction text |

Example: recipes that include pasta **and** chicken:

`/api/v1/recipes/search?includeIngredients=pasta&includeIngredients=chicken`

### Tracing

Optional request header `X-Trace-Id`. If omitted, the server generates one and returns it on the response. It is written to logs and included on error bodies. It is not an idempotency key.

### Concurrency

Update and delete lock the recipe row (`SELECT … FOR UPDATE`). A concurrent writer waits up to **3 seconds**, then receives `409 Conflict` with:

`Recipe was updated concurrently. Retry the request.`

A second delete of an already-removed id returns `404`.

### Assumptions

- Recipe names are **not** unique. `POST` always inserts a new recipe.
- Create is **not** idempotent.
- Units are free text (no enum list yet).
- Instruction `stepNumber` values are not unique-constrained.

### Error responses

Typical body:

```json
{
  "timestamp": "2026-09-21T23:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Recipe not found with id: 99",
  "traceId": "..."
}
```

| Status | When |
|---|---|
| 400 | Validation failed or body is malformed |
| 404 | Recipe id does not exist |
| 409 | Concurrent update/delete, or data integrity conflict |
| 500 | Unexpected server error |

## Tests

Unit and Web MVC slice tests (no local Postgres required):

```bash
mvn test
```
