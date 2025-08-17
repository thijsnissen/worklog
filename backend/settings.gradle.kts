pluginManagement {
    includeBuild("gradle/conventions")
}

rootProject.name = "worklog"

include( ":adapters", ":app", ":service", ":integration", ":lib-http-client", ":lib-http-server", ":lib-http-openapi")

project(":adapters").projectDir = file("modules/adapters")
project(":app").projectDir = file("modules/app")
project(":service").projectDir = file("modules/service")
project(":integration").projectDir = file("modules/integration")
project(":lib-http-client").projectDir = file("modules/libs/http/client")
project(":lib-http-server").projectDir = file("modules/libs/http/server")
project(":lib-http-openapi").projectDir = file("modules/libs/http/openapi")
