"""
KROK 3: Predykcja z PERCENTILE-BASED F_app.

Kluczowa innowacja:
- Zamiast zakładać stały rozkład PUNKTÓW aplikantów,
- Zakładamy stały rozkład PERCENTYLI aplikantów w populacji CKE.

To jest robust do zmian trudności matur!
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson

print("\n" + "="*80)
print("🧪 TEST: PERCENTILE-BASED PREDICTION")
print("="*80)

# 1. Wczytaj dane
print("\n📂 Wczytywanie danych...")
from cke_loader import load_cke_to_exam_df
from cke_synthetic import generate_synthetic_cke_2023
from pp_data_loader import load_politechnika_poznanska
from pp_point_calculator import calculate_points_for_dataframe
from pp_field_mapping import pp_to_field_mapping

# Wczytaj CKE 2024
exam_df_2024 = load_cke_to_exam_df(
    '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
)
print(f"   ✅ CKE 2024: {len(exam_df_2024)} maturzystów")

# Wczytaj PP data
apps_hist_df, apps_agg_df, program_rules_temp = load_politechnika_poznanska()

# Wybierz program testowy (Sztuczna Inteligencja)
programs_2024 = apps_agg_df[apps_agg_df['year']==2024]
test_program = programs_2024.iloc[0]

program_id = str(test_program['program_id'])
program_name = test_program['Kierunek']
seat_limit = int(test_program['seat_limit'])
prev_cutoff_2024 = float(test_program['prev_cutoff'])
applicants_count_2024 = int(test_program['applicants_count'])
field_id = pp_to_field_mapping.get(int(program_id))

print(f"\n📊 Program: {program_name}")
print(f"   ID: {program_id}, field_id: {field_id}")
print(f"   Limit: {seat_limit}, Próg 2024: {prev_cutoff_2024:.1f}")
print(f"   Aplikanci 2024: {applicants_count_2024}")

# 2. Oblicz punkty dla CAŁEJ populacji CKE 2024
print("\n🧮 KROK 1: Obliczanie punktów dla CKE 2024...")
exam_df_2024['s_points'] = calculate_points_for_dataframe(
    exam_df_2024,
    field_of_study_id=field_id,
    show_progress=False
)
print(f"   ✅ CKE 2024 punkty: min={exam_df_2024['s_points'].min():.1f}, "
      f"max={exam_df_2024['s_points'].max():.1f}, "
      f"mean={exam_df_2024['s_points'].mean():.1f}")

# 3. Zbuduj CDF populacji CKE 2024 (punkty → percentile)
print("\n📈 KROK 2: Budowanie CDF populacji CKE 2024 (punkty → percentile)...")
points_2024_sorted = np.sort(exam_df_2024['s_points'].values)
n_2024 = len(points_2024_sorted)
percentiles_2024 = np.arange(1, n_2024+1) / n_2024

# Funkcja: punkty_2024 → percentyl_2024
points_to_percentile_2024 = interpolate.interp1d(
    points_2024_sorted,
    percentiles_2024,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# Test
test_point = 700
test_pct = points_to_percentile_2024(test_point)
print(f"   ✅ Mapping utworzony")
print(f"   Test: {test_point} pkt w 2024 = {test_pct*100:.1f}th percentile")

# 4. Wczytaj aplikantów z 2024 i przekształć na percentile
print("\n📊 KROK 3: Przekształcanie aplikantów 2024 na percentile...")
program_apps_2024 = apps_hist_df[
    (apps_hist_df['program_id'] == int(program_id)) &
    (apps_hist_df['year'] == 2024)
]
print(f"   ✅ {len(program_apps_2024)} aplikantów z 2024")

# Punkty aplikantów
applicants_points_2024 = program_apps_2024['s_points'].values

# Przekształć na percentile w populacji CKE 2024
applicants_percentiles = points_to_percentile_2024(applicants_points_2024)

print(f"   📊 Percentile aplikantów:")
print(f"      Min: {applicants_percentiles.min()*100:.1f}th")
print(f"      Max: {applicants_percentiles.max()*100:.1f}th")
print(f"      Median: {np.median(applicants_percentiles)*100:.1f}th")
print(f"      Mean: {applicants_percentiles.mean()*100:.1f}th")

# 5. Zbuduj F_app w przestrzeni PERCENTYLI
print("\n📈 KROK 4: Budowanie F_app_percentile...")

# Histogram percentyli aplikantów
bins_pct = np.linspace(0, 1, 201)  # 0%, 0.5%, 1%, ..., 100%
hist_pct, edges_pct = np.histogram(applicants_percentiles, bins=bins_pct, density=False)
centers_pct = 0.5 * (edges_pct[:-1] + edges_pct[1:])

# CDF percentyli aplikantów
pdf_pct = hist_pct / hist_pct.sum()
cdf_pct = np.cumsum(pdf_pct)

F_app_percentile = interpolate.interp1d(
    centers_pct,
    cdf_pct,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

print(f"   ✅ F_app_percentile zbudowany")
print(f"   📊 F_app(50th percentile) = {F_app_percentile(0.5):.3f}")
print(f"   📊 F_app(90th percentile) = {F_app_percentile(0.9):.3f}")
print(f"   📊 F_app(95th percentile) = {F_app_percentile(0.95):.3f}")

# 6. Wygeneruj syntetyczne CKE 2025
print("\n🔬 KROK 5: Generowanie syntetycznego CKE 2025...")
exam_df_2025 = generate_synthetic_cke_2023(exam_df_2024)
exam_df_2025['year'] = 2025
print(f"   ✅ CKE 2025: {len(exam_df_2025)} maturzystów (syntetyczne)")

# 7. Oblicz punkty dla CKE 2025
print("\n🧮 KROK 6: Obliczanie punktów dla CKE 2025...")
exam_df_2025['s_points'] = calculate_points_for_dataframe(
    exam_df_2025,
    field_of_study_id=field_id,
    show_progress=False
)
print(f"   ✅ CKE 2025 punkty: min={exam_df_2025['s_points'].min():.1f}, "
      f"max={exam_df_2025['s_points'].max():.1f}, "
      f"mean={exam_df_2025['s_points'].mean():.1f}")

# 8. Zbuduj CDF populacji CKE 2025 (punkty → percentile)
print("\n📈 KROK 7: Budowanie CDF populacji CKE 2025...")
points_2025_sorted = np.sort(exam_df_2025['s_points'].values)
n_2025 = len(points_2025_sorted)
percentiles_2025 = np.arange(1, n_2025+1) / n_2025

# Funkcja: punkty_2025 → percentyl_2025
points_to_percentile_2025 = interpolate.interp1d(
    points_2025_sorted,
    percentiles_2025,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# Funkcja odwrotna: percentyl_2025 → punkty_2025
percentile_to_points_2025 = interpolate.interp1d(
    percentiles_2025,
    points_2025_sorted,
    kind='linear',
    bounds_error=False,
    fill_value=(points_2025_sorted[0], points_2025_sorted[-1])
)

print(f"   ✅ Mapping 2025 utworzony")

# 9. Przekształć F_app_percentile → F_app_points_2025
print("\n🔄 KROK 8: Mapowanie F_app z percentyli na punkty 2025...")

# Dla każdego percentyla p: ile aplikantów ma ≤p?
# Następnie: jaki to odpowiada punktom w 2025?

# Sample percentile space
sample_percentiles = np.linspace(0, 1, 1000)
f_app_values = F_app_percentile(sample_percentiles)

# Przekształć percentile → points_2025
sample_points_2025 = percentile_to_points_2025(sample_percentiles)

# Funkcja: points_2025 → F_app (poprzez percentile)
F_app_2025 = interpolate.interp1d(
    sample_points_2025,
    f_app_values,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

print(f"   ✅ F_app_2025 (w przestrzeni punktów 2025) gotowy!")
print(f"   📊 DEBUG:")
print(f"      F_app(400 pkt w 2025) = {F_app_2025(400):.3f}")
print(f"      F_app(700 pkt w 2025) = {F_app_2025(700):.3f}")
print(f"      F_app(900 pkt w 2025) = {F_app_2025(900):.3f}")

# 10. Predykcja dla kandydatów
print("\n🎯 KROK 9: Predykcja szans przyjęcia...")

test_scores = [400, 500, 600, 700, 750, 800, 850, 880, 900, 910, 920, 930, 940, 950, 960, 970]

def acceptance_prob_poisson(s, F_app_func, lam, K):
    """Oblicza P(przyjęcie | punkty=s)."""
    cdf_val = float(F_app_func(s))
    tail = max(0.0, 1.0 - cdf_val)
    mu = lam * tail
    prob = float(poisson.cdf(K-1, mu))
    return prob

results = []
print("   📊 Przykładowe obliczenia:")
for i, candidate_s in enumerate(test_scores):
    p_accept = acceptance_prob_poisson(
        candidate_s,
        F_app_2025,
        applicants_count_2024,
        seat_limit
    )
    
    # Debug co 4ty
    if i % 4 == 0:
        cdf_val = F_app_2025(candidate_s)
        tail = 1.0 - cdf_val
        rivals = applicants_count_2024 * tail
        pct = points_to_percentile_2025(candidate_s)
        print(f"      {candidate_s} pkt = {pct*100:.1f}th pct → rivals={rivals:.1f}, P={p_accept*100:.1f}%")
    
    results.append((candidate_s, p_accept))

# 11. Wyświetl wyniki
print("\n" + "="*80)
print("🎓 WYNIKI PREDYKCJI 2025 (PERCENTILE-BASED)")
print("="*80)
print(f"Program: {program_name}")
print(f"Próg 2024: {prev_cutoff_2024:.1f}")
print(f"Limit: {seat_limit}, Aplikanci (założenie): {applicants_count_2024}")
print("\n" + "-"*80)
print(f"{'Punkty 2025':>15} | {'Percentile':>12} | {'Szansa':>10} | {'Status':>20}")
print("-"*80)

for s, p in results:
    pct = points_to_percentile_2025(s)
    
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
    
    print(f"{s:15.0f} | {pct*100:11.1f}% | {p*100:9.1f}% | {status:>20}")

print("="*80)

# 12. Porównanie: percentile-based vs naive
print("\n📊 PORÓWNANIE PODEJŚĆ:")
print("-"*80)

# Naive: używamy F_app z 2024 bezpośrednio na punktach 2025
print("Dla 930 punktów:")
print(f"   2025 syntetyczne: 930 pkt = {points_to_percentile_2025(930)*100:.1f}th percentile")
print(f"   2024 rzeczywiste: 930 pkt = {points_to_percentile_2024(930)*100:.1f}th percentile")
print(f"   → Różnica percentyli: {(points_to_percentile_2025(930) - points_to_percentile_2024(930))*100:.1f} pp")

print("\n✅ Percentile-based approach uwzględnia zmiany trudności egzaminów!")
print("="*80)

