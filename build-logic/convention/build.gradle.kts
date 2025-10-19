plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidJacoco") {
            id = "mahdiAbbasainMohamadiAndroidCodeChallenge.android.jacoco"
            implementationClass = "AndroidJacocoConventionPlugin"
        }
        register("androidDetekt") {
            id = "mahdiAbbasainMohamadiAndroidCodeChallenge.android.detekt"
            implementationClass = "DetektConventionPlugin"
        }
        register("androidKtlint") {
            id = "mahdiAbbasainMohamadiAndroidCodeChallenge.android.ktlint"
            implementationClass = "KtlintConventionPlugin"
        }
    }
}
