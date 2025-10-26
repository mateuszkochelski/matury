"""
FINAL TEST: Predykcja z percentile-based F_app dla różnych wartości punktów.
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson

print("\n" + "="*80)
print("🎯 FINAL TEST: Percentile-based Prediction")
print("="*80)

# 1. Wczytaj training_df
print("\n📂 KROK 1: Wczytywanie danych...")
with open('/tmp/training_data_pp.pkl', 'rb') as f:
    training_df = pickle.load(f)

# 2. Wybierz program z wysokim progiem (AI)
test_program_id = 3852  # AI - wysoki próg 926.5
program_data = training_df[training_df['program_id'] == test_program_id].copy()
applicants = program_data[program_data['applied']==1].copy()

# Info o programie
prev_cutoff = applicants['prev_cutoff'].iloc[0]
seat_limit = applicants['seat_limit'].iloc[0]
year = applicants['year'].iloc[0]  # Rok z którego są dane

print(f"   Program ID: {test_program_id}")
print(f"   Próg {year}: {prev_cutoff:.1f}")
print(f"   Limit miejsc: {seat_limit}")
print(f"   Aplikanci: {len(applicants)}")

# 3. Zbuduj CDF populacji CKE dla tego roku (punkty → percentile)
print(f"\n📊 KROK 2: Budowanie CDF populacji CKE {year}...")
all_cke_year = program_data['s_points'].values
points_sorted = np.sort(all_cke_year)
n = len(points_sorted)
percentiles = np.arange(1, n+1) / n

points_to_percentile = interpolate.interp1d(
    points_sorted,
    percentiles,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# 4. Przekształć aplikantów na percentyle
print(f"\n📊 KROK 3: Przekształcanie aplikantów na percentyle...")
applicants_pct = points_to_percentile(applicants['s_points'].values)
applicants_pct = applicants_pct[~np.isnan(applicants_pct)]

print(f"   Aplikanci percentyle:")
print(f"      Min: {applicants_pct.min()*100:.1f}th")
print(f"      25%: {np.percentile(applicants_pct, 25)*100:.1f}th")
print(f"      Median: {np.median(applicants_pct)*100:.1f}th")
print(f"      75%: {np.percentile(applicants_pct, 75)*100:.1f}th")
print(f"      Max: {applicants_pct.max()*100:.1f}th")

# 5. Zbuduj F_app_percentile (CDF aplikantów w przestrzeni percentyli)
print(f"\n📈 KROK 4: Budowanie F_app_percentile...")
bins_pct = np.linspace(0, 1, 201)
hist_pct, edges_pct = np.histogram(applicants_pct, bins=bins_pct, density=False)
centers_pct = 0.5 * (edges_pct[:-1] + edges_pct[1:])

pdf_pct = hist_pct / hist_pct.sum()
cdf_pct = np.cumsum(pdf_pct)

F_app_percentile = interpolate.interp1d(
    centers_pct,
    cdf_pct,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# 6. Dla "2025": symuluj zmianę trudności egzaminu (skaluj punkty o ±5%)
print(f"\n🔮 KROK 5: Symulacja 2025 (łatwiejsza matura: +5% punktów)...")
# Symuluj że w 2025 matura była łatwiejsza → wszyscy mają więcej punktów
scale_factor = 1.05
all_cke_2025 = all_cke_year * scale_factor
all_cke_2025 = np.clip(all_cke_2025, 0, 1000)

points_2025_sorted = np.sort(all_cke_2025)
n_2025 = len(points_2025_sorted)
percentiles_2025 = np.arange(1, n_2025+1) / n_2025

points_to_percentile_2025 = interpolate.interp1d(
    points_2025_sorted,
    percentiles_2025,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# 7. Mapuj F_app_percentile → punkty 2025
percentile_to_points_2025 = interpolate.interp1d(
    percentiles_2025,
    points_2025_sorted,
    kind='linear',
    bounds_error=False,
    fill_value=(points_2025_sorted[0], points_2025_sorted[-1])
)

def F_app_2025(s_2025):
    """CDF aplikantów w przestrzeni punktów 2025."""
    pct_2025 = points_to_percentile_2025(s_2025)
    return F_app_percentile(pct_2025)

# 8. Funkcja prawdopodobieństwa przyjęcia
def acceptance_prob_poisson(s, F_app_func, lam, K):
    """
    P(przyjęcie | punkty=s) używając Poissona.
    
    Args:
        s: punkty kandydata
        F_app_func: CDF aplikantów
        lam: łączna liczba aplikantów (lambda)
        K: liczba miejsc
    """
    cdf_val = float(F_app_func(s))
    tail = max(0.0, 1.0 - cdf_val)
    mu = lam * tail  # Oczekiwana liczba rywali z wyższymi punktami
    prob = float(poisson.cdf(K-1, mu))  # P(rivali < K)
    return prob, mu

# 9. Predykcja dla różnych wartości punktów
print(f"\n🎯 KROK 6: PREDYKCJE dla różnych punktów (2025)...")
print(f"   Założenia:")
print(f"   - Aplikanci: {len(applicants)} (jak w {year})")
print(f"   - Limit miejsc: {seat_limit}")
print(f"   - Matura 2025: +5% punktów (łatwiejsza)")

# Oblicz próg w "punktach 2025" (skalowany)
threshold_2025 = prev_cutoff * scale_factor

# Test punktów wokół skalowanego progu
test_points = [
    threshold_2025 - 100,
    threshold_2025 - 50,
    threshold_2025 - 20,
    threshold_2025 - 10,
    threshold_2025,
    threshold_2025 + 10,
    threshold_2025 + 20,
    threshold_2025 + 50,
    threshold_2025 + 100,
]

print(f"\n{'Punkty 2025':>12} | {'Percentile':>11} | {'F_app':>8} | {'Rywale':>8} | {'P(przyjęcie)':>12} | Status")
print("-"*80)

results = []
for s in test_points:
    pct = points_to_percentile_2025(s)
    f_app = F_app_2025(s)
    p_accept, expected_rivals = acceptance_prob_poisson(s, F_app_2025, len(applicants), seat_limit)
    
    if p_accept >= 0.95:
        status = "✅ Bardzo pewne"
    elif p_accept >= 0.8:
        status = "✅ Prawdopodobne"
    elif p_accept >= 0.6:
        status = "⚡ Umiarkowane"
    elif p_accept >= 0.3:
        status = "⚠️ Ryzykowne"
    else:
        status = "❌ Mało prawdopodobne"
    
    results.append((s, pct, f_app, expected_rivals, p_accept, status))
    print(f"{s:12.0f} | {pct*100:10.1f}% | {f_app:8.3f} | {expected_rivals:8.1f} | {p_accept*100:11.1f}% | {status}")

print("-"*80)
print(f"Próg {year} (nieskalowany): {prev_cutoff:.1f}")
print(f"Próg 2025 (skalowany +5%): {threshold_2025:.1f}")
print("="*80)

# 10. Porównanie z "naiwnym" podejściem (bez percentyli)
print(f"\n💡 PORÓWNANIE: Percentile vs Naive (bez korekty trudności)")
print("-"*80)

# Naive: użyj F_app z {year} bezpośrednio dla punktów 2025
applicants_sorted_raw = np.sort(applicants['s_points'].values)

def F_app_naive(s):
    count_below = (applicants_sorted_raw < s).sum()
    return count_below / len(applicants_sorted_raw)

print(f"{'Punkty':>8} | {'Percentile':>11} | {'P(accept) - Naive':>18} | {'P(accept) - Pct':>16} | {'Różnica':>10}")
print("-"*80)

for s, pct, f_app, expected_rivals, p_accept_pct, status in results:
    p_accept_naive, _ = acceptance_prob_poisson(s, F_app_naive, len(applicants), seat_limit)
    diff = p_accept_pct - p_accept_naive
    
    print(f"{s:8.0f} | {pct*100:10.1f}% | {p_accept_naive*100:17.1f}% | {p_accept_pct*100:15.1f}% | {diff*100:+9.1f}%")

print("="*80)
print("\n✅ TEST ZAKOŃCZONY!")
print("="*80)

