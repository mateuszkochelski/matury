# 📊 API Absolwentów - Dokumentacja

## 🎯 Przegląd

API Absolwentów zostało stworzone na podstawie analizy danych z pliku `graduates-major-dictionary.csv` zawierającego 694 różne metryki dotyczące losów zawodowych absolwentów szkół wyższych w Polsce.

## 📋 Struktura agregacji danych

### 🎓 **Grupowania główne**

#### 1. **WSKAŹNIKI ZATRUDNIENIA**
- **Grupa:** Zatrudnienie absolwentów  
- **Wymiary agregacji:**
  - Per rok po studiach (P1, P2, P3, P4, P5)
  - Per doświadczenie pracy (DOSW/NDOSW)
  - Per typ zatrudnienia (ETAT/SAMOZ)
  - Per okres studiowania (STUD/NSTUD)

#### 2. **WSKAŹNIKI BEZROBOCIA**  
- **Grupa:** Bezrobocie absolwentów
- **Wymiary agregacji:**
  - Per rok po studiach (P1-P5)
  - Per doświadczenie pracy (DOSW/NDOSW)
  - Względny Wskaźnik Bezrobocia (WWB)

#### 3. **WSKAŹNIKI ZAROBKÓW**
- **Grupa:** Zarobki absolwentów
- **Wymiary agregacji:**
  - Per rok po studiach (P1-P5)
  - Per typ zatrudnienia (ETAT/całkowite)
  - Względny Wskaźnik Zarobków (WWZ)

#### 4. **WSKAŹNIKI GEOGRAFICZNE**
- **Grupa:** Lokalizacja absolwentów
- **Wymiary agregacji:**
  - Per kategoria miejscowości (największe miasta/miasta powiatowe/mniejsze miejscowości)
  - Per województwo

#### 5. **WSKAŹNIKI KARIERY AKADEMICKIEJ**
- **Grupa:** Kontynuacja edukacji
- **Wymiary agregacji:**
  - Per typ studiów (II stopień, doktoranckie)
  - Per rok kontynuacji (P1-P5)

## 🌐 Endpointy API

### 📂 **FILTRY**

```
GET /api/graduates/filtry/wojewodztwa
```
**Opis:** Lista wszystkich województw  
**Odpowiedź:** `["dolnośląskie", "kujawsko-pomorskie", ...]`

```
GET /api/graduates/filtry/poziomy
```
**Opis:** Lista poziomów studiów  
**Odpowiedź:** `["I stopień", "II stopień", "jednolite"]`

```
GET /api/graduates/filtry/lata
```
**Opis:** Lista lat ukończenia studiów  
**Odpowiedź:** `[2019, 2020, 2021, ...]`

```
GET /api/graduates/filtry/dziedziny
```
**Opis:** Lista dziedzin nauki  
**Odpowiedź:** `["nauki techniczne", "nauki ekonomiczne", ...]`

### 👥 **WSKAŹNIKI ZATRUDNIENIA**

```
GET /api/graduates/statystyki/zatrudnienie/wojewodztwa
```
**Opis:** Średni Względny Wskaźnik Zatrudnienia per województwo per rok po studiach  
**Parametry:**
- `wojewodztwo` (opcjonalny) - filtr województwa
- `poziom` (opcjonalny) - filtr poziomu studiów  
- `rokDyplomu` (opcjonalny) - filtr roku dyplomu

**Przykład odpowiedzi:**
```json
[
  {
    "wojewodztwo": "dolnośląskie",
    "zatrudnieniePierwszyRok": 85.5,
    "zatrudnienieDrugiRok": 88.2,
    "zatrudnienieTrzeciRok": 91.0,
    "zatrudnienieCzwartyRok": 92.5,
    "zatrudnieniePiatyRok": 93.1,
    "srednieZatrudnienie": 90.06
  }
]
```

```
GET /api/graduates/statystyki/zatrudnienie/pracodawcy/wojewodztwa
```
**Opis:** Średnia miesięczna liczba pracodawców i rotacja zatrudnienia per województwo

### 🚫 **WSKAŹNIKI BEZROBOCIA**

```
GET /api/graduates/statystyki/bezrobocie/wojewodztwa
```
**Opis:** Procent absolwentów doświadczających bezrobocia per województwo per rok po studiach

```
GET /api/graduates/statystyki/bezrobocie/wwb/wojewodztwa
```
**Opis:** Względny Wskaźnik Bezrobocia per województwo per rok po studiach

### 💰 **WSKAŹNIKI ZAROBKÓW**

```
GET /api/graduates/statystyki/zarobki/wojewodztwa
```
**Opis:** Średnie miesięczne wynagrodzenie per województwo per rok po studiach

```
GET /api/graduates/statystyki/zarobki/wwz/wojewodztwa
```
**Opis:** Względny Wskaźnik Zarobków per województwo per rok po studiach

**Przykład odpowiedzi WWZ:**
```json
[
  {
    "wojewodztwo": "mazowieckie",
    "wwzPierwszyRok": 1.15,
    "wwzDrugiRok": 1.22,
    "wwzTrzeciRok": 1.28,
    "wwzCzwartyRok": 1.35,
    "wwzPiatyRok": 1.42,
    "sredniaWwz": 1.284
  }
]
```

### 🗺️ **WSKAŹNIKI GEOGRAFICZNE**

```
GET /api/graduates/statystyki/geografia
```
**Opis:** Średnie zarobki i wskaźniki per kategoria miejscowości

**Przykład odpowiedzi:**
```json
{
  "zarobkiNajwiekszeMiasta": 6500.00,
  "zarobkiMiastaPowiatowe": 5200.00,
  "zarobkiMniejszeMiejscowosci": 4300.00,
  "wwzNajwiekszeMiasta": 1.25,
  "wwzMiastaPowiatowe": 1.05,
  "wwzMniejszeMiejscowosci": 0.88
}
```

### 🎓 **WSKAŹNIKI KARIERY AKADEMICKIEJ**

```
GET /api/graduates/statystyki/kariera-akademicka/wojewodztwa
```
**Opis:** Procent absolwentów kontynuujących edukację per województwo

### 📊 **AGREGACJE ZAAWANSOWANE**

```
GET /api/graduates/statystyki/dziedziny
```
**Opis:** Statystyki per dziedzina nauki (WWZ, WWB, zatrudnienie w 3. roku)

```
GET /api/graduates/statystyki/uczelnie  
```
**Opis:** Statystyki per uczelnia (WWZ, WWB, zatrudnienie w 3. roku)

```
GET /api/graduates/ranking/kierunki
```
**Opis:** Ranking kierunków pod względem Względnego Wskaźnika Zarobków w 3. roku  
**Parametry:** `minCount` - minimalna liczba absolwentów (domyślnie 10)

### ⏰ **CZAS DO PODJĘCIA PRACY**

```
GET /api/graduates/statystyki/czas-do-pracy
```
**Opis:** Średni czas (w miesiącach) od uzyskania dyplomu do podjęcia pierwszej pracy  
**Parametry:**
- `wojewodztwo` (opcjonalny) - filtr województwa
- `poziom` (opcjonalny) - filtr poziomu studiów  
- `rokDyplomu` (opcjonalny) - filtr roku dyplomu

### 🎓 **KONTYNUACJA STUDIÓW II STOPNIA**

```
GET /api/graduates/statystyki/kontynuacja-studiow/wojewodztwa
```
**Opis:** Procent absolwentów I stopnia kontynuujących studia II stopnia per województwo  

### 📊 **PODSTAWOWE INFORMACJE O ABSOLWENTACH**

```
GET /api/graduates/statystyki/podstawowe-info
```
**Opis:** Podstawowe dane o absolwentach: rok, stopień, forma studiów, liczba absolwentów

```
GET /api/graduates/statystyki/podsumowanie/wojewodztwa
```
**Opis:** Zagregowane podsumowanie absolwentów per województwo

## 🔧 **Przykłady użycia**

### 📈 Analiza zarobków absolwentów informatyki w województwie mazowieckim

```bash
GET /api/graduates/statystyki/zarobki/wwz/wojewodztwa?wojewodztwo=mazowieckie&dziedzina=nauki%20techniczne
```

### 🎯 Top 10 kierunków o najwyższych zarobkach

```bash
GET /api/graduates/ranking/kierunki?minCount=50
```

### 📍 Porównanie geograficzne zarobków

```bash
GET /api/graduates/statystyki/geografia?poziom=II%20stopień&rokDyplomu=2020
```

## 🏗️ **Struktura bazy danych**

### Tabela `graduates`

**Główne kolumny:**
- `id` - klucz główny
- `wojewodztwo` - województwo uczelni
- `poziom` - poziom studiów
- `rok_dyplomu` - rok ukończenia
- `dziedzina` - dziedzina nauki
- `nazwa_kierunku` - nazwa kierunku
- `nazwa_uczelni` - nazwa uczelni

**Wskaźniki zatrudnienia (per rok P1-P5):**
- `czy_praca_p[1-5]` - procent zatrudnionych
- `czy_etat_p[1-5]` - procent na umowie o pracę
- `czy_samoz_p[1-5]` - procent samozatrudnionych

**Wskaźniki bezrobocia:**
- `czy_bezr_p[1-5]` - procent bezrobotnych
- `wwb_p[1-5]` - Względny Wskaźnik Bezrobocia

**Wskaźniki zarobków:**
- `e_zar_p[1-5]` - średnie zarobki
- `wwz_p[1-5]` - Względny Wskaźnik Zarobków
- `e_zar_etat_p[1-5]` - zarobki z umowy o pracę

### 📚 **Indeksy dla wydajności**

```sql
-- Indeksy podstawowe
CREATE INDEX idx_graduates_wojewodztwo ON graduates(wojewodztwo);
CREATE INDEX idx_graduates_poziom ON graduates(poziom);
CREATE INDEX idx_graduates_rok_dyplomu ON graduates(rok_dyplomu);

-- Indeksy kompozytowe
CREATE INDEX idx_graduates_woj_poziom_rok ON graduates(wojewodztwo, poziom, rok_dyplomu);
```

## 🚀 **Uruchomienie**

1. **Konfiguracja bazy danych PostgreSQL**
2. **Uruchomienie migracji:** `V4__add_graduates_table.sql`
3. **Import danych:** Seeder automatycznie załaduje słownik danych
4. **Dostęp do API:** `http://localhost:8080/api/graduates`
5. **Dokumentacja Swagger:** `http://localhost:8080/swagger-ui.html`

## 📝 **Planowane rozszerzenia**

- Import rzeczywistych danych absolwentów z pliku CSV
- Filtry zaawansowane (kombinacje wielu parametrów)
- Eksport danych do CSV/Excel
- Cache'owanie wyników agregacji
- API do analizy trendów czasowych

---

*Ostatnia aktualizacja: Grudzień 2024*