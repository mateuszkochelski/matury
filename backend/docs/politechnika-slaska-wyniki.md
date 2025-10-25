# Politechnika Śląska – zestawienie 5 scenariuszy

_Endpoint_: http://localhost:8080/api/recruitment-calculator/calculate

Każdy scenariusz wysyłany jest jako jedno zapytanie z kompletem wyników matur, a odpowiedź API zapisywana poniżej wraz z rozbiciem na poszczególne składniki formuły.

## balanced-tech

Mocna matematyka rozszerzona i fizyka, solidny egzamin zawodowy.

**Wyniki matur:**
  - polish_language (BASIC): 82
  - english_language (BASIC): 88
  - mathematics (BASIC): 86
  - mathematics (EXTENDED): 74
  - physics (EXTENDED): 79
  - vocational_exam (VOCATIONAL_TECHNICIAN): 84
  - art_exam (N/A): 410

**Kierunek:** Pozostałe kierunki kształcenia (`default`)
- Suma punktów: 122.00
- Rozbicie składników:
    - Matematyka (poziom podstawowy): 43.0 pkt (surowy: 86.0, współczynnik: 0.5, źródło: mathematics [BASIC])
    - Najlepszy wynik z przedmiotu dodatkowego lub egzaminu zawodowego: 79.0 pkt (surowy: 79.0, współczynnik: 1.0, źródło: physics [EXTENDED])

## bilingual-creative

Dwujęzyczny angielski, biologia rozszerzona i geografia – profil olimpijski.

**Wyniki matur:**
  - polish_language (EXTENDED): 70
  - english_language (BILINGUAL): 42
  - mathematics (BASIC): 68
  - mathematics (EXTENDED): 64
  - biology (EXTENDED): 71
  - geography (BASIC): 73
  - art_exam (N/A): 360

**Kierunek:** Pozostałe kierunki kształcenia (`default`)
- Suma punktów: 105.00
- Rozbicie składników:
    - Matematyka (poziom podstawowy): 34.0 pkt (surowy: 68.0, współczynnik: 0.5, źródło: mathematics [BASIC])
    - Najlepszy wynik z przedmiotu dodatkowego lub egzaminu zawodowego: 71.0 pkt (surowy: 71.0, współczynnik: 1.0, źródło: biology [EXTENDED])

## vocational-master

Dominujący wynik z egzaminu zawodowego i informatyka rozszerzona.

**Wyniki matur:**
  - polish_language (BASIC): 69
  - english_language (BASIC): 71
  - mathematics (BASIC): 76
  - mathematics (EXTENDED): 55
  - informatics (EXTENDED): 81
  - vocational_exam (VOCATIONAL_TECHNICIAN): 95
  - art_exam (N/A): 380

**Kierunek:** Pozostałe kierunki kształcenia (`default`)
- Suma punktów: 119.00
- Rozbicie składników:
    - Matematyka (poziom podstawowy): 38.0 pkt (surowy: 76.0, współczynnik: 0.5, źródło: mathematics [BASIC])
    - Najlepszy wynik z przedmiotu dodatkowego lub egzaminu zawodowego: 81.0 pkt (surowy: 81.0, współczynnik: 1.0, źródło: informatics [EXTENDED])

## science-olympian

Świetne rozszerzenia z matematyki i fizyki, wysoki angielski.

**Wyniki matur:**
  - polish_language (BASIC): 75
  - english_language (EXTENDED): 88
  - mathematics (BASIC): 90
  - mathematics (EXTENDED): 90
  - chemistry (BASIC): 78
  - physics (EXTENDED): 92
  - art_exam (N/A): 450

**Kierunek:** Pozostałe kierunki kształcenia (`default`)
- Suma punktów: 137.00
- Rozbicie składników:
    - Matematyka (poziom podstawowy): 45.0 pkt (surowy: 90.0, współczynnik: 0.5, źródło: mathematics [BASIC])
    - Najlepszy wynik z przedmiotu dodatkowego lub egzaminu zawodowego: 92.0 pkt (surowy: 92.0, współczynnik: 1.0, źródło: physics [EXTENDED])

## language-heavy-geo

Silne języki obce i geografia, do tego biologia i umiarkowany wynik zawodowy.

**Wyniki matur:**
  - polish_language (BASIC): 78
  - english_language (BILINGUAL): 45
  - german_language (EXTENDED): 80
  - mathematics (BASIC): 72
  - mathematics (EXTENDED): 66
  - geography (EXTENDED): 70
  - biology (BASIC): 62
  - vocational_exam (VOCATIONAL_TECHNICIAN): 60
  - art_exam (N/A): 400

**Kierunek:** Pozostałe kierunki kształcenia (`default`)
- Suma punktów: 116.00
- Rozbicie składników:
    - Matematyka (poziom podstawowy): 36.0 pkt (surowy: 72.0, współczynnik: 0.5, źródło: mathematics [BASIC])
    - Najlepszy wynik z przedmiotu dodatkowego lub egzaminu zawodowego: 80.0 pkt (surowy: 80.0, współczynnik: 1.0, źródło: german_language [EXTENDED])

