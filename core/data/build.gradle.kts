plugins {
    id("aicompose.android.library")
    id("aicompose.hilt")
}

android {
    namespace = "com.largeblueberry.data"

    androidResources {
        enable = false
    }

    testOptions {
        // 저장소가 쓰는 android.util.Log를 JVM 테스트에서 예외 대신 무시하게 한다
        unitTests.isReturnDefaultValues = true
        // Worker·Scheduler 테스트(Robolectric)가 병합된 매니페스트로 WorkManager를 띄운다
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // 채보: 모델 실행(:core:ml) + 후처리·악보 생성(:core:music)
    implementation(project(":core:ml"))
    implementation(project(":core:music"))
    // 채보를 화면 수명과 상관없이 돌리는 백그라운드 작업
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.work.testing)
}
