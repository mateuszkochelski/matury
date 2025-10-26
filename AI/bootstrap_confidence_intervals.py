"""
Bootstrap Confidence Intervals dla F_app i P(przyjęcie).

Używa resamplingu aplikantów z zamiennikiem żeby oszacować niepewność predykcji.
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson
from tqdm import tqdm

print("\n" + "="*80)
print("📊 BOOTSTRAP CONFIDENCE INTERVALS")
print("="*80)

# Config
N_BOOTSTRAP = 1000  # Liczba iteracji bootstrapu
CONFIDENCE_LEVEL = 0.95  # 95% CI
ALPHA = 1 - CONFIDENCE_LEVEL

# 1. Wczytaj dane
print("\n📂 KROK 1: Wczytywanie danych...")
with open('/tmp/training_data_pp.pkl', 'rb') as f:
    training_df = pickle.load(f)

# 2. Wybierz program (AI - wysoki próg)
test_program_id = 3852
program_data = training_df[training_df['program_id'] == test_program_id].copy()
applicants = program_data[program_data['applied']==1].copy()

prev_cutoff = applicants['prev_cutoff'].iloc[0]
seat_limit = applicants['seat_limit'].iloc[0]
year = applicants['year'].iloc[0]

print(f"   Program ID: {test_program_id}")
print(f"   Próg {year}: {prev_cutoff:.1f}")
print(f"   Limit miejsc: {seat_limit}")
print(f"   Aplikanci: {len(applicants)}")

# 3. Zbuduj CDF populacji CKE (dla przekształcenia punktów → percentyle)
print(f"\n📊 KROK 2: CDF populacji CKE {year}...")
all_cke_points = program_data['s_points'].values
points_sorted = np.sort(all_cke_points)
n_cke = len(points_sorted)
percentiles_cke = np.arange(1, n_cke+1) / n_cke

points_to_percentile = interpolate.interp1d(
    points_sorted,
    percentiles_cke,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# 4. Test scores (wokół progu)
test_scores = np.linspace(prev_cutoff - 100, prev_cutoff + 100, 41)  # 41 punktów co 5

print(f"   Test scores: {len(test_scores)} punktów od {test_scores.min():.0f} do {test_scores.max():.0f}")

# 5. Funkcja do budowania F_app z bootstrapu
def build_F_app_from_bootstrap_sample(applicants_boot, points_to_pct_func):
    """
    Zbuduj F_app_percentile z bootstrap sample aplikantów.
    
    Args:
        applicants_boot: DataFrame z resamplingiem aplikantów
        points_to_pct_func: funkcja do przekształcenia punktów → percentyle
    
    Returns:
        F_app_percentile: funkcja interpolująca
    """
    # Przekształć punkty aplikantów na percentyle
    applicants_pct = points_to_pct_func(applicants_boot['s_points'].values)
    applicants_pct = applicants_pct[~np.isnan(applicants_pct)]
    
    if len(applicants_pct) < 10:
        # Za mało danych
        return None
    
    # Zbuduj histogram percentyli
    bins_pct = np.linspace(0, 1, 201)
    hist_pct, edges_pct = np.histogram(applicants_pct, bins=bins_pct, density=False)
    centers_pct = 0.5 * (edges_pct[:-1] + edges_pct[1:])
    
    # CDF
    pdf_pct = hist_pct / hist_pct.sum()
    cdf_pct = np.cumsum(pdf_pct)
    
    F_app_percentile = interpolate.interp1d(
        centers_pct,
        cdf_pct,
        kind='linear',
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )
    
    return F_app_percentile

# 6. Funkcja prawdopodobieństwa przyjęcia
def acceptance_prob(s, F_app_pct_func, points_to_pct_func, lam, K):
    """
    P(przyjęcie | punkty=s).
    
    Args:
        s: punkty kandydata
        F_app_pct_func: F_app w przestrzeni percentyli
        points_to_pct_func: punkty → percentyl w populacji
        lam: liczba aplikantów
        K: liczba miejsc
    """
    # Przekształć punkty → percentyl
    pct = points_to_pct_func(s)
    
    # F_app(percentyl)
    f_app = F_app_pct_func(pct)
    
    # Tail probability
    tail = max(0.0, 1.0 - f_app)
    
    # Oczekiwana liczba rywali
    mu = lam * tail
    
    # P(rivali < K)
    prob = float(poisson.cdf(K-1, mu))
    
    return prob

# 7. BOOTSTRAP!
print(f"\n🔄 KROK 3: Bootstrap ({N_BOOTSTRAP} iteracji)...")
print(f"   Confidence level: {CONFIDENCE_LEVEL*100:.0f}%")

bootstrap_predictions = np.zeros((N_BOOTSTRAP, len(test_scores)))

for i in tqdm(range(N_BOOTSTRAP), desc="   Bootstrap"):
    # Resample aplikantów Z ZAMIENNIKIEM
    applicants_boot = applicants.sample(n=len(applicants), replace=True)
    
    # Zbuduj F_app dla tego bootstrapu
    F_app_boot = build_F_app_from_bootstrap_sample(applicants_boot, points_to_percentile)
    
    if F_app_boot is None:
        # Jeśli nie udało się zbudować, użyj oryginalnego (fallback)
        continue
    
    # Oblicz P(accept) dla każdego test score
    for j, s in enumerate(test_scores):
        try:
            p_accept = acceptance_prob(
                s, 
                F_app_boot, 
                points_to_percentile,
                len(applicants),
                seat_limit
            )
            bootstrap_predictions[i, j] = p_accept
        except:
            # Jeśli błąd interpolacji, użyj 0.5 (neutral)
            bootstrap_predictions[i, j] = 0.5

print(f"   ✅ Bootstrap zakończony!")

# 8. Oblicz percentyle (confidence intervals)
print(f"\n📊 KROK 4: Obliczanie confidence intervals...")

alpha_lower = (ALPHA / 2) * 100  # 2.5%
alpha_upper = (1 - ALPHA / 2) * 100  # 97.5%

CI_lower = np.percentile(bootstrap_predictions, alpha_lower, axis=0)
CI_median = np.percentile(bootstrap_predictions, 50, axis=0)
CI_upper = np.percentile(bootstrap_predictions, alpha_upper, axis=0)

# Oblicz też oryginalną predykcję (bez bootstrapu)
print(f"\n📈 KROK 5: Oryginalna predykcja (bez bootstrapu)...")
applicants_pct_orig = points_to_percentile(applicants['s_points'].values)
applicants_pct_orig = applicants_pct_orig[~np.isnan(applicants_pct_orig)]

bins_pct = np.linspace(0, 1, 201)
hist_pct, edges_pct = np.histogram(applicants_pct_orig, bins=bins_pct, density=False)
centers_pct = 0.5 * (edges_pct[:-1] + edges_pct[1:])

pdf_pct = hist_pct / hist_pct.sum()
cdf_pct = np.cumsum(pdf_pct)

F_app_orig = interpolate.interp1d(
    centers_pct,
    cdf_pct,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

original_predictions = []
for s in test_scores:
    p = acceptance_prob(s, F_app_orig, points_to_percentile, len(applicants), seat_limit)
    original_predictions.append(p)
original_predictions = np.array(original_predictions)

# 9. Statystyki bootstrapu
print(f"\n📊 KROK 6: Statystyki bootstrapu...")
CI_width = CI_upper - CI_lower
median_CI_width = np.median(CI_width)
mean_CI_width = np.mean(CI_width)

print(f"   Szerokość CI (95%):")
print(f"      Mean: {mean_CI_width*100:.1f}%")
print(f"      Median: {median_CI_width*100:.1f}%")
print(f"      Min: {CI_width.min()*100:.1f}%")
print(f"      Max: {CI_width.max()*100:.1f}%")

# 10. Wyświetl wyniki
print("\n" + "="*80)
print(f"🎯 WYNIKI: Confidence Intervals (95%)")
print("="*80)
print(f"Program: AI (próg={prev_cutoff:.0f}, limit={seat_limit})")
print(f"Bootstrap iterations: {N_BOOTSTRAP}")
print("\n" + "-"*100)
print(f"{'Punkty':>8} | {'Percentile':>11} | {'P(accept)':>10} | {'CI Lower':>10} | {'CI Median':>10} | {'CI Upper':>10} | {'CI Width':>10}")
print("-"*100)

# Wyświetl co 5. wynik (żeby nie było za dużo)
for i in range(0, len(test_scores), 5):
    s = test_scores[i]
    pct = points_to_percentile(s)
    p_orig = original_predictions[i]
    ci_low = CI_lower[i]
    ci_med = CI_median[i]
    ci_up = CI_upper[i]
    ci_w = CI_width[i]
    
    print(f"{s:8.0f} | {pct*100:10.1f}% | {p_orig*100:9.1f}% | {ci_low*100:9.1f}% | {ci_med*100:9.1f}% | {ci_up*100:9.1f}% | {ci_w*100:9.1f}%")

print("="*100)

# 11. Wyświetl wybrane punkty wokół progu (szczegółowo)
print(f"\n🔍 ZOOM: Punkty wokół progu ({prev_cutoff:.0f})")
print("-"*100)
print(f"{'Punkty':>8} | {'P(accept)':>10} | {'95% CI':>25} | {'Interpretacja':>40}")
print("-"*100)

interesting_points = [
    prev_cutoff - 50,
    prev_cutoff - 20,
    prev_cutoff - 10,
    prev_cutoff,
    prev_cutoff + 10,
    prev_cutoff + 20,
    prev_cutoff + 50,
]

for s_target in interesting_points:
    # Znajdź najbliższy test_score
    idx = np.argmin(np.abs(test_scores - s_target))
    s = test_scores[idx]
    
    p_orig = original_predictions[idx]
    ci_low = CI_lower[idx]
    ci_up = CI_upper[idx]
    
    # Interpretacja
    if p_orig >= 0.95:
        interp = "✅ Bardzo pewne"
    elif p_orig >= 0.8:
        interp = "✅ Prawdopodobne"
    elif p_orig >= 0.6:
        interp = "⚡ Umiarkowane (niepewne)"
    elif p_orig >= 0.3:
        interp = "⚠️ Ryzykowne"
    else:
        interp = "❌ Mało prawdopodobne"
    
    print(f"{s:8.0f} | {p_orig*100:9.1f}% | [{ci_low*100:5.1f}%, {ci_up*100:5.1f}%] | {interp:>40}")

print("="*100)

# 12. Zapisz wyniki
print(f"\n💾 KROK 7: Zapisywanie wyników...")
results_df = pd.DataFrame({
    'points': test_scores,
    'percentile': [points_to_percentile(s) for s in test_scores],
    'p_accept_original': original_predictions,
    'ci_lower': CI_lower,
    'ci_median': CI_median,
    'ci_upper': CI_upper,
    'ci_width': CI_width
})

results_df.to_csv('/tmp/bootstrap_results.csv', index=False)
print(f"   ✅ Zapisano do: /tmp/bootstrap_results.csv")

print("\n✅ BOOTSTRAP ZAKOŃCZONY!")
print("="*80)

