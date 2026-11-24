# AiCompose 컨벤션 빈도 (archgraph, 시제품)

- 대상: 소스 타입 176개, 소스 함수 313개(생성자·합성·지역 제외). 비율의 모집단은 같은 종류 전체다.
- 축: 타입은 직접 상위 타입·애노테이션·이름 첫/끝 단어, 함수는 애노테이션·이름 첫/끝 단어. 한 무리는 3개 이상.
- 특징: 타입은 선언 프로퍼티(가시성·val/var·머리 타입)·생성자 매개변수·프로퍼티 초기화 호출·애노테이션, 함수는 매개변수·반환 타입·호출·애노테이션·suspend·확장·최상위 여부.
- 문턱: 특징을 가진 구성원 3개 이상, 무리 안 60% 이상, 모집단 비율의 2.0배 이상.
- **서술이지 규칙이 아니다.** 다수가 새 방식인지 옛 방식인지는 알 수 없다. 예외는 위반이 아니라 다르게 쓴 곳이다.
- 개요다: 구성원이 같은 특징은 "함께:"로 한 줄에, 예외 이름은 3개 이하일 때만 적고(넘으면 비율만), 특징의 패키지 이름과 예외의 디렉터리는 뺐다. 전부와 전체 이름·경로는 `--full`.
- 구성원이 같은 무리는 합쳤다. 구성원 대부분(60% 이상)이 더 큰 무리에 들어 있으면 그 무리 대비로도 2.0배를 넘는 특징만 적는다. 기본 특징(kotlin 표준 타입·함수·애노테이션, 가시성)은 무리마다 한 줄로 따로 적는다.
- 타입: 무리 33개 중 문턱을 넘는 특징이 있는 무리 2개.
- 함수: 무리 53개 중 문턱을 넘는 특징이 있는 무리 15개(상한, 큰 무리부터).

## 타입 · 이름 끝 단어 `Model` — 12개

- 8/12 (67%, 전체 8/176의 14.7배) 애노테이션 @HiltViewModel

## 타입 · 이름 끝 단어 `Params` — 4개

- 기본: 생성자 매개변수 : Double 3/4; 생성자 매개변수 : Int 3/4

## 함수 · 애노테이션 `@androidx.compose.runtime.Composable` — 65개

- 63/65 (97%, 전체 71/313의 4.3배) 최상위 함수
  - 예외: getDisplayName() (`AppThemeDisplayNameProvider.kt:16`), getDisplayName() (`ThemeDisplayNameProvider.kt:11`)

## 함수 · 이름 끝 단어 `Screen` — 15개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 15/15)

- 12/15 (80%, 전체 17/313의 14.7배) 호출 Modifier.fillMaxSize()
  - 예외: CardViewScreen() (`CardViewScreen.kt:29`), SheetMusicScreen() (`SheetMusicScreen.kt:64`), SheetMusicHistoryScreen() (`ScoreHistoryScreen.kt:20`)
- 기본: 애노테이션 @kotlin.OptIn 10/15

## 함수 · 이름 끝 단어 `Record` — 12개

- 9/12 (75%, 전체 43/313의 5.5배) suspend
  - 예외: latestScoreByRecord() (`RecordTranscription.kt:21`), deleteRecord() (`LibraryViewModel.kt:58`), renameRecord() (`LibraryViewModel.kt:67`)

## 함수 · 이름 첫 단어 `set` — 10개

- 8/10 (80%, 전체 43/313의 5.8배) suspend
  - 예외: setOnCompletionListener() (`AudioPlayer.kt:11`), setBpm() (`RecordViewModel.kt:93`)

## 함수 · 이름 첫 단어 `delete` — 8개

- 6/8 (75%, 전체 43/313의 5.5배) suspend
  - 예외: deleteRecord() (`LibraryViewModel.kt:58`), deleteScore() (`ScoreHistoryViewModel.kt:30`)

## 함수 · 이름 끝 단어 `Item` — 7개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 7/7)

- 7/7 (100%, 전체 23/313의 13.6배) 호출 Modifier.fillMaxWidth() · 함께: Modifier.padding()
- 6/7 (86%, 전체 8/313의 33.5배) 호출 Modifier.clickable()
  - 예외: ThemeOptionItem() (`ThemeSettingScreen.kt:109`)
- 6/7 (86%, 전체 9/313의 29.8배) 호출 Alignment.Companion.CenterVertically · 함께: Row(); Icon()
  - 예외: OpenSourceLicenseItem() (`OpenSourceLicenseSection.kt:41`)
- 6/7 (86%, 전체 19/313의 14.1배) 호출 MaterialTheme.colorScheme · 함께: MaterialTheme.typography
  - 예외: SettingItem() (`SettingItem.kt:25`)
- 5/7 (71%, 전체 6/313의 37.3배) 호출 RowScope.Modifier.weight()
  - 예외: OpenSourceLicenseItem() (`OpenSourceLicenseSection.kt:41`), LanguageItem() (`LanguageSettingScreen.kt:65`)
- 5/7 (71%, 전체 9/313의 24.8배) 호출 Typography.bodyLarge
  - 예외: ThemeOptionItem() (`ThemeSettingScreen.kt:109`), SettingItem() (`SettingItem.kt:25`)
- 5/7 (71%, 전체 14/313의 16.0배) 호출 Icons.Default
  - 예외: OpenSourceLicenseItem() (`OpenSourceLicenseSection.kt:41`), ThemeOptionItem() (`ThemeSettingScreen.kt:109`)

## 함수 · 이름 끝 단어 `Score` — 7개

- 6/7 (86%, 전체 43/313의 6.2배) suspend
  - 예외: deleteScore() (`ScoreHistoryViewModel.kt:30`)

## 함수 · 이름 첫 단어 `Score` — 6개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 6/6)

- 5/6 (83%, 전체 23/313의 11.3배) 호출 Modifier.fillMaxWidth()
  - 예외: ScoreHistoryItemPreview() (`ScoreHistoryItem.kt:81`)

## 함수 · 이름 첫 단어 `Record` — 5개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 5/5, 애노테이션 `@androidx.compose.ui.tooling.preview.Preview` + 이름 끝 단어 `Preview` 3/5)

- 4/5 (80%, 전체 4/313의 62.6배) 호출 RecordScreenUi()
  - 예외: RecordScreenUi() (`RecordScreenUi.kt:64`)
- 3/5 (60%, 전체 3/313의 62.6배) 호출 RecordUiState()
  - 예외: RecordScreenState() (`RecordScreenState.kt:29`), RecordScreenUi() (`RecordScreenUi.kt:64`)

## 함수 · 이름 첫 단어 `Sheet` — 5개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 5/5)

- 3/5 (60%, 전체 3/313의 62.6배) 호출 SheetMusicContent()
  - 예외: SheetMusicContent() (`SheetMusicScreen.kt:76`), SheetMusicHistoryScreen() (`ScoreHistoryScreen.kt:20`)

## 함수 · 이름 첫 단어 `write` — 4개

- 3/4 (75%, 전체 3/313의 78.3배) 매개변수 : ByteArrayOutputStream · 함께: 호출 ByteArrayOutputStream.write()
  - 예외: writeAtomically() (`TranscriptionRepositoryImpl.kt:150`)
- 기본: 호출 kotlin.Int.ushr() 3/4; 호출 kotlin.Int.and() 3/4; private 3/4

## 함수 · 이름 끝 단어 `Audio` — 3개

- 3/3 (100%, 전체 5/313의 62.6배) 호출 LibraryViewModel.audioPlayer · 함께: LibraryUiState.copy(); LibraryViewModel._uiState; MutableStateFlow.update()

## 함수 · 이름 끝 단어 `Card` — 3개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 3/3, 이름 첫 단어 `Score` 2/3)

- 3/3 (100%, 전체 7/313의 44.7배) 호출 CardDefaults.cardElevation() · 함께: Card(); CardDefaults.cardColors(); Modifier.height() 외 1
- 기본: private 3/3

## 함수 · 이름 첫 단어 `Term` — 3개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 3/3)

- 3/3 (100%, 전체 30/313의 10.4배) 호출 Column() · 함께: Modifier.padding()

## 함수 · 이름 첫 단어 `Theme` — 3개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 3/3)

- 3/3 (100%, 전체 30/313의 10.4배) 호출 Column()

