plugins {
    id("aicompose.android.feature")
}

android {
    namespace = "com.largeblueberry.setting"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(libs.androidx.preference)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
}
