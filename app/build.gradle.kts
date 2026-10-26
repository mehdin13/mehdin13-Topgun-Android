import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Firebase (push) is switched on automatically when app/google-services.json exists (CI writes it from a secret).
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "app.topgun.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.topgun.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        // Keys come from local.properties / CI, never from source control.
        // Google OAuth *web* client ID (for Credential Manager) and the Google Maps key.
        val localProps = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        // trim() and strip accidental quotes: a secret pasted as "AIza..." would otherwise break the key silently
        fun key(name: String) = (localProps.getProperty(name) ?: providers.gradleProperty(name).orNull ?: "").trim().trim('"', '\'').trim()
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${key("topgun.googleWebClientId")}\"")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"${key("topgun.revenueCatKey")}\"")
        manifestPlaceholders["mapsApiKey"] = key("topgun.mapsApiKey")
        // Masked fingerprint of the key baked into this build, shown next to map errors so a stale key is obvious.
        val mapsKey = key("topgun.mapsApiKey")
        val mapsKeyHint = if (mapsKey.length > 14) mapsKey.take(10) + "…" + mapsKey.takeLast(4) else if (mapsKey.isEmpty()) "(empty)" else "(too short)"
        buildConfigField("String", "MAPS_KEY_HINT", "\"$mapsKeyHint\"")
    }

    // CI signs with a fixed test key (so the Google sign-in SHA-1 stays the same between builds).
    // Locally these variables are unset and Android Studio's own debug key is used.
    val ciKeystore = System.getenv("TOPGUN_KEYSTORE_PATH")
    signingConfigs {
        if (ciKeystore != null) {
            create("ci") {
                storeFile = file(ciKeystore)
                storePassword = System.getenv("TOPGUN_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TOPGUN_KEY_ALIAS")
                keyPassword = System.getenv("TOPGUN_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // local.properties may override the API (see gradle.properties); otherwise the live API is used.
            val localApi = Properties().apply {
                rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
            }.getProperty("topgun.apiBaseUrl")?.trim().orEmpty()
            buildConfigField("String", "API_BASE_URL", "\"${localApi.ifEmpty { providers.gradleProperty("topgun.apiBaseUrl.debug").get() }}\"")
            if (ciKeystore != null) signingConfig = signingConfigs.getByName("ci")
        }
        release {
            if (ciKeystore != null) signingConfig = signingConfigs.getByName("ci")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("topgun.apiBaseUrl.release").get()}\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.browser)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.security.crypto)

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.youtube.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.sceneview)
    implementation(libs.arsceneview)
    implementation(libs.lottie.compose)
    implementation(libs.revenuecat.purchases)
    implementation(libs.revenuecat.purchases.ui)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
