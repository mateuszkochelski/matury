"""
CKE Data Loader - preprocessing surowych danych maturalnych CKE.

Przekształca dane z formatu CKE (PD1-PD6) na format exam_df wymagany przez pipeline_1.py
"""

import pandas as pd
import numpy as np


def load_cke_to_exam_df(filepath):
    """
    Przekształca surowe dane CKE na format exam_df wymagany przez pipeline.
    
    Args:
        filepath: ścieżka do egzamin_maturalny_anonimowe_dane_2024.xlsx
    
    Returns:
        DataFrame z kolumnami:
        - year, polish_basic, math_basic, math_ext, phys_ext, chem_ext, 
          bio_ext, info_ext, geog_ext, eng_ext, ger_ext, fre_ext, ...
    """
    print(f"   Wczytuję {filepath}...")
    df_raw = pd.read_excel(filepath)
    print(f"   Wczytano {len(df_raw)} wierszy")
    
    # Inicjalizuj exam_df
    exam_df = pd.DataFrame()
    
    # === PRZEDMIOTY OBOWIĄZKOWE ===
    # Polski podstawa (zawsze MPOP)
    exam_df['polish_basic'] = df_raw['wynik_PO_pisemny']
    
    # Matematyka podstawa (zawsze MMAP na poziomie podstawowym)
    exam_df['math_basic'] = df_raw['wynik_MA']
    
    # Język obcy podstawa (pisemny) - używamy jako JO w wzorach
    exam_df['foreign_basic'] = df_raw['wynik_JO_pisemny']
    
    # === PRZEDMIOTY ROZSZERZONE (z PD1-PD6) ===
    
    # Mapa: kod CKE → nazwa kolumny
    subject_map = {
        'MMAP': 'math_ext',          # Matematyka rozszerzona
        'MFAP': 'phys_ext',          # Fizyka
        'MCHP': 'chem_ext',          # Chemia
        'MBIP': 'bio_ext',           # Biologia
        'MINP': 'info_ext',          # Informatyka
        'MGEP': 'geog_ext',          # Geografia
        'MHIP': 'hist_ext',          # Historia
        'MHSP': 'hist_art_ext',      # Historia sztuki
        'MJAP': 'eng_ext',           # Angielski rozszerzony
        'MJNP': 'ger_ext',           # Niemiecki rozszerzony
        'MJFP': 'fre_ext',           # Francuski rozszerzony
        'MJHP': 'spa_ext',           # Hiszpański rozszerzony
        'MJRP': 'rus_ext',           # Rosyjski rozszerzony
        'MWOP': 'wos_ext',           # WOS rozszerzony
        'MPOP': 'polish_ext',        # Polski rozszerzony
        'MFIP': 'phil_ext',          # Filozofia
    }
    
    # Inicjalizuj kolumny rozszerzone (NaN)
    for col_name in subject_map.values():
        exam_df[col_name] = np.nan
    
    # Przeszukaj PD1-PD6 i wypełnij odpowiednie kolumny
    print("   Parsowanie przedmiotów dodatkowych (PD1-PD6)...")
    for i in range(1, 7):
        kod_col = f'kod_PD{i}'
        wynik_col = f'wynik_PD{i}'
        
        for kod_cke, col_name in subject_map.items():
            mask = df_raw[kod_col] == kod_cke
            # Jeśli ktoś ma przedmiot, nadpisz (bierzemy pierwszą wartość)
            exam_df.loc[mask & exam_df[col_name].isna(), col_name] = df_raw.loc[mask, wynik_col]
    
    # Zamień NaN na 0 (brak przedmiotu = 0 punktów w rekrutacji)
    for col in exam_df.columns:
        exam_df[col] = exam_df[col].fillna(0)
    
    # Dodaj kolumnę year na końcu (default 2024)
    exam_df['year'] = 2024
    
    print(f"   ✅ Przetworzono na exam_df: {exam_df.shape}")
    
    return exam_df


if __name__ == "__main__":
    # Test
    filepath = '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    exam_df = load_cke_to_exam_df(filepath)
    
    print("\n" + "="*80)
    print("📊 EXAM_DF - Wynik preprocessingu CKE")
    print("="*80)
    print(f"Shape: {exam_df.shape}")
    print(f"\nColumns: {list(exam_df.columns)}")
    print("\nPierwsze 5 wierszy:")
    print(exam_df.head())
    
    print("\n\nStatystyki (wybrane kolumny):")
    print(exam_df[['polish_basic', 'math_basic', 'math_ext', 'phys_ext', 'bio_ext', 'eng_ext']].describe())
    
    # Sprawdź % maturzystów z rozszerzeniem
    print("\n\n📈 % maturzystów z rozszerzeniem:")
    for col in ['math_ext', 'phys_ext', 'chem_ext', 'bio_ext', 'info_ext', 'geog_ext', 'eng_ext']:
        pct = (exam_df[col] > 0).sum() / len(exam_df) * 100
        print(f"   {col:15s}: {pct:5.1f}%")

