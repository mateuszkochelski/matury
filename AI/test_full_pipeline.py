"""
Test pełnego pipeline z realnymi danymi.

WAŻNE: Multiprocessing wymaga if __name__ == '__main__' na macOS/Windows!
"""

from cke_synthetic import load_multi_year_cke
from pp_data_loader import load_politechnika_poznanska
from training_data_builder import prepare_training_data


def main():
    print("\n" + "="*80)
    print("🧪 TEST PEŁNEGO PIPELINE - KROK 1")
    print("="*80)

    # 1. Wczytaj dane CKE (2023 syntetyczne + 2024 realne)
    print("\n📂 Wczytywanie danych CKE...")
    exam_df = load_multi_year_cke(
        '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx',
        include_synthetic_2023=True
    )

    print(f"   ✅ CKE: {len(exam_df)} maturzystów")
    print(f"   📊 Rozkład lat: {exam_df['year'].value_counts().to_dict()}")

    # 2. Wczytaj dane PP
    print("\n📂 Wczytywanie danych Politechniki Poznańskiej...")
    apps_hist_df, apps_agg_df, program_rules = load_politechnika_poznanska()

    print(f"   ✅ PP: {len(apps_hist_df)} aplikacji")
    print(f"   📊 Rozkład lat: {apps_hist_df['year'].value_counts().to_dict()}")
    print(f"   📊 Liczba programów: {apps_hist_df['program_id'].nunique()}")

    # 3. Przygotuj dane treningowe (WEKTORYZACJA + opcjonalnie PARALLEL)
    print("\n🔧 Przygotowanie danych treningowych (z percentylami)...")
    print("   💡 Tip: Zmień n_jobs=1 na n_jobs=None dla paralelizacji (szybsze na wielu rdzeniach)")
    training_df = prepare_training_data(
        exam_df,
        apps_hist_df,
        program_rules,
        undersample_ratio=10,
        filter_no_prev_cutoff=True,
        use_java_api=True,  # Użyj Python PP calculator (szybki!)
        n_jobs=1  # Sequential (bezpieczne). Zmień na None dla parallel (wymaga if __name__ == '__main__')
    )

    print("\n✅ KROK 1 ZAKOŃCZONY POMYŚLNIE!")
    print(f"Dataset shape: {training_df.shape}")
    print(f"Kolumny: {training_df.columns.tolist()}")
    print(f"\nPierwsze 5 wierszy applied=1:")
    print(training_df[training_df['applied']==1].head())
    print(f"\nStatystyki s_points:")
    print(training_df.groupby('applied')['s_points'].describe())

    # Sprawdź percentyle
    pct_cols = [col for col in training_df.columns if col.endswith('_pct')]
    if len(pct_cols) > 0:
        print(f"\n📊 Kolumny percentylowe: {len(pct_cols)}")
        print(f"Przykłady: {pct_cols[:5]}")
        print(f"\nStatystyki percentyli (aplikanci applied=1):")
        for col in pct_cols[:3]:  # Pierwsze 3
            valid = training_df[training_df['applied']==1][col].dropna()
            valid = valid[valid > 0]
            if len(valid) > 0:
                print(f"  {col}: median={valid.median()*100:.1f}th, mean={valid.mean()*100:.1f}th")
    else:
        print("\n⚠️  Brak kolumn percentylowych!")

    # Zapisz do cache (pickle)
    import pickle
    with open('/tmp/training_data_pp.pkl', 'wb') as f:
        pickle.dump(training_df, f)
    print("\n💾 Zapisano do: /tmp/training_data_pp.pkl")


if __name__ == '__main__':
    main()

