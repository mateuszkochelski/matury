#!/usr/bin/env python3
"""
Tworzy mapowanie program_id (PP data) -> field_of_study_id (backend DB)
poprzez fuzzy matching nazw kierunków.
"""

import json
import re
from difflib import SequenceMatcher

def normalize_name(name):
    """Normalizuj nazwę kierunku do porównania."""
    name = name.lower()
    name = re.sub(r'[/-]', ' ', name)
    name = re.sub(r'\s+', ' ', name)
    name = name.strip()
    # Usuń angielskie wersje w nawiasach/po slashu
    name = re.sub(r'/.*$', '', name)
    return name

def similarity(a, b):
    """Oblicz podobieństwo między dwoma stringami (0-1)."""
    return SequenceMatcher(None, a, b).ratio()

def main():
    # 1. Wczytaj dane PP
    print("📚 Wczytuję dane PP...")
    from pp_data_loader import load_politechnika_poznanska
    _, _, program_rules = load_politechnika_poznanska()
    
    pp_programs = []
    for pid, rule in program_rules.items():
        pp_programs.append({
            'program_id': int(pid),
            'name': rule['name'],
            'name_norm': normalize_name(rule['name'])
        })
    
    print(f"   ✅ {len(pp_programs)} programów z PP")
    
    # 2. Wczytaj field_of_study.json (backend DB)
    print("\n📚 Wczytuję field_of_study.json...")
    with open('../data/field_of_study.json', 'r', encoding='utf-8') as f:
        all_fields = json.load(f)
    
    # Filtruj tylko PP (university_id=33)
    pp_fields = [f for f in all_fields if f.get('university_id') == 33]
    
    for field in pp_fields:
        field['name_norm'] = normalize_name(field['name'])
    
    print(f"   ✅ {len(pp_fields)} kierunków z backend DB")
    
    # 3. Fuzzy matching
    print("\n🔍 Tworzę mapowanie (fuzzy matching)...")
    mapping = {}
    unmatched = []
    
    for pp_prog in pp_programs:
        best_match = None
        best_score = 0
        
        for field in pp_fields:
            score = similarity(pp_prog['name_norm'], field['name_norm'])
            if score > best_score:
                best_score = score
                best_match = field
        
        if best_score >= 0.5:  # Threshold dla dopasowania
            mapping[pp_prog['program_id']] = {
                'field_of_study_id': best_match['id'],
                'pp_name': pp_prog['name'],
                'db_name': best_match['name'],
                'similarity': round(best_score, 3)
            }
            status = "✅" if best_score >= 0.8 else "⚠️"
            print(f"   {status} [{best_score:.2f}] {pp_prog['program_id']} -> {best_match['id']}: {pp_prog['name']} ≈ {best_match['name']}")
        else:
            unmatched.append(pp_prog)
            print(f"   ❌ [{best_score:.2f}] {pp_prog['program_id']}: {pp_prog['name']} (no good match)")
    
    # 4. Zapisz mapowanie
    output_file = 'pp_field_mapping.json'
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(mapping, f, indent=2, ensure_ascii=False)
    
    print(f"\n✅ Mapowanie zapisane do: {output_file}")
    print(f"   📊 Dopasowano: {len(mapping)}/{len(pp_programs)} ({100*len(mapping)/len(pp_programs):.1f}%)")
    print(f"   📊 Brak dopasowania: {len(unmatched)}")
    
    if unmatched:
        print(f"\n⚠️  Programy bez dopasowania:")
        for prog in unmatched[:10]:
            print(f"      - {prog['program_id']}: {prog['name']}")
        if len(unmatched) > 10:
            print(f"      ... i {len(unmatched)-10} więcej")

if __name__ == '__main__':
    main()

