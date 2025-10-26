"""
Test predykcji dla różnych wartości punktów, szczególnie wokół progu.
"""

import pickle
import pandas as pd
import numpy as np

print("\n" + "="*80)
print("🧪 TEST: Predykcje wokół progu")
print("="*80)

# Wczytaj training_df
print("\n📂 Wczytywanie training_df...")
with open('/tmp/training_data_pp.pkl', 'rb') as f:
    training_df = pickle.load(f)

print(f"   ✅ {len(training_df):,} wierszy")
print(f"   ✅ {training_df['applied'].sum():,} aplikantów (applied=1)")

# Wybierz jeden program do testu
programs = training_df['program_id'].value_counts()
print(f"\n📊 Dostępne programy: {len(programs)}")

# Wybierz program z dużą liczbą aplikantów i wysokim progiem
program_stats = training_df[training_df['applied']==1].groupby('program_id').agg({
    's_points': ['count', 'mean', 'std'],
    'prev_cutoff': 'first',
    'seat_limit': 'first'
}).reset_index()
program_stats.columns = ['program_id', 'applicants', 'mean_points', 'std_points', 'prev_cutoff', 'seat_limit']
program_stats = program_stats[program_stats['prev_cutoff'] > 700]  # Wysoki próg
program_stats = program_stats.sort_values('prev_cutoff', ascending=False)

print("\n🎯 Programy z wysokim progiem (>700):")
print(program_stats.head(10))

# Wybierz pierwszy
test_program_id = int(program_stats.iloc[0]['program_id'])
test_cutoff = float(program_stats.iloc[0]['prev_cutoff'])
test_seat_limit = int(program_stats.iloc[0]['seat_limit'])

print(f"\n🎓 Testowy program: ID={test_program_id}")
print(f"   Próg: {test_cutoff:.1f}")
print(f"   Limit miejsc: {test_seat_limit}")

# Pobierz dane tego programu
program_data = training_df[training_df['program_id'] == test_program_id].copy()
print(f"   Dane: {len(program_data):,} wierszy ({program_data['applied'].sum()} aplikantów)")

# Statystyki aplikantów
applicants = program_data[program_data['applied']==1]
print(f"\n📊 Statystyki aplikantów:")
print(f"   Min punktów: {applicants['s_points'].min():.1f}")
print(f"   25%: {applicants['s_points'].quantile(0.25):.1f}")
print(f"   Median: {applicants['s_points'].median():.1f}")
print(f"   75%: {applicants['s_points'].quantile(0.75):.1f}")
print(f"   Max: {applicants['s_points'].max():.1f}")
print(f"   Próg: {test_cutoff:.1f}")

# Wylicz "gęstość" aplikantów wokół progu
print(f"\n🔍 Rozkład aplikantów wokół progu {test_cutoff:.0f}:")
for delta in [-100, -50, -20, -10, 0, 10, 20, 50, 100]:
    lower = test_cutoff + delta - 10
    upper = test_cutoff + delta + 10
    count = ((applicants['s_points'] >= lower) & (applicants['s_points'] < upper)).sum()
    pct = count / len(applicants) * 100
    print(f"   {lower:.0f}-{upper:.0f}: {count:3d} aplikantów ({pct:5.1f}%)")

# Sprawdź rozkład wszystkich kandydatów (nie tylko aplikantów)
all_candidates = program_data.copy()
print(f"\n📊 Rozkład WSZYSTKICH kandydatów (applied=0 + applied=1):")
print(f"   Percentyle punktów:")
for pct in [10, 25, 50, 75, 90, 95, 99]:
    val = all_candidates['s_points'].quantile(pct/100)
    print(f"   {pct}th: {val:.1f}")

# Sprawdź F_app empiryczne (CDF aplikantów)
applicants_sorted = np.sort(applicants['s_points'].values)
print(f"\n📈 F_app (empiryczny CDF aplikantów):")
test_points = [
    test_cutoff - 100,
    test_cutoff - 50,
    test_cutoff - 20,
    test_cutoff - 10,
    test_cutoff,
    test_cutoff + 10,
    test_cutoff + 20,
    test_cutoff + 50,
    test_cutoff + 100,
]

for s in test_points:
    count_below = (applicants_sorted < s).sum()
    f_app = count_below / len(applicants_sorted)
    print(f"   F_app({s:5.0f}) = {f_app:.3f} ({f_app*100:5.1f}% aplikantów poniżej)")

print("\n" + "="*80)
print("💡 INTERPRETACJA:")
print("="*80)
print(f"1. Próg ({test_cutoff:.0f} pkt) to punkt odcięcia z poprzedniego roku")
print(f"2. F_app(próg) pokazuje jaki % aplikantów miał MNIEJ niż próg")
print(f"3. Im więcej aplikantów powyżej progu, tym bardziej konkurencyjny program")
print(f"4. Dla predykcji 2025: używamy F_app + Poisson(rivali < K miejsc)")
print("="*80)

