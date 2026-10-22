// Type-checks the Compose UI (app/) on the JVM with Compose Multiplatform, because the Android SDK and Google's
// Maven (dl.google.com) are not reachable in the Claude cloud session. Android-only pieces are replaced by
// shims in shims/ (see check.sh). This proves the code compiles; it does NOT run it and is not the real build.
plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
}
dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
kotlin { jvmToolchain(21) }
