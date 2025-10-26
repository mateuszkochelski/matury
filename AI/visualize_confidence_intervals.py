"""
Wizualizacja Bootstrap Confidence Intervals z niepewnością progu.
"""

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

print("\n" + "="*80)
print("📊 WIZUALIZACJA: Bootstrap Confidence Intervals + Niepewność Progu")
print("="*80)

# Wczytaj wyniki
print("\n📂 Wczytywanie wyników bootstrapu...")
results_df = pd.read_csv('/tmp/bootstrap_threshold_uncertainty.csv')
thresholds_df = pd.read_csv('/tmp/bootstrap_thresholds.csv')

print(f"   ✅ Predykcje: {len(results_df)} punktów")
print(f"   ✅ Bootstrap progów: {len(thresholds_df)} iteracji")
print(f"   Range: {results_df['points'].min():.0f} - {results_df['points'].max():.0f}")

# Statystyki progów
threshold_median = thresholds_df['threshold'].median()
threshold_mean = thresholds_df['threshold'].mean()
threshold_ci_lower = thresholds_df['threshold'].quantile(0.025)
threshold_ci_upper = thresholds_df['threshold'].quantile(0.975)
threshold_std = thresholds_df['threshold'].std()

print(f"\n📊 Statystyki progu 2025:")
print(f"   Median: {threshold_median:.1f}")
print(f"   Mean: {threshold_mean:.1f}")
print(f"   Std: {threshold_std:.1f}")
print(f"   95% CI: [{threshold_ci_lower:.1f}, {threshold_ci_upper:.1f}]")

# Stary próg dla odniesienia
threshold_2024 = 926.5

# 1. Główny wykres: P(accept) z CI + niepewność progu
print("\n📈 Tworzenie wykresu...")

fig, axes = plt.subplots(3, 1, figsize=(14, 14))

# Subplot 1: P(accept) z CI
ax1 = axes[0]

# Median predykcja
ax1.plot(results_df['points'], results_df['p_accept_median'] * 100, 
         'b-', linewidth=2, label='P(przyjęcie) - median')

# Confidence interval (filled area)
ax1.fill_between(results_df['points'], 
                  results_df['ci_lower'] * 100,
                  results_df['ci_upper'] * 100,
                  alpha=0.3, color='blue', label='95% CI (bootstrap)')

# Linie poziome dla referencji
ax1.axhline(y=50, color='gray', linestyle='--', alpha=0.3, linewidth=1)
ax1.axhline(y=80, color='green', linestyle='--', alpha=0.3, linewidth=1)
ax1.axhline(y=95, color='darkgreen', linestyle='--', alpha=0.3, linewidth=1)

# Linie pionowe dla progów
ax1.axvline(x=threshold_median, color='red', linestyle='-', linewidth=2.5, alpha=0.8, 
            label=f'Próg 2025 (median): {threshold_median:.0f}')
ax1.axvline(x=threshold_ci_lower, color='red', linestyle='--', linewidth=1.5, alpha=0.6,
            label=f'Próg 2025 (95% CI): [{threshold_ci_lower:.0f}, {threshold_ci_upper:.0f}]')
ax1.axvline(x=threshold_ci_upper, color='red', linestyle='--', linewidth=1.5, alpha=0.6)
ax1.axvline(x=threshold_2024, color='orange', linestyle=':', linewidth=2, alpha=0.7, 
            label=f'Próg 2024: {threshold_2024:.0f}')

ax1.set_xlabel('Punkty rekrutacyjne', fontsize=13, fontweight='bold')
ax1.set_ylabel('Prawdopodobieństwo przyjęcia (%)', fontsize=13, fontweight='bold')
ax1.set_title('Bootstrap Confidence Intervals - Prawdopodobieństwo przyjęcia na AI', fontsize=15, fontweight='bold')
ax1.legend(loc='upper left', fontsize=10, framealpha=0.9)
ax1.grid(True, alpha=0.3)
ax1.set_ylim([-5, 105])

# Dodaj tekst z kluczowymi statystykami
stats_text = f'Limit miejsc: 55\nAplikanci 2024: 630\nPróg 2024: {threshold_2024:.1f}'
ax1.text(0.98, 0.02, stats_text, 
         transform=ax1.transAxes,
         ha='right', va='bottom',
         fontsize=9,
         bbox=dict(boxstyle='round,pad=0.5', facecolor='white', alpha=0.8, edgecolor='gray'))

# Annotacje dla kluczowych punktów
key_points = [
    (threshold_median - 50, "Mało prawdopodobne"),
    (threshold_median, "Próg 2025\n(NIEPEWNY!)"),
    (threshold_median + 50, "Bardzo pewne")
]

for x, label in key_points:
    idx = np.argmin(np.abs(results_df['points'].values - x))
    y = results_df['p_accept_median'].iloc[idx] * 100
    
    if x == threshold_median:
        ax1.annotate(label, 
                     xy=(x, y), 
                     xytext=(x - 30, y + 15),
                     fontsize=10,
                     fontweight='bold',
                     color='red',
                     arrowprops=dict(arrowstyle='->', color='red', lw=1.5))
    elif y < 10:
        ax1.annotate(label, 
                     xy=(x, y), 
                     xytext=(x, 20),
                     fontsize=9,
                     alpha=0.7,
                     arrowprops=dict(arrowstyle='->', color='gray', lw=1))
    else:
        ax1.annotate(label, 
                     xy=(x, y), 
                     xytext=(x, y - 15),
                     fontsize=9,
                     alpha=0.7,
                     arrowprops=dict(arrowstyle='->', color='gray', lw=1))

# Subplot 2: Szerokość CI (uncertainty)
ax2 = axes[1]

ax2.plot(results_df['points'], results_df['ci_width'] * 100, 
         'r-', linewidth=2, label='Szerokość CI (niepewność)')
ax2.fill_between(results_df['points'], 0, results_df['ci_width'] * 100,
                  alpha=0.3, color='red')

# Linie pionowe dla progów
ax2.axvline(x=threshold_median, color='red', linestyle='-', linewidth=2.5, alpha=0.8, 
            label=f'Próg 2025 (median): {threshold_median:.0f}')
ax2.axvline(x=threshold_ci_lower, color='red', linestyle='--', linewidth=1.5, alpha=0.6)
ax2.axvline(x=threshold_ci_upper, color='red', linestyle='--', linewidth=1.5, alpha=0.6)

ax2.set_xlabel('Punkty rekrutacyjne', fontsize=13, fontweight='bold')
ax2.set_ylabel('Szerokość CI (%)', fontsize=13, fontweight='bold')
ax2.set_title('Niepewność predykcji (szerokość 95% CI)', fontsize=15, fontweight='bold')
ax2.legend(loc='upper right', fontsize=10, framealpha=0.9)
ax2.grid(True, alpha=0.3)

# Annotacja dla max uncertainty
max_unc_idx = results_df['ci_width'].idxmax()
max_unc_x = results_df.loc[max_unc_idx, 'points']
max_unc_y = results_df.loc[max_unc_idx, 'ci_width'] * 100

ax2.annotate(f'Max niepewność\n{max_unc_y:.1f}%', 
             xy=(max_unc_x, max_unc_y), 
             xytext=(max_unc_x + 20, max_unc_y + 10),
             fontsize=10,
             fontweight='bold',
             color='darkred',
             arrowprops=dict(arrowstyle='->', color='darkred', lw=1.5))

# Subplot 3: Histogram progów 2025
ax3 = axes[2]

# Histogram z mniejszą liczbą binów i lepszym zakresem
ax3.hist(thresholds_df['threshold'], bins=20, color='coral', alpha=0.7, edgecolor='black', linewidth=1.2)

# Linie dla statystyk
ax3.axvline(x=threshold_median, color='darkred', linestyle='-', linewidth=3, 
            label=f'Median: {threshold_median:.1f} pkt', zorder=10)
ax3.axvline(x=threshold_ci_lower, color='darkred', linestyle='--', linewidth=2, alpha=0.7,
            label=f'95% CI: [{threshold_ci_lower:.1f}, {threshold_ci_upper:.1f}]', zorder=10)
ax3.axvline(x=threshold_ci_upper, color='darkred', linestyle='--', linewidth=2, alpha=0.7, zorder=10)

# Linia dla progu 2024
ax3.axvline(x=threshold_2024, color='orange', linestyle=':', linewidth=2.5,
            label=f'Próg 2024: {threshold_2024:.1f} pkt', zorder=10)

# Lepszy zakres osi X (skupienie na istotnym obszarze)
x_min = max(threshold_ci_lower - 20, thresholds_df['threshold'].min() - 5)
x_max = min(threshold_ci_upper + 20, thresholds_df['threshold'].max() + 5)
ax3.set_xlim([x_min, x_max])

ax3.set_xlabel('Przewidywany próg 2025 (punkty)', fontsize=13, fontweight='bold')
ax3.set_ylabel('Liczba iteracji bootstrapu', fontsize=13, fontweight='bold')
ax3.set_title('Rozkład przewidywanych progów 2025 (N=1000 bootstrap)', fontsize=14, fontweight='bold')
ax3.legend(loc='upper left', fontsize=11, framealpha=0.9)
ax3.grid(True, alpha=0.3, axis='y')

# Annotacja - lepsze umiejscowienie
y_max = ax3.get_ylim()[1]
ax3.text(threshold_median, y_max * 0.95, 
         f'Niepewność: ±{threshold_std:.1f} pkt (1σ)\nZakres 95% CI: {threshold_ci_upper - threshold_ci_lower:.1f} pkt',
         ha='center', va='top',
         fontsize=11,
         fontweight='bold',
         bbox=dict(boxstyle='round,pad=0.8', facecolor='yellow', alpha=0.7, edgecolor='black', linewidth=1.5))

plt.tight_layout()
plt.savefig('/tmp/bootstrap_confidence_intervals.png', dpi=150, bbox_inches='tight')
print(f"   ✅ Zapisano wykres: /tmp/bootstrap_confidence_intervals.png")

# 2. Tabela z kluczowymi punktami
print("\n📊 Kluczowe punkty:")
print("-"*80)

key_scores = [
    threshold_median - 50,
    threshold_median - 20,
    threshold_median - 10,
    threshold_median,
    threshold_median + 10,
    threshold_median + 20,
    threshold_median + 50,
]

print(f"{'Punkty':>8} | {'P(accept)':>10} | {'CI Width':>10} | {'Interpretacja'}")
print("-"*80)

for s in key_scores:
    idx = np.argmin(np.abs(results_df['points'].values - s))
    row = results_df.iloc[idx]
    
    p = row['p_accept_median'] * 100
    ci_w = row['ci_width'] * 100
    ci_l = row['ci_lower'] * 100
    ci_u = row['ci_upper'] * 100
    
    if ci_w < 10:
        interp = "✅ Pewne (wąskie CI)"
    elif ci_w < 30:
        interp = "⚡ Umiarkowana niepewność"
    elif ci_w < 60:
        interp = "⚠️ Duża niepewność"
    else:
        interp = "❌ Bardzo niepewne!"
    
    print(f"{row['points']:8.0f} | {p:9.1f}% | {ci_w:9.1f}% | {interp}")
    print(f"{'':>8} | CI: [{ci_l:5.1f}%, {ci_u:5.1f}%]")

print("="*80)

# 3. Statystyki globalne
print("\n📊 STATYSTYKI GLOBALNE:")
print("-"*80)

mean_ci_width = results_df['ci_width'].mean() * 100
median_ci_width = results_df['ci_width'].median() * 100
max_ci_width = results_df['ci_width'].max() * 100

# Punkty z wąskim CI (< 10%)
narrow_ci_count = (results_df['ci_width'] < 0.1).sum()
narrow_ci_pct = narrow_ci_count / len(results_df) * 100

# Punkty z szerokim CI (> 50%)
wide_ci_count = (results_df['ci_width'] > 0.5).sum()
wide_ci_pct = wide_ci_count / len(results_df) * 100

print(f"Szerokość CI:")
print(f"  Mean: {mean_ci_width:.1f}%")
print(f"  Median: {median_ci_width:.1f}%")
print(f"  Max: {max_ci_width:.1f}%")
print(f"\nPunkty z wąskim CI (<10%): {narrow_ci_count}/{len(results_df)} ({narrow_ci_pct:.0f}%)")
print(f"Punkty z szerokim CI (>50%): {wide_ci_count}/{len(results_df)} ({wide_ci_pct:.0f}%)")

print("\n✅ WIZUALIZACJA ZAKOŃCZONA!")
print("="*80)
print(f"\n📁 Pliki:")
print(f"   - Wykres: /tmp/bootstrap_confidence_intervals.png")
print(f"   - Dane: /tmp/bootstrap_results.csv")
print("="*80)

