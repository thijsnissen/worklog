import utils.DockerImageTag

group = "nl.thijsnissen"
version = "0.1.0-SNAPSHOT"

description = "Simple service for logging Toggl Track entries in Jira Tempo"

plugins {
    application
    id("com.google.cloud.tools.jib")
    id("org.springframework.boot")
}

val dockerImageTagProvider = providers.of(DockerImageTag::class) { parameters.semver = version.toString() }

jib {
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8080")
    }

    from {
        platforms {
            platform {
                os = "linux"
                architecture = "amd64"
            }
            platform {
                os = "linux"
                architecture = "arm64"
            }
        }
        image = "gcr.io/distroless/java21:nonroot@sha256:fb0d294a2ba6edffc3776a87dd2dce9771801a3fb0aa7319d51300239dd51aeb"
    }
    to {
        image = rootProject.name
        tags = setOf(
            dockerImageTagProvider.get(),
            "latest"
        )
    }
}

tasks.bootJar {
    enabled = true
}
