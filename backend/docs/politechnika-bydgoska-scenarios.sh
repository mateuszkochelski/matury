#!/usr/bin/env bash
# Scenariusze dla kalkulatora Politechniki Bydgoskiej (PBS).
# Generuje raport Markdown z wynikami dla szesciu typow kierunkow.
#
# Usage:
#   BASE_URL=http://localhost:8080/api ./politechnika-bydgoska-scenarios.sh
#   OUTPUT_FILE=/tmp/pbs-raport.md ./politechnika-bydgoska-scenarios.sh

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to run this script." >&2
  exit 1
fi

BASE_URL=${BASE_URL:-http://localhost:8080/api}
OUTPUT_FILE=${OUTPUT_FILE:-backend/docs/politechnika-bydgoska-wyniki.md}
UNIVERSITY_ID="politechnika-bydgoska"
FIELDS=(
  "architecture"
  "engineering-general"
  "animal-sciences"
  "ict"
  "finance"
  "law"
)

SCENARIOS_JSON='[
  {
    "id": "rysunek-plus-fizyka",
    "description": "Mocna matematyka i fizyka, do tego rozbudowane portfolio artystyczne.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "EXTENDED", "score": 66 },
      { "subjectCode": "english_language", "level": "EXTENDED", "score": 78 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 82 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 71 },
      { "subjectCode": "physics", "level": "EXTENDED", "score": 75 },
      { "subjectCode": "chemistry", "level": "EXTENDED", "score": 68 },
      { "subjectCode": "art_exam", "score": 295 }
    ]
  },
  {
    "id": "bio-rolnictwo",
    "description": "Profil biologiczno-rolniczy z dodatkiem geografii i jezyka dwujezycznego.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "BASIC", "score": 74 },
      { "subjectCode": "english_bilingual", "level": "BILINGUAL", "score": 38 },
      { "subjectCode": "mathematics", "level": "BASIC", "score": 76 },
      { "subjectCode": "biology", "level": "EXTENDED", "score": 80 },
      { "subjectCode": "geography", "level": "EXTENDED", "score": 72 },
      { "subjectCode": "chemistry", "level": "BASIC", "score": 70 }
    ]
  },
  {
    "id": "humanistyczny",
    "description": "Jezyk polski, historia i matematyka rozszerzona pod katem prawa oraz finansow.",
    "examResults": [
      { "subjectCode": "polish_language", "level": "EXTENDED", "score": 70 },
      { "subjectCode": "english_language", "level": "BASIC", "score": 89 },
      { "subjectCode": "english_language", "level": "EXTENDED", "score": 72 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 68 },
      { "subjectCode": "history", "level": "EXTENDED", "score": 73 },
      { "subjectCode": "civics", "level": "BASIC", "score": 85 }
    ]
  }
]'

field_label() {
  case "$1" in
    architecture) echo "Architektura";;
    engineering-general) echo "Budownictwo / Geodezja / Inzynieria srodowiska";;
    animal-sciences) echo "Zootechnika / Kierunki rolnicze";;
    ict) echo "Cyberbezpieczenstwo / Informatyka / Telekomunikacja";;
    finance) echo "Finanse i rachunkowosc";;
    law) echo "Prawo";;
    *) echo "$1";;
  esac
}

: > "$OUTPUT_FILE"
{
  printf '%s\n\n' "# Politechnika Bydgoska - scenariusze"
  printf '_Endpoint_: %s/recruitment-calculator/calculate\n\n' "$BASE_URL"
  printf '%s\n\n' "Trzy zestawy matur policzone dla szesciu kategorii kierunkow PBS. Dane mozna porownac z uczelnianym algorytmem."
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

    total_points=$(echo "$response" | jq -r '.totalPoints // empty')
    if [[ -z "$total_points" ]]; then
      error_message=$(echo "$response" | jq -r '.message // "Brak danych zwroconych przez API"')
      {
        printf '**Kierunek:** %s (`%s`)\n' "$(field_label "$field_id")" "$field_id"
        printf '%s\n\n' "- Blad kalkulatora: $error_message"
      } >> "$OUTPUT_FILE"
      continue
    fi

    breakdown=$(echo "$response" | jq -r '.breakdown[]? | "    - " + .description + ": " + ((.pointsAwarded? // 0) | tostring) + " pkt (surowy: " + ((.rawScore? // 0) | tostring) + ", wspolczynnik: " + ((.coefficient? // 0) | tostring) + (if .subjectCode then ", zrodlo: " + .subjectCode + (if .level then " [" + .level + "]" else "" end) else "" end) + ")"')

    {
      printf '**Kierunek:** %s (`%s`)\n' "$(field_label "$field_id")" "$field_id"
      printf '%s%.2f\n' "- Suma punktow: " "$total_points"
      printf '%s\n' "- Rozbicie skladnikow:"
      if [[ -n "$breakdown" ]]; then
        printf '%s\n\n' "$breakdown"
      else
        printf '%s\n\n' "    - (brak szczegolowego rozbicia)"
      fi
    } >> "$OUTPUT_FILE"
  done
done

echo "Zapisano raport do $OUTPUT_FILE"
