# AiCompose 구조 개요 (archgraph)

- 소스 커밋 `b82eb28` (작업 트리에 커밋 안 된 변경 있음) · 추출 2026-11-24T12:03:50+09:00 · 그래프 archgraph/0.1 · 설정 `없음`
- 모든 줄은 소스에서 추출한 사실이다. 역할 태그와 [c] 표시는 추론이고, 근거를 괄호에 둔다. 판정(규칙 위반 여부)은 하지 않는다.
- 역할 태그: 없음. 도구는 역할을 추측하지 않는다(프로필 꺼짐). 켜려면 설정에 `profile: android-clean`.
- 경로는 각 모듈의 소스 루트 기준이다. `파일:줄`은 그 루트에 붙여서 읽는다.
- 그래프가 읽지 못한 것은 0절에 모았다. 개수나 "없다"를 말하기 전에 0절을 본다.
- **이 개요는 지도다. 정적 분석은 틀리거나 빠질 수 있다.** 사실을 묻는 질문은 개요와 쿼리로 답해도 된다. 판단을 묻는 질문(어긋남, 위험, 의도)은 개요의 항목을 출발점으로 쿼리나 소스로 확인해서 답한다.
- 세부 쿼리: `files <모듈>`(파일·타입·줄·var), `uses <패키지·클래스>`(호출 위치), `bindings <타입>`, `injects <타입>`, `members <타입>`, `deps <모듈>`, `gaps`. 이름이 같은 타입은 `:모듈:FQ이름`.

## 0. 이 요약이 모르는 것

읽지 않은 모듈(도구가 모르는 모듈 종류라 아래 모든 절과 쿼리에 없음): 없음

읽은 모듈 안의 읽지 않은 소스셋(읽은 소스 디렉터리 밖, 테스트 제외. 그 안의 코드는 아래 모든 절에 없음): 없음

그래프가 뜻을 읽지 못한 DI 구문(0건). 걸린 타입의 바인딩·주입은 아래 절에서 빠지거나 다르게 보일 수 있다: 없음
0건·없음은 그래프가 아는 구문만 쓰였다는 뜻이다. 빠진 것이 없다는 보장이 아니다(아는 구문도 잘못 읽을 수 있고, DI 밖의 생성·연결은 보지 않는다).

해석하지 못한 호출(대상이 없어 3절·쿼리 `uses`에 없음): 없음
이유: generated = 빌드하지 않아 생기지 않은 코드(R, BuildConfig 등), classpath = 클래스패스에 없음, other = 그 밖.

추출·투영 범위 밖(있어도 이 요약에 나오지 않는 것):
- 테스트 설정(`testImplementation`, `androidTestImplementation` …)의 모듈 의존. 1절은 그 밖 설정만 보인다.
- 테스트 소스셋(`src/test`, `src/androidTest`), 리소스·매니페스트, 빌드가 만드는 코드(R, BuildConfig, Hilt·Room 생성물).
- 빌드 로직(`buildSrc`, `build-logic` 같은 포함 빌드의 컨벤션 플러그인). 앱 소스가 아니다.
- `sdk.packages` 밖 라이브러리 호출의 위치. 3.2에 모듈별 수만 있다. 위치는 쿼리 `uses <패키지>`로 본다.
- 주입 안 된 객체 생성·정적 호출로 맺는 관계(서비스 로케이터, 수동 생성). 5절은 생성자 주입만이다.

## 1. 모듈과 의존

프로젝트 의존 뒤 괄호는 선언 설정이다(`impl` = implementation, 그 밖은 이름 그대로). 테스트 설정(`testImplementation` 등)은 뺐다.

| 모듈 | 종류 | 계층 | 프로젝트 의존 |
|---|---|---|---|
| `:app` | android-app | - | :feature:sheetmusic (impl), :feature:setting (impl), :feature:library (impl), :feature:record (impl), :core:data (impl), :core:domain (impl), :core:navigation (impl), :core:ui (impl), :core:resources (impl) |
| `:core:data` | android-library | - | :core:domain (impl), :core:ml (impl), :core:music (impl) |
| `:core:domain` | jvm | - | - |
| `:core:ml` | android-library | - | :core:music (api) |
| `:core:music` | jvm | - | - |
| `:core:navigation` | android-library | - | - |
| `:core:resources` | android-library | - | - |
| `:core:ui` | android-library | - | :core:resources (impl), :core:domain (impl) |
| `:feature:library` | android-library | - | :core:resources (impl), :core:ui (impl), :core:domain (impl) |
| `:feature:record` | android-library | - | :core:resources (impl), :core:ui (impl), :core:domain (impl), :core:navigation (impl) |
| `:feature:setting` | android-library | - | :core:resources (impl), :core:ui (impl), :core:domain (impl), :core:navigation (impl) |
| `:feature:sheetmusic` | android-library | - | :core:resources (impl), :core:ui (impl), :core:domain (impl), :core:navigation (impl) |

외부 라이브러리 의존(테스트 설정 제외, 카탈로그 별칭). 모듈 build 파일에 선언한 것:
- `:app`: androidx.activity.compose, androidx.core.splashscreen, androidx.navigation.compose, hilt.work, work.runtime.ktx
- `:core:data`: androidx.datastore.preferences, androidx.hilt.compiler, hilt.work, room.compiler, room.ktx, room.runtime, work.runtime.ktx
- `:core:domain`: javax.inject, kotlinx.coroutines.core
- `:core:ml`: litert
- `:feature:library`: androidx.activity.compose
- `:feature:record`: androidx.activity.compose
- `:feature:setting`: androidx.datastore.preferences, androidx.preference
- `:feature:sheetmusic`: androidx.compose.bom, androidx.compose.ui.test.manifest, androidx.webkit

[c] build 파일에 없고 컨벤션 플러그인이 추가한 것(같은 목록인 모듈은 묶음):
- `:app`: androidx.appcompat, androidx.compose.bom, androidx.compose.foundation, androidx.compose.material.icons.extended, androidx.compose.material3, androidx.compose.ui, androidx.compose.ui.graphics, androidx.compose.ui.tooling, androidx.compose.ui.tooling.preview, androidx.core.ktx, hilt.android, hilt.compiler, hilt.navigation.compose, kotlin-stdlib, material
- `:core:data`: androidx.appcompat, androidx.core.ktx, hilt.android, hilt.compiler, hilt.navigation.compose, kotlin-stdlib, material
- `:core:ml`, `:core:navigation`, `:core:resources`: androidx.appcompat, androidx.core.ktx, kotlin-stdlib, material
- `:core:ui`: androidx.appcompat, androidx.compose.bom, androidx.compose.foundation, androidx.compose.material.icons.extended, androidx.compose.material3, androidx.compose.ui, androidx.compose.ui.graphics, androidx.compose.ui.tooling, androidx.compose.ui.tooling.preview, androidx.core.ktx, kotlin-stdlib, material
- `:feature:library`, `:feature:record`, `:feature:setting`: androidx.appcompat, androidx.compose.bom, androidx.compose.foundation, androidx.compose.material.icons.extended, androidx.compose.material3, androidx.compose.ui, androidx.compose.ui.graphics, androidx.compose.ui.tooling, androidx.compose.ui.tooling.preview, androidx.core.ktx, androidx.lifecycle.runtime.compose, androidx.lifecycle.runtime.ktx, androidx.lifecycle.viewmodel.compose, androidx.lifecycle.viewmodel.ktx, androidx.navigation.compose, hilt.android, hilt.compiler, hilt.navigation.compose, kotlin-stdlib, kotlinx.coroutines.android, material
- `:feature:sheetmusic`: androidx.appcompat, androidx.compose.foundation, androidx.compose.material.icons.extended, androidx.compose.material3, androidx.compose.ui, androidx.compose.ui.graphics, androidx.compose.ui.tooling, androidx.compose.ui.tooling.preview, androidx.core.ktx, androidx.lifecycle.runtime.compose, androidx.lifecycle.runtime.ktx, androidx.lifecycle.viewmodel.compose, androidx.lifecycle.viewmodel.ktx, androidx.navigation.compose, hilt.android, hilt.compiler, hilt.navigation.compose, kotlin-stdlib, kotlinx.coroutines.android, material

## 2. 파일과 타입 (모듈별 개수)

파일·타입·줄·가변 프로퍼티(var)는 쿼리 `files <모듈>`로 본다. 역할 수는 타입마다 첫 역할로 센다.
- `:app` (루트 `app/src/main/java/com/largeblueberry/aicompose/`): 파일 14, 타입 8 — 역할 없음 8; @Composable 함수 12
- `:core:data` (루트 `core/data/src/main/java/com/largeblueberry/data/`): 파일 25, 타입 28 — 역할 없음 28
- `:core:domain` (루트 `core/domain/src/main/java/com/largeblueberry/domain/`): 파일 35, 타입 49 — 역할 없음 49
- `:core:ml` (루트 `core/ml/src/main/java/com/largeblueberry/ml/`): 파일 2, 타입 2 — 역할 없음 2
- `:core:music` (루트 `core/music/src/main/java/com/largeblueberry/music/`): 파일 16, 타입 33 — 역할 없음 33
- `:core:navigation` (루트 `core/navigation/src/main/java/com/largeblueberry/navigation/`): 파일 1, 타입 14 — 역할 없음 14
- `:core:ui` (루트 `core/ui/src/main/java/com/largeblueberry/core_ui/`): 파일 7, 타입 4 — 역할 없음 4; @Composable 함수 3
- `:feature:library` (루트 `feature/library/src/main/java/com/largeblueberry/library/`): 파일 8, 타입 12 — 역할 없음 12; @Composable 함수 6
- `:feature:record` (루트 `feature/record/src/main/java/com/largeblueberry/record/ui/`): 파일 4, 타입 4 — 역할 없음 4; @Composable 함수 7
- `:feature:setting` (루트 `feature/setting/src/main/java/com/largeblueberry/setting/`): 파일 16, 타입 12 — 역할 없음 12; @Composable 함수 23
- `:feature:sheetmusic` (루트 `feature/sheetmusic/src/main/java/com/largeblueberry/feature_sheetmusic/ui/`): 파일 11, 타입 11 — 역할 없음 11; @Composable 함수 14

## 3. 소스 밖 API 호출

3.1은 Android SDK·JDK와 설정 `sdk.packages`(없음)에 속하는 클래스를 호출하는 곳이다. 값·로그 유틸(`java.lang`, `java.util`, `java.text`, `java.time`, `java.math`, `android.util`, `kotlin`)은 빼고 3.2에서 센다. 형식: 묶음(`sdk.packages` 항목, 없으면 패키지) 뒤에 그것을 호출하는 소유자(타입·최상위 함수) 수. 소유자와 위치는 쿼리 `uses <패키지·클래스>`.
- `:app` (-): android 1, android.app 1, android.content 1, android.content.pm 1, android.net 1, android.os 1, android.provider 1
- `:core:data` (-): java.io 4, android.content 3, android.media 3, android.os 1, java.nio.file 1
- `:core:domain` (-): java.io 2
- `:core:ml` (-): android.content 1, android.content.res 1, java.nio 1
- `:core:music` (-): java.io 2, java.nio 1
- `:core:navigation` (-): android.net 1
- `:feature:library` (-): android.content 1, android.media 1
- `:feature:record` (-): android 1, android.content 1, android.content.pm 1, android.content.res 1
- `:feature:setting` (-): android.content 1, android.content.res 1
- `:feature:sheetmusic` (-): android.content 1, android.media 1, android.view 1, android.webkit 1, java.io 1, org.json 1

3.2 그 밖의 라이브러리 호출 수(모듈별 상위 6개). 위치는 쿼리 `uses <패키지>`:
- `:app`: androidx.compose.foundation:foundation-layout-android 61, androidx.compose.material3:material3-android 48, androidx.compose.ui:ui-unit-android 42, androidx.compose.ui:ui-android 34, androidx.compose.foundation:foundation-android 24, androidx.navigation:navigation-runtime-android 22, 기타 102
- `:core:data`: org.jetbrains.kotlin:kotlin-stdlib 96, org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm 45, (kotlin 내장 등) 40, androidx.work:work-runtime 37, JDK(java.lang·java.util 등) 24, Android SDK(값·로그 유틸) 15, 기타 25
- `:core:domain`: org.jetbrains.kotlin:kotlin-stdlib 21, JDK(java.lang·java.util 등) 2
- `:core:ml`: org.jetbrains.kotlin:kotlin-stdlib 10, (kotlin 내장 등) 7, com.google.ai.edge.litert:litert 5, JDK(java.lang·java.util 등) 4
- `:core:music`: org.jetbrains.kotlin:kotlin-stdlib 196, (kotlin 내장 등) 135, JDK(java.lang·java.util 등) 88
- `:core:ui`: androidx.compose.ui:ui-graphics-android 62, androidx.compose.material3:material3-android 15, androidx.compose.ui:ui-unit-android 10, androidx.compose.ui:ui-text-android 10, org.jetbrains.kotlin:kotlin-stdlib 4, androidx.compose.runtime:runtime-android 4, 기타 3
- `:feature:library`: androidx.compose.material3:material3-android 75, androidx.compose.foundation:foundation-layout-android 42, androidx.compose.ui:ui-android 28, org.jetbrains.kotlin:kotlin-stdlib 23, androidx.compose.ui:ui-unit-android 22, org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm 22, 기타 55
- `:feature:record`: androidx.compose.material3:material3-android 52, androidx.compose.foundation:foundation-layout-android 44, androidx.compose.ui:ui-android 33, androidx.compose.ui:ui-unit-android 30, org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm 22, androidx.compose.animation:animation-core-android 17, 기타 56
- `:feature:setting`: androidx.compose.material3:material3-android 145, androidx.compose.foundation:foundation-layout-android 110, androidx.compose.ui:ui-unit-android 69, androidx.compose.ui:ui-android 50, androidx.compose.foundation:foundation-android 22, org.jetbrains.kotlin:kotlin-stdlib 19, 기타 82
- `:feature:sheetmusic`: androidx.compose.material3:material3-android 86, androidx.compose.foundation:foundation-layout-android 64, androidx.compose.ui:ui-android 26, androidx.compose.ui:ui-unit-android 24, androidx.compose.runtime:runtime-android 15, org.jetbrains.kotlin:kotlin-stdlib 12, 기타 62

## 4. DI 바인딩 (Hilt)

바인딩별 위치·주입받는 곳은 쿼리 `bindings <타입>`. 스코프는 `@`, 한정자는 괄호, 구현은 `→`.
주입 래퍼(INJECTS 68개): Provider 0, Lazy 1, 래퍼 없음 67

### SingletonComponent
- 방식별: @Provides 10, @Binds 8, @Inject 생성자 3, @AssistedInject 1
- DI 모듈별 @Provides·@Binds 수: AudioModule 1, DataStoreModule 2, DatabaseModule 3, MlModule 3, RepositoryModule 7, WorkModule 2
- @Inject 생성자(역할별 수): 역할 없음 4
- 보조 주입(@AssistedInject): 1

### ViewModelComponent
- 방식별: @Inject 생성자 8
- @Inject 생성자(역할별 수): 역할 없음 8

### 컴포넌트에 묶이지 않은 바인딩
- 방식별: @Inject 생성자 26
- @Inject 생성자(역할별 수): 역할 없음 26

스코프가 있는 바인딩이 만드는 타입(테스트 대체 제외): @Singleton 12, 그중 가변 프로퍼티(var)가 있는 타입 2. 타입과 var는 쿼리 `bindings <타입>`·`files <모듈>` 또는 `--full`.

## 5. 주입 관계 (생성자 주입)

역할 사이 주입 수. 타입별 목록은 쿼리 `injects <타입>`.
- 한쪽에 역할이 없는 주입: 59
설정에 선언된 역할 규칙(`role-rules`): 없음

