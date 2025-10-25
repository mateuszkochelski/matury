# Recruitment Calculator API

Dokument opisuje sposob konfiguracji i wywolywania kalkulatora punktow rekrutacyjnych dostepnego w module `recruitment`.

## Endpoint

- Metoda: `POST`
- Sciezka: `/api/recruitment-calculator/calculate`
- Naglowek: `Content-Type: application/json`

## Schemat zapytania

Zgodny z rekordem `CalculateRecruitmentPointsRequest`:

```json
{
  "universityId": "politechnika-poznanska",
  "fieldOfStudyId": "architecture",
  "examResults": [
    { "subjectCode": "polish_language", "level": "EXTENDED", "score": 62 },
    { "subjectCode": "english_language", "level": "BILINGUAL", "score": 35 },
    { "subjectCode": "mathematics", "level": "EXTENDED", "score": 78 },
    { "subjectCode": "art_exam", "score": 420 }
  ]
}
```

### Pola

- `universityId`: identyfikator uczelni. Glowny identyfikator i aliasy znajdziesz w `backend/src/main/resources/recruitment/formulas.json` (sekcja `universities`).
- `fieldOfStudyId`: identyfikator kierunku w ramach danej uczelni. Takze posiada aliasy (np. numeryczne kody).
- `examResults`: lista wynikow matur:
  - `subjectCode`: kod przedmiotu z sekcji `subjects` w `formulas.json`. Zawiera m.in. `mathematics`, `polish_language`, jezyki obce (`english_language`, `english_bilingual`, `german_language`, ...), przedmioty przyrodnicze (`physics`, `biology`, `chemistry`, ...), egzamin zawodowy (`vocational_exam`) czy artystyczny (`art_exam`).
  - `level`: poziom egzaminu. Akceptowane wartosci to m.in. `BASIC`, `EXTENDED`, `BILINGUAL`, `VOCATIONAL_TECHNICIAN`. Dla egzaminow bez poziomu (`art_exam`) pole moze byc puste (`null`).
  - `score`: liczba rzeczywista >= 0.0 reprezentujaca wynik procentowy albo punktowy (w zaleznosci od przedmiotu).

#### Dozwolone poziomy

| level | opis | uwagi |
| --- | --- | --- |
| BASIC | Poziom podstawowy | Moze korzystac z konwersji rozszerzenia jesli `allowExtendedToBasicConversion = true`. |
| EXTENDED | Poziom rozszerzony | Brany pod uwage w termach wymagajacych rozszerzenia oraz w kompozytach X/XG. |
| BILINGUAL | Wersja dwujezyczna | W termach jezykowych moze zostac znormalizowana (`normalizeBilingualScores = true`). |
| VOCATIONAL_TECHNICIAN | Egzamin zawodowy technika | Oceniany z dodatkowym mnoznikiem tam, gdzie wlaczono `includeVocationalResults`. |

#### Dostepne przedmioty (`subjectCode`)

| kod | opis |
| --- | --- |
| mathematics | Matematyka |
| polish_language | Jezyk polski |
| english_language | Jezyk angielski |
| german_language | Jezyk niemiecki |
| english_bilingual | Jezyk angielski dwujezyczny |
| spanish_bilingual | Jezyk hiszpanski dwujezyczny |
| french_bilingual | Jezyk francuski dwujezyczny |
| german_bilingual | Jezyk niemiecki dwujezyczny |
| russian_bilingual | Jezyk rosyjski dwujezyczny |
| italian_bilingual | Jezyk wloski dwujezyczny |
| russian_language | Jezyk rosyjski |
| spanish_language | Jezyk hiszpanski |
| french_language | Jezyk francuski |
| belarusian_language | Jezyk bialoruski |
| italian_language | Jezyk wloski |
| lithuanian_language | Jezyk litewski |
| ukrainian_language | Jezyk ukrainski |
| biology | Biologia |
| chemistry | Chemia |
| geography | Geografia |
| physics | Fizyka |
| history | Historia |
| civics | Wiedza o spoleczenstwie |
| informatics | Informatyka |
| philosophy | Filozofia |
| history_of_art | Historia sztuki |
| history_of_music | Historia muzyki |
| latin_and_culture | Lacinski i kultura antyczna |
| lemko_language | Jezyk lemkowski |
| cashubian_language | Jezyk kaszubski |
| german_minority_language | Jezyk niemiecki (mniejszosci narodowej) |
| art_exam | Egzamin z uzdolnien artystycznych (0-500 pkt) |
| vocational_exam | Egzamin zawodowy |

> Uwaga: jezeli formuly wymagaja konkretnego wyniku (`failIfMissing = true`), brak odpowiedniego wpisu w `examResults` spowoduje blad `RecruitmentCalculationException`.

## Swagger-style spec

```yaml
paths:
  /api/recruitment-calculator/calculate:
    post:
      summary: Oblicza wynik rekrutacyjny
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [universityId, fieldOfStudyId, examResults]
              properties:
                universityId:
                  type: string
                  example: politechnika-poznanska
                fieldOfStudyId:
                  type: string
                  example: architecture
                examResults:
                  type: array
                  minItems: 1
                  items:
                    type: object
                    required: [subjectCode, score]
                    properties:
                      subjectCode:
                        type: string
                        enum: [mathematics, polish_language, english_language, german_language, english_bilingual, spanish_bilingual, french_bilingual, german_bilingual, russian_bilingual, italian_bilingual, russian_language, spanish_language, french_language, belarusian_language, italian_language, lithuanian_language, ukrainian_language, biology, chemistry, geography, physics, history, civics, informatics, philosophy, history_of_art, history_of_music, latin_and_culture, lemko_language, cashubian_language, german_minority_language, art_exam, vocational_exam]
                      level:
                        type: string
                        nullable: true
                        enum: [BASIC, EXTENDED, BILINGUAL, VOCATIONAL_TECHNICIAN]
                      score:
                        type: number
                        format: double
                        minimum: 0
      responses:
        "200":
          description: Sukces
          content:
            application/json:
              schema:
                type: object
                properties:
                  universityId:
                    type: string
                  fieldOfStudyId:
                    type: string
                  totalPoints:
                    type: number
                    format: double
                  breakdown:
                    type: array
                    items:
                      type: object
                      properties:
                        termId:
                          type: string
                        description:
                          type: string
                        subjectCode:
                          type: string
                          nullable: true
                        level:
                          type: string
                          nullable: true
                        rawScore:
                          type: number
                        coefficient:
                          type: number
                        pointsAwarded:
                          type: number
      tags:
        - Recruitment Calculator
```

## Jak dziala kalkulator

1. Na podstawie `universityId` oraz `fieldOfStudyId` ladowana jest odpowiednia konfiguracja formuly (`RecruitmentFormulaConfig`).
2. Kazdy termin formuly (`TermConfig`) oblicza punkty niezaleznie:
   - `SPECIFIC_SUBJECT` wybiera wynik danego przedmiotu (z opcjonalna konwersja rozszerzenia do podstawy).
   - `BEST_OF_GROUPS` wyszukuje najlepszy wynik w grupie przedmiotow (przy wsparciu wynikow zawodowych, jezeli dopuszczone).
   - `LANGUAGE_GROUP` wybiera najlepszy jezyk z grupy z normalizacja wynikow dwujezycznych jesli wlaczona.
   - `POZNAN_COMPOSITE_X` (uzywany przez Politechnike Poznanska) laczy najlepsze wyniki podstawowe i rozszerzone przedmiotow X lub porownuje je z wynikiem egzaminu zawodowego.
3. Suma punktow wszystkich terminow trafia do `totalPoints`, a szczegoly kazdego terminu zwracamy w `breakdown`.

Kod zrodlowy: `backend/src/main/java/agh/matury/recruitment/RecruitmentCalculatorService.java`.

## Struktura odpowiedzi

`RecruitmentCalculationResponse`:

- `universityId`, `fieldOfStudyId` – potwierdzenie rozpoznanego kierunku,
- `totalPoints` – wynik koncowy (double),
- `breakdown` – lista `TermBreakdownDTO` zawierajaca:
  - `termId`, `description`,
  - `subjectCode` oraz `level` uzyte do obliczenia,
  - `rawScore`, `coefficient`, `pointsAwarded`.

## Dostepne konfiguracje

GLOWNE ZRODLO: `backend/src/main/resources/recruitment/formulas.json`.

- Sekcja `subjects` – lista wszystkich kodow przedmiotow. W razie potrzeby mozna ja rozszerzyc.
- Sekcja `subjectGroups` – zdefiniowane grupy przedmiotow (np. `pp-x`, `pp-xg`, jezyki obce).
- Sekcja `universities` – definicje uczelni wraz z kierunkami, aliasami oraz formuly skladajacej sie z terminow.

### Uczelnie i kierunki (stan aktualny)

| universityId | nazwa | kierunki (`fieldOfStudyId`) | przykladowy alias |
| --- | --- | --- | --- |
| politechnika-slaska | Politechnika Slaska | `default` (Pozostale kierunki ksztalcenia) | 35 |
| politechnika-poznanska | Politechnika Poznanska | `engineering-x`, `science-xg`, `architecture` | 33 |

> Pelna lista aliasow oraz dodatkowych nazw pol znajdziesz bezposrednio w `formulas.json` (klucze `aliases`).

Zmiany w konfiguracji wymagaja ponownego uruchomienia backendu (plik jest ladowany przy starcie aplikacji).

## Narzedzia pomocnicze

- Skrypty `backend/docs/politechnika-slaska-scenarios.sh` oraz `backend/docs/politechnika-poznanska-scenarios.sh` wysylaja gotowe zestawy wynikow matur do API i zapisuje raporty w formacie Markdown. Ustaw `BASE_URL=http://localhost:8080/api` i uruchom skrypt, aby porownac wyniki z kalkulatorami uczelni.
- `backend/docs/recruitment-calculator-curls.sh` – proste smoke testy przykrywajace kilka scenariuszy (wymagac `jq`).
- `backend/docs/politechnika-poznanska-curl.sh` – pojedynczy przyklad dla kierunku architektury (uzyteczne do szybkich testow).

## Diagnostyka bledow

- Jezeli kalkulator nie rozpoznaje identyfikatora uczelni lub kierunku, otrzymasz wyjatek z komunikatem `Unknown field of study...` lub `Unknown university...`.
- Brak wymaganych wynikow (np. matematyka podstawowa dla kierunku inzyynieryjnego) skutkuje `RecruitmentCalculationException` z opisem brakujacego terminu.
- Wyniki o wartosciach ujemnych lub `score = null` spowoduja odrzucenie calosci z komunikatem `Exam scores must be provided and greater or equal to zero`.

## Rozszerzanie

- Dodanie nowej uczelni lub kierunku polega na dopisaniu odpowiednich struktur w `formulas.json`.
- Przy wprowadzaniu nietypowych kryteriow mozna rozbudowac `RecruitmentCalculatorService` o nowy `term.type`.
- Testy jednostkowe mozna budowac na bazie `RecruitmentCalculatorServicePoznanTest` (`backend/src/test/java/agh/matury/recruitment/RecruitmentCalculatorServicePoznanTest.java`), ktory demonstruje obsluge roznych konfiguracji.

W razie watpliwosci sprawdz logike w `RecruitmentCalculatorService` oraz konfiguracje w `formulas.json` – to dwa kluczowe miejsca sterujace dzialaniem kalkulatora.
