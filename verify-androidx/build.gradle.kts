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

dependencies {
    implementation("androidx.compose.remote:remote-creation-compose:1.0.0-SNAPSHOT")
    implementation("androidx.compose.remote.foundation:foundation:1.0.0-SNAPSHOT")
    implementation("androidx.compose.runtime:runtime:1.11.0")
    implementation("androidx.compose.ui:ui-graphics:1.11.0")
}
