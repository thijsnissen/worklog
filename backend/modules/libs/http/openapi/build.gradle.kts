plugins {
    id("common-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(libs.swagger.models)
    implementation(libs.swagger.core.jakarta)
}
