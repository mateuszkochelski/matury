"""
Training Data Builder - przygotowanie danych do treningu modelu propensity.

Główna funkcja: prepare_training_data()
- Dla każdego (program_id, year) robi matching CKE ↔ PP
- Zwraca DataFrame gotowy do treningu
- DODANO: Subject-specific percentiles dla robustności do zmian trudności egzaminów
"""

import pandas as pd
import numpy as np
from scipy import interpolate
from tqdm import tqdm
from multiprocessing import Pool, cpu_count


# Przedmioty do śledzenia (wszystkie używane w wzorach PP)
SUBJECTS_TO_TRACK = [
    'polish_basic', 'polish_ext',
    'foreign_basic', 'foreign_ext',
    'math_basic', 'math_ext',
    'phys_basic', 'phys_ext',
    'chem_basic', 'chem_ext',
    'bio_basic', 'bio_ext',
    'info_basic', 'info_ext',
    'geog_basic', 'geog_ext',
]


def add_subject_percentiles_to_dataframe(exam_df_multi_year):
    """
    WEKTORYZOWANE obliczanie percentyli - dodaje kolumny *_pct bezpośrednio do DataFrame.
    
    To jest O(students) zamiast O(programs × students)!
    Używa np.searchsorted() - DUŻO szybsze niż apply(interpolate).
    
    Args:
        exam_df_multi_year: DataFrame z CKE z wielu lat (kolumna 'year')
    
    Returns:
        exam_df_multi_year z dodanymi kolumnami: polish_basic_pct, math_ext_pct, etc.
    """
    print("\n📊 WEKTORYZOWANE obliczanie percentyli (RAZ dla wszystkich!)...")
    
    years = sorted(exam_df_multi_year['year'].unique())
    
    for year in years:
        print(f"   📅 Rok {year}...")
        mask = exam_df_multi_year['year'] == year
        
        for subj_col in tqdm(SUBJECTS_TO_TRACK, desc=f"      Percentyle {year}", leave=False):
            if subj_col not in exam_df_multi_year.columns:
                continue
            
            # Wyniki z tego przedmiotu (tylko non-zero)
            all_scores = exam_df_multi_year.loc[mask, subj_col].values
            nonzero_scores = all_scores[all_scores > 0]
            
            if len(nonzero_scores) < 100:
                continue
            
            # Sortuj (dla searchsorted)
            scores_sorted = np.sort(nonzero_scores)
            
            # WEKTORYZACJA: użyj searchsorted zamiast apply(interpolate)!
            # searchsorted zwraca indeks, dzielimy przez n → percentyl
            pct_col = subj_col + '_pct'
            indices = np.searchsorted(scores_sorted, all_scores, side='right')
            percentiles = indices / len(scores_sorted)
            
            # Dla score=0 (nie zdawał) → percentyl=0
            percentiles[all_scores == 0] = 0.0
            
            # Dodaj kolumnę
            exam_df_multi_year.loc[mask, pct_col] = percentiles
        
        print(f"      ✅ {len([c for c in SUBJECTS_TO_TRACK if c in exam_df_multi_year.columns])} przedmiotów")
    
    print(f"   ✅ Percentyle dodane jako kolumny (wektoryzacja ~100x szybsza!)\n")
    return exam_df_multi_year


def match_applicants_to_cke(exam_df_year, apps_hist_year, program_id, program_rule, 
                            use_java_api=True):
    """
    Dla konkretnego programu i roku: matchuje aplikantów PP → osoby z CKE.
    
    UWAGA: exam_df_year powinien już zawierać kolumny *_pct (percentyle przedmiotów)!
    
    Args:
        exam_df_year: DataFrame z CKE dla jednego roku (np. 2024) - z percentylami!
        apps_hist_year: DataFrame z PP dla tego samego roku i programu
        program_id: ID programu
        program_rule: dict z wzorem rekrutacyjnym
        use_java_api: czy używać Java API (zalecane, uwzględnia bonusy)
    
    Returns:
        DataFrame z kolumnami:
        - student_idx: indeks w exam_df_year
        - s_points: punkty obliczone wg wzoru programu
        - applied: 0/1
        - program_id, seat_limit, prev_cutoff, study_mode, year
        - raw scores (polish_basic, math_ext, etc.)
        - percentyle (*_pct)
    """
    # 1. Oblicz s_points dla wszystkich maturzystów
    exam_df_year = exam_df_year.copy()
    
    if use_java_api:
        # Użyj PYTHON implementation (SZYBKA, uwzględnia extended→basic, wzory PP)
        # ~50x szybsza niż HTTP, bez problemów z portami!
        from pp_point_calculator import calculate_points_for_dataframe
        from pp_field_mapping import pp_to_field_mapping
        
        # Mapuj program_id -> field_of_study_id
        field_id = pp_to_field_mapping.get(int(program_id))
        if field_id is None:
            print(f"   ⚠️  Brak mapowania dla program_id={program_id}, używam Python legacy")
            from pipeline_1 import compute_points_for_program
            exam_df_year['s_points'] = exam_df_year.apply(
                lambda r: compute_points_for_program(r, program_rule), 
                axis=1
            )
        else:
            exam_df_year['s_points'] = calculate_points_for_dataframe(
                exam_df_year,
                field_of_study_id=field_id,
                show_progress=False
            )
    else:
        # Fallback: Python legacy implementation (prostsza formuła)
        from pipeline_1 import compute_points_for_program
        exam_df_year['s_points'] = exam_df_year.apply(
            lambda r: compute_points_for_program(r, program_rule), 
            axis=1
        )
    
    # 2. Inicjalizuj wszyscy applied=0
    exam_df_year['applied'] = 0
    exam_df_year['student_idx'] = exam_df_year.index
    
    # 3. Matching: dla każdej unikalnej liczby punktów aplikantów
    applicant_points = apps_hist_year['s_points'].values
    
    for s_target in np.unique(applicant_points):
        # Ile aplikantów ma dokładnie s_target punktów?
        n_applicants = (applicant_points == s_target).sum()
        
        # Znajdź osoby w CKE z taką samą liczbą punktów (i applied=0)
        candidates_mask = (exam_df_year['s_points'] == s_target) & (exam_df_year['applied'] == 0)
        candidates_idx = exam_df_year[candidates_mask].index.tolist()
        
        # Matching
        if len(candidates_idx) >= n_applicants:
            # Wystarczająco kandydatów - losuj bez zwracania
            selected = np.random.choice(candidates_idx, size=n_applicants, replace=False)
            exam_df_year.loc[selected, 'applied'] = 1
        elif len(candidates_idx) > 0:
            # Za mało kandydatów - weź wszystkich + resztę z najbliższych punktów
            exam_df_year.loc[candidates_idx, 'applied'] = 1
            remaining = n_applicants - len(candidates_idx)
            
            # Znajdź osoby z najbliższymi punktami (±1, ±2, ...)
            for delta in [1, 2, 3, 5, 10]:
                if remaining == 0:
                    break
                nearby_mask = (
                    (exam_df_year['s_points'].between(s_target - delta, s_target + delta)) &
                    (exam_df_year['applied'] == 0)
                )
                nearby_idx = exam_df_year[nearby_mask].index.tolist()
                
                if len(nearby_idx) > 0:
                    n_select = min(remaining, len(nearby_idx))
                    selected = np.random.choice(nearby_idx, size=n_select, replace=False)
                    exam_df_year.loc[selected, 'applied'] = 1
                    remaining -= n_select
    
    # 4. Dodaj metadata programu
    if len(apps_hist_year) > 0:
        exam_df_year['seat_limit'] = apps_hist_year['seat_limit'].iloc[0]
        exam_df_year['prev_cutoff'] = apps_hist_year['prev_cutoff'].iloc[0]
        exam_df_year['study_mode'] = apps_hist_year['study_mode'].iloc[0]
    else:
        exam_df_year['seat_limit'] = 0
        exam_df_year['prev_cutoff'] = 0
        exam_df_year['study_mode'] = 'unknown'
    
    exam_df_year['program_id'] = program_id
    exam_df_year['year'] = apps_hist_year['year'].iloc[0] if len(apps_hist_year) > 0 else 2024
    
    # 5. Zwróć kolumny: podstawowe + raw scores + percentyle (już policzone!)
    base_cols = [
        'student_idx', 's_points', 'applied', 'program_id', 
        'seat_limit', 'prev_cutoff', 'study_mode', 'year'
    ]
    
    # Dodaj raw scores (wszystkie przedmioty)
    subject_cols = [col for col in SUBJECTS_TO_TRACK if col in exam_df_year.columns]
    
    # Dodaj percentyle (jeśli są)
    pct_cols = [col + '_pct' for col in SUBJECTS_TO_TRACK if col + '_pct' in exam_df_year.columns]
    
    return_cols = base_cols + subject_cols + pct_cols
    
    return exam_df_year[return_cols]


def _process_single_program(args):
    """
    Helper function dla paralelizacji - przetwarza jeden (program_id, year).
    
    Args:
        args: tuple (exam_year, apps_group, program_id, program_rule, use_java_api, undersample_ratio)
    
    Returns:
        DataFrame z matched+undersampled data lub None jeśli błąd
    """
    exam_year, apps_group, program_id, program_rule, use_java_api, undersample_ratio = args
    
    try:
        # Matching
        program_data = match_applicants_to_cke(
            exam_year, apps_group, program_id, program_rule,
            use_java_api=use_java_api
        )
        
        # Undersample
        program_data_balanced = undersample_non_applicants(
            program_data, ratio=undersample_ratio
        )
        
        return program_data_balanced
    
    except Exception as e:
        print(f"   ❌ Błąd dla programu {program_id}: {e}")
        return None


def undersample_non_applicants(program_data, ratio=10):
    """
    Undersampling: dla każdego applied=1, zachowaj tylko 'ratio' × applied=0.
    
    Args:
        program_data: DataFrame z matched data dla jednego programu
        ratio: ile razy więcej applied=0 niż applied=1 (np. 10 → 1:10)
    
    Returns:
        DataFrame z zbalansowanymi danymi
    """
    applied_1 = program_data[program_data['applied'] == 1]
    applied_0 = program_data[program_data['applied'] == 0]
    
    n_keep = min(len(applied_0), len(applied_1) * ratio)
    
    if n_keep > 0:
        applied_0_sampled = applied_0.sample(n=n_keep, random_state=42)
    else:
        applied_0_sampled = applied_0
    
    return pd.concat([applied_1, applied_0_sampled], ignore_index=True)


def prepare_training_data(
    exam_df_multi_year,  # CKE z wieloma latami (kolumna 'year')
    apps_hist_df,        # PP z wieloma latami (kolumna 'year')
    program_rules,       # Dict: program_id → {name, formula, ...}
    undersample_ratio=10,
    filter_no_prev_cutoff=True,
    use_java_api=True,   # Użyj Java API (zalecane!)
    n_jobs=None          # Liczba rdzeni (None = auto)
):
    """
    GŁÓWNA FUNKCJA: Przygotowuje dane treningowe dla modelu propensity.
    
    Proces:
    1. Dla każdego (program_id, year) osobno:
       - Weź odpowiedni subset CKE i PP
       - Oblicz s_points dla tego programu
       - Matching: aplikanci PP → osoby w CKE
    2. Undersample non-applicants (1:10 ratio)
    3. Połącz wszystkie programy i lata
    
    Args:
        exam_df_multi_year: DataFrame z CKE (wiele lat)
        apps_hist_df: DataFrame z PP (wiele lat)
        program_rules: Dict z wzorami rekrutacyjnymi
        undersample_ratio: ile razy więcej applied=0 niż applied=1
        filter_no_prev_cutoff: czy usunąć kierunki bez prev_cutoff
    
    Returns:
        DataFrame gotowy do treningu:
        - X: [s_points, seat_limit, prev_cutoff, program_id, study_mode, year]
        - y: applied (0/1)
    """
    print("\n" + "="*80)
    print("🔧 BUDOWANIE DANYCH TRENINGOWYCH (z percentylami przedmiotów)")
    print("="*80)
    
    # 1. OBLICZ PERCENTYLE RAZ dla całego datasetu (wektoryzacja!)
    exam_df_multi_year = add_subject_percentiles_to_dataframe(exam_df_multi_year)
    
    # 2. Filtruj kierunki bez prev_cutoff (nowe kierunki)
    if filter_no_prev_cutoff:
        apps_hist_df = apps_hist_df[apps_hist_df['prev_cutoff'].notna()].copy()
        print(f"   ℹ️  Usunięto kierunki bez prev_cutoff")
        print(f"   ✅ Pozostało: {apps_hist_df['program_id'].nunique()} kierunków, "
              f"{len(apps_hist_df)} aplikacji")
    
    # 3. Zgrupuj dane PP per (program_id, year)
    grouped = apps_hist_df.groupby(['program_id', 'year'])
    
    print(f"\n   📊 Liczba kombinacji (program × year): {len(grouped)}")
    print(f"   📊 Lata w danych: {sorted(apps_hist_df['year'].unique())}")
    
    # 4. Przygotuj argumenty dla paralelizacji
    if n_jobs is None:
        n_jobs = max(1, cpu_count() - 1)  # Zostaw 1 rdzeń wolny
    
    print(f"   🚀 Paralelizacja: {n_jobs} rdzeni (z {cpu_count()} dostępnych)")
    
    tasks = []
    for (program_id, year), apps_group in grouped:
        # Pobierz CKE dla tego roku
        exam_year = exam_df_multi_year[exam_df_multi_year['year'] == year]
        
        if len(exam_year) == 0:
            continue
        
        # Pobierz wzór rekrutacyjny
        program_rule = program_rules.get(str(program_id))
        if program_rule is None:
            continue
        
        tasks.append((
            exam_year.copy(),  # WAŻNE: copy() żeby uniknąć race conditions
            apps_group.copy(),
            program_id,
            program_rule,
            use_java_api,
            undersample_ratio
        ))
    
    print(f"   📋 Zadań do przetworzenia: {len(tasks)}")
    
    # 5. Przetwarzanie RÓWNOLEGŁE
    all_training_data = []
    
    if n_jobs == 1:
        # Sequential (dla debugowania)
        for task in tqdm(tasks, desc="   🔄 Matching programs (sequential)"):
            result = _process_single_program(task)
            if result is not None:
                all_training_data.append(result)
    else:
        # Parallel
        with Pool(processes=n_jobs) as pool:
            results = list(tqdm(
                pool.imap(_process_single_program, tasks),
                total=len(tasks),
                desc="   🔄 Matching programs (parallel)"
            ))
        
        # Filtruj None (błędy)
        all_training_data = [r for r in results if r is not None]
    
    print(f"   ✅ Przetworzono: {len(all_training_data)}/{len(tasks)} zadań")
    
    # 6. Połącz wszystkie dane
    if len(all_training_data) == 0:
        raise ValueError("Brak danych treningowych! Sprawdź compatibility CKE ↔ PP")
    
    training_df = pd.concat(all_training_data, ignore_index=True)
    
    print(f"\n   ✅ Dataset treningowy gotowy!")
    print(f"   📊 Rozmiar: {len(training_df):,} wierszy")
    print(f"   📊 applied=1: {(training_df['applied']==1).sum():,} ({(training_df['applied']==1).mean()*100:.1f}%)")
    print(f"   📊 applied=0: {(training_df['applied']==0).sum():,} ({(training_df['applied']==0).mean()*100:.1f}%)")
    print(f"   📊 Ratio: 1:{(training_df['applied']==0).sum() / max(1, (training_df['applied']==1).sum()):.1f}")
    print("="*80 + "\n")
    
    return training_df


if __name__ == "__main__":
    # Test na małym subsecie
    from cke_synthetic import load_multi_year_cke
    from pp_data_loader import load_politechnika_poznanska
    
    print("\n🧪 TEST: Przygotowanie danych treningowych")
    
    # Wczytaj dane
    exam_df = load_multi_year_cke(
        '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx',
        include_synthetic_2023=True
    )
    
    apps_hist_df, _, program_rules = load_politechnika_poznanska()
    
    # Przygotuj dane (z małym subsample dla testu)
    training_df = prepare_training_data(
        exam_df.sample(50000, random_state=42),  # Sample dla szybkości
        apps_hist_df,
        program_rules,
        undersample_ratio=10
    )
    
    print("\n✅ Test zakończony pomyślnie!")
    print(f"Training data shape: {training_df.shape}")
    print(f"\nPierwsze 10 wierszy:")
    print(training_df.head(10))


