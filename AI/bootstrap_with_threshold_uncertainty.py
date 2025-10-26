"""
Bootstrap Confidence Intervals z niepewnością PROGU.

POPRAWNE podejście:
1. Bootstrap resample aplikantów → F_app_boot
2. Symuluj aplikantów 2025 z F_app_boot
3. Oblicz PRÓG 2025 z symulacji
4. P(przyjęcie) = P(punkty > próg_2025)
5. Bootstrap daje rozkład możliwych progów!
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from tqdm import tqdm

print("\n" + "="*80)
print("🎯 BOOTSTRAP z NIEPEWNOŚCIĄ PROGU")
print("="*80)

# Config
N_BOOTSTRAP = 1000
CONFIDENCE_LEVEL = 0.95
ALPHA = 1 - CONFIDENCE_LEVEL

# 1. Wczytaj dane
print("\n📂 KROK 1: Wczytywanie danych...")
with open('/tmp/training_data_pp.pkl', 'rb') as f:
    training_df = pickle.load(f)

# 2. Wybierz program (AI)
test_program_id = 3852
program_data = training_df[training_df['program_id'] == test_program_id].copy()
applicants = program_data[program_data['applied']==1].copy()

prev_cutoff = applicants['prev_cutoff'].iloc[0]
seat_limit = applicants['seat_limit'].iloc[0]
year = applicants['year'].iloc[0]

print(f"   Program ID: {test_program_id}")
print(f"   Rok bazowy: {year}")
print(f"   Próg {year}: {prev_cutoff:.1f} (dla odniesienia)")
print(f"   Limit miejsc: {seat_limit}")
print(f"   Aplikanci {year}: {len(applicants)}")

# 3. CDF populacji CKE (punkty → percentyle)
print(f"\n📊 KROK 2: CDF populacji CKE...")
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

percentile_to_points = interpolate.interp1d(
    percentiles_cke,
    points_sorted,
    kind='linear',
    bounds_error=False,
    fill_value=(points_sorted[0], points_sorted[-1])
)

# 4. Test scores - GĘSTE wokół progu!
# Zakres: próg ±100, ale GĘSTO wokół progu (co 5 pkt w zakresie ±50, co 10 pkt dalej)
test_scores_far = np.arange(prev_cutoff - 100, prev_cutoff - 50, 10)  # Co 10 pkt daleko
test_scores_near = np.arange(prev_cutoff - 50, prev_cutoff + 50, 5)   # Co 5 pkt blisko
test_scores_far2 = np.arange(prev_cutoff + 50, prev_cutoff + 101, 10)  # Co 10 pkt daleko

test_scores = np.concatenate([test_scores_far, test_scores_near, test_scores_far2])
test_scores = np.sort(test_scores)

print(f"   Test scores: {len(test_scores)} punktów (gęsto wokół progu!)")
print(f"   Zakres: {test_scores.min():.0f} - {test_scores.max():.0f}")

# 5. BOOTSTRAP z symulacją progu!
print(f"\n🔄 KROK 3: Bootstrap z symulacją progu ({N_BOOTSTRAP} iteracji)...")
print(f"   Każda iteracja:")
print(f"      1. Resample aplikantów → F_app_boot")
print(f"      2. Symuluj {len(applicants)} aplikantów 2025")
print(f"      3. Oblicz próg 2025 = {seat_limit}. najwyższy wynik")
print(f"      4. P(przyjęcie | punkty) = P(punkty > próg)")

bootstrap_thresholds = []
bootstrap_predictions = np.zeros((N_BOOTSTRAP, len(test_scores)))

for i in tqdm(range(N_BOOTSTRAP), desc="   Bootstrap"):
    # 1. Resample aplikantów z zamiennikiem
    applicants_boot = applicants.sample(n=len(applicants), replace=True)
    
    # 2. Przekształć na percentyle
    applicants_pct_boot = points_to_percentile(applicants_boot['s_points'].values)
    applicants_pct_boot = applicants_pct_boot[~np.isnan(applicants_pct_boot)]
    
    if len(applicants_pct_boot) < 10:
        continue
    
    # 3. Zbuduj F_app_percentile dla tego bootstrapu
    bins_pct = np.linspace(0, 1, 201)
    hist_pct, edges_pct = np.histogram(applicants_pct_boot, bins=bins_pct, density=False)
    centers_pct = 0.5 * (edges_pct[:-1] + edges_pct[1:])
    
    pdf_pct = hist_pct / hist_pct.sum()
    cdf_pct = np.cumsum(pdf_pct)
    
    F_app_percentile_boot = interpolate.interp1d(
        centers_pct,
        cdf_pct,
        kind='linear',
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )
    
    # 4. SYMULUJ aplikantów 2025 z F_app_boot
    # Sample percentyles z F_app, potem przekształć na punkty
    n_applicants_2025 = len(applicants)  # Zakładamy tyle samo aplikantów
    
    # Inverse CDF sampling - POPRAWNE (ciągłe wartości)
    random_probs = np.random.uniform(0, 1, n_applicants_2025)
    
    # Zbuduj INVERSE CDF (F_app^{-1})
    # F_app(pct) = cdf_value → Inverse: pct = F_app^{-1}(cdf_value)
    # Używamy interpolacji linearnej dla ciągłych wartości
    inverse_F_app = interpolate.interp1d(
        cdf_pct,
        centers_pct,
        kind='linear',
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )
    
    # Sample percentyle z inverse CDF
    simulated_percentiles = inverse_F_app(random_probs)
    
    # Przekształć percentyle → punkty (w dystrybucji 2025 = tej samej co bazowa)
    simulated_points_2025 = percentile_to_points(simulated_percentiles)
    
    # 5. OBLICZ PRÓG z symulacji
    if len(simulated_points_2025) >= seat_limit:
        simulated_sorted = np.sort(simulated_points_2025)[::-1]  # Descending
        threshold_boot = simulated_sorted[seat_limit - 1]  # seat_limit-ty najwyższy
        bootstrap_thresholds.append(threshold_boot)
    else:
        # Za mało danych
        threshold_boot = prev_cutoff
        bootstrap_thresholds.append(threshold_boot)
    
    # 6. Oblicz P(przyjęcie) dla każdego test score
    # P(przyjęcie) = P(punkty > próg) = 1 jeśli punkty > próg, 0 jeśli punkty <= próg
    # Ale to zbyt surowe, użyjmy wygładzonej wersji:
    for j, s in enumerate(test_scores):
        if s > threshold_boot:
            bootstrap_predictions[i, j] = 1.0
        else:
            # Smooth transition (logistic)
            # P = 1 / (1 + exp(-k * (s - threshold)))
            k = 0.1  # Szerokość transition
            bootstrap_predictions[i, j] = 1.0 / (1.0 + np.exp(-k * (s - threshold_boot)))

print(f"   ✅ Bootstrap zakończony!")

# 7. Statystyki PROGÓW
print(f"\n📊 KROK 4: Statystyki PROGÓW 2025 (z bootstrapu)...")
bootstrap_thresholds = np.array(bootstrap_thresholds)

threshold_mean = bootstrap_thresholds.mean()
threshold_median = np.median(bootstrap_thresholds)
threshold_std = bootstrap_thresholds.std()
threshold_ci_lower = np.percentile(bootstrap_thresholds, 2.5)
threshold_ci_upper = np.percentile(bootstrap_thresholds, 97.5)

print(f"   Przewidywany próg 2025:")
print(f"      Mean: {threshold_mean:.1f}")
print(f"      Median: {threshold_median:.1f}")
print(f"      Std: {threshold_std:.1f}")
print(f"      95% CI: [{threshold_ci_lower:.1f}, {threshold_ci_upper:.1f}]")
print(f"\n   vs próg {year}: {prev_cutoff:.1f}")
print(f"   Różnica (median): {threshold_median - prev_cutoff:+.1f}")

# 8. Confidence Intervals dla P(accept)
print(f"\n📊 KROK 5: Confidence Intervals dla P(przyjęcie)...")

CI_lower = np.percentile(bootstrap_predictions, 2.5, axis=0)
CI_median = np.percentile(bootstrap_predictions, 50, axis=0)
CI_upper = np.percentile(bootstrap_predictions, 97.5, axis=0)
CI_width = CI_upper - CI_lower

# 9. Wyniki
print("\n" + "="*80)
print(f"🎯 WYNIKI: Bootstrap z niepewnością PROGU")
print("="*80)
print(f"Program: AI (limit={seat_limit})")
print(f"Próg {year}: {prev_cutoff:.1f}")
print(f"Przewidywany próg 2025: {threshold_median:.1f} [{threshold_ci_lower:.1f}, {threshold_ci_upper:.1f}]")
print(f"Bootstrap iterations: {N_BOOTSTRAP}")

print("\n" + "-"*90)
print(f"{'Punkty':>8} | {'P(accept)':>10} | {'95% CI':>25} | {'CI Width':>10} | {'Interpretacja'}")
print("-"*90)

interesting_points = [
    threshold_median - 50,
    threshold_median - 20,
    threshold_median - 10,
    threshold_median,
    threshold_median + 10,
    threshold_median + 20,
    threshold_median + 50,
]

for s_target in interesting_points:
    idx = np.argmin(np.abs(test_scores - s_target))
    s = test_scores[idx]
    
    p_med = CI_median[idx]
    ci_low = CI_lower[idx]
    ci_up = CI_upper[idx]
    ci_w = CI_width[idx]
    
    if p_med >= 0.95:
        interp = "✅ Bardzo pewne"
    elif p_med >= 0.8:
        interp = "✅ Prawdopodobne"
    elif p_med >= 0.6:
        interp = "⚡ Umiarkowane"
    elif p_med >= 0.3:
        interp = "⚠️ Ryzykowne"
    else:
        interp = "❌ Mało prawdopodobne"
    
    marker = " ← PRÓG (median)" if abs(s - threshold_median) < 5 else ""
    
    print(f"{s:8.0f} | {p_med*100:9.1f}% | [{ci_low*100:5.1f}%, {ci_up*100:5.1f}%] | {ci_w*100:9.1f}% | {interp}{marker}")

print("="*90)

# 10. Histogram progów
print(f"\n📊 Histogram przewidywanych progów 2025:")
bins = np.linspace(bootstrap_thresholds.min(), bootstrap_thresholds.max(), 20)
hist, edges = np.histogram(bootstrap_thresholds, bins=bins)

for i in range(len(hist)):
    if hist[i] > 0:
        bar = '█' * int(hist[i] / hist.max() * 50)
        print(f"   {edges[i]:6.1f} - {edges[i+1]:6.1f}: {bar} ({hist[i]})")

print("\n💡 INTERPRETACJA:")
print("-"*80)
print("1. Bootstrap symuluje rozkład aplikantów 2025 → rozkład możliwych progów")
print(f"2. Próg 2025 jest NIEPEWNY: median={threshold_median:.1f}, 95% CI=[{threshold_ci_lower:.1f}, {threshold_ci_upper:.1f}]")
print("3. P(przyjęcie) uwzględnia tę niepewność progu!")
print("4. Im dalej od mediany progu, tym większa pewność (węższe CI)")
print("="*80)

# 11. Zapisz wyniki
results_df = pd.DataFrame({
    'points': test_scores,
    'p_accept_median': CI_median,
    'ci_lower': CI_lower,
    'ci_upper': CI_upper,
    'ci_width': CI_width
})

results_df.to_csv('/tmp/bootstrap_threshold_uncertainty.csv', index=False)

# Zapisz też próg
threshold_df = pd.DataFrame({
    'bootstrap_iter': range(len(bootstrap_thresholds)),
    'threshold': bootstrap_thresholds
})
threshold_df.to_csv('/tmp/bootstrap_thresholds.csv', index=False)

print(f"\n💾 Zapisano:")
print(f"   - /tmp/bootstrap_threshold_uncertainty.csv")
print(f"   - /tmp/bootstrap_thresholds.csv")
print("\n✅ ZAKOŃCZONO!")
print("="*80)

