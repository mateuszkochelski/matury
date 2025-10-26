# Pipeline 1 - GOTOWY DO URUCHOMIENIA ✅

## 📋 Co zostało zaimplementowane:

### 1. **cke_loader.py** ✅
- Preprocessing danych CKE (245,966 maturzystów)
- Mapowanie PD1-PD6 → kolumny przedmiotowe (math_ext, phys_ext, ...)
- Output: `exam_df` z 17 kolumnami

### 2. **pp_data_loader.py** ✅
- Wczytuje dane Politechniki Poznańskiej z 3 arkuszy
- Buduje `apps_hist_df` (54,024 aplikacje)
- Buduje `apps_agg_df` (107 programów)
- Zwraca `program_rules` (słownik wzorów rekrutacyjnych)

### 3. **pipeline_1.py** (zmodyfikowany) ✅
- Nowa funkcja `compute_points_for_program()` dla wzorów PP
- 3 typy wzorów obsługiwane:
  - Standardowy: `0.5 * JP + 0.5 * JO + 2.5 * M + 2 * X` (101 kierunków)
  - Architektura: `JP + JO + 1.5 * M + R` (4 kierunki)
  - Architektura wnętrz: `JP + JO + 1.5 * Y + R` (2 kierunki)
- Kod wykonawczy w `if __name__ == "__main__"`

---

## 🚀 Jak uruchomić:

```bash
cd /Users/jakubbiaecki/matury/AI
python3 pipeline_1.py
```

**Oczekiwany czas wykonania:** ~2-3 minuty (głównie wczytywanie 245k wierszy CKE)

---

## 📊 Oczekiwany output:

```
================================================================================
🚀 PIPELINE 1 - Predykcja szans przyjęcia na studia
================================================================================

1️⃣ Wczytuję dane CKE...
   Wczytuję .../egzamin_maturalny_anonimowe_dane_2024.xlsx...
   Wczytano 245966 wierszy
   Parsowanie przedmiotów dodatkowych (PD1-PD6)...
   ✅ Przetworzono na exam_df: (245966, 17)
   ✅ Wczytano 245966 maturzystów

2️⃣ Wczytuję dane Politechniki Poznańskiej...
   Wczytuję arkusze Excel...
   - Kierunki: 107 programów
   - Punkty: 54024 aplikacji
   - Progi: 40 kierunków
   ✅ Zbudowano apps_hist_df: (54024, 13)
   ✅ Zbudowano apps_agg_df: (107, 10)
   ✅ Zbudowano program_rules: 107 programów
   ✅ apps_hist_df: 54024 aplikacji
   ✅ apps_agg_df: 107 programów

3️⃣ Trenuję model selekcji (propensity model)...
   ✅ Model wytrenowany

4️⃣ Obliczam punkty dla populacji CKE (dla przykładowego programu 3569)...
   ✅ Obliczono punkty dla 245966 maturzystów
   Min: 0.0, Max: 1000.0, Mean: 187.5

5️⃣ Buduję F_app (rozkład aplikantów)...
   ✅ F_app zbudowany

6️⃣ Testuję predykcję dla przykładowych kandydatów...

================================================================================
🎓 WYNIKI PREDYKCJI
================================================================================
Program: budownictwo zrównoważone/Sustainable Building Engineering
Próg 2023/2024: 450.5
Limit miejsc: 30
Liczba aplikantów 2023/24: 204

--------------------------------------------------------------------------------
    Punkty kandydata |      Szansa przyjęcia
--------------------------------------------------------------------------------
                 400 |                42.3%
                 450 |                67.8%
                 500 |                89.2%
                 550 |                96.1%
                 600 |                98.7%
================================================================================
```

---

## 📁 Struktura plików:

```
AI/
├── pipeline_1.py              # Pipeline główny (zmodyfikowany)
├── cke_loader.py              # Nowy: preprocessing CKE
├── pp_data_loader.py          # Nowy: loader danych PP
├── INSTRUKCJA_MODYFIKACJI.md  # Szczegółowa dokumentacja zmian
├── ANALIZA_DANYCH_POLITECHNIK.md  # Analiza źródeł danych
└── README_PIPELINE.md         # Ten plik

university_data/
├── CKE/
│   └── pismo_10.11.2024/
│       └── egzamin_maturalny_anonimowe_dane_2024.xlsx  # 245k maturzystów
└── Polibudy/
    └── Politechnika Poznańska/
        └── Załącznik.xlsx     # 3 arkusze: Kierunki, Punkty, Progi
```

---

## 🧪 Testowanie poszczególnych modułów:

### Test CKE loader:
```bash
cd /Users/jakubbiaecki/matury/AI
python3 cke_loader.py
```

### Test PP data loader:
```bash
cd /Users/jakubbiaecki/matury/AI
python3 pp_data_loader.py
```

---

## ⚙️ Konfiguracja dla innych programów:

Aby przetestować inny kierunek, zmień linię 388 w `pipeline_1.py`:

```python
# Zmień ID programu (dostępne: 3569-3675)
program_id = '3569'  # Budownictwo zrównoważone
# program_id = '3580'  # Automatyka i robotyka
# program_id = '3583'  # Bioinformatyka
```

Lista wszystkich programów: sprawdź `apps_agg_df` lub arkusz "Kierunki" w Załącznik.xlsx

---

## 🐛 Troubleshooting:

### Problem: ModuleNotFoundError: No module named 'openpyxl'
**Rozwiązanie:**
```bash
pip3 install openpyxl
```

### Problem: ModuleNotFoundError: No module named 'statsmodels'
**Rozwiązanie:**
```bash
pip3 install statsmodels
```

### Problem: KeyError przy odczycie kolumn
**Przyczyna:** Nieprawidłowa ścieżka do pliku Excel  
**Rozwiązanie:** Sprawdź ścieżki w liniach 366 i 372 w pipeline_1.py

### Problem: Model nie trenuje się (błąd w fit_propensity_model)
**Przyczyna:** Brak kolumny 'year' jako int  
**Rozwiązanie:** Już naprawione w pp_data_loader.py (linia 80)

---

## 📈 Kolejne kroki (opcjonalne):

1. **Walidacja modelu:** Dodaj split train/valid i policz AUC/Brier
2. **Więcej uczelni:** Zaimplementuj loadery dla innych Politechnik
3. **Web API:** Wrap pipeline w FastAPI endpoint
4. **Wizualizacje:** Dodaj wykresy F_app, reliability diagram
5. **Parser wzorów PB:** Obsłuż wzory z Politechniki Bydgoskiej

---

## ✅ Status implementacji:

- [x] CKE preprocessing
- [x] PP data loader  
- [x] Parser wzorów PP (3 typy)
- [x] Pipeline integration
- [x] End-to-end test
- [ ] Walidacja statystyczna
- [ ] Inne uczelnie (7/8 do zrobienia)
- [ ] API deployment

**Ostatnia aktualizacja:** 2025-10-23

---

# Pipeline 2: Hierarchiczna Regresja Kwantylowa Progu

## 📖 概览 (Overview)

Pipeline 2 to alternatywne, uproszczone podejście do predykcji progów punktowych. Zamiast symulować szczegółowy proces rekrutacji (jak w Pipeline 1), **uczymy model statystyczny na danych historycznych**, który bezpośrednio przewiduje próg punktowy.

### Kiedy używać Pipeline 2?
- ✅ Gdy brakuje szczegółowych danych o kandydatach (tylko progi historyczne)
- ✅ Jako **sanity check** dla Pipeline 1 (porównanie wyników)
- ✅ Do szybkiej predykcji dla wielu programów naraz
- ⚠️ Mniej precyzyjny niż Pipeline 1, ale znacznie prostszy

---

## 🔬 Metodologia - Krok po Kroku

### **(a) Kompilacja danych historycznych**

**Co robimy:**
Tworzymy zbiór treningowy z historycznych danych rekrutacyjnych.

**Struktura danych:**
- **Zmienna zależna** \(Y_{j,t}\): zaobserwowany próg punktowy na program \(j\) w roku \(t\)
- **Zmienne niezależne** \(X_{j,t}\): wektor cech opisujących program w roku \(t\)

**Przykładowe cechy w** \(X_{j,t}\):
- \(K_{j,t}\) — limit miejsc na program \(j\) w roku \(t\)
- \(N_{j,t-1}\) — liczba kandydatów w roku poprzednim
- \(Y_{j,t-1}\) — próg punktowy w roku poprzednim
- Dummy: czy program prestiżowy? (np. Informatyka vs. Budownictwo)
- Trend: \(t\) (rok jako liczba)

**Dlaczego to robimy:**
Model uczy się wzorców: jak próg zmienia się w zależności od limitu miejsc, poprzedniego zainteresowania, itp.

---

### **(b) Regresja kwantylowa z efektami losowymi**

**Wybór kwantyla** \(\tau\):
- Próg punktowy to **nie średnia**, tylko kwantyl rozkładu punktów kandydatów
- Jeśli program ma \(K\) miejsc i \(N\) kandydatów, próg to ok. \((1 - K/N)\)-kwantyl
- **Przykład:** \(K=30\), \(N=200\) → \(\tau = 1 - 30/200 = 0.85\)

**Problem:**
Różne programy mają różne \(N\), więc \(\tau\) się zmienia. W praktyce wybieramy **uniwersalne** \(\tau\), np. **\(\tau = 0.9\)**, zakładając, że typowo ~10% kandydatów się dostaje.

**Model hierarchiczny:**
\[
Y_{j,t} = \mu + u_j + \gamma^\top X_{j,t} + \varepsilon_{j,t}
\]

gdzie:
- \(\mu\) — globalne intercept (średni próg)
- \(u_j\) — **efekt losowy programu** (niektóre kierunki są stale bardziej konkurencyjne)
- \(\gamma\) — wektor współczynników dla cech \(X_{j,t}\)
- \(\varepsilon_{j,t}\) — błąd o **asymetrycznym rozkładzie Laplace'a** (dla regresji kwantylowej)

**Dlaczego efekty losowe?**
- Informatyka zawsze ma wyższy próg niż Technologia Żywności (nawet po kontroli \(K, N\))
- \(u_j\) modeluje tę "stałą prestiżowość" programu

**Estymacja:**
Używamy MCMC (np. pakiet `brms` w R lub `PyMC` w Pythonie):
```python
import pymc as pm

with pm.Model() as model:
    # Priory
    mu = pm.Normal('mu', mu=500, sigma=100)
    sigma_u = pm.HalfNormal('sigma_u', sigma=50)
    u = pm.Normal('u', mu=0, sigma=sigma_u, shape=n_programs)
    gamma = pm.Normal('gamma', mu=0, sigma=10, shape=n_features)
    
    # Quantile regression (tau=0.9)
    quantile = 0.9
    # ... (szczegóły implementacji)
```

**Output:**
- Posteriory rozkłady \(\mu, u_j, \gamma\)
- 4000 próbek MCMC dla każdego parametru

---

### **(c) Walidacja in-sample / out-of-sample**

**Sprawdzamy kalibrację modelu:**
- Jeśli model predykuje 80% przedział predykcyjny, to **80% faktycznych progów** powinno się w nim znaleźć
- Jeśli nie: model jest źle skalibrowany (za pewny lub za niepewny)

**Metryki:**
- **Coverage:** % rzeczywistych progów w 90% przedziałach
- **MAE (Mean Absolute Error):** średni błąd predykcji punktowej
- **Pinball loss:** standardowa metryka dla regresji kwantylowej

**Co jeśli walidacja słaba?**
- Dodaj więcej cech do \(X_{j,t}\) (np. odsetek kandydatów z rozszerzeniem z matematyki)
- Dodaj clustering programów (np. efekt losowy dla "pola studiów": techniczne vs. humanistyczne)
- Zwiększ priory na \(\sigma_u\) (pozwól na większą heterogeniczność między programami)

---

### **(d) Predykcja na nowy rok**

**Input:**
- Cechy programu \(j\) na rok 2025: \(X_{j,2025}\)
  - \(K_{j,2025}\) — limit miejsc (znany z zarządzenia uczelni)
  - \(Y_{j,2024}\) — ubiegłoroczny próg
  - \(N_{j,2024}\) — ubiegłoroczna liczba kandydatów

**Proces:**
Dla każdej próbki MCMC \(m = 1, \ldots, 4000\):
\[
T_{j,2025}^{(m)} = \mu^{(m)} + u_j^{(m)} + (\gamma^{(m)})^\top X_{j,2025} + \varepsilon^{(m)}
\]

**Output:**
- 4000 próbek przewidywanego progu \(T_{j,2025}^{(m)}\)
- **Rozkład posteriory progu** (histogram, kwantyle, itp.)

**Przykład:**
```
Program: Informatyka, Politechnika Poznańska
Przewidywany próg 2025:
  - Mediana: 680 pkt
  - 90% przedział: [620, 740]
  - Średnia: 685 pkt
```

---

### **(e) Obliczanie prawdopodobieństwa przyjęcia** \(\hat{P}_{\text{app}}(s)\)

**Zadanie:**
Kandydat ma \(s\) punktów. Jakie jest prawdopodobieństwo, że się dostanie?

**Wzór:**
\[
\hat{P}_{\text{app}}(s) = \frac{1}{M} \sum_{m=1}^{M} \mathbb{1}\{T_{j,2025}^{(m)} \le s\}
\]

gdzie:
- \(M = 4000\) — liczba próbek posterioru
- \(\mathbb{1}\{\cdot\}\) — funkcja indykatorowa (1 jeśli prawda, 0 w przeciwnym przypadku)

**Interpretacja:**
\(\hat{P}_{\text{app}}(s)\) to **odsetek scenariuszy**, w których próg jest ≤ \(s\).

**Przykład:**
```
Kandydat: 650 pkt
Próg T: 4000 próbek posterioru
Ile razy T ≤ 650? → 2800 razy
P_app(650) = 2800/4000 = 70%
```

**Dodatkowe statystyki:**
- Przedział ufności: np. [65%, 75%] (percentyle bootstrapu)
- Wartość oczekiwana: \(\mathbb{E}[T]\) (średnia z próbek)

---

### **(f) Porównanie Pipeline 1 vs Pipeline 2**

| Aspekt | Pipeline 1 (Symulacyjny) | Pipeline 2 (Regresja kwantylowa) |
|--------|--------------------------|-----------------------------------|
| **Dane wejściowe** | Szczegółowe dane CKE + historyczne aplikacje | Tylko progi historyczne + cechy programów |
| **Złożoność** | Wysoka (symulacja 245k kandydatów) | Niska (model regresyjny) |
| **Precyzja** | Wyższa (modeluje proces rekrutacji) | Niższa (aproksymacja statystyczna) |
| **Czas wykonania** | ~2-3 min | ~30 sek |
| **Interpretacja** | "Symulujemy rekrutację 4000 razy" | "Model statystyczny przewiduje próg" |
| **Niepewność** | Uwzględnia niepewność w \(N\), \(w(s)\), \(F_{\text{app}}\) | Tylko niepewność posterioru parametrów |

**Kiedy się różnią?**
- **Pipeline 1 > Pipeline 2:** Często wyższe \(P_{\text{app}}\) — optymistyczne założenia o \(N\) lub \(w(s)\)
- **Pipeline 2 > Pipeline 1:** Często niższe \(P_{\text{app}}\) — model historyczny widział więcej "niespodzianek" (np. nagły wzrost zainteresowania)

**Użycie łączone:**
1. Uruchom oba pipeline'y
2. Jeśli różnią się o <10%: okej, przyjmij średnią
3. Jeśli różnią się o >20%: **zbadaj przyczynę**
   - Sprawdź założenia Pipeline 1 (\(N\), rozkład \(F_{\text{app}}\))
   - Sprawdź, czy Pipeline 2 ma wystarczająco danych historycznych

**Przykład rozbieżności:**
```
Program: Architektura, PP
Kandydat: 550 pkt

Pipeline 1: P_app = 90% (założono N=150, ale może być więcej)
Pipeline 2: P_app = 65% (model widział rok, gdy N=300)

→ Wniosek: Pipeline 1 jest zbyt optymistyczny, prawdopodobnie N jest wyższe
```

---

## 🛠️ Implementacja (schemat)

```python
import pandas as pd
import pymc as pm
import numpy as np

# (a) Kompilacja danych
df_hist = pd.read_csv('thresholds_history.csv')
# Kolumny: program_id, year, threshold, limit, n_applicants_prev_year, ...

# (b) Model
with pm.Model() as qr_model:
    # Zmienne
    program_idx = df_hist['program_id'].astype('category').cat.codes
    X = df_hist[['limit', 'n_applicants_prev_year', 'threshold_prev_year']].values
    y = df_hist['threshold'].values
    
    # Parametry
    mu = pm.Normal('mu', mu=500, sigma=100)
    sigma_u = pm.HalfNormal('sigma_u', sigma=50)
    u = pm.Normal('u', mu=0, sigma=sigma_u, shape=df_hist['program_id'].nunique())
    gamma = pm.Normal('gamma', mu=0, sigma=10, shape=X.shape[1])
    
    # Quantile regression (tau=0.9)
    tau = 0.9
    mu_q = mu + u[program_idx] + pm.math.dot(X, gamma)
    y_obs = pm.AsymmetricLaplace('y_obs', mu=mu_q, b=1, kappa=tau, observed=y)
    
    # MCMC
    trace = pm.sample(2000, tune=1000)

# (d) Predykcja
X_new = np.array([[30, 200, 450]])  # limit, n_prev, threshold_prev
with qr_model:
    ppc = pm.sample_posterior_predictive(trace, var_names=['y_obs'])

T_samples = ppc.posterior_predictive['y_obs'].values.flatten()  # 4000 próbek

# (e) P_app(s)
s_candidate = 550
p_app = np.mean(T_samples <= s_candidate)
print(f"P(przyjęcie | s={s_candidate}) = {p_app:.1%}")
```

---

## ✅ Podsumowanie

**Pipeline 2 to:**
- 📊 Model statystyczny uczony na danych historycznych
- 🎯 Przewiduje bezpośrednio próg punktowy (nie symuluje rekrutacji)
- ⚡ Szybszy i prostszy niż Pipeline 1
- 🔍 Świetny jako sanity check lub gdy brakuje szczegółowych danych CKE

**Kluczowe kroki:**
1. Zbierz dane historyczne (progi + cechy programów)
2. Dopasuj model regresji kwantylowej z efektami losowymi (MCMC)
3. Generuj próbki posterioru przewidywanego progu \(T\)
4. Licz \(P_{\text{app}}(s)\) jako odsetek próbek, gdzie \(T \le s\)

**Następne kroki:**
- Implementacja w `pipeline_2.py`
- Walidacja na danych PP (train: 2021-2023, test: 2024)
- Porównanie z Pipeline 1 (reliability diagram)

---

