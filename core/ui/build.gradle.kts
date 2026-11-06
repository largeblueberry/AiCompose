plugins {
    id("aicompose.android.library")
    id("aicompose.android.compose")
}

android {
    namespace = "com.largeblueberry.ui"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:domain"))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}