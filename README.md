# Worklog
Simple service for logging Toggl Track entries in Jira Tempo.

## Setup
The Toggl Track entry description should start with the Jira issue key for which time is being logged.
An optional description can then be provided using a delimiter which can be configured through `WORKLOG_SERVICE_IMPL_CONFIG_DELIMITER`, which defaults to a colon (`:`), e.g.:

```
<JiraIssueKey><:>?<description>?
```

The following environment variables should be provided:
```
TOGGL_TRACK_CLIENT_HTTP_CONFIG_HOST=""
TOGGL_TRACK_CLIENT_HTTP_CONFIG_API_KEY=""
TOGGL_TRACK_CLIENT_HTTP_CONFIG_TIME_ZONE=""
JIRA_CLIENT_HTTP_CONFIG_HOST=""
JIRA_CLIENT_HTTP_CONFIG_USER_EMAIL=""
JIRA_CLIENT_HTTP_CONFIG_API_KEY=""
TEMPO_CLIENT_HTTP_CONFIG_HOST=""
TEMPO_CLIENT_HTTP_CONFIG_API_KEY=""
TEMPO_CLIENT_HTTP_CONFIG_ACCOUNT_ID=""
```

## Backend
Run the application from the `./backend` directory through `./gradlew bootRun`.

Interact with the application in one of the following ways:
- Through the API documented at [http://localhost:8080/api/v1/docs](http://localhost:8080/api/v1/docs)
- Through Swagger UI at [http://localhost:8080/api/v1/swagger-ui](http://localhost:8080/api/v1/swagger-ui)

Building an image can be done in one of two ways:
- For a local image, run `./gradlew jibDockerBuild --no-configuration-cache`
- To build an image and push it to the remote registry, run `./gradlew jib --no-configuration-cache`

Generate the OpenApiDocs by running `./gradlew generateOpenApiDocs --no-configuration-cache`.

The `--no-configuration-cache` flag is included because [jib](https://github.com/GoogleContainerTools/jib) and [OpenAPI Generator](https://plugins.gradle.org/plugin/org.openapi.generator) do not yet support configuration cache.

## Frontend
To interact with the application through the GUI exposed at [http://localhost:8080](http://localhost:8080), you first need to generate the corresponding files. Run the following command from the `./frontend` directory using [Node.js](https://nodejs.org/en): `npm install && npm run build`. Then, start the backend as described above.

## Stack
This application is an example of a [reactive](https://spring.io/reactive) backend system build with [Kotlin]([https://kotlinlang.org/), [Spring Boot](https://spring.io/projects/spring-boot) and [Gradle](https://gradle.org/) using the [Ports and Adapters](https://alistair.cockburn.us/hexagonal-architecture/) architecture.
It uses [H2 Database Engine](https://h2database.com/html/main.html) for in-memory data storage.

The frontend uses basic [HTML](https://html.spec.whatwg.org), [CSS](https://www.w3.org/TR/css/) and [TypeScript](https://www.typescriptlang.org/) with [Vite](https://vite.dev/) as the build tool.

This project is an experiment exploring Kotlin-specific features within the Spring Framework.
Additionally, it aims to reduce annotation-based configuration in favor of explicit definitions.
