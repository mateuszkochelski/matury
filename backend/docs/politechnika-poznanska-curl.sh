#!/usr/bin/env bash
# Quick helper to check the recruitment calculator with a Politechnika Poznańska scenario.
# Usage: BASE_URL=http://localhost:8080/api ./politechnika-poznanska-curl.sh

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to run this script." >&2
  exit 1
fi

BASE_URL=${BASE_URL:-http://localhost:8080/api}

curl -s -X POST "$BASE_URL/recruitment-calculator/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "universityId": "politechnika-poznanska",
    "fieldOfStudyId": "3014",
    "examResults": [
      { "subjectCode": "polish_language", "level": "EXTENDED", "score": 62 },
      { "subjectCode": "english_language", "level": "BILINGUAL", "score": 35 },
      { "subjectCode": "mathematics", "level": "EXTENDED", "score": 78 },
      { "subjectCode": "art_exam", "score": 420 }
    ]
  }' | jq .
