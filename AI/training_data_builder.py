"""
Training Data Builder - przygotowanie danych do treningu modelu propensity.

Główna funkcja: prepare_training_data()
- Dla każdego (program_id, year) robi matching CKE ↔ PP
- Zwraca DataFrame gotowy do treningu
"""

import pandas as pd
import numpy as np
from tqdm import tqdm


def match_applicants_to_cke(exam_df_year, apps_hist_year, program_id, program_rule):
    """
    Dla konkretnego programu i roku: matchuje aplikantów PP → osoby z CKE.
    
    Args:
        exam_df_year: DataFrame z CKE dla jednego roku (np. 2024)
        apps_hist_year: DataFrame z PP dla tego samego roku i programu
        program_id: ID programu
        program_rule: dict z wzorem rekrutacyjnym
    
    Returns:
        DataFrame z kolumnami:
        - student_idx: indeks w exam_df_year
        - s_points: punkty obliczone wg wzoru programu
        - applied: 0/1
        - program_id, seat_limit, prev_cutoff, study_mode, year
    """
    from pipeline_1 import compute_points_for_program
    
    # 1. Oblicz s_points dla wszystkich maturzystów
    exam_df_year = exam_df_year.copy()
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
    
    # 5. Zwróć tylko potrzebne kolumny
    return exam_df_year[[
        'student_idx', 's_points', 'applied', 'program_id', 
        'seat_limit', 'prev_cutoff', 'study_mode', 'year'
    ]]


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
    filter_no_prev_cutoff=True
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
    print("🔧 BUDOWANIE DANYCH TRENINGOWYCH")
    print("="*80)
    
    # 1. Filtruj kierunki bez prev_cutoff (nowe kierunki)
    if filter_no_prev_cutoff:
        apps_hist_df = apps_hist_df[apps_hist_df['prev_cutoff'].notna()].copy()
        print(f"   ℹ️  Usunięto kierunki bez prev_cutoff")
        print(f"   ✅ Pozostało: {apps_hist_df['program_id'].nunique()} kierunków, "
              f"{len(apps_hist_df)} aplikacji")
    
    # 2. Zgrupuj dane PP per (program_id, year)
    grouped = apps_hist_df.groupby(['program_id', 'year'])
    
    print(f"\n   📊 Liczba kombinacji (program × year): {len(grouped)}")
    print(f"   📊 Lata w danych: {sorted(apps_hist_df['year'].unique())}")
    
    # 3. Pętla po wszystkich programach i latach
    all_training_data = []
    
    for (program_id, year), apps_group in tqdm(grouped, desc="   🔄 Matching programs"):
        # Pobierz CKE dla tego roku
        exam_year = exam_df_multi_year[exam_df_multi_year['year'] == year]
        
        if len(exam_year) == 0:
            print(f"   ⚠️  Brak danych CKE dla roku {year}, pomijam program {program_id}")
            continue
        
        # Pobierz wzór rekrutacyjny
        program_rule = program_rules.get(str(program_id))
        if program_rule is None:
            print(f"   ⚠️  Brak wzoru dla programu {program_id}, pomijam")
            continue
        
        # Matching
        try:
            program_data = match_applicants_to_cke(
                exam_year, apps_group, program_id, program_rule
            )
            
            # Undersample
            program_data_balanced = undersample_non_applicants(
                program_data, ratio=undersample_ratio
            )
            
            all_training_data.append(program_data_balanced)
        
        except Exception as e:
            print(f"   ❌ Błąd dla programu {program_id}, rok {year}: {e}")
            continue
    
    # 4. Połącz wszystkie dane
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


