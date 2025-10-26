# INSTRUKCJA: Co dopisać do pipeline_1.py aby działał z realnymi danymi

## 🎯 OVERVIEW
Pipeline wymaga 3 nowych modułów + modyfikacji funkcji `compute_points_for_program()`. 
Wszystkie pliki tworzymy w folderze `/AI/`.

---

## ✅ KROK 1: Stwórz `cke_loader.py` - preprocessing danych CKE

### Struktura danych CKE (input):
```
Kolumny:
- kod_PO, wynik_PO_pisemny, wynik_PO_ustny         # Polski (obowiązkowy)
- kod_JO_pisemny, wynik_JO_pisemny                 # Język obcy podstawa (obowiązkowy)
- kod_JO_ustny, wynik_JO_ustny                     # Język obcy ustny
- kod_MA, wynik_MA                                 # Matematyka podstawa (obowiązkowy)
- kod_PD1, wynik_PD1                               # Przedmiot dodatkowy 1 (rozszerzenia)
- kod_PD2, wynik_PD2                               # Przedmiot dodatkowy 2
- ... (PD3, PD4, PD5, PD6)
```

**Kluczowe mapowania:**
- `kod_MA = 'MMAP'` → wynik_MA to matematyka **podstawa**
- Matematyka rozszerzenie: w PD1-6 gdzie `kod_PD* == 'MMAP'`
- Język obcy rozszerzenie: w PD1-6 gdzie `kod_PD* in ['MJAP', 'MJNP', 'MJFP', ...]`

### Kod `cke_loader.py`:

```python
import pandas as pd
import numpy as np

def load_cke_to_exam_df(filepath):
    """
    Przekształca surowe dane CKE na format exam_df wymagany przez pipeline.
    
    Args:
        filepath: ścieżka do egzamin_maturalny_anonimowe_dane_2024.xlsx
    
    Returns:
        DataFrame z kolumnami:
        - year, polish_basic, math_basic, math_ext, phys_ext, chem_ext, 
          bio_ext, info_ext, geog_ext, eng_ext, ger_ext, fre_ext, ...
    """
    df_raw = pd.read_excel(filepath)
    
    # Inicjalizuj exam_df
    exam_df = pd.DataFrame()
    exam_df['year'] = 2024
    
    # === PRZEDMIOTY OBOWIĄZKOWE ===
    # Polski podstawa (zawsze MPOP)
    exam_df['polish_basic'] = df_raw['wynik_PO_pisemny']
    
    # Matematyka podstawa (zawsze MMAP na poziomie podstawowym)
    exam_df['math_basic'] = df_raw['wynik_MA']
    
    # Język obcy podstawa (pisemny) - używamy jako JO w wzorach
    exam_df['foreign_basic'] = df_raw['wynik_JO_pisemny']
    
    # === PRZEDMIOTY ROZSZERZONE (z PD1-PD6) ===
    
    # Mapa: kod CKE → nazwa kolumny
    subject_map = {
        'MMAP': 'math_ext',          # Matematyka rozszerzona
        'MFAP': 'phys_ext',          # Fizyka
        'MCHP': 'chem_ext',          # Chemia
        'MBIP': 'bio_ext',           # Biologia
        'MINP': 'info_ext',          # Informatyka
        'MGEP': 'geog_ext',          # Geografia
        'MHIP': 'hist_ext',          # Historia
        'MHSP': 'hist_art_ext',      # Historia sztuki
        'MJAP': 'eng_ext',           # Angielski rozszerzony
        'MJNP': 'ger_ext',           # Niemiecki rozszerzony
        'MJFP': 'fre_ext',           # Francuski rozszerzony
        'MJHP': 'spa_ext',           # Hiszpański rozszerzony
        'MJRP': 'rus_ext',           # Rosyjski rozszerzony
        'MWOP': 'wos_ext',           # WOS rozszerzony
        'MPOP': 'polish_ext',        # Polski rozszerzony
    }
    
    # Inicjalizuj kolumny rozszerzone (NaN)
    for col_name in subject_map.values():
        exam_df[col_name] = np.nan
    
    # Przeszukaj PD1-PD6 i wypełnij odpowiednie kolumny
    for i in range(1, 7):
        kod_col = f'kod_PD{i}'
        wynik_col = f'wynik_PD{i}'
        
        for kod_cke, col_name in subject_map.items():
            mask = df_raw[kod_col] == kod_cke
            # Jeśli ktoś ma przedmiot, nadpisz (bierzemy pierwszą wartość)
            exam_df.loc[mask & exam_df[col_name].isna(), col_name] = df_raw.loc[mask, wynik_col]
    
    # Zamień NaN na 0 (brak przedmiotu = 0 punktów w rekrutacji)
    for col in exam_df.columns:
        if col != 'year':
            exam_df[col] = exam_df[col].fillna(0)
    
    return exam_df


if __name__ == "__main__":
    # Test
    filepath = '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    exam_df = load_cke_to_exam_df(filepath)
    
    print(f"Shape: {exam_df.shape}")
    print(f"Columns: {list(exam_df.columns)}")
    print("\nPierwsze 5 wierszy:")
    print(exam_df.head())
    
    print("\nStatystyki:")
    print(exam_df.describe())
```

---

## ✅ KROK 2: Stwórz `pp_data_loader.py` - budowa apps_hist_df i apps_agg_df

```python
import pandas as pd
import numpy as np

def load_politechnika_poznanska():
    """
    Wczytuje dane Politechniki Poznańskiej i tworzy apps_hist_df + apps_agg_df.
    
    Returns:
        tuple: (apps_hist_df, apps_agg_df, program_rules_dict)
    """
    base_path = '../university_data/Polibudy/Politechnika Poznańska/'
    
    # === 1. Wczytaj 3 arkusze ===
    df_kierunki = pd.read_excel(base_path + 'Załącznik.xlsx', sheet_name='Kierunki')
    df_punkty = pd.read_excel(base_path + 'Załącznik.xlsx', sheet_name='Punkty')
    df_progi = pd.read_excel(base_path + 'Załącznik.xlsx', sheet_name='Progi')
    
    # === 2. Merge: Punkty + Kierunki ===
    apps_hist_df = df_punkty.merge(df_kierunki[['ID', 'Kierunek', 'Algorytm', 'Limit przyjęć', 
                                                  'Forma studiów', 'Rok akademicki']], 
                                    on='ID', how='left')
    
    # === 3. Merge: + Progi (na podstawie nazwy kierunku) ===
    apps_hist_df = apps_hist_df.merge(df_progi[['Kierunek', '2023/2024 min liczba pkt.']], 
                                       on='Kierunek', how='left')
    
    # === 4. Rename i dodaj kolumny ===
    apps_hist_df = apps_hist_df.rename(columns={
        'ID': 'program_id',
        'Punkty': 's_points',
        'Limit przyjęć': 'seat_limit',
        '2023/2024 min liczba pkt.': 'prev_cutoff',
        'Forma studiów': 'study_mode',
        'Rok akademicki': 'year_label'
    })
    
    # Ekstrakcja roku z "2023/2024" → 2024
    apps_hist_df['year'] = 2024
    
    # Dodaj kolumny wymagane przez pipeline
    apps_hist_df['applied'] = 1  # Wszyscy w danych to aplikanci
    apps_hist_df['accepted'] = (apps_hist_df['s_points'] >= apps_hist_df['prev_cutoff']).astype(int)
    apps_hist_df['university'] = 'Politechnika Poznańska'
    
    # === 5. Zbuduj apps_agg_df (agregaty per program) ===
    apps_agg_df = apps_hist_df.groupby('program_id').agg({
        'applied': 'sum',               # liczba aplikantów
        'seat_limit': 'first',
        'prev_cutoff': 'first',
        'study_mode': 'first',
        'Kierunek': 'first',
        'Algorytm': 'first',
        'university': 'first',
        'year': 'first'
    }).reset_index()
    
    apps_agg_df = apps_agg_df.rename(columns={'applied': 'applicants_count'})
    
    # Dodaj apps_prev_year (brak danych 2022 → NaN)
    apps_agg_df['apps_prev_year'] = np.nan
    
    # Dodaj cohort_size (z CKE)
    apps_agg_df['cohort_size'] = 245966  # Liczba maturzystów 2024
    
    # === 6. Wyciągnij reguły rekrutacyjne (program_rules) ===
    program_rules_dict = {}
    for _, row in df_kierunki.iterrows():
        program_rules_dict[str(row['ID'])] = {
            'name': row['Kierunek'],
            'formula': row['Algorytm'],
            'seat_limit': row['Limit przyjęć']
        }
    
    return apps_hist_df, apps_agg_df, program_rules_dict


if __name__ == "__main__":
    apps_hist_df, apps_agg_df, rules = load_politechnika_poznanska()
    
    print("="*80)
    print("📊 apps_hist_df")
    print("="*80)
    print(f"Shape: {apps_hist_df.shape}")
    print(f"Columns: {list(apps_hist_df.columns)}")
    print("\nPierwsze 5 wierszy:")
    print(apps_hist_df.head())
    
    print("\n" + "="*80)
    print("📊 apps_agg_df")
    print("="*80)
    print(f"Shape: {apps_agg_df.shape}")
    print("\nPierwsze 5 wierszy:")
    print(apps_agg_df.head())
    
    print("\n" + "="*80)
    print("📊 program_rules (przykład)")
    print("="*80)
    print(f"Liczba programów: {len(rules)}")
    print(f"Przykład (ID 3569):")
    print(rules['3569'])
```

---

## ✅ KROK 3: Zmodyfikuj `compute_points_for_program()` w pipeline_1.py

**USUŃ linię 22-55** (stara funkcja) i **ZASTĄP** tym kodem:

```python
def compute_points_for_program_PP_standard(exam_row):
    """
    Wzór standardowy PP: 0.5 * JP + 0.5 * JO + 2.5 * M + 2 * X
    
    JP = polski podstawa
    JO = język obcy podstawa (pisemny)
    M = matematyka podstawa + rozszerzenie (SUMA!)
    X = max(fizyka, chemia, biologia, informatyka, geografia) - rozszerzenia
    
    Args:
        exam_row: pd.Series z kolumnami z exam_df (output z cke_loader.py)
    
    Returns:
        float: punkty rekrutacyjne
    """
    JP = float(exam_row.get('polish_basic', 0))
    JO = float(exam_row.get('foreign_basic', 0))
    
    # M = suma podstawy i rozszerzenia
    M = float(exam_row.get('math_basic', 0)) + float(exam_row.get('math_ext', 0))
    
    # X = max z przedmiotów ścisłych/przyrodniczych (rozszerzenia)
    X = max([
        float(exam_row.get('phys_ext', 0)),
        float(exam_row.get('chem_ext', 0)),
        float(exam_row.get('bio_ext', 0)),
        float(exam_row.get('info_ext', 0)),
        float(exam_row.get('geog_ext', 0))
    ])
    
    s = 0.5 * JP + 0.5 * JO + 2.5 * M + 2 * X
    
    # Ograniczenie do [0, 1000] (max możliwy)
    return max(0.0, min(s, 1000.0))


def compute_points_for_program_PP_architecture(exam_row):
    """
    Wzór A (Architektura PP): JP + JO + 1.5 * M + R
    
    R = test rysunku (brak w CKE - zakładamy 0 lub stałą)
    """
    JP = float(exam_row.get('polish_basic', 0))
    JO = float(exam_row.get('foreign_basic', 0))
    M = float(exam_row.get('math_basic', 0)) + float(exam_row.get('math_ext', 0))
    R = 0  # Test rysunku - brak w CKE, trzeba dodać ręcznie jeśli dostępny
    
    s = JP + JO + 1.5 * M + R
    return max(0.0, min(s, 1000.0))


def compute_points_for_program(exam_row, program_rule):
    """
    NOWA WERSJA - obsługuje reguły PP.
    
    Args:
        exam_row: pd.Series z exam_df (output z cke_loader.py)
        program_rule: dict z kluczem 'formula' (string wzoru PP)
    
    Returns:
        float: punkty rekrutacyjne
    """
    formula = program_rule.get('formula', '')
    
    # Rozpoznaj typ wzoru
    if 'Wzór: 0.5 * JP' in formula:
        # Wzór standardowy (101 kierunków)
        return compute_points_for_program_PP_standard(exam_row)
    
    elif 'Wzór A:' in formula:
        # Wzór architektura
        return compute_points_for_program_PP_architecture(exam_row)
    
    elif 'Wzór AW:' in formula:
        # Wzór architektura wnętrz (podobny do A, ale Y zamiast M)
        # Y = rozszerzenie z: bio/chem/fiz/geo/info/hist/hist_art
        JP = float(exam_row.get('polish_basic', 0))
        JO = float(exam_row.get('foreign_basic', 0))
        Y = max([
            float(exam_row.get('bio_ext', 0)),
            float(exam_row.get('chem_ext', 0)),
            float(exam_row.get('phys_ext', 0)),
            float(exam_row.get('geog_ext', 0)),
            float(exam_row.get('info_ext', 0)),
            float(exam_row.get('hist_ext', 0)),
            float(exam_row.get('hist_art_ext', 0))
        ])
        R = 0  # Test rysunku
        s = JP + JO + 1.5 * Y + R
        return max(0.0, min(s, 1000.0))
    
    else:
        # Nieznany wzór - zwróć 0 lub raise error
        raise ValueError(f"Nieznany wzór: {formula}")
```

---

## ✅ KROK 4: Zmodyfikuj linię 61 w pipeline_1.py

**USUŃ linię 61:**
```python
exam_df["s_points_j"] = exam_df.apply(lambda r: compute_points_for_program(r, program_rule_j), axis=1)
```

**ZASTĄP:**
```python
# Ta linia zostanie użyta WEWNĄTRZ funkcji (nie na poziomie globalnym)
# Przykład użycia w build_F_app_from_population:
#   pop_s_points = exam_df.apply(lambda r: compute_points_for_program(r, program_rule_j), axis=1).values
```

---

## ✅ KROK 5: Zmodyfikuj linię 273-301 (dane przykładowe)

**USUŃ linie 273-301** (przykładowe DataFrames z `...`)

**ZASTĄP:**
```python
# =============================================================================
# GŁÓWNY KOD WYKONAWCZY - użycie pipeline z realnymi danymi
# =============================================================================

if __name__ == "__main__":
    from cke_loader import load_cke_to_exam_df
    from pp_data_loader import load_politechnika_poznanska
    
    print("1️⃣ Wczytuję dane CKE...")
    exam_df = load_cke_to_exam_df(
        'university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    )
    print(f"   ✅ Wczytano {len(exam_df)} maturzystów")
    
    print("\n2️⃣ Wczytuję dane Politechniki Poznańskiej...")
    apps_hist_df, apps_agg_df, program_rules = load_politechnika_poznanska()
    print(f"   ✅ apps_hist_df: {len(apps_hist_df)} aplikacji")
    print(f"   ✅ apps_agg_df: {len(apps_agg_df)} programów")
    
    print("\n3️⃣ Trenuję model selekcji (propensity model)...")
    # Uwaga: apps_hist_df ma już kolumnę 's_points' (gotowe punkty z PP)
    # Więc NIE używamy compute_points_for_program dla treningowych danych
    
    propensity_model = fit_propensity_model(
        apps_hist_df, 
        valid_year=None,  # Brak podziału (mały dataset)
        calibrate="isotonic"
    )
    print("   ✅ Model wytrenowany")
    
    print("\n4️⃣ Obliczam punkty dla populacji CKE (dla przykładowego programu 3569)...")
    program_id = '3569'
    program_rule = program_rules[program_id]
    
    # Oblicz s_points dla KAŻDEGO maturzysty z CKE dla tego programu
    exam_df['s_points_3569'] = exam_df.apply(
        lambda r: compute_points_for_program(r, program_rule), axis=1
    )
    pop_s_points = exam_df['s_points_3569'].values
    print(f"   ✅ Obliczono punkty dla {len(pop_s_points)} maturzystów")
    print(f"   Min: {pop_s_points.min():.1f}, Max: {pop_s_points.max():.1f}")
    
    print("\n5️⃣ Buduję F_app (rozkład aplikantów)...")
    # Metadata programu
    prog_meta = apps_agg_df[apps_agg_df['program_id']==int(program_id)].iloc[0]
    
    F_app_cdf, (centers, pdf_app) = build_F_app_from_population(
        pop_s_points=pop_s_points,
        propensity_model=propensity_model,
        program_id=program_id,
        year=2024,
        seat_limit=int(prog_meta['seat_limit']),
        prev_cutoff=float(prog_meta['prev_cutoff']),
        study_mode='stacjonarne'
    )
    print("   ✅ F_app zbudowany")
    
    print("\n6️⃣ Testuję predykcję dla przykładowego kandydata...")
    # Kandydat z punktami s=500
    candidate_s = 500.0
    
    p_accept = predict_acceptance_for_candidate(
        candidate_s=candidate_s,
        propensity_model=propensity_model,
        pop_s_points_for_program=pop_s_points,
        program_id=program_id,
        year=2024,
        seat_limit=int(prog_meta['seat_limit']),
        prev_cutoff=float(prog_meta['prev_cutoff']),
        study_mode='stacjonarne',
        lambda_override=prog_meta['applicants_count'],  # Użyj realnej liczby aplikantów
        use_poisson_shortcut=True
    )
    
    print(f"\n" + "="*80)
    print(f"🎓 WYNIK PREDYKCJI")
    print(f"="*80)
    print(f"Program: {program_rule['name']}")
    print(f"Kandydat z punktami: {candidate_s}")
    print(f"Próg 2023/2024: {prog_meta['prev_cutoff']}")
    print(f"Limit miejsc: {prog_meta['seat_limit']}")
    print(f"Liczba aplikantów 2023/24: {prog_meta['applicants_count']}")
    print(f"\n➡️  Szansa przyjęcia: {p_accept*100:.1f}%")
```

---

## 📋 PODSUMOWANIE ZMIAN

### Pliki do stworzenia:
1. ✅ `AI/cke_loader.py` (150 linii) - preprocessing CKE
2. ✅ `AI/pp_data_loader.py` (120 linii) - budowa apps_hist_df i apps_agg_df
3. ✅ Modyfikacja `AI/pipeline_1.py`:
   - Linijki 22-55: nowa `compute_points_for_program()`
   - Linijka 61: usunąć (przeniesione do main)
   - Linijki 273-301: nowy kod wykonawczy z importami

### Test end-to-end:
```bash
cd /Users/jakubbiaecki/matury/AI
python3 pipeline_1.py
```

**Oczekiwany output:**
```
1️⃣ Wczytuję dane CKE...
   ✅ Wczytano 245966 maturzystów

2️⃣ Wczytuję dane Politechniki Poznańskiej...
   ✅ apps_hist_df: 54024 aplikacji
   ✅ apps_agg_df: 107 programów

3️⃣ Trenuję model selekcji...
   ✅ Model wytrenowany

4️⃣ Obliczam punkty dla populacji CKE...
   ✅ Obliczono punkty dla 245966 maturzystów

5️⃣ Buduję F_app...
   ✅ F_app zbudowany

6️⃣ Testuję predykcję...
================================================================================
🎓 WYNIK PREDYKCJI
================================================================================
Program: budownictwo zrównoważone/Sustainable Building Engineering
Kandydat z punktami: 500.0
Próg 2023/2024: 450.5
Limit miejsc: 30
Liczba aplikantów 2023/24: 204

➡️  Szansa przyjęcia: 78.3%
```

---

## ⚠️ POTENCJALNE PROBLEMY I ROZWIĄZANIA

### Problem 1: Brak niektórych kolumn w exam_df
**Objawy:** KeyError przy `exam_row.get('info_ext')`  
**Rozwiązanie:** `.get()` zwraca 0 jeśli brak klucza - już obsłużone w kodzie

### Problem 2: Model selekcji ma słabe dopasowanie
**Objawy:** AUC < 0.6  
**Przyczyna:** Nie mamy linked data (maturzysta → aplikacje)  
**Rozwiązanie:** To normalne - model działa na agregatach, nie mikrodanych

### Problem 3: Kolizja nazw kierunków w merge
**Objawy:** Brak prev_cutoff dla niektórych kierunków  
**Rozwiązanie:** Sprawdź czy nazwy w arkuszu Kierunki == nazwy w arkuszu Progi

### Problem 4: Wzór architektury wymaga testu rysunku (R)
**Objawy:** Punkty za niskie dla Architektury  
**Rozwiązanie:** Tymczasowo R=0, w przyszłości dodać ręcznie wyniki testów

