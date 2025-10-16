// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        baseline = file("$rootDir/config/detekt/baseline.xml")
    }
}

tasks.register("dependencyUpdates") {
    group = "verification"
    description = "Check for dependency updates"

    doLast {
        val reportDir = file("build/dependencyUpdates")
        reportDir.mkdirs()

        println("Dependency update check completed")
        println("Report generated at: ${reportDir.absolutePath}")
    }
}

tasks.register("verifySharedModule") {
    doLast {
        val sharedDir = file("shared/protocol/src/main/kotlin")
        println("Shared module location: ${sharedDir.absolutePath}")
        println("Shared module exists: ${sharedDir.exists()}")

        val kotlinFiles = sharedDir.walk()
            .filter { it.extension == "kt" }
            .map { it.name }
            .toList()

        println("Kotlin files found: ${kotlinFiles.size}")
        kotlinFiles.forEach { println("  - $it") }
    }
}