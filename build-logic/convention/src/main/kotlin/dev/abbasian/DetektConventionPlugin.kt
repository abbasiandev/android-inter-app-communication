import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            configureDetekt()
        }
    }

    private fun Project.applyPlugins() {
        pluginManager.apply("io.gitlab.arturbosch.detekt")
    }

    private fun Project.configureDetekt() {
        configure<DetektExtension> {
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            baseline = file("$rootDir/config/detekt/baseline.xml")

            parallel = true

            ignoreFailures = false
        }
    }
}
