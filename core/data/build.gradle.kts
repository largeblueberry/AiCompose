plugins {
    id("aicompose.android.library")
    id("aicompose.hilt")
}

android {
    namespace = "com.largeblueberry.data"

    androidResources {
        enable = false
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

    testImplementation(libs.kotlinx.coroutines.test)
}
