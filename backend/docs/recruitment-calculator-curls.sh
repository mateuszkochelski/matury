#!/usr/bin/env bash
# Smoke tests for the recruitment calculator endpoint.
# Usage: BASE_URL=http://localhost:8080/api ./recruitment-calculator-curls.sh

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to run this script." >&2
  exit 1
fi

BASE_URL=${BASE_URL:-http://localhost:8080/api}
TOLERANCE=0.01

run_test() {
  local name=$1
  local payload=$2
  local expected=$3

  echo "==> $name"
  local response
  if ! response=$(curl -s -X POST "$BASE_URL/recruitment-calculator/calculate" \
    -H "Content-Type: application/json" \
    -d "$payload"); then
    echo "Request failed for $name" >&2
    exit 1
  fi

  echo "$response" | jq .

  local actual
  actual=$(echo "$response" | jq -r '.totalPoints')

  if [[ "$actual" == "null" ]]; then
    echo "Missing totalPoints in response for $name" >&2
    exit 1
  fi

  if python3 - "$expected" "$actual" "$TOLERANCE" <<'PY'; then
import sys
expected = float(sys.argv[1])
actual = float(sys.argv[2])
tol = float(sys.argv[3])
sys.exit(0 if abs(actual - expected) <= tol else 1)
PY
    printf "✔ %s passed (expected %.2f, got %.2f)\n\n" "$name" "$expected" "$actual"
  else
    printf "✖ %s failed (expected %.2f, got %.2f)\n" "$name" "$expected" "$actual" >&2
    exit 1
  fi
}

run_test "Politechnika Śląska – default (extended subject wins)" '{
  "universityId": "politechnika-slaska",
  "fieldOfStudyId": "default",
  "examResults": [
    { "subjectCode": "mathematics", "level": "BASIC", "score": 82 },
    { "subjectCode": "physics", "level": "EXTENDED", "score": 76 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 90 }
  ]
}' 117.0

run_test "Politechnika Poznańska – Informatyka (X composite from vocational)" '{
  "universityId": "33",
  "fieldOfStudyId": "3004",
  "examResults": [
    { "subjectCode": "polish_language", "level": "BASIC", "score": 80 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 85 },
    { "subjectCode": "mathematics", "level": "BASIC", "score": 90 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 70 },
    { "subjectCode": "informatics", "level": "EXTENDED", "score": 75 },
    { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 88 }
  ]
}' 834.5

run_test "Politechnika Poznańska – Architektura (bilingual + art exam)" '{
  "universityId": "politechnika-poznanska",
  "fieldOfStudyId": "3014",
  "examResults": [
    { "subjectCode": "polish_language", "level": "EXTENDED", "score": 62 },
    { "subjectCode": "english_language", "level": "BILINGUAL", "score": 35 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 78 },
    { "subjectCode": "art_exam", "score": 420 }
  ]
}' 851.5

run_test "Politechnika Śląska – vocational result chosen" '{
  "universityId": "politechnika-slaska",
  "fieldOfStudyId": "default",
  "examResults": [
    { "subjectCode": "mathematics", "level": "BASIC", "score": 70 },
    { "subjectCode": "informatics", "level": "VOCATIONAL_TECHNICIAN", "score": 85 }
  ]
}' 98.75

run_test "Politechnika Śląska – lookup by numeric IDs" '{
  "universityId": "35",
  "fieldOfStudyId": "1983",
  "examResults": [
    { "subjectCode": "mathematics", "level": "BASIC", "score": 78 },
    { "subjectCode": "physics", "level": "EXTENDED", "score": 81 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 92 }
  ]
}' 120.0

run_test "Politechnika Śląska – bilingual subject treated as extended" '{
  "universityId": "politechnika-slaska",
  "fieldOfStudyId": "default",
  "examResults": [
    { "subjectCode": "mathematics", "level": "BASIC", "score": 75 },
    { "subjectCode": "english_bilingual", "level": "BILINGUAL", "score": 88 }
  ]
}' 125.5

run_test "Politechnika Białostocka – Automatyka i Robotyka (waga przedmiotu)" '{
  "universityId": "politechnika-bialostocka",
  "fieldOfStudyId": "wr-technical",
  "examResults": [
    { "subjectCode": "mathematics", "level": "BASIC", "score": 78 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 82 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 70 },
    { "subjectCode": "english_language", "level": "EXTENDED", "score": 68 },
    { "subjectCode": "physics", "level": "EXTENDED", "score": 80 },
    { "subjectCode": "chemistry", "level": "EXTENDED", "score": 90 }
  ]
}' 350.0

run_test "Politechnika Białostocka – Biotechnologia (dwa przedmioty dodatkowe)" '{
  "universityId": "politechnika-bialostocka",
  "fieldOfStudyId": "2489",
  "examResults": [
    { "subjectCode": "mathematics", "level": "BASIC", "score": 82 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 70 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 85 },
    { "subjectCode": "german_language", "level": "EXTENDED", "score": 60 },
    { "subjectCode": "physics", "level": "EXTENDED", "score": 78 },
    { "subjectCode": "geography", "level": "EXTENDED", "score": 64 }
  ]
}' 443.25

run_test "Politechnika Bydgoska – Architektura (egzamin artystyczny)" '{
  "universityId": "politechnika-bydgoska",
  "fieldOfStudyId": "architecture",
  "examResults": [
    { "subjectCode": "polish_language", "level": "EXTENDED", "score": 65 },
    { "subjectCode": "english_language", "level": "EXTENDED", "score": 78 },
    { "subjectCode": "mathematics", "level": "BASIC", "score": 72 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 68 },
    { "subjectCode": "physics", "level": "EXTENDED", "score": 75 },
    { "subjectCode": "art_exam", "score": 310 }
  ]
}' 828.0

run_test "Politechnika Bydgoska – Cyberbezpieczeństwo (matma rozszerzona wygrywa)" '{
  "universityId": "politechnika-bydgoska",
  "fieldOfStudyId": "ict",
  "examResults": [
    { "subjectCode": "english_language", "level": "BASIC", "score": 85 },
    { "subjectCode": "mathematics", "level": "BASIC", "score": 90 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 74 },
    { "subjectCode": "physics", "level": "BASIC", "score": 80 },
    { "subjectCode": "informatics", "level": "EXTENDED", "score": 88 }
  ]
}' 883.0

run_test "Politechnika Lubelska – Mechatronika (pelne wyniki)" '{
  "universityId": "politechnika-lubelska",
  "fieldOfStudyId": "pollub-mechanical",
  "examResults": [
    { "subjectCode": "polish_language", "level": "BASIC", "score": 78 },
    { "subjectCode": "polish_language", "level": "EXTENDED", "score": 64 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 82 },
    { "subjectCode": "english_language", "level": "EXTENDED", "score": 74 },
    { "subjectCode": "mathematics", "level": "BASIC", "score": 88 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 72 },
    { "subjectCode": "informatics", "level": "EXTENDED", "score": 90 },
    { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 92 }
  ]
}' 703.03

run_test "Politechnika Lubelska – Zarzadzanie (brak matmy to 20 pkt)" '{
  "universityId": "politechnika-lubelska",
  "fieldOfStudyId": "pollub-business",
  "examResults": [
    { "subjectCode": "polish_language", "level": "BASIC", "score": 65 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 70 },
    { "subjectCode": "geography", "level": "EXTENDED", "score": 60 },
    { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 80 }
  ]
}' 347.71

run_test "Politechnika Czestochowska - Automatyka i Robotyka (pelne wyniki)" '{
  "universityId": "politechnika-czestochowska",
  "fieldOfStudyId": "automatyka i robotyka",
  "examResults": [
    { "subjectCode": "polish_language", "level": "BASIC", "score": 70 },
    { "subjectCode": "polish_language", "level": "EXTENDED", "score": 60 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 85 },
    { "subjectCode": "english_language", "level": "EXTENDED", "score": 75 },
    { "subjectCode": "mathematics", "level": "BASIC", "score": 80 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 70 },
    { "subjectCode": "physics", "level": "BASIC", "score": 78 },
    { "subjectCode": "physics", "level": "EXTENDED", "score": 66 },
    { "subjectCode": "vocational_exam", "level": "VOCATIONAL_TECHNICIAN", "score": 90 }
  ]
}' 648.0

run_test "Politechnika Czestochowska - Zarzadzanie (brak matmy i przedmiotu dodatkowego to 20 pkt)" '{
  "universityId": "politechnika-czestochowska",
  "fieldOfStudyId": "zarzadzanie",
  "examResults": [
    { "subjectCode": "polish_language", "level": "BASIC", "score": 60 },
    { "subjectCode": "english_language", "level": "BASIC", "score": 70 }
  ]
}' 158.0

echo "All recruitment calculator smoke tests passed."
