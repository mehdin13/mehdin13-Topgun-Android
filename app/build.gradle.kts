plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.google.services)  // enable once app/google-services.json (Firebase) is added
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
        val localProps = java.util.Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        fun key(name: String) = (localProps.getProperty(name) ?: providers.gradleProperty(name).orNull ?: "").trim()
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${key("topgun.googleWebClientId")}\"")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"${key("topgun.revenueCatKey")}\"")
        manifestPlaceholders["mapsApiKey"] = key("topgun.mapsApiKey")
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("topgun.apiBaseUrl.debug").get()}\"")
        }
        release {
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
