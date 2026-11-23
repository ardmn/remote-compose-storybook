// Compiles the stories, rewritten to androidx.compose.remote.* exactly as the Code tab shows them,
// against AndroidX's own artifacts: the release in ../reference/androidx-api/RELEASE from Google
// Maven, or else a snapshot of androidx-main from androidx.dev, pinned by build in
// ../reference/androidx-api/SNAPSHOT_BUILD. Proof that what a user copies compiles.
rootProject.name = "verify-androidx"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

val snapshotBuild =
    file("../reference/androidx-api/SNAPSHOT_BUILD").takeIf { it.isFile }?.readText()?.trim()

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        if (snapshotBuild != null) {
            maven("https://androidx.dev/snapshots/builds/$snapshotBuild/artifacts/repository") {
                content { includeGroupByRegex("androidx\\..*") }
            }
        }
    }
}
