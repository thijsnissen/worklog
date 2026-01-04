group = "nl.thijsnissen"

description = "Simple service for logging Toggl Track entries in Jira Tempo"

plugins {
    id("application-conventions")
    id("common-conventions")
    id("open-api-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":adapters"))
    implementation(project(":service"))
    implementation(project(":lib-http-client"))
    implementation(project(":lib-http-server"))
    implementation(project(":lib-http-openapi"))
}

application { mainClass = "nl.thijsnissen.worklog.ApplicationKt" }
