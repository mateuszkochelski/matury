"""
Integracja Python z Java API kalkulatora punktów.

Używa prawdziwego kalkulatora z backendu (uwzględnia wszystkie bonusy, konwersje, etc.)
"""

import requests
import pandas as pd
import numpy as np
import json
import os
from typing import Dict, List
from concurrent.futures import ThreadPoolExecutor, as_completed
from tqdm import tqdm


class JavaCalculatorAPI:
    """Klient API Java kalkulatora punktów."""
    
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.api_endpoint = f"{base_url}/api/recruitment-calculator/calculate"
        
        # Wczytaj mapowanie program_id (PP) -> field_of_study_id (backend DB)
        mapping_path = os.path.join(os.path.dirname(__file__), 'pp_field_mapping.json')
        if os.path.exists(mapping_path):
            with open(mapping_path, 'r', encoding='utf-8') as f:
                raw_mapping = json.load(f)
                # Konwertuj do prostego dict: program_id -> field_of_study_id
                self.pp_to_field_mapping = {
                    int(k): v['field_of_study_id'] 
                    for k, v in raw_mapping.items()
                }
        else:
            print(f"⚠️  Warning: Mapping file not found: {mapping_path}")
            self.pp_to_field_mapping = {}
    
    def calculate_points(
        self,
        university_id: str,
        field_of_study_id: str,
        exam_results: List[Dict]
    ) -> Dict:
        """
        Wywołuje Java API kalkulatora.
        
        Args:
            university_id: ID uczelni (np. 'politechnika-poznanska')
            field_of_study_id: ID kierunku (np. 'engineering-x', '3852')
            exam_results: Lista dict z kluczami: subjectCode, level, score
        
        Returns:
            Dict z kluczami: totalPoints, breakdown, universityId, fieldOfStudyId
        
        Example:
            >>> api = JavaCalculatorAPI()
            >>> result = api.calculate_points(
            ...     university_id='politechnika-poznanska',
            ...     field_of_study_id='3852',
            ...     exam_results=[
            ...         {'subjectCode': 'polish_language', 'level': 'BASIC', 'score': 80},
            ...         {'subjectCode': 'mathematics', 'level': 'EXTENDED', 'score': 90}
            ...     ]
            ... )
            >>> print(result['totalPoints'])
            870.0
        """
        payload = {
            "universityId": university_id,
            "fieldOfStudyId": field_of_study_id,
            "examResults": exam_results
        }
        
        try:
            response = requests.post(
                self.api_endpoint,
                json=payload,
                headers={"Content-Type": "application/json"},
                timeout=5
            )
            response.raise_for_status()
            return response.json()
        
        except requests.exceptions.RequestException as e:
            raise RuntimeError(f"Java API error: {e}")
    
    def get_field_of_study_id(self, program_id: int) -> str:
        """
        Konwertuje program_id (z danych PP) na field_of_study_id (backend DB).
        
        Args:
            program_id: ID programu z PP (np. 3852)
        
        Returns:
            str: field_of_study_id dla API (np. '3005')
        
        Raises:
            ValueError: jeśli brak mapowania
        """
        if program_id not in self.pp_to_field_mapping:
            raise ValueError(f"No mapping found for program_id={program_id}")
        return str(self.pp_to_field_mapping[program_id])
    
    def calculate_points_from_cke_row(
        self,
        exam_row: pd.Series,
        university_id: str = 'politechnika-poznanska',
        field_of_study_id: str = None,
        program_id: int = None
    ) -> float:
        """
        Oblicza punkty dla wiersza z exam_df (CKE).
        
        Args:
            exam_row: pd.Series z kolumnami z cke_loader.py
            university_id: ID uczelni
            field_of_study_id: ID kierunku w backend DB (opcjonalne jeśli podano program_id)
            program_id: ID programu z PP (opcjonalne, będzie zmapowane na field_of_study_id)
        
        Returns:
            float: Punkty rekrutacyjne
        
        Example:
            >>> api = JavaCalculatorAPI()
            >>> # Wariant 1: podaj program_id (zalecane dla PP)
            >>> exam_df['s_points'] = exam_df.apply(
            ...     lambda r: api.calculate_points_from_cke_row(r, program_id=3852),
            ...     axis=1
            ... )
            >>> # Wariant 2: podaj bezpośrednio field_of_study_id
            >>> points = api.calculate_points_from_cke_row(row, field_of_study_id='3005')
        """
        # Konwertuj program_id -> field_of_study_id jeśli podano
        if program_id is not None:
            try:
                field_of_study_id = self.get_field_of_study_id(program_id)
            except ValueError as e:
                print(f"Warning: {e}, returning 0")
                return 0.0
        
        if field_of_study_id is None:
            raise ValueError("Podaj program_id lub field_of_study_id")
        # Mapowanie CKE → Java API subject codes
        exam_results = []
        
        # Polski podstawa
        if exam_row.get('polish_basic', 0) > 0:
            exam_results.append({
                'subjectCode': 'polish_language',
                'level': 'BASIC',
                'score': float(exam_row['polish_basic'])
            })
        
        # Polski rozszerzony
        if exam_row.get('polish_ext', 0) > 0:
            exam_results.append({
                'subjectCode': 'polish_language',
                'level': 'EXTENDED',
                'score': float(exam_row['polish_ext'])
            })
        
        # Matematyka podstawa
        if exam_row.get('math_basic', 0) > 0:
            exam_results.append({
                'subjectCode': 'mathematics',
                'level': 'BASIC',
                'score': float(exam_row['math_basic'])
            })
        
        # Matematyka rozszerzenie
        if exam_row.get('math_ext', 0) > 0:
            exam_results.append({
                'subjectCode': 'mathematics',
                'level': 'EXTENDED',
                'score': float(exam_row['math_ext'])
            })
        
        # Język obcy podstawa
        if exam_row.get('foreign_basic', 0) > 0:
            # Domyślnie angielski (nie wiemy który to język w CKE)
            exam_results.append({
                'subjectCode': 'english_language',
                'level': 'BASIC',
                'score': float(exam_row['foreign_basic'])
            })
        
        # Przedmioty ścisłe - rozszerzenia
        subject_mapping = {
            'phys_ext': 'physics',
            'chem_ext': 'chemistry',
            'bio_ext': 'biology',
            'info_ext': 'informatics',
            'geog_ext': 'geography',
            'hist_ext': 'history',
            'eng_ext': 'english_language',
            'ger_ext': 'german_language'
        }
        
        for cke_col, java_subject in subject_mapping.items():
            if exam_row.get(cke_col, 0) > 0:
                exam_results.append({
                    'subjectCode': java_subject,
                    'level': 'EXTENDED',
                    'score': float(exam_row[cke_col])
                })
        
        # Wywołaj API
        try:
            result = self.calculate_points(
                university_id=university_id,
                field_of_study_id=field_of_study_id,
                exam_results=exam_results
            )
            return result['totalPoints']
        
        except RuntimeError as e:
            print(f"Warning: API error for row, returning 0: {e}")
            return 0.0
    
    def calculate_points_for_dataframe(
        self,
        exam_df: pd.DataFrame,
        program_id: int,
        university_id: str = 'politechnika-poznanska',
        max_workers: int = 20,
        show_progress: bool = True
    ) -> pd.Series:
        """
        Równolegle oblicza punkty dla całego DataFrame (SZYBKIE!).
        
        Args:
            exam_df: DataFrame z CKE
            program_id: ID programu z PP
            university_id: ID uczelni
            max_workers: Liczba wątków (domyślnie 20)
            show_progress: Czy pokazywać progress bar
        
        Returns:
            pd.Series: Punkty dla każdego wiersza
        
        Example:
            >>> api = JavaCalculatorAPI()
            >>> exam_df['s_points'] = api.calculate_points_for_dataframe(
            ...     exam_df, program_id=3852, max_workers=20
            ... )
        """
        # Reset index żeby mieć integer 0..N-1
        df_reset = exam_df.reset_index(drop=True)
        results = [0.0] * len(df_reset)
        
        def process_row(i_row):
            i, row = i_row
            return i, self.calculate_points_from_cke_row(
                row, 
                university_id=university_id,
                program_id=program_id
            )
        
        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            futures = {
                executor.submit(process_row, (i, row)): i 
                for i, row in df_reset.iterrows()
            }
            
            iterator = as_completed(futures)
            if show_progress:
                iterator = tqdm(
                    iterator, 
                    total=len(futures),
                    desc=f"📊 Calc points (program {program_id})",
                    unit="student"
                )
            
            for future in iterator:
                try:
                    i, points = future.result()
                    results[i] = points
                except Exception as e:
                    # Błędy są już logowane w calculate_points_from_cke_row
                    pass
        
        # Zwróć Series z oryginalnym indexem
        return pd.Series(results, index=exam_df.index)
    
    def health_check(self) -> bool:
        """Sprawdza czy backend działa."""
        try:
            response = requests.get(
                f"{self.base_url}/actuator/health",
                timeout=2
            )
            return response.status_code == 200
        except:
            return False


# =============================================================================
# PRZYKŁAD UŻYCIA
# =============================================================================

if __name__ == "__main__":
    print("\n" + "="*80)
    print("🧪 TEST: Integracja Python ↔ Java API")
    print("="*80)
    
    api = JavaCalculatorAPI()
    
    # 1. Health check
    print("\n1️⃣ Sprawdzam czy backend działa...")
    if api.health_check():
        print("   ✅ Backend is UP")
    else:
        print("   ❌ Backend is DOWN - uruchom: cd backend && ./gradlew bootRun")
        exit(1)
    
    # 2. Test prostego zapytania
    print("\n2️⃣ Test API z przykładowymi wynikami...")
    result = api.calculate_points(
        university_id='politechnika-poznanska',
        field_of_study_id='engineering-x',
        exam_results=[
            {'subjectCode': 'polish_language', 'level': 'BASIC', 'score': 80},
            {'subjectCode': 'english_language', 'level': 'BASIC', 'score': 70},
            {'subjectCode': 'mathematics', 'level': 'BASIC', 'score': 60},
            {'subjectCode': 'mathematics', 'level': 'EXTENDED', 'score': 80},
            {'subjectCode': 'physics', 'level': 'EXTENDED', 'score': 90}
        ]
    )
    
    print(f"   📊 Wynik: {result['totalPoints']} punktów")
    print(f"   📋 Breakdown:")
    for term in result['breakdown']:
        print(f"      - {term['description']}: {term['pointsAwarded']} pkt")
    
    # 3. Test z CKE row
    print("\n3️⃣ Test z wierszem CKE...")
    from cke_loader import load_cke_to_exam_df
    
    exam_df = load_cke_to_exam_df(
        '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    )
    
    # Weź przykładową osobę
    sample_row = exam_df.iloc[100]
    points = api.calculate_points_from_cke_row(sample_row, field_of_study_id='engineering-x')
    
    print(f"   📊 Osoba #{100}:")
    print(f"      PL: {sample_row['polish_basic']:.0f}")
    print(f"      MAT podst: {sample_row['math_basic']:.0f}")
    print(f"      MAT rozsz: {sample_row['math_ext']:.0f}")
    print(f"      FIZ rozsz: {sample_row['phys_ext']:.0f}")
    print(f"      ➡️  Punkty: {points:.1f}")
    
    print("\n" + "="*80)
    print("✅ Integracja działa!")
    print("="*80)

