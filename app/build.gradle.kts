import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.largeblueberry.aicompose"
    compileSdk = 35

    androidResources {
        generateLocaleConfig = false
        localeFilters += setOf("ko", "en")  // 이렇게 변경
    }

    defaultConfig {
        applicationId = "com.largeblueberry.aicompose"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"\"")
        buildConfigField("String", "BASE_URL", "\"\"")

    }

    buildTypes {
        debug {
            // local.properties에서 BASE_URL 읽기
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
            } else {
                throw GradleException("❌ local.properties 파일이 없습니다! 프로젝트 루트에 local.properties 파일을 생성해주세요.")
            }

            val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID")
                ?: throw GradleException("❌ GOOGLE_CLIENT_ID를 local.properties에 설정해주세요!")

            val baseUrl = localProperties.getProperty("BASE_URL")
                ?: throw GradleException("❌ BASE_URL을 local.properties에 설정해주세요!")

            buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
            }

            val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""
            val baseUrl = localProperties.getProperty("BASE_URL") ?: ""

            buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ===== 프로젝트 모듈 =====
    implementation(project(":feature:sheetmusic"))
    implementation(project(":feature:setting"))
    implementation(project(":core:ui"))
    implementation(project(":core:resources"))
    implementation(project(":core:domain"))
    implementation(project(":core:auth"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:library"))
    implementation(project(":feature:record"))
    implementation(project(":core:analytics-impl"))

    // ===== 기본 Android 라이브러리 (libs.versions.toml 활용) =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    // ===== COMPOSE BOM - 모든 Compose 라이브러리 버전 통합 관리 =====
    implementation(platform(libs.androidx.compose.bom))

    // Compose 핵심 라이브러리들 (BOM에 의해 버전 자동 관리)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime") // 명시적으로 추가하여 충돌 방지

    // Material Design
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Activity Compose 통합
    implementation("androidx.activity:activity-compose")

    // 디버깅 및 미리보기 도구
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ===== Navigation =====
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // ===== Hilt (중복 제거 및 정리) =====
    implementation(libs.hilt.android)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")

    // ===== 테스트 =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}