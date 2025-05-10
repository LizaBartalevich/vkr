pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven") } // Добавляем репозиторий Chaquopy
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven") } // Добавляем репозиторий Chaquopy
    }
}
rootProject.name = "MyNewApplication"
include(":app")