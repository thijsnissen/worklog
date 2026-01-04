import utils.DockerImageTag
import utils.versionCatalogUnsafe

version = versionCatalogUnsafe.findVersion("semver").get()

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
        image = "gcr.io/distroless/java25:nonroot@sha256:fa9bfc14924fa3b43d43944d93887155d19843b3aa45610b659496f928fe2a9c"
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
