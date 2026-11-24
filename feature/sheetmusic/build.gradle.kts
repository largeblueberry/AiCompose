plugins {
    id("aicompose.android.feature")
}

android {
    namespace = "com.largeblueberry.feature_sheetmusic"

    androidResources {
        enable = false
    }

    testOptions {
        // ViewModel이 쓰는 android.util.Log를 JVM 테스트에서 예외 대신 무시하게 한다
        unitTests.isReturnDefaultValues = true
        // Robolectric 테스트(ScoreWebView)가 병합된 매니페스트로 테스트용 액티비티를 띄운다
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(libs.androidx.webkit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
