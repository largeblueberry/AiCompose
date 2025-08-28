# 이어름 (Eaream) - AI 허밍 악보 생성 앱

[English Version](README.md)

> 허밍을 악보로 변환하는 AI 기반 안드로이드 애플리케이션

[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=flat-square&logo=kotlin&logoColor=white)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)]()
[![Clean Architecture](https://img.shields.io/badge/Clean%20Architecture-FF6B6B?style=flat-square)]()

## 프로젝트 개요

이어름은 사용자의 허밍을 AI 서버로 전송하여 악보로 변환받는 안드로이드 애플리케이션입니다.
음악 이론 지식이 없는 일반 사용자도 직관적으로 사용할 수 있는 모바일 UX에 중점을 두고 개발했습니다.

### 개발 정보
- **개발 기간**: 2025.03.07 ~ 2025.12.19 (졸업작품)
- **개발 환경**: Android Studio, Kotlin
- **배포 계획**: 2025년 11월 Google Play Store
- **역할**: Android 클라이언트 개발 (UI/UX, 아키텍처 설계, 전체 앱 개발)

### 사용자 경험
- **직관적인 UI**: 음악 비전공자도 쉽게 사용할 수 있는 심플한 인터페이스
- **사용량 관리**: 일일 사용 제한 및 사용량 추적 시스템
- **다국어 지원**: 한국어/영어 지원 (리소스 모듈화)

### 🔐 계정 및 데이터 관리
- **Google 로그인**: Firebase Auth를 통한 간편 인증
- **데이터 저장**: Room을 통해 로컬로 3gp 파일 관리. 서버를 통해 악보와 midi 파일 받아서 사용

## 🏗 안드로이드 아키텍처

### 전체 구조
```
📦 Eaream Android App
├── 📂 app (Application Module)
├── 📂 feature (기능별 모듈)
│   ├── recording      # 음성 녹음
│   ├── sheetmusic     # 생성된 악보 보기
│   ├── library        # 녹음된 허밍 관리 및 업로드 관리
│   ├── settings       # 앱 설정 및 다국어 지원
│   └── auth           # 로그인 및 사용자 관리
├── 📂 core (공통 모듈)
│   └── ui            # 디자인 시스템, 공통 컴포넌트
│
├── 📂 data (데이터 레이어)
│   ├── local         # Room 엔티티, DAO
│   └── remote        # retrofit 관련 로직
│
└── 📂 domain (도메인 레이어)
    ├── usertracker  # 사용자 사용량 추적
        ├── model         # 도메인 모델
        ├── usecase       # 비즈니스 로직
        └── repository    # 레포지토리 인터페이스
```

### 주요 기술적 구현

#### 아키텍처 패턴
- **Clean Architecture + MVVM**: 확장성과 테스트 용이성을 위한 계층 분리
- **모듈화**: feature 단위로 모듈을 분리하여 빌드 시간 최적화 및 코드 재사용성 향상
- **단일 액티비티**: Navigation Component를 활용한 효율적인 화면 전환

#### UI/UX 구현
- **Jetpack Compose**: XML에서 Compose로 전면 전환
- **Material 3 Design**: 최신 디자인 가이드라인 적용
- **반응형 UI**: 다양한 화면 크기 대응
- **다크 테마**: 시스템 설정에 따른 자동 테마 전환

#### 성능 최적화
- **Hilt 의존성 주입**: 보일러플레이트 코드 감소 및 테스트 용이성
- **Coroutines + Flow**: 비동기 작업 및 상태 관리 최적화

#### 오디오 처리
- **AudioRecord API**: 음성 녹음 구현
- **권한 관리**: 마이크 권한 요청 및 처리

#### 네트워크 및 데이터
- **Retrofit + OkHttp**: RESTful API 통신
- **Firebase**: Authentication
- **Room Database**: 로컬 데이터베이스를 활용해 녹음 파일 관리

## 기술 스택

### 개발 환경
- **IDE**: Android Studio Narwhal
- **언어**: Kotlin 100%
- **최소 SDK**: API 35
- **타겟 SDK**: API 35

## 라이선스

MIT License

## About Developer

**개발자**: [신용우]  
**연락처**: [onlym3480@naver.com]  