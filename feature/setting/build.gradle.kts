import org.gradle.kotlin.dsl.implementation
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}


val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.largeblueberry.setting"
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = 35

        buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")


        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""}\""
        )

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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:auth"))
    implementation(project(":core:navigation"))
    implementation(project(":core:analytics-api"))

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
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
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")


    //test
    // Kotlin Coroutines Test (코루틴 테스트를 위한 디스패처 및 유틸리티)
    // runTest, StandardTestDispatcher, setMain, resetMain, advanceUntilIdle 등을 사용하기 위함
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // MockK (모의 객체 생성 및 행위 정의를 위한 라이브러리)
    // mockk, every, coEvery, just, runs, any, coVerify 등을 사용하기 위함
    testImplementation("io.mockk:mockk:1.13.8")
    // JUnit (테스트 프레임워크) - 보통 기본으로 추가되어 있지만, 혹시 없다면 추가
    testImplementation("junit:junit:4.13.2")
}