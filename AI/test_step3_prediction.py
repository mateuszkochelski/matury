"""
KROK 3: Predykcja na syntetycznym CKE 2025 (end-to-end test).
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson

print("\n" + "="*80)
print("🧪 TEST PEŁNEGO PIPELINE - KROK 3: PREDYKCJA 2025")
print("="*80)

# 1. Wczytaj model
print("\n📂 Wczytywanie modelu propensity...")
with open('/tmp/propensity_model_pp.pkl', 'rb') as f:
    propensity_model = pickle.load(f)
print("   ✅ Model wczytany")

# 2. Generuj syntetyczne CKE 2025
print("\n🔬 Generowanie syntetycznego CKE 2025...")
from cke_loader import load_cke_to_exam_df
from cke_synthetic import generate_synthetic_cke_2023

# Wczytaj CKE 2024 i zmień na 2025
exam_df_2024 = load_cke_to_exam_df(
    '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
)
exam_df_2025 = generate_synthetic_cke_2023(exam_df_2024)  # Re-use funkcji
exam_df_2025['year'] = 2025  # Nadpisz rok

print(f"   ✅ CKE 2025: {len(exam_df_2025)} maturzystów (syntetyczne)")

# 3. Wybierz przykładowy program (z roku 2024, bo mamy jego dane)
print("\n📊 Wybór programu testowego...")

# Wczytaj metadata programów z 2024
from pp_data_loader import load_politechnika_poznanska
_, apps_agg_df, program_rules_temp = load_politechnika_poznanska()

# Wybierz program z 2024 (weźmy pierwszy dostępny)
programs_2024 = apps_agg_df[apps_agg_df['year']==2024]
test_program = programs_2024.iloc[0]  # Pierwszy program

program_id = str(test_program['program_id'])
program_name = test_program['Kierunek']
seat_limit = int(test_program['seat_limit'])
prev_cutoff_2024 = float(test_program['prev_cutoff'])
applicants_count_2024 = int(test_program['applicants_count'])

print(f"   Program: {program_name}")
print(f"   ID: {program_id}")
print(f"   Limit miejsc: {seat_limit}")
print(f"   Próg 2024: {prev_cutoff_2024}")
print(f"   Aplikanci 2024: {applicants_count_2024}")
print(f"\n   ℹ️  Założenie: liczba aplikantów w 2025 = {applicants_count_2024} (jak w 2024)")

# 4. Oblicz punkty dla całej populacji CKE 2025 (UŻYWAMY TEGO SAMEGO KALKULATORA CO W TRENINGU!)
print("\n🧮 Obliczanie punktów dla populacji CKE 2025...")
from pp_point_calculator import calculate_points_for_dataframe
from pp_field_mapping import pp_to_field_mapping

# Mapuj program_id -> field_of_study_id
field_id = pp_to_field_mapping.get(int(program_id))
if field_id is None:
    print(f"   ⚠️  Brak mapowania dla program_id={program_id}")
    # Fallback do starego
    from pipeline_1 import compute_points_for_program
    program_rule = program_rules_temp[program_id]
    exam_df_2025['s_points'] = exam_df_2025.apply(
        lambda r: compute_points_for_program(r, program_rule),
        axis=1
    )
else:
    exam_df_2025['s_points'] = calculate_points_for_dataframe(
        exam_df_2025,
        field_of_study_id=field_id,
        show_progress=False
    )

print(f"   ✅ Punkty obliczone (Python PP calculator)")
print(f"   Min: {exam_df_2025['s_points'].min():.1f}")
print(f"   Max: {exam_df_2025['s_points'].max():.1f}")
print(f"   Mean: {exam_df_2025['s_points'].mean():.1f}")

# 5. Zbuduj F_app (rozkład aplikantów)
print("\n📈 Budowanie F_app (rozkład aplikantów)...")

# Przygotuj bins
bin_width = 0.5
s_min, s_max = float(exam_df_2025['s_points'].min()), float(exam_df_2025['s_points'].max())
nbins = int(np.ceil((s_max - s_min) / bin_width)) + 1
bins = np.linspace(s_min, s_max, nbins)

print("   ⚠️  OPTION: Użyć modelu propensity (niedokładny) vs empiryczny rozkład z 2024?")

USE_EMPIRICAL_2024 = True  # Toggle: True = użyj danych z 2024, False = użyj modelu

if USE_EMPIRICAL_2024:
    print("   📊 Używamy EMPIRYCZNEGO rozkładu z 2024 (najdokładniejszy!)")
    # Wczytaj rzeczywiste punkty aplikantów z 2024 dla tego programu
    from pp_data_loader import load_politechnika_poznanska
    apps_hist_df_full, _, _ = load_politechnika_poznanska()
    
    # Filtruj aplikantów dla tego programu w 2024
    program_apps_2024 = apps_hist_df_full[
        (apps_hist_df_full['program_id'] == int(program_id)) &
        (apps_hist_df_full['year'] == 2024)
    ]
    
    print(f"   ✅ Znaleziono {len(program_apps_2024)} aplikantów z 2024")
    
    # Zbuduj histogram z rzeczywistych punktów
    real_points_2024 = program_apps_2024['s_points'].values
    hist_real, edges_real = np.histogram(real_points_2024, bins=bins, density=False)
    centers_real = 0.5 * (edges_real[:-1] + edges_real[1:])
    
    # PDF i CDF z rzeczywistych danych
    pdf_app = hist_real / hist_real.sum()
    cdf_app = np.cumsum(pdf_app)
    
    F_app_cdf = interpolate.interp1d(
        centers_real, cdf_app,
        kind='linear',
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )
    
    total_expected_applicants = applicants_count_2024
    centers = centers_real  # Dla debug later
    
else:
    print("   📊 Używamy MODELU PROPENSITY (może być niedokładny)...")
    # Histogram populacji CKE
    hist, edges = np.histogram(exam_df_2025['s_points'].values, bins=bins, density=False)
    centers = 0.5 * (edges[:-1] + edges[1:])

    # Przewiduj P(apply) dla każdego bina
    feat = pd.DataFrame({
        's_points': centers,
        'seat_limit': seat_limit,
        'prev_cutoff': prev_cutoff_2024,
        'program_id': program_id,
        'study_mode': 'stacjonarne',
        'year': 2025
    })

    p_app = propensity_model.predict_proba(feat)[:, 1]

    print(f"   📊 DEBUG: P(apply) stats:")
    print(f"      Min: {p_app.min():.6f}")
    print(f"      Max: {p_app.max():.6f}")
    print(f"      Mean: {p_app.mean():.6f}")
    print(f"      Median: {np.median(p_app):.6f}")

    # Oczekiwana liczba aplikantów w każdym binie
    expected_applicants = hist.astype(float) * p_app

    total_expected_applicants = expected_applicants.sum()
    print(f"   Oczekiwana liczba aplikantów (model): {total_expected_applicants:.1f}")

    # Jeśli model zwraca 0 lub bardzo mało, użyj prostszego podejścia
    if total_expected_applicants < applicants_count_2024 * 0.5:  # Jeśli model jest bardzo off
        print(f"   ⚠️  Model przewiduje za mało aplikantów ({total_expected_applicants:.1f} vs {applicants_count_2024})")
        print(f"   📊 Używamy weighted distribution based on P(apply) + population")
        # Użyj kombinacji: hist * (małe P(apply) + baseline)
        # Daje to rozkład który preferuje wysokie punkty (bo P(apply) tam wyższe), ale nie ignoruje niskich
        baseline_rate = 0.001  # Każdy ma minimalnie 0.1% szansy aplikacji
        combined_p = p_app + baseline_rate
        expected_applicants = hist.astype(float) * combined_p
        # Przeskaluj do realnej liczby
        scale_factor = applicants_count_2024 / expected_applicants.sum()
        expected_applicants = expected_applicants * scale_factor
        total_expected_applicants = applicants_count_2024
    else:
        print(f"   ✅ Model przewiduje {total_expected_applicants:.0f} aplikantów (realistyczne)")

    # PDF i CDF aplikantów
    pdf_app = expected_applicants / expected_applicants.sum()
    cdf_app = np.cumsum(pdf_app)oby

    # Funkcja CDF (LINEAR interpolation dla smooth transitions!)
    F_app_cdf = interpolate.interp1d(
        centers, cdf_app,
        kind='linear',  # ✅ SMOOTH zamiast schodków
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )

# Wspólny debug (dla obu opcji)
print(f"   ✅ F_app zbudowany")
print(f"   Finalna liczba aplikantów: {total_expected_applicants:.0f}")
print(f"   📊 DEBUG: F_app stats:")
print(f"      Centers range: [{centers.min():.1f}, {centers.max():.1f}]")
print(f"      CDF at 400: {F_app_cdf(400):.4f}")
print(f"      CDF at 700: {F_app_cdf(700):.4f}")
print(f"      CDF at 900: {F_app_cdf(900):.4f}")
print(f"      CDF at 950: {F_app_cdf(950):.4f}")

# 6. Predykcja dla przykładowych kandydatów
print("\n🎯 Predykcja szans przyjęcia...")

test_scores = [400, 500, 600, 700, 750, 800, 850, 880, 900, 910, 920, 930, 940, 950, 960, 970]

def acceptance_prob_poisson(s, F_app_cdf_func, lam, K, debug=False):
    """
    Oblicza P(przyjęcie | punkty=s) używając rozkładu Poissona.
    
    Args:
        s: Punkty kandydata
        F_app_cdf_func: CDF rozkładu aplikantów
        lam: Liczba aplikantów (lambda)
        K: Liczba miejsc
        debug: Czy wyświetlać debug info
    
    Returns:
        float: Prawdopodobieństwo przyjęcia (0-1)
    
    Logika:
        - tail = 1 - F_app(s) = frakcja aplikantów z punktami > s
        - mu = lam * tail = oczekiwana liczba rywali z punktami > s
        - P(accept) = P(rywali < K) = poisson.cdf(K-1, mu)
    """
    cdf_val = float(F_app_cdf_func(s))
    tail = max(0.0, 1.0 - cdf_val)
    mu = lam * tail
    prob = float(poisson.cdf(K-1, mu))
    
    if debug:
        print(f"      s={s:.0f}: CDF={cdf_val:.4f}, tail={tail:.4f}, rivals={mu:.1f}, P(accept)={prob*100:.1f}%")
    
    return prob

print("   📊 DEBUG: Przykładowe obliczenia:")
results = []
for i, candidate_s in enumerate(test_scores):
    p_accept = acceptance_prob_poisson(
        candidate_s,
        F_app_cdf,
        applicants_count_2024,  # Użyj realnej liczby z 2024
        seat_limit,
        debug=(i in [0, 7, 8, 9, 10, 11, 12, 13, 14, 15])  # Debug więcej punktów
    )
    results.append((candidate_s, p_accept))

# 7. Wyświetl wyniki
print("\n" + "="*80)
print("🎓 WYNIKI PREDYKCJI NA ROK 2025")
print("="*80)
print(f"Program: {program_name}")
print(f"Próg 2024: {prev_cutoff_2024}")
print(f"Limit miejsc: {seat_limit}")
print(f"Liczba aplikantów 2024: {applicants_count_2024}")
print(f"Założenie: liczba aplikantów 2025 = {applicants_count_2024} (jak w 2024)")
print("\n" + "-"*80)
print(f"{'Punkty kandydata':>20} | {'Szansa przyjęcia':>20} | {'Status':>15}")
print("-"*80)

for s, p in results:
    # Status based on probability (NO data leakage!)
    if p >= 0.95:
        status = "✅ Bardzo pewne"
    elif p >= 0.8:
        status = "✅ Prawdopodobne"
    elif p >= 0.6:
        status = "⚡ Umiarkowane"
    elif p >= 0.3:
        status = "⚠️ Ryzykowne"
    else:
        status = "❌ Mało prawdopodobne"
    
    # Context info (próg 2024 dla porównania, NIE decision rule)
    context = ""
    if s < prev_cutoff_2024:
        context = f" (< {prev_cutoff_2024:.0f})"
    
    print(f"{s:20.0f} | {p*100:19.1f}% | {status:>20}{context}")

print("="*80)

print("\n✅ KROK 3 ZAKOŃCZONY POMYŚLNIE!")
print("="*80)

