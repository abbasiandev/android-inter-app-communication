// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
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