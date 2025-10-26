"""
KROK 3: MULTI-YEAR TRAINING dla F_app_percentile.

Używamy danych z 2023 + 2024 żeby lepiej oszacować rozkład percentyli aplikantów.
To redukuje wariancję i daje stabilniejsze predykcje!
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson
import matplotlib.pyplot as plt

print("\n" + "="*80)
print("🧪 MULTI-YEAR PERCENTILE-BASED PREDICTION")
print("="*80)

# Setup
from cke_loader import load_cke_to_exam_df
from cke_synthetic import load_multi_year_cke
from pp_data_loader import load_politechnika_poznanska
from pp_point_calculator import calculate_points_for_dataframe
from pp_field_mapping import pp_to_field_mapping

# 1. Wczytaj dane CKE (2023 syntetyczne + 2024 prawdziwe)
print("\n📂 KROK 1: Wczytywanie danych CKE (multi-year)...")
exam_df_multi = load_multi_year_cke(
    '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx',
    include_synthetic_2023=True
)
print(f"   ✅ Łącznie: {len(exam_df_multi)} maturzystów")
print(f"   📊 Rozkład lat: {exam_df_multi['year'].value_counts().to_dict()}")

# 2. Wczytaj PP data
print("\n📂 KROK 2: Wczytywanie danych PP...")
apps_hist_df, apps_agg_df, _ = load_politechnika_poznanska()

# Wybierz program testowy - preferuj programy z danymi w obu latach
programs_available = apps_agg_df[apps_agg_df['year'].isin([2023, 2024])]

# Sprawdź które programy występują w obu latach (po nazwie, bo ID się zmienia!)
program_names_2023 = set(apps_agg_df[apps_agg_df['year']==2023]['Kierunek'].values)
program_names_2024 = set(apps_agg_df[apps_agg_df['year']==2024]['Kierunek'].values)
common_programs = program_names_2023 & program_names_2024

print(f"   📊 Programy w 2023: {len(program_names_2023)}")
print(f"   📊 Programy w 2024: {len(program_names_2024)}")
print(f"   📊 Wspólne (po nazwie): {len(common_programs)}")

if len(common_programs) > 0:
    # Weź pierwszy wspólny program
    chosen_name = list(common_programs)[0]
    test_program = programs_available[programs_available['Kierunek'] == chosen_name].iloc[0]
    print(f"   ✅ Wybrano program występujący w OBU latach")
else:
    print("   ⚠️  Brak programów wspólnych (ID się zmieniają), biorę z 1 roku")
    test_program = programs_available.iloc[0]

program_id = str(test_program['program_id'])
program_name = test_program['Kierunek']
field_id = pp_to_field_mapping.get(int(program_id))

print(f"   📊 Program: {program_name} (ID: {program_id})")
print(f"   field_of_study_id: {field_id}")

# Sprawdź ile lat mamy dla tego programu (PO NAZWIE, bo ID się zmienia!)
program_years = apps_hist_df[apps_hist_df['Kierunek'] == program_name]['year'].unique()
print(f"   📅 Dostępne lata dla tego programu: {sorted(program_years)}")

# 3. MULTI-YEAR PROCESSING: Dla każdego roku osobno
print("\n" + "="*80)
print("🔄 KROK 3: MULTI-YEAR PROCESSING")
print("="*80)

all_applicants_percentiles = []
years_processed = []

for year in sorted(program_years):
    print(f"\n📅 Przetwarzanie roku {year}...")
    
    # 3a. Filtruj CKE dla tego roku
    exam_df_year = exam_df_multi[exam_df_multi['year'] == year].copy()
    print(f"   📊 CKE {year}: {len(exam_df_year)} maturzystów")
    
    if len(exam_df_year) == 0:
        print(f"   ⚠️  Brak danych CKE dla {year}, pomijam")
        continue
    
    # 3b. Oblicz punkty dla CKE tego roku
    print(f"   🧮 Obliczanie punktów...")
    exam_df_year['s_points'] = calculate_points_for_dataframe(
        exam_df_year,
        field_of_study_id=field_id,
        show_progress=False
    )
    print(f"      ✅ Punkty: min={exam_df_year['s_points'].min():.1f}, "
          f"max={exam_df_year['s_points'].max():.1f}, "
          f"mean={exam_df_year['s_points'].mean():.1f}")
    
    # 3c. Zbuduj CDF populacji CKE (punkty → percentile) dla tego roku
    points_sorted = np.sort(exam_df_year['s_points'].values)
    n = len(points_sorted)
    percentiles = np.arange(1, n+1) / n
    
    points_to_percentile = interpolate.interp1d(
        points_sorted,
        percentiles,
        kind='linear',
        bounds_error=False,
        fill_value=(0.0, 1.0)
    )
    
    # 3d. Wczytaj aplikantów dla tego roku (PO NAZWIE, bo ID się zmienia!)
    program_apps_year = apps_hist_df[
        (apps_hist_df['Kierunek'] == program_name) &
        (apps_hist_df['year'] == year)
    ]
    
    if len(program_apps_year) == 0:
        print(f"   ⚠️  Brak aplikantów PP dla {year}, pomijam")
        continue
    
    print(f"   👥 Aplikanci {year}: {len(program_apps_year)}")
    
    # 3e. Przekształć punkty aplikantów na percentyle
    applicants_points = program_apps_year['s_points'].values
    applicants_pct = points_to_percentile(applicants_points)
    
    # Remove NaNs (jeśli są)
    applicants_pct = applicants_pct[~np.isnan(applicants_pct)]
    
    print(f"   📊 Percentile aplikantów {year}:")
    print(f"      Min: {applicants_pct.min()*100:.1f}th")
    print(f"      Median: {np.median(applicants_pct)*100:.1f}th")
    print(f"      Mean: {applicants_pct.mean()*100:.1f}th")
    print(f"      Max: {applicants_pct.max()*100:.1f}th")
    
    # Dodaj do puli
    all_applicants_percentiles.extend(applicants_pct)
    years_processed.append(year)
    print(f"   ✅ Rok {year} przetworzony!")

# 4. Połącz dane z wszystkich lat (POOLING)
print("\n" + "="*80)
print(f"🔗 KROK 4: POOLING danych z {len(years_processed)} lat")
print("="*80)

all_applicants_percentiles = np.array(all_applicants_percentiles)
print(f"   ✅ Łącznie: {len(all_applicants_percentiles)} aplikantów")
print(f"   📅 Lata: {years_processed}")
print(f"   📊 Połączone percentile:")
print(f"      Min: {all_applicants_percentiles.min()*100:.1f}th")
print(f"      25%: {np.percentile(all_applicants_percentiles, 25)*100:.1f}th")
print(f"      Median: {np.median(all_applicants_percentiles)*100:.1f}th")
print(f"      Mean: {all_applicants_percentiles.mean()*100:.1f}th")
print(f"      75%: {np.percentile(all_applicants_percentiles, 75)*100:.1f}th")
print(f"      Max: {all_applicants_percentiles.max()*100:.1f}th")

# 5. Zbuduj F_app_percentile z POŁĄCZONYCH danych
print("\n📈 KROK 5: Budowanie F_app_percentile (multi-year)...")

bins_pct = np.linspace(0, 1, 201)  # 0%, 0.5%, ..., 100%
hist_pct, edges_pct = np.histogram(all_applicants_percentiles, bins=bins_pct, density=False)
centers_pct = 0.5 * (edges_pct[:-1] + edges_pct[1:])

# CDF
pdf_pct = hist_pct / hist_pct.sum()
cdf_pct = np.cumsum(pdf_pct)

F_app_percentile_multiyear = interpolate.interp1d(
    centers_pct,
    cdf_pct,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

print(f"   ✅ F_app_percentile (multi-year) zbudowany!")
print(f"   📊 Statystyki:")
print(f"      F_app(50th pct) = {F_app_percentile_multiyear(0.5):.3f}")
print(f"      F_app(80th pct) = {F_app_percentile_multiyear(0.8):.3f}")
print(f"      F_app(90th pct) = {F_app_percentile_multiyear(0.9):.3f}")
print(f"      F_app(95th pct) = {F_app_percentile_multiyear(0.95):.3f}")
print(f"      F_app(99th pct) = {F_app_percentile_multiyear(0.99):.3f}")

# 6. Generuj predykcję dla 2025 (używając multi-year F_app)
print("\n" + "="*80)
print("🔮 KROK 6: PREDYKCJA dla 2025")
print("="*80)

# Generuj syntetyczne 2025
from cke_synthetic import generate_synthetic_cke_2023
exam_df_2024 = exam_df_multi[exam_df_multi['year'] == 2024].copy()
exam_df_2025 = generate_synthetic_cke_2023(exam_df_2024)
exam_df_2025['year'] = 2025

print(f"   ✅ CKE 2025: {len(exam_df_2025)} maturzystów (syntetyczne)")

# Oblicz punkty dla 2025
exam_df_2025['s_points'] = calculate_points_for_dataframe(
    exam_df_2025,
    field_of_study_id=field_id,
    show_progress=False
)

print(f"   ✅ Punkty 2025: min={exam_df_2025['s_points'].min():.1f}, "
      f"max={exam_df_2025['s_points'].max():.1f}")

# Zbuduj CDF 2025
points_2025_sorted = np.sort(exam_df_2025['s_points'].values)
n_2025 = len(points_2025_sorted)
percentiles_2025 = np.arange(1, n_2025+1) / n_2025

points_to_percentile_2025 = interpolate.interp1d(
    points_2025_sorted,
    percentiles_2025,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

percentile_to_points_2025 = interpolate.interp1d(
    percentiles_2025,
    points_2025_sorted,
    kind='linear',
    bounds_error=False,
    fill_value=(points_2025_sorted[0], points_2025_sorted[-1])
)

# Mapuj F_app_percentile → F_app_points_2025
sample_percentiles = np.linspace(0, 1, 1000)
f_app_values = F_app_percentile_multiyear(sample_percentiles)
sample_points_2025 = percentile_to_points_2025(sample_percentiles)

F_app_2025 = interpolate.interp1d(
    sample_points_2025,
    f_app_values,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

print(f"   ✅ F_app_2025 gotowy!")

# 7. Predykcja dla kandydatów
print("\n🎯 KROK 7: Obliczanie szans przyjęcia...")

# Pobierz dane o programie z ostatniego roku (PO NAZWIE!)
last_year = max(years_processed)
program_info = apps_agg_df[
    (apps_agg_df['Kierunek'] == program_name) &
    (apps_agg_df['year'] == last_year)
].iloc[0]

seat_limit = int(program_info['seat_limit'])
prev_cutoff = float(program_info['prev_cutoff'])
applicants_count = int(program_info['applicants_count'])

print(f"   Program: {program_name}")
print(f"   Rok bazowy: {last_year}")
print(f"   Limit miejsc: {seat_limit}")
print(f"   Próg {last_year}: {prev_cutoff:.1f}")
print(f"   Aplikanci {last_year}: {applicants_count}")

test_scores = [400, 500, 600, 700, 750, 800, 850, 880, 900, 910, 920, 930, 940, 950, 960, 970]

def acceptance_prob_poisson(s, F_app_func, lam, K):
    cdf_val = float(F_app_func(s))
    tail = max(0.0, 1.0 - cdf_val)
    mu = lam * tail
    prob = float(poisson.cdf(K-1, mu))
    return prob

results = []
for candidate_s in test_scores:
    p_accept = acceptance_prob_poisson(
        candidate_s,
        F_app_2025,
        applicants_count,
        seat_limit
    )
    results.append((candidate_s, p_accept))

# 8. Wyświetl wyniki
print("\n" + "="*80)
print("🎓 WYNIKI PREDYKCJI 2025 (MULTI-YEAR)")
print("="*80)
print(f"Program: {program_name}")
print(f"Próg {last_year}: {prev_cutoff:.1f}, Limit: {seat_limit}")
print(f"Aplikanci (założenie): {applicants_count}")
print(f"Dane treningowe: {len(years_processed)} lata ({years_processed})")
print(f"Łącznie aplikantów w treningu: {len(all_applicants_percentiles)}")
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

# 9. Wizualizacja (opcjonalnie)
print("\n📊 KROK 8: Statystyki F_app_percentile...")
print(f"   Dane z {len(years_processed)} lat łącznie")
print(f"   Sample size: {len(all_applicants_percentiles)} aplikantów")
print(f"   Stabilność: {'✅ Dobra (≥2 lata)' if len(years_processed) >= 2 else '⚠️ Słaba (1 rok)'}")

print("\n✅ MULTI-YEAR APPROACH ZAKOŃCZONY!")
print("="*80)

