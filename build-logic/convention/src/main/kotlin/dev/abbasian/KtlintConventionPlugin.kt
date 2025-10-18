import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            configureKtlint()
        }
    }

    private fun Project.applyPlugins() {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    }

    private fun Project.configureKtlint() {
        configure<KtlintExtension> {
            version.set("1.0.1")
            android.set(true)
            verbose.set(true)
            outputToConsole.set(true)
            ignoreFailures.set(false)

            filter {
                exclude("**/generated/**")
                exclude("**/build/**")
            }
        }
    }
}
