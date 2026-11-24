// Basic Pitch 모델 실행(LiteRT). 창 분할·unwrap·후처리는 :core:music, 여기서는 모델만 돌린다.
plugins {
    id("aicompose.android.library")
}

android {
    namespace = "com.largeblueberry.ml"

    androidResources {
        enable = false
    }

    // 기기 테스트에서 :core:music 골든 데이터(Python basic-pitch 출력)와 비교한다
    sourceSets {
        getByName("androidTest") {
            assets.directories.add("../music/src/test/resources")
        }
    }
}

dependencies {
    api(project(":core:music"))
    implementation(libs.litert)
}
