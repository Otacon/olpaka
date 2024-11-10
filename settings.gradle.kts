rootProject.name = "Olpaka"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Used to retrieve all the platform-dependent native libraries to build Conveyor
        // See composeApp/build.gradle.kts region Conveyour fixes
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

include(":composeApp")