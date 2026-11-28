# AiCompose

## 구조 지도 (docs/map)

작업을 시작하기 전에 지도를 먼저 읽는다. 소스를 넓게 뒤지기 전에 지도로 위치를 좁힌다.

- `docs/map/summary.md`: 모듈·의존·선언·호출·DI 개요(archgraph 생성). **0절 "이 요약이 모르는 것"을 먼저 본다.**
- `docs/map/conventions.md`: 이 프로젝트의 관례를 빈도로 적은 것(archgraph 생성). 새 코드를 쓸 때 같은 무리의 관례를 따른다.
- `docs/map/graph.json`: 세부 쿼리용. 개요에 없는 세부는 쿼리로 본다.
- `docs/map/notes.md`: 사람이 쓰는 층(의도·결정·하지 말 것). 도구가 모르는 것만 적는다.

### 규칙

- `summary.md`, `conventions.md`, `graph.json`은 **생성물이다. 손으로 고치지 않는다.** 바꿀 때는 다시 생성한다.
- 지도가 낡았는지는 `summary.md` 머리의 "소스 커밋"과 `git log -1`을 비교해서 판단한다.
- 모듈·타입·DI·의존을 바꾸는 작업을 마쳤으면 지도를 다시 생성한다. 함수 안쪽만 고친 작업이면 다시 생성하지 않아도 된다.
- 지도와 소스가 다르면 소스가 맞다. 다른 점은 `notes.md`가 아니라 작업 보고에 적는다.
- `notes.md`는 의도나 결정이 바뀌었을 때 고친다. 생성물에 있는 사실을 옮겨 적지 않는다.

### 다시 생성 (Git Bash, 수 분 걸림)

```bash
A=/c/Users/shin/archgraph/build/install/archgraph/bin/archgraph
M=docs/map
$A extract 'C:/Users/shin/AndroidStudioProjects/AiCompose' -o $M/graph.json
$A summary $M/graph.json -o $M/summary.md
$A conventions $M/graph.json -o $M/conventions.md
```

### 세부 쿼리

```bash
$A query docs/map/graph.json <deps|uses|bindings|injects|members|gaps|files> [인자]
```

## 아키텍처 검사 (archgraph.yaml)

- `archgraph.yaml`은 사용자와 합의한 아키텍처 규칙이다(계층 사이 허용 의존 `allowed`, 참조 금지 `deny`, 경계 파일 `protect`).
- 작업을 마칠 때마다 Stop 훅(`.claude/hooks/archgraph-check.sh`)이 검사한다. 위반이나 새 protect 알림이 있으면 보고서가 돌아온다.
- 위반은 코드를 고쳐서 없앤다. 규칙이 틀렸다고 보면 고치지 말고 사용자에게 알린다. **`archgraph.yaml`을 고쳐 위반을 없애지 않는다.**
- protect 알림은 위반이 아니다. 의도한 변경이면 작업 보고에 무엇을 왜 바꿨는지 적는다.
- 훅의 그래프·보고서는 `build/archgraph/`에 있다(커밋하지 않음). `docs/map`의 지도와 별개다.
