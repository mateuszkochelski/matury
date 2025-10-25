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

> Uwaga: jezeli formuly wymagaja konkretnego wyniku (`failIfMissing = true`), brak odpowiedniego wpisu w `examResults` spowoduje blad `RecruitmentCalculationException`.

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
