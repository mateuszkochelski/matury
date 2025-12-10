#!/usr/bin/env bash
# Przepuszcza trzy zestawy matur przez sześć typów kierunków Politechniki Białostockiej.
# Wynik jest zapisywany w formacie Markdown – przydatne do porównań z kalkulatorem PB.
#
# Usage:
#   BASE_URL=http://localhost:8080/api ./politechnika-bialostocka-scenarios.sh
#   OUTPUT_FILE=/tmp/pb.md ./politechnika-bialostocka-scenarios.sh

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to run this script." >&2
  exit 1
fi

BASE_URL=${BASE_URL:-http://localhost:8080/api}
OUTPUT_FILE=${OUTPUT_FILE:-backend/docs/politechnika-bialostocka-wyniki.md}
UNIVERSITY_ID="politechnika-bialostocka"
FIELDS=("default" "wr-technical" "math-applied" "drawing-required" "biotechnology" "environmental-engineering")

SCENARIOS_JSON='
[
  {
    "id": "uni-basic",
    "description": "Klasyczny zestaw – wysoka fizyka rozszerzona i solidny język podstawowy.",
    "examResults": [
      { "subjectCode": "mathematics", "level": "BASIC", "score": 82 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 70 },
      { "subjectCode": "english_language", "level": "BASIC", "score": 85 },
      { "subjectCode": "german_language", "level": "EXTENDED", "score": 60 },
      { "subjectCode": "physics", "level": "EXTENDED", "score": 78 },
      { "subjectCode": "geography", "level": "EXTENDED", "score": 64 },
      { "subjectCode": "art_exam", "score": 360 }
    ]
  },
  {
    "id": "bio-tech",
    "description": "Akcent na biologię i informatykę, do tego rozszerzony angielski.",
    "examResults": [
      { "subjectCode": "mathematics", "level": "BASIC", "score": 74 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 78 },
      { "subjectCode": "english_language", "level": "EXTENDED", "score": 82 },
      { "subjectCode": "biology", "level": "EXTENDED", "score": 85 },
      { "subjectCode": "informatics", "level": "EXTENDED", "score": 88 },
      { "subjectCode": "art_exam", "score": 300 }
    ]
  },
  {
    "id": "language-heavy",
    "description": "Dwa języki (podstawowy + rozszerzony) i chemia jako przedmiot dodatkowy.",
    "examResults": [
      { "subjectCode": "mathematics", "level": "BASIC", "score": 76 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 66 },
      { "subjectCode": "english_language", "level": "BASIC", "score": 90 },
      { "subjectCode": "english_language", "level": "EXTENDED", "score": 72 },
      { "subjectCode": "french_language", "level": "BASIC", "score": 70 },
      { "subjectCode": "chemistry", "level": "EXTENDED", "score": 80 },
      { "subjectCode": "art_exam", "score": 420 }
    ]
  }
]
'

field_label() {
  case "$1" in
    default) echo "Pozostałe kierunki";;
    wr-technical) echo "Automatyka/MBM/Mechatronika/IB";;
    math-applied) echo "Matematyka stosowana";;
    drawing-required) echo "Kierunki z egzaminem z rysunku";;
    biotechnology) echo "Biotechnologia";;
    environmental-engineering) echo "Inżynieria środowiska";;
    *) echo "$1";;
  esac
}

: > "$OUTPUT_FILE"
{
  printf '%s\n\n' "# Politechnika Białostocka – 3 scenariusze"
  printf '_Endpoint_: %s/recruitment-calculator/calculate\n\n' "$BASE_URL"
  printf '%s\n\n' "Każdy scenariusz został policzony dla sześciu typów kierunków PB. Wartości można porównać z uczelnianym wzorem (bez egzaminów zawodowych)."
} >> "$OUTPUT_FILE"

echo "$SCENARIOS_JSON" | jq -c '.[]' | while read -r scenario; do
  scenario_id=$(echo "$scenario" | jq -r '.id')
  description=$(echo "$scenario" | jq -r '.description')
  exams_formatted=$(echo "$scenario" | jq -r '.examResults[] | "  - " + .subjectCode + " (" + ((.level // "N/A")) + "): " + (.score | tostring)')

  {
    printf '## %s\n\n' "$scenario_id"
    printf '%s\n\n' "$description"
    printf '%s\n' "**Wyniki matur:**"
    printf '%s\n\n' "$exams_formatted"
  } >> "$OUTPUT_FILE"

  for field_id in "${FIELDS[@]}"; do
    payload=$(echo "$scenario" | jq --arg uid "$UNIVERSITY_ID" --arg fid "$field_id" '{universityId: $uid, fieldOfStudyId: $fid, examResults: .examResults}')

    if ! response=$(curl -s -X POST "$BASE_URL/recruitment-calculator/calculate" \
      -H "Content-Type: application/json" \
      -d "$payload"); then
      echo "Request failed for scenario $scenario_id / field $field_id" >&2
      exit 1
    fi

    total_points=$(echo "$response" | jq '.totalPoints')
    breakdown=$(echo "$response" | jq -r '.breakdown[] | "    - " + .description + ": " + ((.pointsAwarded? // 0) | tostring) + " pkt (surowy: " + ((.rawScore? // 0) | tostring) + ", współczynnik: " + ((.coefficient? // 0) | tostring) + (if .subjectCode then ", źródło: " + .subjectCode + (if .level then " [" + .level + "]" else "" end) else "" end) + ")"')

    {
      printf '**Kierunek:** %s (`%s`)\n' "$(field_label "$field_id")" "$field_id"
      printf '%s%.2f\n' "- Suma punktów: " "$total_points"
      printf '%s\n' "- Rozbicie składników:"
      printf '%s\n\n' "$breakdown"
    } >> "$OUTPUT_FILE"
  done
done

printf 'Zapisano raport do %s\n' "$OUTPUT_FILE"
