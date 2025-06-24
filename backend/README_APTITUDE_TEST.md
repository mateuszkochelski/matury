# Test Predyspozycji Zawodowych

## Opis

Zaimplementowano psychologiczny test predyspozycji zawodowych oparty na teorii Hollanda. Test składa się z 20 pytań oceniających 6 typów osobowości zawodowej:

1. **Realistyczny (praktyczny)** - osoby preferujące konkretne, manualne zadania
2. **Badawczy (analityczny)** - osoby dociekliwe, ceniące wiedzę i naukę
3. **Artystyczny (kreatywny)** - osoby o bogatej wyobraźni, ceniące kreatywność
4. **Społeczny (pomocny)** - osoby empatyczne, lubiące pracę z ludźmi
5. **Przedsiębiorczy (lider)** - osoby pewne siebie, nastawione na sukces
6. **Konwencjonalny (urzędowy)** - osoby zorganizowane, dokładne

## Struktura API

### Endpointy

#### GET `/aptitude-test/questions`
Pobiera wszystkie pytania testu w odpowiedniej kolejności.

**Odpowiedź:**
```json
[
  {
    "id": 1,
    "questionText": "Preferuję pracę fizyczną lub z użyciem narzędzi...",
    "category": "REALISTIC",
    "orderNumber": 1
  }
]
```

#### POST `/aptitude-test/submit`
Przesyła odpowiedzi i oblicza wyniki testu.

**Żądanie:**
```json
{
  "sessionId": "unique-session-id",
  "answers": [5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1]
}
```

**Odpowiedź:**
```json
{
  "sessionId": "unique-session-id",
  "categoryScores": {
    "REALISTIC": 15,
    "INVESTIGATIVE": 12,
    "ARTISTIC": 8,
    "SOCIAL": 10,
    "ENTERPRISING": 14,
    "CONVENTIONAL": 11
  },
  "categoryPercentages": {
    "REALISTIC": 100.0,
    "INVESTIGATIVE": 80.0,
    "ARTISTIC": 53.33,
    "SOCIAL": 66.67,
    "ENTERPRISING": 93.33,
    "CONVENTIONAL": 73.33
  },
  "dominantCategories": ["REALISTIC", "ENTERPRISING"],
  "interpretation": "Twoje dominujące typy osobowości zawodowej to: Realistyczny (praktyczny) i Przedsiębiorczy (lider)..."
}
```

#### GET `/aptitude-test/result/{sessionId}`
Pobiera wyniki testu dla podanej sesji.

#### GET `/aptitude-test/detailed-result/{sessionId}`
Pobiera szczegółowe wyniki dla każdej kategorii.

**Odpowiedź:**
```json
[
  {
    "category": "REALISTIC",
    "displayName": "Realistyczny (praktyczny)",
    "description": "Osoby preferujące konkretne, manualne zadania...",
    "score": 15,
    "maxScore": 15,
    "percentage": 100.0,
    "suggestedFieldsOfStudy": ["inżynieria", "logistyka", "informatyka techniczna"]
  }
]
```

#### GET `/aptitude-test/categories`
Pobiera informacje o wszystkich kategoriach Hollanda.

## Architektura

### Modele

- **`AptitudeTestQuestion`** - encja pytania testu
- **`AptitudeTestResponse`** - encja odpowiedzi użytkownika
- **`HollandCategory`** - enum z kategoriami i opisami

### DTO

- **`AptitudeTestQuestionDTO`** - transfer pytania
- **`AptitudeTestResponseDTO`** - transfer odpowiedzi
- **`AptitudeTestResultDTO`** - transfer wyników
- **`CategoryResultDTO`** - szczegółowe wyniki kategorii

### Serwisy

- **`AptitudeTestService`** - logika biznesowa testu
- **`AptitudeTestSeeder`** - inicjalizacja pytań w bazie danych

### Repozytoria

- **`AptitudeTestQuestionRepository`** - operacje na pytaniach
- **`AptitudeTestResponseRepository`** - operacje na odpowiedziach

## Baza Danych

### Tabele

1. **`aptitude_test_questions`** - pytania testu
2. **`aptitude_test_responses`** - sesje użytkowników
3. **`aptitude_test_answers`** - poszczególne odpowiedzi

## Algorytm Obliczania Wyników

1. **Sumowanie punktów** - dla każdej kategorii sumowane są punkty z odpowiadających pytań (1-5 pkt)
2. **Obliczanie procentów** - wynik kategorii / maksymalny możliwy wynik * 100%
3. **Identyfikacja dominujących typów** - 2 kategorie z najwyższymi wynikami
4. **Generowanie interpretacji** - opis dominujących typów i sugerowane kierunki studiów

## Walidacja

- Liczba odpowiedzi musi być równa liczbie pytań
- Każda odpowiedź musi być w przedziale 1-5
- Session ID musi być unikalny

## Testy

Zaimplementowano testy jednostkowe sprawdzające:
- Pobieranie pytań
- Walidację odpowiedzi
- Obliczanie wyników
- Obsługę błędów

## Uruchomienie

Test predyspozycji jest automatycznie dostępny po uruchomieniu aplikacji. Pytania są inicjalizowane przy pierwszym starcie aplikacji przez `AptitudeTestSeeder`.

Dostęp do dokumentacji API: `http://localhost:8080/swagger-ui/index.html` 