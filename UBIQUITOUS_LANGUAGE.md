# Ubiquitous Language

## Runtime configuration

### Environment file
A file named `.env` at the repository root that provides runtime configuration values as key-value pairs.

### Runtime configuration
The complete set of values available to the application at startup through Spring properties, environment variables, and system properties.

### Launch working directory
The process working directory from which relative file paths are resolved during application startup.

### Repository root
The top-level directory of the worklog repository that contains the shared `.env` file.

## Application launch modes

### Development run
The normal application startup initiated through `bootRun` from the `:app` module.

### Documentation generation run
The application startup initiated indirectly by `generateOpenApiDocs` to expose the OpenAPI endpoint.

### Forked Spring Boot run
The temporary Spring Boot process started by the springdoc Gradle plugin during documentation generation.

## Build and documentation

### OpenAPI docs generation
The build flow that starts the application and fetches `/api/v1/docs` to produce the OpenAPI document.

### OpenAPI endpoint
The HTTP endpoint at `/api/v1/docs` that serves the generated API description for the running application.

### Springdoc custom boot run working directory
The working directory configured for the forked Spring Boot run used by OpenAPI docs generation.

## Relationships

- The Environment file contributes values to Runtime configuration.
- The Launch working directory determines whether the Environment file can be found when its path is relative.
- Development run uses the `:app` project directory as its Launch working directory.
- Documentation generation run relies on a Forked Spring Boot run.
- OpenAPI docs generation succeeds only when the Documentation generation run starts the application far enough to expose the OpenAPI endpoint.
- The Springdoc custom boot run working directory aligns the Forked Spring Boot run with the Development run.
- The Repository root is the intended location of the Environment file.

## Flagged ambiguities

- "envs" is ambiguous and should be avoided in favor of Runtime configuration when meaning all startup values or Environment file when meaning `.env` specifically.
- "root" is ambiguous and should be avoided in favor of Repository root for the Git project root or `:app` project directory for the module working directory.
- "run" is overloaded and should be qualified as Development run, Documentation generation run, or Forked Spring Boot run.
