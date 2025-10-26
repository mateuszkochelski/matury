# core
import numpy as np
import pandas as pd

# modeling
from sklearn.preprocessing import OneHotEncoder, StandardScaler, PolynomialFeatures
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.calibration import CalibratedClassifierCV
from sklearn.metrics import brier_score_loss, roc_auc_score
from sklearn.model_selection import TimeSeriesSplit, GridSearchCV

# stats / distributions
from scipy import interpolate
from scipy.stats import poisson, binom

# counts model
import statsmodels.api as sm
import statsmodels.formula.api as smf

def compute_points_for_program_PP_standard(exam_row):
    """
    Wzór standardowy PP: 0.5 * JP + 0.5 * JO + 2.5 * M + 2 * X
    
    JP = polski podstawa
    JO = język obcy podstawa (pisemny)
    M = matematyka podstawa + rozszerzenie (SUMA!)
    X = max(fizyka, chemia, biologia, informatyka, geografia) - rozszerzenia
    
    Args:
        exam_row: pd.Series z kolumnami z exam_df (output z cke_loader.py)
    
    Returns:
        float: punkty rekrutacyjne
    """
    JP = float(exam_row.get('polish_basic', 0))
    JO = float(exam_row.get('foreign_basic', 0))
    
    # M = suma podstawy i rozszerzenia
    M = float(exam_row.get('math_basic', 0)) + float(exam_row.get('math_ext', 0))
    
    # X = max z przedmiotów ścisłych/przyrodniczych (rozszerzenia)
    X = max([
        float(exam_row.get('phys_ext', 0)),
        float(exam_row.get('chem_ext', 0)),
        float(exam_row.get('bio_ext', 0)),
        float(exam_row.get('info_ext', 0)),
        float(exam_row.get('geog_ext', 0))
    ])
    
    s = 0.5 * JP + 0.5 * JO + 2.5 * M + 2 * X
    
    # Ograniczenie do [0, 1000] (max możliwy)
    return max(0.0, min(s, 1000.0))


def compute_points_for_program_PP_architecture(exam_row):
    """
    Wzór A (Architektura PP): JP + JO + 1.5 * M + R
    
    R = test rysunku (brak w CKE - zakładamy 0 lub stałą)
    """
    JP = float(exam_row.get('polish_basic', 0))
    JO = float(exam_row.get('foreign_basic', 0))
    M = float(exam_row.get('math_basic', 0)) + float(exam_row.get('math_ext', 0))
    R = 0  # Test rysunku - brak w CKE, trzeba dodać ręcznie jeśli dostępny
    
    s = JP + JO + 1.5 * M + R
    return max(0.0, min(s, 1000.0))


def compute_points_for_program(exam_row, program_rule):
    """
    Oblicza punkty rekrutacyjne dla programu PP na podstawie wyniku CKE.
    
    Args:
        exam_row: pd.Series z exam_df (output z cke_loader.py)
        program_rule: dict z kluczem 'formula' (string wzoru PP)
    
    Returns:
        float: punkty rekrutacyjne
    """
    formula = program_rule.get('formula', '')
    
    # Rozpoznaj typ wzoru
    if 'Wzór: 0.5 * JP' in formula:
        # Wzór standardowy (101 kierunków)
        return compute_points_for_program_PP_standard(exam_row)
    
    elif 'Wzór A:' in formula:
        # Wzór architektura
        return compute_points_for_program_PP_architecture(exam_row)
    
    elif 'Wzór AW:' in formula:
        # Wzór architektura wnętrz (podobny do A, ale Y zamiast M)
        # Y = rozszerzenie z: bio/chem/fiz/geo/info/hist/hist_art
        JP = float(exam_row.get('polish_basic', 0))
        JO = float(exam_row.get('foreign_basic', 0))
        Y = max([
            float(exam_row.get('bio_ext', 0)),
            float(exam_row.get('chem_ext', 0)),
            float(exam_row.get('phys_ext', 0)),
            float(exam_row.get('geog_ext', 0)),
            float(exam_row.get('info_ext', 0)),
            float(exam_row.get('hist_ext', 0)),
            float(exam_row.get('hist_art_ext', 0))
        ])
        R = 0  # Test rysunku
        s = JP + JO + 1.5 * Y + R
        return max(0.0, min(s, 1000.0))
    
    else:
        # Nieznany wzór - zwróć 0 lub raise error
        raise ValueError(f"Nieznany wzór: {formula}")



# exam_df: dane CKE mikro (jeden wiersz = zdający), kolumny = wyniki z przedmiotów
# Obliczanie s_points przeniesione do sekcji wykonawczej (main)


# split czasowy (przykład: ostatni rok jako walidacja)
def time_split_train_valid(df, valid_year):
    train = df[df["year"] < valid_year].copy()
    valid = df[df["year"] == valid_year].copy()
    return train, valid

# cechy
num_cols = ["s_points", "seat_limit", "prev_cutoff"]
cat_cols = ["program_id", "study_mode", "year"]  # year jako kategoria => efekt roku

preprocess = ColumnTransformer(
    transformers=[
        ("num_poly", Pipeline([
            ("poly", PolynomialFeatures(degree=2, include_bias=False)),  # s, s^2, interakcje z num.
            ("sc", StandardScaler())
        ]), num_cols),
        ("cat", OneHotEncoder(handle_unknown="ignore"), cat_cols)
    ]
)

base_logit = LogisticRegression(
    penalty="l2", C=1.0, solver="lbfgs", max_iter=1000, class_weight=None
)

pipe_logit = Pipeline([
    ("prep", preprocess),
    ("clf", base_logit)
])

# strojenie hiperparametrów opcjonalnie:
param_grid = {
    "clf__C": [0.1, 1.0, 3.0]
}
# UWAGA: dla zachowania porządku czasowego użyj walidacji czasowej lub trzymaj grid na danych train tylko
# tutaj pokazowo prosto:
# grid = GridSearchCV(pipe_logit, param_grid=param_grid, cv=3, n_jobs=-1)
# grid.fit(train_X, train_y)
# best_model = grid.best_estimator_

def fit_propensity_model(apps_hist_df, valid_year=None, calibrate="isotonic"):
    if valid_year is not None:
        train_df, valid_df = time_split_train_valid(apps_hist_df, valid_year)
    else:
        train_df = apps_hist_df.copy()
        valid_df = None

    X_train = train_df[num_cols + cat_cols]
    y_train = train_df["applied"].astype(int)

    pipe = pipe_logit.fit(X_train, y_train)

    # kalibracja (na końcu, najlepiej na zbiorze walidacyjnym nieużytym do fit)
    if calibrate in {"isotonic", "sigmoid"}:
        calib = CalibratedClassifierCV(pipe, cv="prefit", method="isotonic" if calibrate=="isotonic" else "sigmoid")
        if valid_df is not None and len(valid_df) > 0:
            calib.fit(valid_df[num_cols + cat_cols], valid_df["applied"].astype(int))
        else:
            # w ostateczności kalibruj na końcu tren (mniej idealne)
            calib.fit(X_train, y_train)
        return calib
    else:
        return pipe

# predykcja P_app(s | j, y, X)
# przykład: model dla KONKRETNEGO programu j – możesz też trenować model globalny z program_id w cechach (jak tu)


def build_F_app_from_population(pop_s_points,  # np. exam_df["s_points_j"].values (rok y)
                                propensity_model,
                                program_id, year, seat_limit, prev_cutoff,
                                study_mode="full_time",
                                bin_width=0.5):
    """
    Zwraca:
      - cdf_func(s): funkcję interpolującą CDF F_app(s)
      - pdf_bins: (bin_edges, pdf_values) dla ewentualnych analiz
    """
    # histogram populacji CKE (gęstość w binach)
    s_min, s_max = float(np.min(pop_s_points)), float(np.max(pop_s_points))
    nbins = int(np.ceil((s_max - s_min) / bin_width)) + 1
    bins = np.linspace(s_min, s_max, nbins)
    hist, edges = np.histogram(pop_s_points, bins=bins, density=False)
    centers = 0.5*(edges[:-1] + edges[1:])
    # zbuduj ramkę z cechami potrzebnymi modelowi selekcji
    feat = pd.DataFrame({
        "s_points": centers,
        "seat_limit": seat_limit,
        "prev_cutoff": prev_cutoff,
        "program_id": program_id,
        "study_mode": study_mode,
        "year": year
    })
    # przewidź skłonność do aplikacji dla każdego koszyka s
    p_app = propensity_model.predict_proba(feat)[:, 1]  # P(apply=1 | s, X)

    # oczekiwane "liczności aplikantów" w binach ~ hist * p_app
    expected_applicants = hist.astype(float) * p_app
    # pdf_app ∝ expected_applicants
    pdf_app = expected_applicants / expected_applicants.sum()
    cdf_app = np.cumsum(pdf_app)
    # funkcja CDF (interpolacja schodkowa)
    cdf_func = interpolate.interp1d(centers, cdf_app, kind="previous",
                                    bounds_error=False, fill_value=(0.0, 1.0))
    return cdf_func, (centers, pdf_app)


    # przykład: Poisson GLM (możesz zmienić na NegativeBinomial jeśli variancja >> średnia)
poisson_formula = """
applicants_count ~ seat_limit + prev_cutoff + apps_prev_year + cohort_size
+ C(program_id) + C(year) + C(study_mode)
"""

def fit_count_model(apps_agg_df):
    model = smf.glm(poisson_formula, data=apps_agg_df,
                    family=sm.families.Poisson())
    res = model.fit()
    return res  # res.predict(X_new) daje E[N]

def predict_lambda(res_glm, seat_limit, prev_cutoff, apps_prev_year,
                   cohort_size, program_id, year, study_mode):
    X_new = pd.DataFrame([{
        "seat_limit": seat_limit,
        "prev_cutoff": prev_cutoff,
        "apps_prev_year": apps_prev_year,
        "cohort_size": cohort_size,
        "program_id": program_id,
        "year": year,
        "study_mode": study_mode
    }])
    lam = float(res_glm.predict(X_new).values[0])
    return max(lam, 1e-6)  # zabezpieczenie przed 0


def acceptance_prob_poisson(s, F_app_cdf_func, lam, K):
    tail = max(0.0, 1.0 - float(F_app_cdf_func(s)))  # P(competitor ≥ s)
    mu = lam * tail
    # P(Poisson(mu) < K) = CDF(K-1)
    return float(poisson.cdf(K-1, mu))

def acceptance_prob_mixture(s, F_app_cdf_func, K, n_grid, pN):
    """
    n_grid: np.array możliwych n (np. 0..n_max)
    pN:     np.array prawdopodobieństw Pr(N=n) tej samej długości (sum=1)
    """
    p_tail = max(0.0, 1.0 - float(F_app_cdf_func(s)))  # P(competitor ≥ s)
    out = 0.0
    for n, pn in zip(n_grid, pN):
        if n <= 0:
            out += pn  # n=0 => przyjęty trywialnie
            continue
        # liczba konkurentów z wynikiem ≥ s ~ Binom(n-1, p_tail)
        out += pn * float(binom.cdf(K-1, n-1, p_tail))
    return out

def poisson_to_grid(lam, k_sigma=4, max_cap=100000):
    n_max = int(min(max_cap, np.ceil(lam + k_sigma*np.sqrt(lam)) + 5))
    n_grid = np.arange(0, n_max+1)
    pN = poisson.pmf(n_grid, lam)
    # normalizacja (ucięcie ogonów)
    pN = pN / pN.sum()
    return n_grid, pN


def predict_acceptance_for_candidate(
    candidate_s,                 # s kandydata (dla programu j)
    propensity_model,            # wytrenowany model selekcji
    pop_s_points_for_program,    # s dla całej populacji CKE w roku y (dla tego programu)
    program_id, year, seat_limit, prev_cutoff, study_mode,
    count_model_res=None,        # GLM Poisson/NegBin (opcjonalnie)
    lambda_override=None,        # jeśli chcesz podać własne lam
    use_poisson_shortcut=True
):
    # 1) Zbuduj F_app(s)
    F_app_cdf, _ = build_F_app_from_population(
        pop_s_points_for_program, propensity_model,
        program_id, year, seat_limit, prev_cutoff, study_mode
    )
    # 2) Oszacuj lambda = E[N]
    if lambda_override is not None:
        lam = float(lambda_override)
    elif count_model_res is not None:
        # wypełnij cechy wg swojego data modelu
        # tu przykładowo trzymamy apps_prev_year=..., cohort_size=...
        lam = predict_lambda(
            count_model_res,
            seat_limit=seat_limit,
            prev_cutoff=prev_cutoff,
            apps_prev_year=np.nan,   # <- podstawić sensowną wartość
            cohort_size=np.nan,      # <- podstawić sensowną wartość
            program_id=program_id,
            year=year,
            study_mode=study_mode
        )
    else:
        # fallback: np. średnia historyczna z innego miejsca
        lam = 100.0

    # 3) Policz p(s)
    if use_poisson_shortcut:
        return acceptance_prob_poisson(candidate_s, F_app_cdf, lam, seat_limit)
    else:
        n_grid, pN = poisson_to_grid(lam, k_sigma=4)
        return acceptance_prob_mixture(candidate_s, F_app_cdf, seat_limit, n_grid, pN)




def evaluate_propensity(apps_hist_df, valid_year, model):
    valid = apps_hist_df[apps_hist_df["year"] == valid_year].copy()
    y_true = valid["applied"].astype(int).values
    y_prob = model.predict_proba(valid[["s_points","seat_limit","prev_cutoff","program_id","study_mode","year"]])[:,1]
    auc = roc_auc_score(y_true, y_prob)
    brier = brier_score_loss(y_true, y_prob)
    return {"AUC": auc, "Brier": brier}

# Wykresy kalibracji (opcjonalnie):
from sklearn.calibration import calibration_curve
import matplotlib.pyplot as plt

def plot_reliability(y_true, y_prob, n_bins=10):
    frac_pos, mean_pred = calibration_curve(y_true, y_prob, n_bins=n_bins, strategy='quantile')
    plt.plot(mean_pred, frac_pos, marker='o')
    plt.plot([0,1],[0,1],'--')
    plt.xlabel("Predicted probability")
    plt.ylabel("Empirical frequency")
    plt.title("Reliability diagram")
    plt.show()



# =============================================================================
# GŁÓWNY KOD WYKONAWCZY - użycie pipeline z realnymi danymi
# =============================================================================

if __name__ == "__main__":
    from cke_loader import load_cke_to_exam_df
    from pp_data_loader import load_politechnika_poznanska
    
    print("\n" + "="*80)
    print("🚀 PIPELINE 1 - Predykcja szans przyjęcia na studia")
    print("="*80)
    
    print("\n1️⃣ Wczytuję dane CKE...")
    exam_df = load_cke_to_exam_df(
        '../university_data/CKE/pismo_10.11.2024/egzamin_maturalny_anonimowe_dane_2024.xlsx'
    )
    print(f"   ✅ Wczytano {len(exam_df)} maturzystów")
    
    print("\n2️⃣ Wczytuję dane Politechniki Poznańskiej...")
    apps_hist_df, apps_agg_df, program_rules = load_politechnika_poznanska()
    print(f"   ✅ apps_hist_df: {len(apps_hist_df)} aplikacji")
    print(f"   ✅ apps_agg_df: {len(apps_agg_df)} programów")
    
    print("\n3️⃣ Trenuję model selekcji (propensity model)...")
    # Uwaga: apps_hist_df ma już kolumnę 's_points' (gotowe punkty z PP)
    # Więc NIE używamy compute_points_for_program dla treningowych danych
    
    propensity_model = fit_propensity_model(
        apps_hist_df, 
        valid_year=None,  # Brak podziału (mały dataset)
        calibrate="isotonic"
    )
    print("   ✅ Model wytrenowany")
    
    print("\n4️⃣ Obliczam punkty dla populacji CKE (dla przykładowego programu 3569)...")
    program_id = '3569'
    program_rule = program_rules[program_id]
    
    # Oblicz s_points dla KAŻDEGO maturzysty z CKE dla tego programu
    exam_df['s_points_3569'] = exam_df.apply(
        lambda r: compute_points_for_program(r, program_rule), axis=1
    )
    pop_s_points = exam_df['s_points_3569'].values
    print(f"   ✅ Obliczono punkty dla {len(pop_s_points)} maturzystów")
    print(f"   Min: {pop_s_points.min():.1f}, Max: {pop_s_points.max():.1f}, Mean: {pop_s_points.mean():.1f}")
    
    print("\n5️⃣ Buduję F_app (rozkład aplikantów)...")
    # Metadata programu
    prog_meta = apps_agg_df[apps_agg_df['program_id']==int(program_id)].iloc[0]
    
    F_app_cdf, (centers, pdf_app) = build_F_app_from_population(
        pop_s_points=pop_s_points,
        propensity_model=propensity_model,
        program_id=program_id,
        year=2024,
        seat_limit=int(prog_meta['seat_limit']),
        prev_cutoff=float(prog_meta['prev_cutoff']),
        study_mode='stacjonarne'
    )
    print("   ✅ F_app zbudowany")
    
    print("\n6️⃣ Testuję predykcję dla przykładowych kandydatów...")
    # Test na różnych poziomach punktów
    test_scores = [400, 450, 500, 550, 600]
    
    results = []
    for candidate_s in test_scores:
        p_accept = predict_acceptance_for_candidate(
            candidate_s=candidate_s,
            propensity_model=propensity_model,
            pop_s_points_for_program=pop_s_points,
            program_id=program_id,
            year=2024,
            seat_limit=int(prog_meta['seat_limit']),
            prev_cutoff=float(prog_meta['prev_cutoff']),
            study_mode='stacjonarne',
            lambda_override=prog_meta['applicants_count'],  # Użyj realnej liczby aplikantów
            use_poisson_shortcut=True
        )
        results.append((candidate_s, p_accept))
    
    print("\n" + "="*80)
    print("🎓 WYNIKI PREDYKCJI")
    print("="*80)
    print(f"Program: {program_rule['name']}")
    print(f"Próg 2023/2024: {prog_meta['prev_cutoff']}")
    print(f"Limit miejsc: {prog_meta['seat_limit']}")
    print(f"Liczba aplikantów 2023/24: {prog_meta['applicants_count']}")
    print("\n" + "-"*80)
    print(f"{'Punkty kandydata':>20} | {'Szansa przyjęcia':>20}")
    print("-"*80)
    for s, p in results:
        print(f"{s:20.0f} | {p*100:19.1f}%")
    print("="*80)