"""
KROK 3: SUBJECT-SPECIFIC PERCENTILES.

Zamiast percentyla końcowych punktów, używamy wektora percentyli
dla każdego przedmiotu we wzorze rekrutacyjnym!

To jest bardziej robust do zmian trudności poszczególnych egzaminów.
"""

import pickle
import pandas as pd
import numpy as np
from scipy import interpolate
from scipy.stats import poisson

print("\n" + "="*80)
print("🧪 SUBJECT-SPECIFIC PERCENTILE APPROACH")
print("="*80)

# Setup
from cke_loader import load_cke_to_exam_df
from cke_synthetic import load_multi_year_cke
from pp_data_loader import load_politechnika_poznanska
from pp_point_calculator import calculate_points_for_dataframe
from pp_field_mapping import pp_to_field_mapping

# 1. Wczytaj dane
print("\n📂 KROK 1: Wczytywanie danych...")
exam_df_multi = load_multi_year_cke(
    '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx',
    include_synthetic_2023=True
)
print(f"   ✅ CKE: {len(exam_df_multi)} maturzystów ({exam_df_multi['year'].value_counts().to_dict()})")

apps_hist_df, apps_agg_df, _ = load_politechnika_poznanska()

# Wybierz program z danymi w 2+ latach
program_names_2023 = set(apps_agg_df[apps_agg_df['year']==2023]['Kierunek'].values)
program_names_2024 = set(apps_agg_df[apps_agg_df['year']==2024]['Kierunek'].values)
common_programs = program_names_2023 & program_names_2024

chosen_name = list(common_programs)[0]  # Pierwszy wspólny
test_program = apps_agg_df[apps_agg_df['Kierunek'] == chosen_name].iloc[0]

program_name = test_program['Kierunek']
program_id = str(test_program['program_id'])
field_id = pp_to_field_mapping.get(int(program_id))

print(f"   📊 Program: {program_name}")
print(f"   ID: {program_id}, field_id: {field_id}")

# 2. Zidentyfikuj przedmioty we wzorze rekrutacyjnym
print("\n📋 KROK 2: Identyfikacja przedmiotów we wzorze...")

# Dla PP większość kierunków używa:
# - Polski basic (może być converted z extended)
# - Język obcy basic (może być converted z extended)
# - Matematyka basic (może być converted z extended)
# - Matematyka extended
# - Najlepszy X z {fizyka, chemia, biologia, informatyka, geografia} basic+extended

# Dla uproszczenia, użyjemy kluczowych przedmiotów:
SUBJECTS_TO_TRACK = {
    'polish_basic': 'Polski podstawa',
    'math_basic': 'Matematyka podstawa',
    'math_ext': 'Matematyka rozszerzenie',
    # Dla "najlepszy X" - będziemy śledzić wszystkie opcje
    'phys_ext': 'Fizyka rozszerzenie',
    'chem_ext': 'Chemia rozszerzenie',
    'bio_ext': 'Biologia rozszerzenie',
    'info_ext': 'Informatyka rozszerzenie',
    'geog_ext': 'Geografia rozszerzenie',
}

print(f"   Śledzimy {len(SUBJECTS_TO_TRACK)} przedmiotów:")
for subj, label in SUBJECTS_TO_TRACK.items():
    print(f"      - {label} ({subj})")

# 3. MULTI-YEAR PROCESSING z percentylami przedmiotowymi
print("\n" + "="*80)
print("🔄 KROK 3: MULTI-YEAR PROCESSING (subject percentiles)")
print("="*80)

program_years = sorted(apps_hist_df[apps_hist_df['Kierunek'] == program_name]['year'].unique())
print(f"   Lata: {program_years}")

all_applicants_subject_percentiles = []  # Lista dict'ów z percentylami

for year in program_years:
    print(f"\n📅 Przetwarzanie roku {year}...")
    
    # 3a. CKE dla tego roku
    exam_df_year = exam_df_multi[exam_df_multi['year'] == year].copy()
    print(f"   📊 CKE {year}: {len(exam_df_year)} maturzystów")
    
    # 3b. Zbuduj CDF dla KAŻDEGO przedmiotu
    print(f"   📈 Budowanie CDF per przedmiot...")
    subject_percentile_funcs = {}
    
    for subj_col, subj_label in SUBJECTS_TO_TRACK.items():
        # Sortuj wyniki z tego przedmiotu (tylko non-zero)
        subject_scores = exam_df_year[subj_col].values
        subject_scores = subject_scores[subject_scores > 0]  # Pomijamy 0 (nie zdawali)
        
        if len(subject_scores) < 100:
            print(f"      ⚠️  {subj_label}: tylko {len(subject_scores)} wyników, pomijam")
            continue
        
        subject_scores_sorted = np.sort(subject_scores)
        n = len(subject_scores_sorted)
        percentiles = np.arange(1, n+1) / n
        
        # Funkcja: score → percentyl
        score_to_pct = interpolate.interp1d(
            subject_scores_sorted,
            percentiles,
            kind='linear',
            bounds_error=False,
            fill_value=(0.0, 1.0)  # 0 → 0th, max → 100th
        )
        
        subject_percentile_funcs[subj_col] = score_to_pct
        
        # Test
        median_score = np.median(subject_scores_sorted)
        median_pct = score_to_pct(median_score)
        print(f"      ✅ {subj_label}: median={median_score:.1f} → {median_pct*100:.1f}th pct")
    
    # 3c. Aplikanci dla tego roku
    program_apps_year = apps_hist_df[
        (apps_hist_df['Kierunek'] == program_name) &
        (apps_hist_df['year'] == year)
    ]
    
    if len(program_apps_year) == 0:
        print(f"   ⚠️  Brak aplikantów, pomijam rok {year}")
        continue
    
    print(f"   👥 Aplikanci {year}: {len(program_apps_year)}")
    
    # 3d. Oblicz punkty końcowe (do późniejszego użycia)
    exam_df_year['s_points'] = calculate_points_for_dataframe(
        exam_df_year,
        field_of_study_id=field_id,
        show_progress=False
    )
    
    # 3e. Przekształć aplikantów na WEKTOR percentyli przedmiotowych
    print(f"   🔄 Przekształcanie aplikantów na percentyle przedmiotowe...")
    
    for idx, app_row in program_apps_year.iterrows():
        applicant_percentiles = {}
        applicant_percentiles['year'] = year
        applicant_percentiles['s_points'] = app_row['s_points']  # Punkty końcowe
        
        # Dla każdego przedmiotu: wynik → percentyl
        for subj_col in SUBJECTS_TO_TRACK.keys():
            if subj_col not in subject_percentile_funcs:
                applicant_percentiles[subj_col + '_pct'] = None
                continue
            
            score = app_row.get(subj_col, 0)
            if score > 0:
                pct = subject_percentile_funcs[subj_col](score)
                applicant_percentiles[subj_col + '_pct'] = float(pct)
            else:
                applicant_percentiles[subj_col + '_pct'] = 0.0  # Nie zdawał
        
        all_applicants_subject_percentiles.append(applicant_percentiles)
    
    print(f"   ✅ Rok {year} przetworzony: {len(program_apps_year)} aplikantów")

# 4. Analiza połączonych danych
print("\n" + "="*80)
print(f"📊 KROK 4: ANALIZA POŁĄCZONYCH DANYCH ({len(all_applicants_subject_percentiles)} aplikantów)")
print("="*80)

# Przekształć na DataFrame
apps_pct_df = pd.DataFrame(all_applicants_subject_percentiles)

print(f"   Lata: {apps_pct_df['year'].value_counts().to_dict()}")
print(f"\n   📊 Statystyki percentyli aplikantów:")

for subj_col, subj_label in SUBJECTS_TO_TRACK.items():
    pct_col = subj_col + '_pct'
    if pct_col not in apps_pct_df.columns:
        continue
    
    valid_pcts = apps_pct_df[pct_col].dropna()
    valid_pcts = valid_pcts[valid_pcts > 0]  # Pomijamy 0 (nie zdawali)
    
    if len(valid_pcts) > 0:
        print(f"      {subj_label}:")
        print(f"         N={len(valid_pcts)}, median={valid_pcts.median()*100:.1f}th, "
              f"mean={valid_pcts.mean()*100:.1f}th")

# 5. Agregacja do composite score percentile
print("\n📈 KROK 5: Agregacja do composite percentile...")

# Dla uproszczenia: używamy percentyla PUNKTÓW KOŃCOWYCH jako proxy
# (W pełnej implementacji: użyj weighted average percentyli lub ML model)

# Najpierw: oblicz percentyle punktów końcowych w całej populacji
all_years_cke = exam_df_multi.copy()
all_years_cke['s_points'] = 0.0

for year in program_years:
    mask = all_years_cke['year'] == year
    exam_df_year_temp = all_years_cke[mask].copy()
    exam_df_year_temp['s_points'] = calculate_points_for_dataframe(
        exam_df_year_temp,
        field_of_study_id=field_id,
        show_progress=False
    )
    all_years_cke.loc[mask, 's_points'] = exam_df_year_temp['s_points'].values

# Percentyl punktów w połączonej populacji
all_points = all_years_cke['s_points'].values
all_points_sorted = np.sort(all_points)
n_all = len(all_points_sorted)
all_percentiles = np.arange(1, n_all+1) / n_all

points_to_percentile_pooled = interpolate.interp1d(
    all_points_sorted,
    all_percentiles,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

# Aplikanci → percentyle punktów (w pooled distribution)
apps_pct_df['s_points_percentile'] = apps_pct_df['s_points'].apply(
    lambda x: points_to_percentile_pooled(x)
)

print(f"   ✅ Composite percentile (z punktów końcowych):")
print(f"      Median: {apps_pct_df['s_points_percentile'].median()*100:.1f}th")
print(f"      Mean: {apps_pct_df['s_points_percentile'].mean()*100:.1f}th")

# 6. Zbuduj F_app w przestrzeni composite percentile
print("\n📈 KROK 6: Budowanie F_app (subject-aware via composite pct)...")

applicants_composite_pct = apps_pct_df['s_points_percentile'].values

bins_pct = np.linspace(0, 1, 201)
hist_pct, edges_pct = np.histogram(applicants_composite_pct, bins=bins_pct, density=False)
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

print(f"   ✅ F_app zbudowany")
print(f"      F_app(90th pct) = {F_app_percentile(0.9):.3f}")
print(f"      F_app(95th pct) = {F_app_percentile(0.95):.3f}")
print(f"      F_app(99th pct) = {F_app_percentile(0.99):.3f}")

# 7. Predykcja dla 2025
print("\n" + "="*80)
print("🔮 KROK 7: PREDYKCJA 2025")
print("="*80)

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

# Przekształć na percentyle (w dystrybucji 2025)
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
f_app_values = F_app_percentile(sample_percentiles)
sample_points_2025 = percentile_to_points_2025(sample_percentiles)

F_app_2025 = interpolate.interp1d(
    sample_points_2025,
    f_app_values,
    kind='linear',
    bounds_error=False,
    fill_value=(0.0, 1.0)
)

print(f"   ✅ F_app_2025 gotowy!")

# 8. Oblicz szanse
print("\n🎯 KROK 8: Obliczanie szans przyjęcia...")

last_year = max(program_years)
program_info = apps_agg_df[
    (apps_agg_df['Kierunek'] == program_name) &
    (apps_agg_df['year'] == last_year)
].iloc[0]

seat_limit = int(program_info['seat_limit'])
prev_cutoff = float(program_info['prev_cutoff'])
applicants_count = int(program_info['applicants_count'])

test_scores = [400, 500, 600, 700, 750, 800, 850, 880, 900, 910, 920, 930, 940, 950]

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

# 9. Wyniki
print("\n" + "="*80)
print("🎓 WYNIKI PREDYKCJI 2025 (SUBJECT-SPECIFIC PERCENTILES)")
print("="*80)
print(f"Program: {program_name}")
print(f"Próg {last_year}: {prev_cutoff:.1f}, Limit: {seat_limit}")
print(f"Aplikanci: {applicants_count}")
print(f"Training: {len(all_applicants_subject_percentiles)} aplikantów, {len(program_years)} lata")
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

# 10. Insights o przedmiotach
print("\n💡 INSIGHTS: Subject-specific approach")
print("-"*80)
print("Śledzimy percentyle dla kluczowych przedmiotów:")
for subj_col, subj_label in SUBJECTS_TO_TRACK.items():
    pct_col = subj_col + '_pct'
    if pct_col not in apps_pct_df.columns:
        continue
    valid = apps_pct_df[pct_col].dropna()
    valid = valid[valid > 0]
    if len(valid) > 10:
        print(f"  {subj_label}: median aplikanta = {valid.median()*100:.1f}th percentile")

print("\n✅ To pozwala na precyzyjniejsze przewidywania przy zmianach trudności!")
print("="*80)

