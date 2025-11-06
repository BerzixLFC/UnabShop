pluginManagement {
    repositories {
        // Fuentes para los plugins
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Fuentes para las dependencias (como firestore, auth, etc.)
        google()
        mavenCentral()
    }
}

rootProject.name = "UnabShop"
include(":app")