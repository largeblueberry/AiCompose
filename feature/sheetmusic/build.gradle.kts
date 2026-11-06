plugins {
    id("aicompose.android.feature")
}

android {
    namespace = "com.largeblueberry.feature_sheetmusic"

    androidResources {
        enable = false
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(libs.coil.compose)
}
