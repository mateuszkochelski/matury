"""
Przykład użycia API - jak odpytywać model o szanse przyjęcia.
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson


def predict_admission_probability(
    candidate_points: float,
    program_id: str,
    year: int,
    propensity_model_path: str = '/tmp/propensity_model_pp.pkl',
    cke_data_path: str = '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
) -> dict:
    """
    GŁÓWNA FUNKCJA API: Oblicza prawdopodobieństwo przyjęcia na studia.
    
    Args:
        candidate_points: Punkty kandydata (np. 650.5)
        program_id: ID programu (np. '3852')
        year: Rok rekrutacji (np. 2025)
        propensity_model_path: Ścieżka do modelu (opcjonalne)
        cke_data_path: Ścieżka do danych CKE (opcjonalne)
    
    Returns:
        dict z kluczami:
        - 'probability': float (0-1) - prawdopodobieństwo przyjęcia
        - 'program_name': str - nazwa programu
        - 'threshold_prev_year': float - próg z poprzedniego roku
        - 'seat_limit': int - limit miejsc
        - 'expected_applicants': int - oczekiwana liczba aplikantów
        - 'status': str - opisowa ocena ('certain', 'likely', 'risky', 'unlikely')
    
    Example:
        >>> result = predict_admission_probability(
        ...     candidate_points=650,
        ...     program_id='3852',
        ...     year=2025
        ... )
        >>> print(f"Szansa przyjęcia: {result['probability']*100:.1f}%")
        Szansa przyjęcia: 85.3%
    """
    from cke_loader import load_cke_to_exam_df
    from cke_synthetic import generate_synthetic_cke_2023
    from pp_data_loader import load_politechnika_poznanska
    from pipeline_1 import compute_points_for_program
    
    # 1. Wczytaj model propensity
    with open(propensity_model_path, 'rb') as f:
        propensity_model = pickle.load(f)
    
    # 2. Wczytaj metadata programu
    _, apps_agg_df, program_rules = load_politechnika_poznanska()
    
    # Znajdź program
    program_meta = apps_agg_df[apps_agg_df['program_id'] == int(program_id)]
    if len(program_meta) == 0:
        raise ValueError(f"Program {program_id} nie znaleziony")
    
    program_meta = program_meta.iloc[0]
    program_rule = program_rules[program_id]
    
    seat_limit = int(program_meta['seat_limit'])
    prev_cutoff = float(program_meta['prev_cutoff'])
    applicants_count_prev = int(program_meta['applicants_count'])
    program_name = program_meta['Kierunek']
    
    # 3. Wygeneruj/wczytaj dane CKE dla danego roku
    exam_df_base = load_cke_to_exam_df(cke_data_path)
    
    # Jeśli rok > 2024, generuj syntetyczne
    if year > 2024:
        exam_df_year = generate_synthetic_cke_2023(exam_df_base)
        exam_df_year['year'] = year
    else:
        exam_df_year = exam_df_base
    
    # 4. Oblicz punkty dla całej populacji CKE
    exam_df_year['s_points'] = exam_df_year.apply(
        lambda r: compute_points_for_program(r, program_rule),
        axis=1
    )
    
    # 5. Zbuduj F_app (rozkład aplikantów)
    bin_width = 0.5
    s_min = float(exam_df_year['s_points'].min())
    s_max = float(exam_df_year['s_points'].max())
    nbins = int(np.ceil((s_max - s_min) / bin_width)) + 1
    bins = np.linspace(s_min, s_max, nbins)
    hist, edges = np.histogram(exam_df_year['s_points'].values, bins=bins)
    centers = 0.5 * (edges[:-1] + edges[1:])
    
    # Przewiduj P(apply) dla każdego bina
    feat = pd.DataFrame({
        's_points': centers,
        'seat_limit': seat_limit,
        'prev_cutoff': prev_cutoff,
        'program_id': program_id,
        'study_mode': 'stacjonarne',
        'year': year
    })
    
    p_app = propensity_model.predict_proba(feat)[:, 1]
    expected_applicants_hist = hist.astype(float) * p_app
    
    # PDF i CDF aplikantów
    pdf_app = expected_applicants_hist / expected_applicants_hist.sum()
    cdf_app = np.cumsum(pdf_app)
    
    F_app_cdf = interpolate.interp1d(
        centers, cdf_app,
        kind='previous',
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )
    
    # 6. Oblicz prawdopodobieństwo przyjęcia
    # Używamy liczby aplikantów z poprzedniego roku jako lambda
    lambda_applicants = applicants_count_prev
    
    tail = max(0.0, 1.0 - float(F_app_cdf(candidate_points)))
    mu = lambda_applicants * tail
    p_acceptance = float(poisson.cdf(seat_limit - 1, mu))
    
    # 7. Określ status
    if candidate_points < prev_cutoff:
        status = 'below_threshold'
    elif p_acceptance >= 0.9:
        status = 'certain'
    elif p_acceptance >= 0.7:
        status = 'likely'
    elif p_acceptance >= 0.4:
        status = 'moderate'
    else:
        status = 'risky'
    
    # 8. Zwróć wynik
    return {
        'probability': p_acceptance,
        'program_name': program_name,
        'program_id': program_id,
        'threshold_prev_year': prev_cutoff,
        'seat_limit': seat_limit,
        'expected_applicants': lambda_applicants,
        'candidate_points': candidate_points,
        'status': status,
        'year': year
    }


# =============================================================================
# PRZYKŁAD UŻYCIA
# =============================================================================

if __name__ == "__main__":
    print("\n" + "="*80)
    print("📊 PRZYKŁAD UŻYCIA API")
    print("="*80)
    
    # Test 1: Kandydat z 850 punktami na AI
    print("\n🎓 Test 1: Sztuczna Inteligencja (ciężki kierunek)")
    result = predict_admission_probability(
        candidate_points=850,
        program_id='3852',  # AI
        year=2025
    )
    
    print(f"   Program: {result['program_name']}")
    print(f"   Punkty kandydata: {result['candidate_points']}")
    print(f"   Próg 2024: {result['threshold_prev_year']}")
    print(f"   ➡️  SZANSA PRZYJĘCIA: {result['probability']*100:.1f}%")
    print(f"   Status: {result['status']}")
    
    # Test 2: Różne punkty na ten sam kierunek
    print("\n📊 Test 2: Jak punkty wpływają na szanse")
    print(f"   Program: {result['program_name']}")
    print(f"   Limit miejsc: {result['seat_limit']}")
    print(f"   Oczekiwani aplikanci: {result['expected_applicants']}")
    print("\n   " + "-"*70)
    print(f"   {'Punkty':>10} | {'Szansa':>12} | {'Status':>20}")
    print("   " + "-"*70)
    
    for points in [700, 750, 800, 850, 900, 950]:
        res = predict_admission_probability(
            candidate_points=points,
            program_id='3852',
            year=2025
        )
        print(f"   {points:10.0f} | {res['probability']*100:11.1f}% | {res['status']:>20}")
    
    print("\n" + "="*80)
    print("✅ API działa poprawnie!")
    print("="*80)


