# AiCompose 컨벤션 빈도 (archgraph, 시제품)

- 대상: 소스 타입 91개, 소스 함수 184개(생성자·합성·지역 제외). 비율의 모집단은 같은 종류 전체다.
- 축: 타입은 직접 상위 타입·애노테이션·이름 첫/끝 단어, 함수는 애노테이션·이름 첫/끝 단어. 한 무리는 3개 이상.
- 특징: 타입은 선언 프로퍼티(가시성·val/var·머리 타입)·생성자 매개변수·프로퍼티 초기화 호출·애노테이션, 함수는 매개변수·반환 타입·호출·애노테이션·suspend·확장·최상위 여부.
- 문턱: 특징을 가진 구성원 3개 이상, 무리 안 60% 이상, 모집단 비율의 2.0배 이상.
- **서술이지 규칙이 아니다.** 다수가 새 방식인지 옛 방식인지는 알 수 없다. 예외는 위반이 아니라 다르게 쓴 곳이다.
- 개요다: 구성원이 같은 특징은 "함께:"로 한 줄에, 예외 이름은 3개 이하일 때만 적고(넘으면 비율만), 특징의 패키지 이름과 예외의 디렉터리는 뺐다. 전부와 전체 이름·경로는 `--full`.
- 구성원이 같은 무리는 합쳤다. 구성원 대부분(60% 이상)이 더 큰 무리에 들어 있으면 그 무리 대비로도 2.0배를 넘는 특징만 적는다. 기본 특징(kotlin 표준 타입·함수·애노테이션, 가시성)은 무리마다 한 줄로 따로 적는다.
- 타입: 무리 22개 중 문턱을 넘는 특징이 있는 무리 1개.
- 함수: 무리 47개 중 문턱을 넘는 특징이 있는 무리 15개(상한, 큰 무리부터).

## 타입 · 이름 끝 단어 `Model` — 10개

- 8/10 (80%, 전체 8/91의 9.1배) 애노테이션 @HiltViewModel
  - 예외: LibraryModel (`LibraryModel.kt:3`), RecordModel (`RecordModel.kt:3`)
- 7/10 (70%, 전체 7/91의 9.1배) public val : StateFlow
  - 예외: LibraryModel (`LibraryModel.kt:3`), RecordModel (`RecordModel.kt:3`), SettingViewModel (`SettingViewModel.kt:7`)

## 함수 · 애노테이션 `@androidx.compose.runtime.Composable` — 58개

- 56/58 (97%, 전체 60/184의 3.0배) 최상위 함수
  - 예외: getDisplayName() (`AppThemeDisplayNameProvider.kt:16`), getDisplayName() (`ThemeDisplayNameProvider.kt:11`)
- 37/58 (64%, 전체 37/184의 3.2배) 호출 Text()

## 함수 · 이름 끝 단어 `Screen` — 15개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 15/15)

- 13/15 (87%, 전체 24/184의 6.6배) 호출 Modifier.fillMaxSize()
  - 예외: CardViewScreen() (`CardViewScreen.kt:29`), SheetMusicHistoryScreen() (`ScoreHistoryScreen.kt:20`)
- 기본: 애노테이션 @kotlin.OptIn 11/15

## 함수 · 애노테이션 `@kotlin.OptIn` — 15개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 15/15)

- 9/15 (60%, 전체 9/184의 12.3배) 호출 TopAppBar()
- 9/15 (60%, 전체 10/184의 11.0배) 호출 IconButton() · 함께: Icon()

## 함수 · 이름 끝 단어 `Record` — 12개

- 9/12 (75%, 전체 35/184의 3.9배) suspend
  - 예외: createAudioRecord() (`StopRecordingUseCase.kt:19`), deleteRecord() (`LibraryViewModel.kt:44`), renameRecord() (`LibraryViewModel.kt:61`)

## 함수 · 애노테이션 `@javax.inject.Singleton` — 10개

- 6/10 (60%, 전체 6/184의 18.4배) 애노테이션 @Binds

## 함수 · 이름 첫 단어 `delete` — 9개

- 7/9 (78%, 전체 35/184의 4.1배) suspend
  - 예외: deleteRecord() (`LibraryViewModel.kt:44`), deleteScore() (`ScoreHistoryViewModel.kt:26`)

## 함수 · 애노테이션 `@androidx.room.Query` — 7개
(겹침: 이름 첫 단어 `get` 5/7)

- 5/7 (71%, 전체 35/184의 3.8배) suspend
  - 예외: getAllRecords() (`AudioRecordDao.kt:12`), getAllScoresFlow() (`ScoreDao.kt:14`)

## 함수 · 이름 첫 단어 `on` — 7개

- 5/7 (71%, 전체 22/184의 6.0배) override
  - 예외: onLanguageSelected() (`LanguageSettingViewModel.kt:50`), onThemeSelected() (`ThemeViewModel.kt:41`)

## 함수 · 이름 첫 단어 `set` — 7개

- 6/7 (86%, 전체 35/184의 4.5배) suspend
  - 예외: setOnCompletionListener() (`AudioPlayer.kt:11`)

## 함수 · 이름 끝 단어 `Item` — 6개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 6/6)

- 6/6 (100%, 전체 7/184의 26.3배) 호출 Alignment.Companion.CenterVertically · 함께: Row(); Icon(); Modifier.fillMaxWidth()
- 5/6 (83%, 전체 5/184의 30.7배) 호출 RowScope.Modifier.weight()
  - 예외: LanguageItem() (`LanguageSettingScreen.kt:65`)
- 5/6 (83%, 전체 7/184의 21.9배) 호출 Modifier.clickable() · 함께: Icons.Default
  - 예외: ThemeOptionItem() (`ThemeSettingScreen.kt:109`)
- 5/6 (83%, 전체 17/184의 9.0배) 호출 MaterialTheme.colorScheme · 함께: MaterialTheme.typography
  - 예외: SettingItem() (`SettingItem.kt:25`)
- 4/6 (67%, 전체 7/184의 17.5배) 호출 Typography.bodyLarge
  - 예외: ThemeOptionItem() (`ThemeSettingScreen.kt:109`), SettingItem() (`SettingItem.kt:25`)
- 4/6 (67%, 전체 16/184의 7.7배) 호출 Spacer()
  - 예외: LanguageItem() (`LanguageSettingScreen.kt:65`), ScoreHistoryItem() (`ScoreHistoryItem.kt:27`)

## 함수 · 이름 끝 단어 `Music` — 5개

- 4/5 (80%, 전체 35/184의 4.2배) suspend
  - 예외: generateSheetMusic() (`SheetMusicViewModel.kt:28`)

## 함수 · 이름 첫 단어 `Record` — 5개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 5/5, 애노테이션 `@androidx.compose.ui.tooling.preview.Preview` + 이름 끝 단어 `Preview` 3/5)

- 4/5 (80%, 전체 4/184의 36.8배) 호출 RecordScreenUi()
  - 예외: RecordScreenUi() (`RecordScreenUi.kt:56`)

## 함수 · 이름 끝 단어 `Section` — 5개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 5/5)

- 3/5 (60%, 전체 17/184의 6.5배) 호출 Int.sp
  - 예외: AppSettingsSection() (`SettingsScreen.kt:146`), AboutSection() (`SettingsScreen.kt:168`)

## 함수 · 이름 첫 단어 `save` — 5개

- 5/5 (100%, 전체 35/184의 5.3배) suspend
- 3/5 (60%, 전체 4/184의 27.6배) 매개변수 : RecordModel
  - 예외: saveSheetMusic() (`SheetMusicRepositoryImpl.kt:39`), saveSheetMusic() (`SheetMusicRepository.kt:8`)

## 함수 · 이름 끝 단어 `Content` — 4개
(겹침: 애노테이션 `@androidx.compose.runtime.Composable` 4/4)

- 4/4 (100%, 전체 18/184의 10.2배) 호출 Modifier.height() · 함께: MaterialTheme.typography; Modifier.fillMaxSize()
- 3/4 (75%, 전체 6/184의 23.0배) 호출 Button() · 함께: FontWeight.Companion.Bold; Modifier.fillMaxWidth()
  - 예외: LoadingContent() (`SheetMusicScreen.kt:113`)
- 3/4 (75%, 전체 9/184의 15.3배) 호출 Typography.bodyMedium
  - 예외: IdleContent() (`SheetMusicScreen.kt:98`)
- 3/4 (75%, 전체 12/184의 11.5배) 호출 Alignment.Companion.CenterHorizontally
  - 예외: SuccessContent() (`SheetMusicScreen.kt:124`)
- 기본: private 3/4

