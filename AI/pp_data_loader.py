"""
Politechnika Poznańska Data Loader.

Ładuje dane z 3 arkuszy Excel i tworzy apps_hist_df, apps_agg_df, program_rules.
"""

import pandas as pd
import numpy as np


def load_politechnika_poznanska():
    """
    Wczytuje dane Politechniki Poznańskiej i tworzy apps_hist_df + apps_agg_df.
    
    Returns:
        tuple: (apps_hist_df, apps_agg_df, program_rules_dict)
    """
    base_path = '../university_data/Polibudy/Politechnika Poznańska/'
    
    print("   Wczytuję arkusze Excel...")
    # === 1. Wczytaj 3 arkusze ===
    df_kierunki = pd.read_excel(base_path + 'Załącznik.xlsx', sheet_name='Kierunki')
    df_punkty = pd.read_excel(base_path + 'Załącznik.xlsx', sheet_name='Punkty')
    df_progi = pd.read_excel(base_path + 'Załącznik.xlsx', sheet_name='Progi')
    
    print(f"   - Kierunki: {len(df_kierunki)} programów")
    print(f"   - Punkty: {len(df_punkty)} aplikacji")
    print(f"   - Progi: {len(df_progi)} kierunków")
    
    # === 2. Merge: Punkty + Kierunki ===
    apps_hist_df = df_punkty.merge(
        df_kierunki[['ID', 'Kierunek', 'Algorytm', 'Limit przyjęć', 
                     'Forma studiów', 'Rok akademicki']], 
        on='ID', 
        how='left'
    )
    
    # === 3. Merge: + Progi (na podstawie nazwy kierunku) ===
    apps_hist_df = apps_hist_df.merge(
        df_progi[['Kierunek', '2023/2024 min liczba pkt.']], 
        on='Kierunek', 
        how='left'
    )
    
    # === 4. Rename i dodaj kolumny ===
    apps_hist_df = apps_hist_df.rename(columns={
        'ID': 'program_id',
        'Punkty': 's_points',
        'Limit przyjęć': 'seat_limit',
        '2023/2024 min liczba pkt.': 'prev_cutoff',
        'Forma studiów': 'study_mode',
        'Rok akademicki': 'year_label'
    })
    
    # Ekstrakcja roku z "2023/2024" → 2023 (pierwszy rok = rok zdawania matury)
    # Rekrutacja 2023/2024 = maturzyści z 2023
    # Rekrutacja 2024/2025 = maturzyści z 2024
    apps_hist_df['year'] = apps_hist_df['year_label'].str.split('/').str[0].astype(int)
    
    # Dodaj kolumny wymagane przez pipeline
    apps_hist_df['applied'] = 1  # Wszyscy w danych to aplikanci
    apps_hist_df['university'] = 'Politechnika Poznańska'
    
    # Konwertuj study_mode na lowercase dla spójności
    apps_hist_df['study_mode'] = apps_hist_df['study_mode'].str.lower()
    
    # === 4b. Obsłuż brakujące prev_cutoff (nowe kierunki) ===
    # Wypełnij NaN medianą (albo można usunąć: apps_hist_df.dropna(subset=['prev_cutoff']))
    median_cutoff = apps_hist_df['prev_cutoff'].median()
    apps_hist_df['prev_cutoff'] = apps_hist_df['prev_cutoff'].fillna(median_cutoff)
    print(f"   ℹ️  Wypełniono brakujące prev_cutoff medianą: {median_cutoff}")
    
    # Oblicz accepted PO wypełnieniu prev_cutoff
    apps_hist_df['accepted'] = (apps_hist_df['s_points'] >= apps_hist_df['prev_cutoff']).astype(int)
    
    # === 5. Zbuduj apps_agg_df (agregaty per program) ===
    apps_agg_df = apps_hist_df.groupby('program_id').agg({
        'applied': 'sum',               # liczba aplikantów
        'seat_limit': 'first',
        'prev_cutoff': 'first',
        'study_mode': 'first',
        'Kierunek': 'first',
        'Algorytm': 'first',
        'university': 'first',
        'year': 'first'
    }).reset_index()
    
    apps_agg_df = apps_agg_df.rename(columns={'applied': 'applicants_count'})
    
    # Dodaj apps_prev_year (brak danych 2022 → NaN)
    apps_agg_df['apps_prev_year'] = np.nan
    
    # Dodaj cohort_size (z CKE)
    apps_agg_df['cohort_size'] = 245966  # Liczba maturzystów 2024
    
    # === 6. Wyciągnij reguły rekrutacyjne (program_rules) ===
    program_rules_dict = {}
    for _, row in df_kierunki.iterrows():
        program_rules_dict[str(row['ID'])] = {
            'name': row['Kierunek'],
            'formula': row['Algorytm'],
            'seat_limit': row['Limit przyjęć']
        }
    
    print(f"   ✅ Zbudowano apps_hist_df: {apps_hist_df.shape}")
    print(f"   ✅ Zbudowano apps_agg_df: {apps_agg_df.shape}")
    print(f"   ✅ Zbudowano program_rules: {len(program_rules_dict)} programów")
    
    return apps_hist_df, apps_agg_df, program_rules_dict


if __name__ == "__main__":
    apps_hist_df, apps_agg_df, rules = load_politechnika_poznanska()
    
    print("\n" + "="*80)
    print("📊 apps_hist_df (historyczne aplikacje)")
    print("="*80)
    print(f"Shape: {apps_hist_df.shape}")
    print(f"Columns: {list(apps_hist_df.columns)}")
    print("\nPierwsze 5 wierszy:")
    print(apps_hist_df.head().to_string())
    
    print("\n" + "="*80)
    print("📊 apps_agg_df (agregaty per program)")
    print("="*80)
    print(f"Shape: {apps_agg_df.shape}")
    print(f"Columns: {list(apps_agg_df.columns)}")
    print("\nPierwsze 5 wierszy:")
    print(apps_agg_df.head().to_string())
    
    print("\n" + "="*80)
    print("📊 program_rules (przykłady)")
    print("="*80)
    print(f"Liczba programów: {len(rules)}")
    print(f"\nPrzykład (ID 3569):")
    import json
    print(json.dumps(rules['3569'], indent=2, ensure_ascii=False))
    
    print("\nRozkład wzorów:")
    formulas = [r['formula'] for r in rules.values()]
    from collections import Counter
    for formula, count in Counter(formulas).most_common(5):
        print(f"   {count:3d}x: {formula}")

