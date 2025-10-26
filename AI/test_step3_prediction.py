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

# 4. Oblicz punkty dla całej populacji CKE 2025
print("\n🧮 Obliczanie punktów dla populacji CKE 2025...")
from pipeline_1 import compute_points_for_program

program_rule = program_rules_temp[program_id]

exam_df_2025['s_points'] = exam_df_2025.apply(
    lambda r: compute_points_for_program(r, program_rule),
    axis=1
)

print(f"   ✅ Punkty obliczone")
print(f"   Min: {exam_df_2025['s_points'].min():.1f}")
print(f"   Max: {exam_df_2025['s_points'].max():.1f}")
print(f"   Mean: {exam_df_2025['s_points'].mean():.1f}")

# 5. Zbuduj F_app (rozkład aplikantów) używając modelu propensity
print("\n📈 Budowanie F_app (rozkład aplikantów)...")

# Histogram populacji CKE
bin_width = 0.5
s_min, s_max = float(exam_df_2025['s_points'].min()), float(exam_df_2025['s_points'].max())
nbins = int(np.ceil((s_max - s_min) / bin_width)) + 1
bins = np.linspace(s_min, s_max, nbins)
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

# Oczekiwana liczba aplikantów w każdym binie
expected_applicants = hist.astype(float) * p_app

# PDF i CDF aplikantów
pdf_app = expected_applicants / expected_applicants.sum()
cdf_app = np.cumsum(pdf_app)

# Funkcja CDF
F_app_cdf = interpolate.interp1d(
    centers, cdf_app,
    kind='previous',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

total_expected_applicants = expected_applicants.sum()
print(f"   ✅ F_app zbudowany")
print(f"   Oczekiwana liczba aplikantów (model): {total_expected_applicants:.0f}")
print(f"   ℹ️  Używamy realnej liczby z 2024: {applicants_count_2024}")

# 6. Predykcja dla przykładowych kandydatów
print("\n🎯 Predykcja szans przyjęcia...")

test_scores = [400, 500, 600, 700, 750, 800, 850, 900, 950]

def acceptance_prob_poisson(s, F_app_cdf_func, lam, K):
    tail = max(0.0, 1.0 - float(F_app_cdf_func(s)))
    mu = lam * tail
    return float(poisson.cdf(K-1, mu))

results = []
for candidate_s in test_scores:
    p_accept = acceptance_prob_poisson(
        candidate_s,
        F_app_cdf,
        applicants_count_2024,  # Użyj realnej liczby z 2024
        seat_limit
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
    if s < prev_cutoff_2024:
        status = "⚠️ Poniżej progu"
    elif p > 0.9:
        status = "✅ Bardzo pewne"
    elif p > 0.7:
        status = "✅ Pewne"
    elif p > 0.5:
        status = "⚡ Granica"
    else:
        status = "❌ Ryzykowne"
    
    print(f"{s:20.0f} | {p*100:19.1f}% | {status:>15}")

print("="*80)

print("\n✅ KROK 3 ZAKOŃCZONY POMYŚLNIE!")
print("="*80)

