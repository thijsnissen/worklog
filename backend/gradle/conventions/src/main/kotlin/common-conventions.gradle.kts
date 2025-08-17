plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.ncorti.ktfmt.gradle")
}

dependencies {
    implementation(versionCatalogUnsafe.findLibrary("dotenv.kotlin").get())
    implementation(versionCatalogUnsafe.findLibrary("kotlin.logging.jvm").get())
    implementation(versionCatalogUnsafe.findLibrary("kotlinx.coroutines").get())
    implementation(versionCatalogUnsafe.findLibrary("slf4j").get())
    runtimeOnly(versionCatalogUnsafe.findLibrary("logback").get())
    testImplementation(versionCatalogUnsafe.findLibrary("kotlinx.coroutines.test").get())
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Werror",
            "-Wextra",
            "-verbose",
            "-Xcontext-parameters",
        )
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            sources {
                kotlin.srcDir("src/it/kotlin")
                resources.srcDir("src/it/resources")
            }
        }

        val endToEndTest by registering(JvmTestSuite::class) {
            sources {
                kotlin.srcDir("src/e2e/kotlin")
                resources.srcDir("src/e2e/resources")
            }
        }

        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
            }

            targets.all {
                testTask.configure {
                    testLogging { events("passed", "skipped", "failed") }
                }
            }
        }
    }
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations["testImplementation"])
}
configurations.named("endToEndTestImplementation") {
    extendsFrom(configurations["integrationTestImplementation"])
}

tasks.check {
    dependsOn("integrationTest", "endToEndTest")
}

ktfmt {
    kotlinLangStyle()
}
