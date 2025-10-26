"""
KROK 2: Trening modelu propensity na przygotowanych danych.
"""

import pickle
import pandas as pd
import numpy as np
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.calibration import CalibratedClassifierCV
from sklearn.metrics import roc_auc_score, brier_score_loss, classification_report

print("\n" + "="*80)
print("🧪 TEST PEŁNEGO PIPELINE - KROK 2: TRENING MODELU")
print("="*80)

# 1. Wczytaj przygotowane dane
print("\n📂 Wczytywanie danych treningowych...")
with open('/tmp/training_data_pp.pkl', 'rb') as f:
    training_df = pickle.load(f)

print(f"   ✅ Wczytano: {len(training_df):,} wierszy")
print(f"   📊 applied=1: {(training_df['applied']==1).sum():,} ({(training_df['applied']==1).mean()*100:.1f}%)")
print(f"   📊 applied=0: {(training_df['applied']==0).sum():,} ({(training_df['applied']==0).mean()*100:.1f}%)")

# 2. Przygotuj features i target
print("\n🔧 Przygotowanie features...")

# Features dla modelu
feature_cols = ['s_points', 'seat_limit', 'prev_cutoff']
cat_cols = ['program_id', 'study_mode', 'year']

X = training_df[feature_cols + cat_cols].copy()
y = training_df['applied'].astype(int)

# 3. Zbuduj preprocessing pipeline
print("\n🏗️  Budowanie pipeline...")

preprocess = ColumnTransformer(
    transformers=[
        ('num', StandardScaler(), feature_cols),
        ('cat', OneHotEncoder(handle_unknown='ignore', sparse_output=False), cat_cols)
    ]
)

base_logit = LogisticRegression(
    penalty='l2',
    C=1.0,
    solver='lbfgs',
    max_iter=1000,
    class_weight='balanced',  # Dodatkowe balansowanie
    random_state=42,
    verbose=1
)

pipe_logit = Pipeline([
    ('prep', preprocess),
    ('clf', base_logit)
])

# 4. Split train/test (random, bo program_id się zmienia między latami)
print("\n📊 Split: 80% train, 20% test (random)...")

from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

print(f"   Train: {len(X_train):,} wierszy (applied=1: {y_train.sum():,})")
print(f"   Test:  {len(X_test):,} wierszy (applied=1: {y_test.sum():,})")

# 5. Trenuj model
print("\n🚀 Trenowanie modelu...")
print("   (może potrwać ~1-2 minuty dla 13M wierszy)")

pipe_logit.fit(X_train, y_train)

print("   ✅ Model wytrenowany!")

# 6. Ewaluacja
print("\n📈 Ewaluacja modelu...")

# Predykcje
y_train_pred = pipe_logit.predict_proba(X_train)[:, 1]
y_test_pred = pipe_logit.predict_proba(X_test)[:, 1]

# Metryki
train_auc = roc_auc_score(y_train, y_train_pred)
test_auc = roc_auc_score(y_test, y_test_pred)

train_brier = brier_score_loss(y_train, y_train_pred)
test_brier = brier_score_loss(y_test, y_test_pred)

print(f"\n   📊 TRAIN:")
print(f"      AUC:   {train_auc:.4f}")
print(f"      Brier: {train_brier:.4f}")

print(f"\n   📊 TEST (holdout 20%):")
print(f"      AUC:   {test_auc:.4f}")
print(f"      Brier: {test_brier:.4f}")

# 7. Kalibracja (opcjonalnie)
print("\n🎯 Kalibracja modelu (Isotonic)...")

calib = CalibratedClassifierCV(
    pipe_logit,
    cv='prefit',
    method='isotonic'
)

# Kalibrujemy na części test (w produkcji użyjemy osobnego zbioru)
calib.fit(X_test, y_test)

print("   ✅ Model skalibrowany!")

# Test kalibracji
y_test_calib = calib.predict_proba(X_test)[:, 1]
test_calib_auc = roc_auc_score(y_test, y_test_calib)
test_calib_brier = brier_score_loss(y_test, y_test_calib)

print(f"\n   📊 TEST (po kalibracji):")
print(f"      AUC:   {test_calib_auc:.4f}")
print(f"      Brier: {test_calib_brier:.4f}")

# 8. Przykładowe predykcje
print("\n🔬 Przykładowe predykcje:")
print("   Prawdopodobieństwo aplikacji dla różnych punktów:")

example_program_id = '3569'  # budownictwo zrównoważone
example_data = pd.DataFrame({
    's_points': [300, 400, 500, 600, 700, 800],
    'seat_limit': [30] * 6,
    'prev_cutoff': [450.5] * 6,
    'program_id': [example_program_id] * 6,
    'study_mode': ['stacjonarne'] * 6,
    'year': [2024] * 6
})

p_apply = calib.predict_proba(example_data)[:, 1]

print(f"\n   Program: {example_program_id} (budownictwo zrównoważone)")
print("   " + "-"*50)
print(f"   {'Punkty':>10} | {'P(aplikacja)':>15}")
print("   " + "-"*50)
for s, p in zip(example_data['s_points'], p_apply):
    print(f"   {s:10.0f} | {p*100:14.2f}%")

# 9. Zapisz model
print("\n💾 Zapisywanie modelu...")
with open('/tmp/propensity_model_pp.pkl', 'wb') as f:
    pickle.dump(calib, f)

print("   ✅ Zapisano do: /tmp/propensity_model_pp.pkl")

print("\n" + "="*80)
print("✅ KROK 2 ZAKOŃCZONY POMYŚLNIE!")
print("="*80)

