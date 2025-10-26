#!/usr/bin/env python3
"""
Kalkulator punktów rekrutacyjnych dla Politechniki Poznańskiej.

Reimplementacja logiki z Java backend w Pythonie (bez HTTP overhead).
Uwzględnia:
- Konwersję extended -> basic
- Łączenie basic + extended tego samego przedmiotu (POZNAN_COMPOSITE_X)
- Wszystkie bonusy i wzory PP
"""

import pandas as pd
import numpy as np
from typing import Dict, Optional


def convert_extended_to_basic(extended_score: float) -> float:
    """
    Konwertuje wynik z poziomu rozszerzonego na podstawowy.
    
    Formuła z CKE/Java backend:
    - jeśli extended <= 29: basic = 2 * extended
    - jeśli extended > 29: basic = 0.5 * extended + 50
    
    Args:
        extended_score: Wynik z poziomu rozszerzonego (0-100)
    
    Returns:
        float: Ekwiwalent na poziomie podstawowym (0-100)
    """
    if extended_score <= 29.0:
        return 2.0 * extended_score
    return 0.5 * extended_score + 50.0


def get_polish_basic_score(exam_row: pd.Series) -> float:
    """
    Polski podstawa (z konwersją z rozszerzenia jeśli brak podstawy).
    """
    basic = exam_row.get('polish_basic', 0.0)
    extended = exam_row.get('polish_ext', 0.0)
    
    if basic > 0:
        return basic
    elif extended > 0:
        return convert_extended_to_basic(extended)
    return 0.0


def get_foreign_basic_score(exam_row: pd.Series) -> float:
    """
    Język obcy - najlepszy (z konwersją z rozszerzenia jeśli brak podstawy).
    
    W danych CKE mamy tylko 'foreign_basic' + poszczególne rozszerzenia.
    Bierzemy najlepszy możliwy wynik.
    """
    basic = exam_row.get('foreign_basic', 0.0)
    
    # Rozszerzenia języków obcych
    extended_langs = [
        exam_row.get('eng_ext', 0.0),
        exam_row.get('ger_ext', 0.0),
        # Inne języki jeśli są w danych
    ]
    
    # Najlepsze rozszerzenie
    best_extended = max(extended_langs)
    
    if basic > 0:
        return basic
    elif best_extended > 0:
        return convert_extended_to_basic(best_extended)
    return 0.0


def get_math_basic_score(exam_row: pd.Series) -> float:
    """
    Matematyka podstawa (z konwersją z rozszerzenia jeśli brak podstawy).
    """
    basic = exam_row.get('math_basic', 0.0)
    extended = exam_row.get('math_ext', 0.0)
    
    if basic > 0:
        return basic
    elif extended > 0:
        return convert_extended_to_basic(extended)
    return 0.0


def get_math_extended_score(exam_row: pd.Series) -> float:
    """
    Matematyka rozszerzenie.
    """
    return exam_row.get('math_ext', 0.0)


def get_composite_x_score(exam_row: pd.Series, allowed_subjects: list) -> float:
    """
    POZNAN_COMPOSITE_X: Najlepszy przedmiot spośród allowed_subjects.
    
    Dla każdego przedmiotu liczy: basic + extended (tego samego przedmiotu).
    Jeśli brak basic, konwertuje extended -> basic i dodaje extended.
    
    Args:
        exam_row: Wiersz z danymi CKE
        allowed_subjects: Lista przedmiotów dozwolonych (np. ['phys', 'chem', 'bio', 'info', 'geog'])
    
    Returns:
        float: Najlepszy wynik composite (basic + extended)
    """
    best_score = 0.0
    
    for subject in allowed_subjects:
        basic = exam_row.get(f'{subject}_basic', 0.0)
        extended = exam_row.get(f'{subject}_ext', 0.0)
        
        # Composite = basic + extended
        if basic > 0:
            composite = basic + extended
        elif extended > 0:
            # Konwertuj extended -> basic i dodaj extended
            composite = convert_extended_to_basic(extended) + extended
        else:
            composite = 0.0
        
        if composite > best_score:
            best_score = composite
    
    return best_score


def calculate_pp_standard_points(exam_row: pd.Series, subject_x_list: list = None) -> float:
    """
    Oblicza punkty rekrutacyjne dla standardowych kierunków PP.
    
    Formuła:
    - 0.5 * JP_basic
    - 0.5 * JO_basic (najlepszy język obcy)
    - 2.5 * MAT_basic
    - 2.5 * MAT_extended
    - 2.0 * X_composite (najlepszy z przedmiotów X: bio/chem/phys/info/geog)
    
    Args:
        exam_row: Wiersz z exam_df (CKE)
        subject_x_list: Lista przedmiotów X (domyślnie: phys, chem, bio, info, geog)
    
    Returns:
        float: Punkty rekrutacyjne (0-~800 dla matur, może być wyższe z bonusami)
    """
    if subject_x_list is None:
        subject_x_list = ['phys', 'chem', 'bio', 'info', 'geog']
    
    # 1. Polski podstawa
    jp = get_polish_basic_score(exam_row)
    
    # 2. Język obcy podstawa (najlepszy)
    jo = get_foreign_basic_score(exam_row)
    
    # 3. Matematyka podstawa
    mat_basic = get_math_basic_score(exam_row)
    
    # 4. Matematyka rozszerzenie
    mat_ext = get_math_extended_score(exam_row)
    
    # 5. Najlepszy przedmiot X (composite: basic + extended)
    x_composite = get_composite_x_score(exam_row, subject_x_list)
    
    # Suma z wagami
    total = (
        0.5 * jp +
        0.5 * jo +
        2.5 * mat_basic +
        2.5 * mat_ext +
        2.0 * x_composite
    )
    
    # Ogranicz do [0, 1000] (choć w praktyce max to ~990)
    return max(0.0, min(total, 1000.0))


def calculate_pp_x_only_points(exam_row: pd.Series) -> float:
    """
    Oblicza punkty dla kierunków z przedmiotem X (bez geografii).
    
    Używane dla kierunków: automatyka, elektrotechnika, elektromobilność, etc.
    X: phys, chem, bio, info (bez geog)
    """
    return calculate_pp_standard_points(
        exam_row, 
        subject_x_list=['phys', 'chem', 'bio', 'info']
    )


def calculate_pp_architecture_points(exam_row: pd.Series) -> float:
    """
    Oblicza punkty dla architektury (oddzielna formuła).
    
    TODO: Zaimplementuj jeśli potrzebne (ma inną formułę z egzaminem plastycznym)
    Na razie używamy standardowej.
    """
    return calculate_pp_standard_points(exam_row)


def calculate_points_for_program(
    exam_row: pd.Series,
    field_of_study_id: int,
    program_rules: Optional[Dict] = None
) -> float:
    """
    Oblicza punkty dla konkretnego kierunku PP (na podstawie field_of_study_id).
    
    Args:
        exam_row: Wiersz z exam_df (CKE)
        field_of_study_id: ID kierunku z backend DB (np. 3005, 2999)
        program_rules: Opcjonalnie, dict z regułami (legacy, może być None)
    
    Returns:
        float: Punkty rekrutacyjne
    """
    # Mapowanie field_of_study_id -> typ formuły
    # Na podstawie formulas.json:
    
    # Grupa 1: engineering-x (X bez geografii)
    engineering_x_ids = {
        1434, 2999, 3097, 3000, 3025, 3021, 2693, 3011, 3026, 
        3001, 3022, 3023, 3024
    }
    
    # Grupa 2: science-xg (X + geografia)
    science_xg_ids = {
        3002, 3016, 3017, 3003, 3098, 3004, 3005, 3006, 3007,
        3008, 3009, 3010, 3012, 3100, 3013, 3027, 3028, 3099,
        3029, 2379, 3018, 3019, 3020, 1436, 1438, 1440
    }
    
    # Grupa 3: architektura
    architecture_ids = {3014, 3015}  # Architektura, Architektura Wnętrz
    
    if field_of_study_id in engineering_x_ids:
        return calculate_pp_x_only_points(exam_row)
    elif field_of_study_id in science_xg_ids:
        return calculate_pp_standard_points(exam_row)  # XG (z geografią)
    elif field_of_study_id in architecture_ids:
        return calculate_pp_architecture_points(exam_row)
    else:
        # Domyślnie standardowa formuła XG
        return calculate_pp_standard_points(exam_row)


def calculate_points_for_dataframe(
    exam_df: pd.DataFrame,
    field_of_study_id: int,
    show_progress: bool = False
) -> pd.Series:
    """
    Oblicza punkty dla całego DataFrame (vectorized gdzie możliwe).
    
    Args:
        exam_df: DataFrame z CKE
        field_of_study_id: ID kierunku
        show_progress: Czy pokazywać progress (TQDM)
    
    Returns:
        pd.Series: Punkty dla każdego wiersza
    """
    if show_progress:
        from tqdm import tqdm
        tqdm.pandas(desc=f"📊 Calc points (field {field_of_study_id})")
        return exam_df.progress_apply(
            lambda row: calculate_points_for_program(row, field_of_study_id),
            axis=1
        )
    else:
        return exam_df.apply(
            lambda row: calculate_points_for_program(row, field_of_study_id),
            axis=1
        )


# =============================================================================
# TESTY
# =============================================================================

if __name__ == '__main__':
    print("\n" + "="*80)
    print("🧪 TEST: Python PP Point Calculator")
    print("="*80)
    
    from cke_loader import load_cke_to_exam_df
    
    # Wczytaj dane
    exam_df = load_cke_to_exam_df(
        '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    ).head(100)
    
    print(f"\n📊 Testing with {len(exam_df)} students...")
    
    # Test 1: Pojedynczy wiersz
    print("\n1️⃣ Single row test (field_of_study_id=3005 - Sztuczna Inteligencja):")
    row = exam_df.iloc[0]
    points = calculate_points_for_program(row, field_of_study_id=3005)
    print(f"   Student 0: {points:.1f} points")
    print(f"     PL_basic: {row['polish_basic']:.0f}")
    print(f"     MAT_basic: {row['math_basic']:.0f}")
    print(f"     MAT_ext: {row['math_ext']:.0f}")
    print(f"     Best X: {max([row.get(f'{s}_ext', 0) for s in ['phys', 'chem', 'bio', 'info', 'geog']]):.0f}")
    
    # Test 2: Cały DataFrame
    print("\n2️⃣ DataFrame test (100 students):")
    import time
    start = time.time()
    points_series = calculate_points_for_dataframe(exam_df, field_of_study_id=3005)
    elapsed = time.time() - start
    
    print(f"   ✅ Done in {elapsed:.2f}s")
    print(f"   Speed: ~{len(exam_df)/elapsed:.0f} calcs/sec")
    print(f"   Points stats:")
    print(f"     Min: {points_series.min():.1f}")
    print(f"     Max: {points_series.max():.1f}")
    print(f"     Mean: {points_series.mean():.1f}")
    print(f"     Median: {points_series.median():.1f}")
    
    print("\n" + "="*80)
    print("✅ Python calculator działa!")
    print("="*80)

