plugins {
    id("common-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":lib-http-client"))
    implementation(project(":lib-http-openapi"))
    implementation(project(":lib-http-server"))
    implementation(project(":service"))
    runtimeOnly(libs.h2.database)
    runtimeOnly(libs.h2.r2dbc)
    testImplementation(project(":app"))
    testImplementation(testFixtures(project(":service")))
}
