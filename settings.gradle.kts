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
        maven {
            url = uri("https://sdk-repo.dev.vida.id/android")
            credentials(HttpHeaderCredentials::class) {
                name = "x-api-key"
                value = "8iVpbW8vRQL2L1uB"
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }

    }
}

rootProject.name = "VidaCompose"
include(":app")
 