plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.largeblueberry.library"
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    // ===== 기본 Android =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // ===== 모듈 =====
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":data:local"))
    implementation(project(":data:remote"))

    // ===== HILT =====
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")

    // ===== ROOM =====
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // ===== COMPOSE =====
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose 통합
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.runtime:runtime-livedata")

    // ===== LIFECYCLE & VIEWMODEL =====
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ===== COROUTINES =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ===== NAVIGATION =====
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // ===== NETWORK =====
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ===== DEBUG =====
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ===== TESTS =====
    // Unit Tests
    testImplementation(libs.junit)
    testImplementation("com.google.dagger:hilt-android-testing:2.56.2")
    kspTest("com.google.dagger:hilt-android-compiler:2.56.2")

    // Instrumented Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.56.2")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.56.2")
}