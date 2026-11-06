plugins {
    id("aicompose.android.feature")
}

android {
    namespace = "com.largeblueberry.record"

    androidResources {
        enable = false
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}