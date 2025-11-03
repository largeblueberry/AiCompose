plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)  // ✅ Compose 컴파일러 플러그인 추가 (필수!)
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
        compose = true  // ✅ Compose 활성화 (필수!)
        buildConfig = true
    }
}

dependencies {
    // ===== 기본 Android 라이브러리 =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ===== 프로젝트 모듈 =====
    api(project(":core:resources"))

    // ===== COMPOSE BOM - 버전 통합 관리 =====
    implementation(platform(libs.androidx.compose.bom))  // ✅ libs.versions.toml 사용

    // ===== 핵심 Compose 라이브러리 =====
    // 핵심 UI (Color, Modifier 등 기본 요소)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")

    // Foundation (Box, Column, Row 등 레이아웃)
    implementation("androidx.compose.foundation:foundation")

    // Runtime (remember, State 등)
    implementation("androidx.compose.runtime:runtime")

    // Material Design 3
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ===== 개발 도구 (디버그 빌드에서만) =====
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ===== Compose 테스트 =====
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}