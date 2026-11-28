#!/usr/bin/env bash
# archgraph 검사 훅 (Claude Code Stop). 규칙은 저장소 루트의 archgraph.yaml.
#
# - 지난 검사 뒤 바뀐 것이 없으면 아무것도 하지 않는다.
# - .kt·Gradle 파일이 바뀌었으면 다시 추출(약 1분)한 뒤 검사, 아니면 protect만 본다.
# - 위반이 있거나 새 protect 알림이 있으면 종료 코드 2로 Claude에게 보고서를 돌려준다(Claude가 이어서 처리).
#   같은 상태로 다시 멈추면(stop_hook_active) 막지 않고 사용자에게만 알린다(무한 반복 방지).
# - 매 검사 결과를 archgraph 쪽 사용 기록(bench/work/usage-aicompose)에 남긴다.
set -u

ROOT="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel)}"
cd "$ROOT" || exit 0
A=/c/Users/shin/archgraph/build/install/archgraph/bin/archgraph
OUT=build/archgraph
LOGDIR="${ARCHGRAPH_USAGE_LOG:-/c/Users/shin/archgraph/bench/work/usage-aicompose}"
mkdir -p "$OUT" "$LOGDIR"

[ -x "$A" ] || { echo '{"systemMessage":"archgraph check: 도구가 설치되어 있지 않아 건너뜀"}'; exit 0; }

input="$(cat)"
active=no
echo "$input" | grep -q '"stop_hook_active" *: *true' && active=yes

# 상태 서명: HEAD + 변경 목록 + 추적 파일의 diff + 추적 안 된 파일 내용
untracked_sig() { git ls-files -o --exclude-standard -z "$@" | xargs -0 -r cat 2>/dev/null | sha1sum | cut -c1-40; }
all_sig=$( { git rev-parse HEAD; git status --porcelain=v1 -uall; git diff HEAD | sha1sum; untracked_sig; } 2>/dev/null | sha1sum | cut -c1-40)
code_sig=$( { git rev-parse HEAD; git diff HEAD -- '*.kt' '*.kts' '*.toml' | sha1sum; untracked_sig -- '*.kt' '*.kts' '*.toml'; } 2>/dev/null | sha1sum | cut -c1-40)

[ "$(cat "$OUT/all.sig" 2>/dev/null)" = "$all_sig" ] && exit 0
echo "$all_sig" > "$OUT/all.sig"

graph_arg=()
if [ "$(cat "$OUT/code.sig" 2>/dev/null)" != "$code_sig" ] || [ ! -f "$OUT/graph.json" ]; then
  if "$A" extract "$ROOT" -o "$OUT/graph.json" > "$OUT/extract.log" 2>&1; then
    echo "$code_sig" > "$OUT/code.sig"
  else
    rm -f "$OUT/code.sig"
    echo '{"systemMessage":"archgraph check: 추출 실패(build/archgraph/extract.log). protect만 검사"}'
  fi
fi
[ -f "$OUT/code.sig" ] && [ -f "$OUT/graph.json" ] && graph_arg=("$OUT/graph.json")

"$A" check "${graph_arg[@]}" --config archgraph.yaml --root "$ROOT" -o "$OUT/check.md" 2> "$OUT/check.err"
code=$?
summary=$(tail -n 1 "$OUT/check.err")
notices=$(grep -A1000 '^## 알림' "$OUT/check.md" | grep '^- ' | sha1sum | cut -c1-40)
new_notices=no
[ "$(cat "$OUT/notices.sig" 2>/dev/null)" != "$notices" ] && grep -q '^## 알림 [1-9]' "$OUT/check.md" && new_notices=yes
echo "$notices" > "$OUT/notices.sig"

{
  echo "## $(date '+%Y-%m-%d %H:%M:%S') · HEAD $(git rev-parse --short HEAD) · $summary · stop_hook_active=$active · 그래프 ${graph_arg[*]:-없음}"
  echo
  cat "$OUT/check.md"
  echo
} >> "$LOGDIR/checks.md"

if [ "$code" -ge 2 ]; then
  echo "{\"systemMessage\":\"archgraph check 실행 오류(build/archgraph/check.err)\"}"; exit 0
fi
if { [ "$code" -eq 1 ] || [ "$new_notices" = yes ]; } && [ "$active" = no ]; then
  {
    echo "archgraph check: $summary. 보고서(build/archgraph/check.md):"
    echo
    cat "$OUT/check.md"
    echo
    echo "위반은 archgraph.yaml(사용자와 합의한 규칙)과 어긋난 곳이다. 코드를 고치거나, 규칙이 틀렸다고 보면 고치지 말고 사용자에게 알린다. archgraph.yaml을 고쳐 위반을 없애지 않는다."
    echo "protect 알림은 위반이 아니다. 의도한 변경이면 작업 보고에 무엇을 왜 바꿨는지 적는다."
  } >&2
  exit 2
fi
echo "{\"systemMessage\":\"archgraph check: $summary\"}"
exit 0
