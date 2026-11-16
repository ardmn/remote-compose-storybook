// Compiles the stories, rewritten to androidx.compose.remote.* exactly as the Code tab shows them,
// against AndroidX's own artifacts: a snapshot of androidx-main from androidx.dev, pinned by build
// in ../reference/androidx-api/SNAPSHOT_BUILD. Proof that what a user copies compiles.
rootProject.name = "verify-androidx"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

val snapshotBuild = file("../reference/androidx-api/SNAPSHOT_BUILD").readText().trim()

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://androidx.dev/snapshots/builds/$snapshotBuild/artifacts/repository") {
            content { includeGroupByRegex("androidx\\..*") }
        }
    }
}
