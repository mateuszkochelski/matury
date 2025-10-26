"""
Generator syntetycznych danych CKE dla roku 2023.

Tworzy realistyczne dane na podstawie CKE 2024 poprzez:
- Przeskalowanie wyników (symulacja trudności egzaminów)
- Lekką zmianę rozkładu przedmiotów rozszerzonych
- Podobną liczebność kohorty

UWAGA: To tylko placeholder do rozwoju pipeline!
Po otrzymaniu prawdziwych danych CKE 2023, ta funkcja NIE będzie używana.
"""

import pandas as pd
import numpy as np


def generate_synthetic_cke_2023(exam_df_2024):
    """
    Generuje syntetyczne dane CKE dla roku 2023 na podstawie danych 2024.
    
    Args:
        exam_df_2024: DataFrame z cke_loader.py dla roku 2024
    
    Returns:
        DataFrame w tym samym formacie, ale z year=2023
    
    Metoda:
    - Przeskalowanie wyników: multiply by N(1.0, 0.05) per subject
    - Drobne zmiany w % zdających rozszerzenia
    - Podobna liczba maturzystów (±5%)
    """
    np.random.seed(42)  # Dla reprodukowalności
    
    # === 1. Sample podobnej liczby osób ===
    n_2024 = len(exam_df_2024)
    n_2023 = int(n_2024 * np.random.uniform(0.95, 1.05))
    
    # Losuj osoby (z powtórzeniami jeśli n_2023 > n_2024)
    if n_2023 <= n_2024:
        indices = np.random.choice(n_2024, size=n_2023, replace=False)
    else:
        indices = np.random.choice(n_2024, size=n_2023, replace=True)
    
    exam_df_2023 = exam_df_2024.iloc[indices].copy().reset_index(drop=True)
    
    # === 2. Przeskaluj wyniki (symulacja zmian trudności) ===
    # Każdy przedmiot dostaje własny czynnik skalowania
    
    subject_cols = [col for col in exam_df_2023.columns if col != 'year']
    
    for col in subject_cols:
        # Losuj czynnik skalowania: N(1.0, 0.05) → średnio bez zmian, ±5% std
        scale_factor = np.random.normal(1.0, 0.05)
        scale_factor = np.clip(scale_factor, 0.85, 1.15)  # Nie za ekstremalne
        
        # Skaluj wyniki
        exam_df_2023[col] = exam_df_2023[col] * scale_factor
        
        # Clip do [0, 100] (wyniki procentowe)
        exam_df_2023[col] = exam_df_2023[col].clip(0, 100)
    
    # === 3. Zmień rok ===
    exam_df_2023['year'] = 2023
    
    # === 4. Lekkie zmiany w rozkładzie rozszerzeń (opcjonalnie) ===
    # Symulujemy, że np. 2% mniej osób zdawało fizykę rozszerzoną w 2023
    # (niektórzy dostają phys_ext=0)
    
    for ext_col in ['phys_ext', 'chem_ext', 'bio_ext', 'info_ext', 'geog_ext']:
        if ext_col in exam_df_2023.columns:
            # 2-5% osób traci rozszerzenie (zmiana popularności)
            drop_rate = np.random.uniform(0.00, 0.05)
            mask = (exam_df_2023[ext_col] > 0) & (np.random.rand(len(exam_df_2023)) < drop_rate)
            exam_df_2023.loc[mask, ext_col] = 0
    
    print(f"   ✅ Wygenerowano syntetyczne CKE 2023: {len(exam_df_2023)} maturzystów")
    print(f"   ℹ️  Skalowanie wyników: {scale_factor:.3f} (ostatni przedmiot)")
    
    return exam_df_2023


def load_multi_year_cke(filepath_2024, include_synthetic_2023=True):
    """
    Wczytuje dane CKE z wielu lat (realne + syntetyczne).
    
    Args:
        filepath_2024: ścieżka do pliku CKE 2024
        include_synthetic_2023: czy generować syntetyczne dane 2023
    
    Returns:
        DataFrame z kolumnami jak exam_df + year (2023, 2024, ...)
    """
    from cke_loader import load_cke_to_exam_df
    
    print("   📂 Wczytuję dane CKE...")
    
    # Wczytaj 2024 (realne)
    exam_df_2024 = load_cke_to_exam_df(filepath_2024)
    
    if include_synthetic_2023:
        print("   🔬 Generuję syntetyczne dane CKE 2023...")
        exam_df_2023 = generate_synthetic_cke_2023(exam_df_2024)
        
        # Połącz
        exam_df_all = pd.concat([exam_df_2023, exam_df_2024], ignore_index=True)
        print(f"   ✅ Łącznie: {len(exam_df_all)} maturzystów (2 lata)")
    else:
        exam_df_all = exam_df_2024
    
    return exam_df_all


if __name__ == "__main__":
    # Test
    from cke_loader import load_cke_to_exam_df
    
    print("\n" + "="*80)
    print("🧪 TEST: Generowanie syntetycznych danych CKE 2023")
    print("="*80)
    
    filepath = '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    exam_df_2024 = load_cke_to_exam_df(filepath)
    
    print(f"\n📊 Oryginalny CKE 2024: {len(exam_df_2024)} maturzystów")
    print(f"   Średnia math_basic: {exam_df_2024['math_basic'].mean():.1f}")
    print(f"   % z math_ext: {(exam_df_2024['math_ext'] > 0).mean()*100:.1f}%")
    
    exam_df_2023 = generate_synthetic_cke_2023(exam_df_2024)
    
    print(f"\n🔬 Syntetyczny CKE 2023: {len(exam_df_2023)} maturzystów")
    print(f"   Średnia math_basic: {exam_df_2023['math_basic'].mean():.1f}")
    print(f"   % z math_ext: {(exam_df_2023['math_ext'] > 0).mean()*100:.1f}%")
    
    print("\n📈 Porównanie rozkładów:")
    print(f"   2024 polish_basic: mean={exam_df_2024['polish_basic'].mean():.1f}, std={exam_df_2024['polish_basic'].std():.1f}")
    print(f"   2023 polish_basic: mean={exam_df_2023['polish_basic'].mean():.1f}, std={exam_df_2023['polish_basic'].std():.1f}")
    
    print("\n✅ Syntetyczne dane wygenerowane pomyślnie!")
    print("="*80)


