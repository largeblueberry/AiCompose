// build.gradle.kts (feature_setting 모듈)

// ✅ 1. 플러그인 블록 위로 파일 읽기 로직을 이동
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

// ✅ 2. local.properties 파일을 여기서 한 번만 읽도록 수정
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.largeblueberry.feature_setting"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // ✅ 3. buildConfigField를 buildTypes가 아닌 defaultConfig로 이동
        // 모든 빌드 유형에 이 설정이 적용됩니다.
        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""}\""
        )
    }

    buildTypes {
        debug {
            // ✅ 4. 여기 있던 buildConfigField 관련 코드를 모두 삭제
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ✅ 4. 여기 있던 buildConfigField 관련 코드를 모두 삭제
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
        buildConfig = true // 이 설정은 필수이며, 올바르게 유지하셨습니다.
    }
}

dependencies {
    // ... dependencies는 기존과 동일합니다 ...
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.material)

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // DataStore (설정 저장용)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")
}