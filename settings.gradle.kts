pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "AiCompose"
include(":app")
include(":feature:sheetmusic")
include(":feature:setting")
include(":core:ui")
include(":feature:auth")
include(":domain:usertracker")
include(":feature:library")
include(":data:local")
include(":data:remote")
include(":feature:record")
include(":core:navigation")
include(":core:auth")
