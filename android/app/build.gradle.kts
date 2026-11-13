import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// The shared signing key (ADR-001 §2.1) comes from local.properties, on laptops and in CI.
val localProperties =
    Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { load(it) }
    }
val keystorePath: String? = localProperties.getProperty("echocare.keystore")

android {
    namespace = "ro.echocare"
    compileSdk = 37

    defaultConfig {
        applicationId = "ro.echocare"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (keystorePath != null) {
            create("echocare") {
                storeFile = file(keystorePath)
                storePassword = localProperties.getProperty("echocare.keystore.password")
                keyAlias = localProperties.getProperty("echocare.key.alias", "echocare")
                keyPassword = localProperties.getProperty("echocare.key.password")
            }
        }
    }

    buildTypes {
        // shortcut: without the shared key, debug falls back to the per-machine debug key; drop this once every laptop has it.
        val shared = signingConfigs.findByName("echocare")
        debug {
            // Installs beside the release app instead of clashing with it.
            applicationIdSuffix = ".dev"
            if (shared != null) signingConfig = shared
        }
        release {
            signingConfig = shared
            isMinifyEnabled = false
        }
    }

    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
}
