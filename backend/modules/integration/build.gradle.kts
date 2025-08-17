plugins {
    id("common-conventions")
    id("spring-conventions")
    id("java-test-fixtures")
}

dependencies {
    integrationTestImplementation(libs.okhttp.mockwebserver)
    integrationTestImplementation(project(":adapters"))
    integrationTestImplementation(project(":app"))
    integrationTestImplementation(project(":lib-http-client"))
    integrationTestImplementation(project(":lib-http-server"))
    integrationTestImplementation(project(":service"))
    integrationTestImplementation(testFixtures(project(":service")))
    testFixturesImplementation(libs.okhttp.mockwebserver)
    testFixturesImplementation(libs.spring.boot.starter.test)
    testFixturesImplementation(testFixtures(project(":service")))
}
