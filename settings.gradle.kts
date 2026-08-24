// settings.gradle.kts — configuração raiz do build Gradle (épico 8.0).
// Declara os repositórios de plugins e dependências e os módulos do projeto.
// Repositórios: google() (AndroidX/Room) + mavenCentral() (Kotlin/etc).

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

rootProject.name = "Hora do Remédio"
include(":app")
