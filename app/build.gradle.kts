plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val debugKeystore = file("debug.keystore")
val debugStorePassword = providers.gradleProperty("C11005_DEBUG_STORE_PASSWORD")
    .orElse(providers.environmentVariable("C11005_DEBUG_STORE_PASSWORD"))
    .orNull
val debugKeyAlias = providers.gradleProperty("C11005_DEBUG_KEY_ALIAS")
    .orElse(providers.environmentVariable("C11005_DEBUG_KEY_ALIAS"))
    .orNull
val debugKeyPassword = providers.gradleProperty("C11005_DEBUG_KEY_PASSWORD")
    .orElse(providers.environmentVariable("C11005_DEBUG_KEY_PASSWORD"))
    .orNull
val hasCustomDebugSigning = debugKeystore.isFile &&
    listOf(debugStorePassword, debugKeyAlias, debugKeyPassword).all { !it.isNullOrBlank() }

android {
    namespace = "com.example.money_log"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.money_log"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            if (hasCustomDebugSigning) {
                storeFile = debugKeystore
                storePassword = debugStorePassword
                keyAlias = debugKeyAlias
                keyPassword = debugKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
