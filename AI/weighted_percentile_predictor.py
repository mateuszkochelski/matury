"""
Weighted Composite Percentile - używa wag ze wzoru rekrutacyjnego!

Zamiast percentyla punktów końcowych, używamy weighted average percentyli przedmiotów.
To uwzględnia że Fizyka (waga 2.0) >> Polski (waga 0.5)!
"""

import pandas as pd
import numpy as np
import json


def load_formula_weights(field_of_study_id):
    """
    Wczytuje wagi przedmiotów z formulas.json dla danego kierunku.
    
    Returns:
        Dict: {
            'polish_basic': 0.5,
            'math_basic': 2.5,
            'math_ext': 2.5,
            'phys_ext': 2.0,  # z BEST_OF_GROUPS
            ...
        }
    """
    with open('../data/field_of_study.json', 'r', encoding='utf-8') as f:
        fields = json.load(f)
    
    # Znajdź kierunek
    field = None
    for f in fields:
        if f['id'] == field_of_study_id:
            field = f
            break
    
    if field is None:
        raise ValueError(f"Nie znaleziono field_of_study_id={field_of_study_id}")
    
    # Parsuj formułę
    formula = field.get('formula', {})
    weights = {}
    
    # SPECIFIC_SUBJECT
    for component in formula.get('components', []):
        if component.get('type') == 'SPECIFIC_SUBJECT':
            subject_key = component.get('subject', '').lower()
            level = component.get('level', 'BASIC').lower()
            coef = component.get('coefficient', 1.0)
            
            # Mapuj na nasze kolumny
            subj_name = subject_key.replace('_language', '')
            col_name = f"{subj_name}_{level}"
            
            weights[col_name] = coef
        
        elif component.get('type') == 'BEST_OF_GROUPS':
            # Dla każdego przedmiotu w grupie przypisz tę samą wagę
            coef = component.get('coefficient', 1.0)
            for group in component.get('groups', []):
                for option in group.get('options', []):
                    subject_key = option.get('subject', '').lower()
                    level = option.get('level', 'BASIC').lower()
                    
                    subj_name = subject_key
                    col_name = f"{subj_name}_{level}"
                    
                    # Przypisz wagę (jeśli już jest, weź max)
                    if col_name in weights:
                        weights[col_name] = max(weights[col_name], coef)
                    else:
                        weights[col_name] = coef
        
        elif component.get('type') == 'LANGUAGE_GROUP':
            # Polski/obcy
            coef = component.get('coefficient', 1.0)
            for option in component.get('options', []):
                subject_key = option.get('subject', '').lower()
                level = option.get('level', 'BASIC').lower()
                
                subj_name = subject_key.replace('_language', '')
                col_name = f"{subj_name}_{level}"
                
                weights[col_name] = coef
    
    return weights


def compute_weighted_composite_percentile(row, weights):
    """
    Oblicza weighted average percentyli przedmiotów.
    
    Args:
        row: Series z kolumnami *_pct (percentyle) i raw scores
        weights: Dict {'polish_basic': 0.5, 'math_ext': 2.5, ...}
    
    Returns:
        float: Weighted composite percentile (0-1)
    """
    total_weight = 0.0
    weighted_sum = 0.0
    
    for subject, weight in weights.items():
        pct_col = subject + '_pct'
        
        if pct_col not in row.index:
            continue
        
        pct_val = row[pct_col]
        
        # Pomijaj 0 (nie zdawał tego przedmiotu)
        if pct_val > 0:
            weighted_sum += pct_val * weight
            total_weight += weight
    
    if total_weight == 0:
        return 0.0
    
    return weighted_sum / total_weight


def add_weighted_composite_percentile(training_df, field_of_study_id):
    """
    Dodaje kolumnę 'composite_pct' do training_df.
    
    Args:
        training_df: DataFrame z percentylami (*_pct)
        field_of_study_id: ID kierunku (dla wag)
    
    Returns:
        training_df z nową kolumną 'composite_pct'
    """
    print(f"\n📊 Obliczanie weighted composite percentile...")
    print(f"   Kierunek: {field_of_study_id}")
    
    # Wczytaj wagi
    weights = load_formula_weights(field_of_study_id)
    print(f"   📋 Wagi przedmiotów:")
    for subj, w in sorted(weights.items(), key=lambda x: -x[1]):
        print(f"      {subj:20s}: {w:.2f}")
    
    # Oblicz composite percentile dla każdego wiersza
    training_df['composite_pct'] = training_df.apply(
        lambda row: compute_weighted_composite_percentile(row, weights),
        axis=1
    )
    
    print(f"\n   ✅ Composite percentile obliczony!")
    print(f"   📊 Statystyki (applied=1):")
    valid = training_df[training_df['applied']==1]['composite_pct']
    valid = valid[valid > 0]
    if len(valid) > 0:
        print(f"      Min: {valid.min()*100:.1f}th")
        print(f"      Median: {valid.median()*100:.1f}th")
        print(f"      Mean: {valid.mean()*100:.1f}th")
        print(f"      Max: {valid.max()*100:.1f}th")
    
    return training_df


if __name__ == "__main__":
    # Test
    import pickle
    
    print("\n🧪 TEST: Weighted Composite Percentile")
    
    # Wczytaj training_df
    print("\n📂 Wczytywanie training_df...")
    with open('/tmp/training_data_pp.pkl', 'rb') as f:
        training_df = pickle.load(f)
    
    print(f"   ✅ {len(training_df)} wierszy")
    
    # Wybierz pierwszy program z training_df
    test_program_id = training_df['program_id'].iloc[0]
    print(f"   📊 Test program_id: {test_program_id}")
    
    # Mapuj na field_of_study_id
    from pp_field_mapping import pp_to_field_mapping
    field_id = pp_to_field_mapping.get(int(test_program_id))
    
    if field_id is None:
        print(f"   ⚠️  Brak mapowania dla program_id={test_program_id}")
        exit(1)
    
    # Oblicz weighted composite
    training_df_subset = training_df[training_df['program_id'] == test_program_id].copy()
    training_df_subset = add_weighted_composite_percentile(training_df_subset, field_id)
    
    print("\n✅ Test zakończony!")
    print(f"\nPierwsze 5 wierszy (applied=1):")
    cols_to_show = ['s_points', 'polish_basic_pct', 'math_ext_pct', 'composite_pct']
    print(training_df_subset[training_df_subset['applied']==1][cols_to_show].head())

