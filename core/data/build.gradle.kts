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
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.support) {
        exclude(group = "com.google.ai.edge.litert", module = "litert-support-api")
    }
}
