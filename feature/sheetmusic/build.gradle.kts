plugins {
    id("aicompose.android.feature")
}

android {
    namespace = "com.largeblueberry.feature_sheetmusic"

    androidResources {
        enable = false
    }

    // ViewModel이 쓰는 android.util.Log를 JVM 테스트에서 예외 대신 무시하게 한다
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(libs.androidx.webkit)
    testImplementation(libs.kotlinx.coroutines.test)
}
