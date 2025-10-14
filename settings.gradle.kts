pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mahdiAbbasainMohamadiAndroidCodeChallenge"

include(":shared:protocol")
include(":location-app:app")
include(":internet-app:app")