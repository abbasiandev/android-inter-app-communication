import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class AndroidJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            configureJacoco()
        }
    }

    private fun Project.applyPlugins() {
        pluginManager.apply("jacoco")
    }

    private fun Project.configureJacoco() {
        configure<JacocoPluginExtension> {
            toolVersion = "0.8.11"
        }

        applicationGradle {
            buildTypes {
                debug {
                    enableUnitTestCoverage = true
                }
            }

            testOptions {
                unitTests.all {
                    it.configure<JacocoTaskExtension> {
                        isIncludeNoLocationClasses = true
                        excludes = listOf("jdk.internal.*")
                    }
                }
            }
        }

        tasks.withType<Test>().configureEach {
            configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }

        tasks.register<JacocoReport>("jacocoTestReport") {
            dependsOn("testDebugUnitTest")

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
                },
            )
        }
    }
}
