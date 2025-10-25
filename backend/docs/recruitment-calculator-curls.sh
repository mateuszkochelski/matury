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

  if ! awk -v a="$actual" -v e="$expected" -v tol="$TOLERANCE" \
    'BEGIN { diff = a - e; if (diff < 0) diff = -diff; exit (diff > tol) }'; then
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

echo "All recruitment calculator smoke tests passed."
