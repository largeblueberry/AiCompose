plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.largeblueberry.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ===== COMPOSE =====
    // Compose BOM - 모든 Compose 라이브러리 버전 관리 (필수)
    // BOM을 사용하면 아래 개별 라이브러리에서 버전을 명시할 필요가 없습니다.
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))

    // 핵심 Compose UI (필수: Color 클래스 및 기본 UI 요소 포함)
    implementation("androidx.compose.ui:ui")
    // UI 툴링 프리뷰 (개발 편의성: @Preview 사용을 위해 필요)
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling") // Android Studio에서 UI 툴링 활성화

    // Compose Foundation (필수: Box, Column, Row 등 기본 레이아웃 및 상호작용 요소)
    implementation("androidx.compose.foundation:foundation")

    // Material Design 3 (권장: 대부분의 UI는 Material Design 기반이므로 포함하는 것이 좋음)
    implementation("androidx.compose.material3:material3")
}