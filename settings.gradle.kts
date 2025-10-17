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
include(":location-app:feature:location")
include(":location-app:feature:command")
include(":location-app:core:common")
include(":location-app:core:security")
include(":location-app:core:database")
include(":internet-app:app")
