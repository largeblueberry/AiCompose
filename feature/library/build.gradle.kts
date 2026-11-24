plugins {
    id("aicompose.android.feature")
}

android {
    namespace = "com.largeblueberry.library"

    androidResources {
        enable = false
    }

    testOptions {
        // ViewModel이 쓰는 android.util.Log를 JVM 테스트에서 예외 대신 무시하게 한다
        unitTests.isReturnDefaultValues = true
        // ViewModel 테스트(Robolectric)가 AudioPlayer에 넘길 Context를 띄운다
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
}
