plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.aegis.shield"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.aegis.shield"
        minSdk = 26
        targetSdk = 34
        versionCode = 220
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Default to debug keystore for CI/CD if release keystore properties are absent
            val keystoreFile = project.findProperty("KEYSTORE_FILE") as? String ?: "${System.getProperty("user.home")}/.android/debug.keystore"
            if (file(keystoreFile).exists()) {
                storeFile = file(keystoreFile)
                storePassword = project.findProperty("KEYSTORE_PASSWORD") as? String ?: "android"
                keyAlias = project.findProperty("KEY_ALIAS") as? String ?: "androiddebugkey"
                keyPassword = project.findProperty("KEY_PASSWORD") as? String ?: "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
