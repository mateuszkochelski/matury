#!/usr/bin/env bash
# Oblicza punkty rekrutacyjne dla 5 przekrojowych scenariuszy matur
# na Politechnice Poznańskiej i zapisuje wynik do pliku Markdown.
#
# Usage:
#   BASE_URL=http://localhost:8080/api ./politechnika-poznanska-scenarios.sh
#   OUTPUT_FILE=/tmp/poznan.md ./politechnika-poznanska-scenarios.sh

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to run this script." >&2
  exit 1
fi

BASE_URL=${BASE_URL:-http://localhost:8080/api}
OUTPUT_FILE=${OUTPUT_FILE:-backend/docs/politechnika-poznanska-wyniki.md}
UNIVERSITY_ID="politechnika-poznanska"
FIELDS=("engineering-x" "science-xg" "architecture")

SCENARIOS_JSON='
[
  {
    "id": "balanced-tech",
    "description": "Mocna matematyka rozszerzona i fizyka, solidny egzamin zawodowy.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "BASIC", "score": 82 },
      { "subjectCode": "english_language", "level": "BASIC", "score": 88 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 86 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 74 },
      { "subjectCode": "physics", "level": "EXTENDED", "score": 79 },
      { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 84 },
      { "subjectCode": "art_exam", "score": 410 }
    ]
  },
  {
    "id": "bilingual-creative",
    "description": "Dwujęzyczny angielski, biologia rozszerzona i geografia – profil olimpijski.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "EXTENDED", "score": 70 },
      { "subjectCode": "english_language", "level": "BILINGUAL", "score": 42 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 68 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 64 },
      { "subjectCode": "biology", "level": "EXTENDED", "score": 71 },
      { "subjectCode": "geography", "level": "BASIC", "score": 73 },
      { "subjectCode": "art_exam", "score": 360 }
    ]
  },
  {
    "id": "vocational-master",
    "description": "Dominujący wynik z egzaminu zawodowego i informatyka rozszerzona.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "BASIC", "score": 69 },
      { "subjectCode": "english_language", "level": "BASIC", "score": 71 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 76 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 55 },
      { "subjectCode": "informatics", "level": "EXTENDED", "score": 81 },
      { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 95 },
      { "subjectCode": "art_exam", "score": 380 }
    ]
  },
  {
    "id": "science-olympian",
    "description": "Świetne rozszerzenia z matematyki i fizyki, wysoki angielski.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "BASIC", "score": 75 },
      { "subjectCode": "english_language", "level": "EXTENDED", "score": 88 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 90 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 90 },
      { "subjectCode": "chemistry", "level": "BASIC", "score": 78 },
      { "subjectCode": "physics", "level": "EXTENDED", "score": 92 },
      { "subjectCode": "art_exam", "score": 450 }
    ]
  },
  {
    "id": "language-heavy-geo",
    "description": "Silne języki obce i geografia, do tego biologia i umiarkowany wynik zawodowy.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "BASIC", "score": 78 },
      { "subjectCode": "english_language", "level": "BILINGUAL", "score": 45 },
      { "subjectCode": "german_language", "level": "EXTENDED", "score": 80 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 72 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 66 },
      { "subjectCode": "geography", "level": "EXTENDED", "score": 70 },
      { "subjectCode": "biology", "level": "BASIC", "score": 62 },
      { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 60 },
      { "subjectCode": "art_exam", "score": 400 }
    ]
  }
]
'

field_label() {
  case "$1" in
    engineering-x) echo "Kierunki z przedmiotem X";;
    science-xg) echo "Kierunki z przedmiotem XG";;
    architecture) echo "Kierunki architektoniczne";;
    *) echo "$1";;
  esac
}

: > "$OUTPUT_FILE"
{
  printf '%s\n\n' "# Politechnika Poznańska – zestawienie 5 scenariuszy" &&
  printf '_Endpoint_: %s/recruitment-calculator/calculate\n\n' "$BASE_URL" &&
  printf '%s\n\n' "Każdy scenariusz został przepuszczony przez trzy typy kierunków Politechniki Poznańskiej. Wyniki możesz porównać z oficjalnymi kalkulatorami uczelni."
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
