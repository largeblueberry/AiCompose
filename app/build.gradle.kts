plugins {
    id("aicompose.android.application")
    id("aicompose.android.compose")
    id("aicompose.hilt")
}

android {
    namespace = "com.largeblueberry.aicompose"

    androidResources {
        generateLocaleConfig = false
        localeFilters += setOf("ko", "en")
    }

    defaultConfig {
        applicationId = "com.largeblueberry.aicompose"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // 1. Feature Modules
    implementation(projects.feature.sheetmusic)
    implementation(projects.feature.setting)
    implementation(projects.feature.library)
    implementation(projects.feature.record)

    // 2. Core Modules
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)
    implementation(projects.core.resources)

    // 3. App-Specific Dependencies
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
