plugins {
    id("com.android.library") version "9.5.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21"
}

android {
    namespace = "rcstorybook.verify"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
}

// Written by `tools/storybook_tools.py sources --verify-out` (scripts/verify-androidx.sh).
// Each Code tab snippet is also compiled on its own, in its own package.
android.sourceSets.named("main") {
    kotlin.directories.add("build/generated/stories")
    kotlin.directories.add("build/generated/snippets")
}

// The released versions in ../reference/androidx-api/RELEASE (`artifact=version` lines), or the
// androidx.dev snapshot's 1.0.0-SNAPSHOT when there is no RELEASE file. A RELEASE file must pin
// every artifact: a missing line would otherwise compile that one against a snapshot.
val releaseFile =
    rootProject
        .file("../reference/androidx-api/RELEASE")
        .takeIf { it.isFile }
val releasePins =
    releaseFile
        ?.readLines()
        .orEmpty()
        .filter { "=" in it }
        .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }

fun version(artifact: String): String =
    if (releaseFile == null) "1.0.0-SNAPSHOT"
    else releasePins[artifact]?.takeIf { it.isNotEmpty() } ?: error("$releaseFile does not pin $artifact")

dependencies {
    implementation("androidx.compose.remote:remote-creation-compose:" + version("remote-creation-compose"))
    implementation("androidx.compose.remote.foundation:foundation:" + version("foundation"))
    implementation("androidx.wear.compose.remote:remote-material3:" + version("remote-material3"))
    implementation("androidx.compose.runtime:runtime:1.11.0")
    implementation("androidx.compose.ui:ui-graphics:1.11.0")
}
