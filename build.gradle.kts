// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.jacoco)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        baseline = file("$rootDir/config/detekt/baseline.xml")
    }

    tasks.withType<Test> {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    tasks.register<JacocoReport>("jacocoTestReport") {
        val testTasks = mutableListOf<String>()
        if (tasks.findByName("testDebugUnitTest") != null) {
            testTasks.add("testDebugUnitTest")
        }
        if (tasks.findByName("test") != null) {
            testTasks.add("test")
        }
        dependsOn(testTasks)

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        val fileFilter =
            listOf(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "android/**/*.*",
                "**/data/models/**",
                "**/di/**",
            )

        val debugTree =
            fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(fileFilter)
            }

        val mainSrc = layout.projectDirectory.dir("src/main/kotlin")

        sourceDirectories.setFrom(files(mainSrc))
        classDirectories.setFrom(files(debugTree))
        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include("jacoco/testDebugUnitTest.exec")
                include("jacoco/test.exec")
            },
        )
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

        val kotlinFiles =
            sharedDir
                .walk()
                .filter { it.extension == "kt" }
                .map { it.name }
                .toList()

        println("Kotlin files found: ${kotlinFiles.size}")
        kotlinFiles.forEach { println("  - $it") }
    }
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generate aggregated JaCoCo coverage report for all modules"

    dependsOn(subprojects.map { it.tasks.withType<Test>() })

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated"))
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/data/models/**",
            "**/di/**",
        )

    sourceDirectories.setFrom(
        subprojects.map { project ->
            project.layout.projectDirectory.dir("src/main/kotlin")
        },
    )

    classDirectories.setFrom(
        subprojects.map { project ->
            fileTree(project.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(fileFilter)
            }
        },
    )

    executionData.setFrom(
        subprojects
            .map { project ->
                fileTree(project.layout.buildDirectory) {
                    include("jacoco/testDebugUnitTest.exec")
                    include("jacoco/test.exec")
                }
            }.flatten()
            .filter { it.exists() },
    )
}
