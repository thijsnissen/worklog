package utils

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations

interface DockerImageTagParameters : ValueSourceParameters {
    val semver: Property<String>
}

abstract class DockerImageTag @Inject constructor(
    private val execOperations: ExecOperations
) : ValueSource<String, DockerImageTagParameters> {
    private val gitShortHash: String? by lazy {
        "git rev-parse HEAD".exec()?.take(8)?.trim()
    }

    private val gitCurrentBranch: String? by lazy {
        "git rev-parse --abbrev-ref HEAD".exec()?.trim()
    }

    private val now: String by lazy {
        Instant.now()
            .atZone(ZoneId.of("Europe/Amsterdam"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
    }

    private fun combine(branch: String, postfix: String): String {
        val normalizedBranch = branch.replace(Regex("[^A-Za-z0-9]"), "-").trim()
        val truncatedBranch = normalizedBranch.take(minOf(63 - postfix.length - 1, normalizedBranch.length))

        return "$truncatedBranch-$postfix"
    }

    private fun String.exec(): String? {
        return runCatching {
            val output = ByteArrayOutputStream()

            execOperations.exec {
                commandLine(split(" "))
                standardOutput = output
            }

            output.toString(StandardCharsets.UTF_8)
        }.getOrNull()
    }

    override fun obtain(): String {
        val semverDate = "${parameters.semver.get()}-$now"

        return gitCurrentBranch?.let { branch ->
            gitShortHash?.let { shortHash ->
                val tag = "$semverDate-$shortHash"

                if (branch == "main" || branch == "master") tag
                else combine(branch, tag)
            }
        } ?: "$semverDate-local"
    }
}
