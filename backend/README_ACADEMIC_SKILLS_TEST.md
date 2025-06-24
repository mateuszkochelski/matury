# Test Uzdolnień Kierunkowych (Kompetencje Akademickie)

## Opis

Zaimplementowano test uzdolnień kierunkowych oparty na teorii inteligencji wielorakiej Gardnera. Test składa się z 20 pytań oceniających 5 obszarów uzdolnień:

1. **Zdolności logiczno-matematyczne** - talent do myślenia analitycznego i operowania liczbami
2. **Zdolności językowe (werbalne)** - zdolność efektywnego posługiwania się językiem
3. **Zdolności artystyczne (kreatywne)** - uzdolnienia w zakresie sztuk i kreatywności
4. **Zdolności techniczne** - zdolności manualno-techniczne i rozumienie urządzeń
5. **Zdolności przyrodnicze** - zainteresowania i zdolności w obszarze nauk przyrodniczych

## Struktura API

### Endpointy

#### GET `/academic-skills-test/questions`
Pobiera wszystkie pytania testu w odpowiedniej kolejności.

**Odpowiedź:**
```json
[
  {
    "id": 1,
    "questionText": "Łatwo przychodzi mi rozwiązywanie zadań matematycznych lub logicznych.",
    "category": "LOGICAL_MATHEMATICAL",
    "orderNumber": 1
  }
]
```

#### POST `/academic-skills-test/submit`
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
    "LOGICAL_MATHEMATICAL": 18,
    "LINGUISTIC": 16,
    "ARTISTIC": 12,
    "TECHNICAL": 14,
    "NATURAL_SCIENCES": 10
  },
  "categoryPercentages": {
    "LOGICAL_MATHEMATICAL": 90.0,
    "LINGUISTIC": 80.0,
    "ARTISTIC": 60.0,
    "TECHNICAL": 70.0,
    "NATURAL_SCIENCES": 50.0
  },
  "dominantCategories": ["LOGICAL_MATHEMATICAL", "LINGUISTIC"],
  "interpretation": "Twoje najsilniejsze uzdolnienia kierunkowe to: Zdolności logiczno-matematyczne i Zdolności językowe (werbalne)..."
}
```

#### GET `/academic-skills-test/result/{sessionId}`
Pobiera wyniki testu dla podanej sesji.

#### GET `/academic-skills-test/detailed-result/{sessionId}`
Pobiera szczegółowe wyniki dla każdej kategorii.

**Odpowiedź:**
```json
[
  {
    "category": "LOGICAL_MATHEMATICAL",
    "displayName": "Zdolności logiczno-matematyczne",
    "description": "Talent do myślenia analitycznego, rozumowania logicznego i operowania liczbami...",
    "score": 18,
    "maxScore": 20,
    "percentage": 90.0,
    "suggestedFieldsOfStudy": ["matematyka", "fizyka", "informatyka", "ekonomia", "inżynieria"]
  }
]
```

#### GET `/academic-skills-test/categories`
Pobiera informacje o wszystkich kategoriach uzdolnień kierunkowych.

## Architektura

### Modele

- **`AcademicSkillsTestQuestion`** - encja pytania testu
- **`AcademicSkillsTestResponse`** - encja odpowiedzi użytkownika
- **`AcademicSkillCategory`** - enum z kategoriami i opisami

### DTO

- **`AcademicSkillsTestQuestionDTO`** - transfer pytania
- **`AcademicSkillsTestResponseDTO`** - transfer odpowiedzi
- **`AcademicSkillsTestResultDTO`** - transfer wyników
- **`AcademicSkillCategoryResultDTO`** - szczegółowe wyniki kategorii

### Serwisy

- **`AcademicSkillsTestService`** - logika biznesowa testu
- **`AcademicSkillsTestSeeder`** - inicjalizacja pytań w bazie danych

### Repozytoria

- **`AcademicSkillsTestQuestionRepository`** - operacje na pytaniach
- **`AcademicSkillsTestResponseRepository`** - operacje na odpowiedziach

## Baza Danych

### Tabele

1. **`academic_skills_test_questions`** - pytania testu
2. **`academic_skills_test_responses`** - sesje użytkowników
3. **`academic_skills_test_answers`** - poszczególne odpowiedzi

## Algorytm Obliczania Wyników

1. **Sumowanie punktów** - dla każdej kategorii sumowane są punkty z odpowiadających pytań (1-5 pkt)
2. **Obliczanie procentów** - wynik kategorii / maksymalny możliwy wynik * 100%
3. **Identyfikacja dominujących obszarów** - 2 kategorie z najwyższymi wynikami
4. **Generowanie interpretacji** - szczegółowy opis z wynikami punktowymi i sugerowanymi kierunkami

## Kategorie Uzdolnień

### 1. Zdolności logiczno-matematyczne (4 pytania, max 20 pkt)
- **Opis**: Talent do myślenia analitycznego, rozumowania logicznego i operowania liczbami
- **Kierunki**: matematyka, fizyka, informatyka, ekonomia, inżynieria
- **Pytania**: 1-4

### 2. Zdolności językowe - werbalne (4 pytania, max 20 pkt)
- **Opis**: Zdolność efektywnego posługiwania się językiem w mowie i piśmie
- **Kierunki**: filologie, dziennikarstwo, prawo, komunikacja społeczna, lingwistyka
- **Pytania**: 5-8

### 3. Zdolności artystyczne - kreatywne (4 pytania, max 20 pkt)
- **Opis**: Uzdolnienia w zakresie sztuk wizualnych, muzycznych i kreatywności
- **Kierunki**: akademie sztuk pięknych, architektura, design, muzyka, film
- **Pytania**: 9-12

### 4. Zdolności techniczne (4 pytania, max 20 pkt)
- **Opis**: Zdolności manualno-techniczne i rozumienie zasad działania urządzeń
- **Kierunki**: mechanika, elektronika, robotyka, informatyka (hardware), budownictwo
- **Pytania**: 13-16

### 5. Zdolności przyrodnicze (4 pytania, max 20 pkt)
- **Opis**: Zainteresowania i zdolności w obszarze nauk przyrodniczych
- **Kierunki**: biologia, biotechnologia, ochrona środowiska, medycyna, geologia
- **Pytania**: 17-20

## Walidacja

- Liczba odpowiedzi musi być równa liczbie pytań (20)
- Każda odpowiedź musi być w przedziale 1-5
- Session ID musi być unikalny

## Interpretacja Wyników

Test identyfikuje najsilniejsze uzdolnienia użytkownika i dostarcza:
- Wyniki punktowe dla każdej kategorii
- Wyniki procentowe
- Dominujące obszary uzdolnień
- Szczegółową interpretację z opisami kategorii
- Sugerowane kierunki studiów dla dominujących obszarów
- Wskazówki dotyczące kierunków interdyscyplinarnych

## Testy

Zaimplementowano testy jednostkowe sprawdzające:
- Pobieranie pytań
- Walidację odpowiedzi
- Obliczanie wyników
- Obsługę błędów

## Uruchomienie

Test uzdolnień kierunkowych jest automatycznie dostępny po uruchomieniu aplikacji. Pytania są inicjalizowane przy pierwszym starcie aplikacji przez `AcademicSkillsTestSeeder`.

Dostęp do dokumentacji API: `http://localhost:8080/swagger-ui/index.html` 