import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    alias(libs.plugins.compose.compiler)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.largeblueberry.aicompose"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.largeblueberry.aicompose"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // local.properties에서 BASE_URL 읽기
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
            }else {
                println("❌ local.properties 파일이 없습니다!")
            }

            buildConfigField("String", "GOOGLE_CLIENT_ID",
                "\"${localProperties.getProperty("GOOGLE_CLIENT_ID")}\"")

            buildConfigField("String", "BASE_URL", "\"${localProperties.getProperty("BASE_URL", 
                "https://teamproject.p-e.kr")}\"")
        }
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
        viewBinding = true
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":feature:sheetmusic"))
    implementation(project(":feature:setting"))
    //모듈
    implementation(project(":core:ui"))
    implementation(project(":feature:auth"))

    // 기본 Android 라이브러리
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.hilt.android)
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room
    val room_version = "2.6.1" // 최신 버전으로 업데이트
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7") // 업데이트
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // 안정 버전 사용
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ===== COMPOSE =====
    // Compose BOM - 모든 Compose 라이브러리 버전 관리
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))

    // 핵심 Compose UI (BOM 사용 시 버전 명시 불필요)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")

    // Material Design 3 (권장)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose 통합
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7") // 추가
    implementation("androidx.compose.runtime:runtime-livedata")

    // Navigation Compose (추가 권장)
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Compose Animation (추가 권장)
    implementation("androidx.compose.animation:animation")

    // 디버깅 도구
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 테스트
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}