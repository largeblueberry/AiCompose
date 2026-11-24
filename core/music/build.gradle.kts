import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 순수 JVM 모듈: 채보 후처리(Basic Pitch note creation 포팅), 단성화, 양자화, 조성 추정, MIDI·MusicXML 생성.
// 안드로이드·프로젝트 의존 없이 두어 기기 없이 테스트한다(docs/map/notes.md "모듈과 의존 방향").
plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    testImplementation(libs.junit)
    // 골든 JSON 읽기용(JsonElement API만 써서 직렬화 플러그인은 필요 없다)
    testImplementation(libs.kotlinx.serialization.json)
}
