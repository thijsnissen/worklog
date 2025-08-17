plugins {
    id("common-conventions")
    id("java-test-fixtures")
}

dependencies { testFixturesImplementation(libs.junit.jupiter) }
