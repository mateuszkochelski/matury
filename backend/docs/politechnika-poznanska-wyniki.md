# Politechnika Poznańska – zestawienie 5 scenariuszy

_Endpoint_: http://localhost:8080/api/recruitment-calculator/calculate

Każdy scenariusz został przepuszczony przez trzy typy kierunków Politechniki Poznańskiej. Wyniki możesz porównać z oficjalnymi kalkulatorami uczelni.

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

**Kierunek:** Kierunki z przedmiotem X (`engineering-x`)
- Suma punktów: 824.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 41.0 pkt (surowy: 82.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 44.0 pkt (surowy: 88.0, współczynnik: 0.5, źródło: english_language [BASIC])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 217.5 pkt (surowy: 87.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 185.0 pkt (surowy: 74.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik X (przedmiot lub egzamin zawodowy): 337.0 pkt (surowy: 168.5, współczynnik: 2.0, źródło: physics [COMBINED_SUBJECT])

**Kierunek:** Kierunki z przedmiotem XG (`science-xg`)
- Suma punktów: 824.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 41.0 pkt (surowy: 82.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 44.0 pkt (surowy: 88.0, współczynnik: 0.5, źródło: english_language [BASIC])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 217.5 pkt (surowy: 87.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 185.0 pkt (surowy: 74.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik XG (przedmiot lub egzamin zawodowy): 337.0 pkt (surowy: 168.5, współczynnik: 2.0, źródło: physics [COMBINED_SUBJECT])

**Kierunek:** Kierunki architektoniczne (`architecture`)
- Suma punktów: 821.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 82.0 pkt (surowy: 82.0, współczynnik: 1.0, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 88.0 pkt (surowy: 88.0, współczynnik: 1.0, źródło: english_language [BASIC])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 130.5 pkt (surowy: 87.0, współczynnik: 1.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 111.0 pkt (surowy: 74.0, współczynnik: 1.5, źródło: mathematics [EXTENDED])
    - Egzamin z uzdolnien artystycznych (0-500): 410.0 pkt (surowy: 410.0, współczynnik: 1.0, źródło: art_exam)

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

**Kierunek:** Kierunki z przedmiotem X (`engineering-x`)
- Suma punktów: 770.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 42.5 pkt (surowy: 85.0, współczynnik: 0.5, źródło: polish_language [BASIC_FROM_EXTENDED])
    - Jezyk obcy nowozytny - najlepszy wynik: 50.0 pkt (surowy: 100.0, współczynnik: 0.5, źródło: english_language [BILINGUAL_ADJUSTED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 205.0 pkt (surowy: 82.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 160.0 pkt (surowy: 64.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik X (przedmiot lub egzamin zawodowy): 313.0 pkt (surowy: 156.5, współczynnik: 2.0, źródło: biology [COMBINED_SUBJECT])

**Kierunek:** Kierunki z przedmiotem XG (`science-xg`)
- Suma punktów: 770.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 42.5 pkt (surowy: 85.0, współczynnik: 0.5, źródło: polish_language [BASIC_FROM_EXTENDED])
    - Jezyk obcy nowozytny - najlepszy wynik: 50.0 pkt (surowy: 100.0, współczynnik: 0.5, źródło: english_language [BILINGUAL_ADJUSTED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 205.0 pkt (surowy: 82.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 160.0 pkt (surowy: 64.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik XG (przedmiot lub egzamin zawodowy): 313.0 pkt (surowy: 156.5, współczynnik: 2.0, źródło: biology [COMBINED_SUBJECT])

**Kierunek:** Kierunki architektoniczne (`architecture`)
- Suma punktów: 764.00
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 85.0 pkt (surowy: 85.0, współczynnik: 1.0, źródło: polish_language [BASIC_FROM_EXTENDED])
    - Jezyk obcy nowozytny - najlepszy wynik: 100.0 pkt (surowy: 100.0, współczynnik: 1.0, źródło: english_language [BILINGUAL_ADJUSTED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 123.0 pkt (surowy: 82.0, współczynnik: 1.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 96.0 pkt (surowy: 64.0, współczynnik: 1.5, źródło: mathematics [EXTENDED])
    - Egzamin z uzdolnien artystycznych (0-500): 360.0 pkt (surowy: 360.0, współczynnik: 1.0, źródło: art_exam)

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

**Kierunek:** Kierunki z przedmiotem X (`engineering-x`)
- Suma punktów: 781.25
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 34.5 pkt (surowy: 69.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 35.5 pkt (surowy: 71.0, współczynnik: 0.5, źródło: english_language [BASIC])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 193.75 pkt (surowy: 77.5, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 137.5 pkt (surowy: 55.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik X (przedmiot lub egzamin zawodowy): 380.0 pkt (surowy: 190.0, współczynnik: 2.0, źródło: vocational_exam [VOCATIONAL])

**Kierunek:** Kierunki z przedmiotem XG (`science-xg`)
- Suma punktów: 781.25
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 34.5 pkt (surowy: 69.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 35.5 pkt (surowy: 71.0, współczynnik: 0.5, źródło: english_language [BASIC])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 193.75 pkt (surowy: 77.5, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 137.5 pkt (surowy: 55.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik XG (przedmiot lub egzamin zawodowy): 380.0 pkt (surowy: 190.0, współczynnik: 2.0, źródło: vocational_exam [VOCATIONAL])

**Kierunek:** Kierunki architektoniczne (`architecture`)
- Suma punktów: 718.75
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 69.0 pkt (surowy: 69.0, współczynnik: 1.0, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 71.0 pkt (surowy: 71.0, współczynnik: 1.0, źródło: english_language [BASIC])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 116.25 pkt (surowy: 77.5, współczynnik: 1.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 82.5 pkt (surowy: 55.0, współczynnik: 1.5, źródło: mathematics [EXTENDED])
    - Egzamin z uzdolnien artystycznych (0-500): 380.0 pkt (surowy: 380.0, współczynnik: 1.0, źródło: art_exam)

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

**Kierunek:** Kierunki z przedmiotem X (`engineering-x`)
- Suma punktów: 923.00
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 37.5 pkt (surowy: 75.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 47.0 pkt (surowy: 94.0, współczynnik: 0.5, źródło: english_language [BASIC_FROM_EXTENDED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 237.5 pkt (surowy: 95.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 225.0 pkt (surowy: 90.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik X (przedmiot lub egzamin zawodowy): 376.0 pkt (surowy: 188.0, współczynnik: 2.0, źródło: physics [COMBINED_SUBJECT])

**Kierunek:** Kierunki z przedmiotem XG (`science-xg`)
- Suma punktów: 923.00
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 37.5 pkt (surowy: 75.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 47.0 pkt (surowy: 94.0, współczynnik: 0.5, źródło: english_language [BASIC_FROM_EXTENDED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 237.5 pkt (surowy: 95.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 225.0 pkt (surowy: 90.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik XG (przedmiot lub egzamin zawodowy): 376.0 pkt (surowy: 188.0, współczynnik: 2.0, źródło: physics [COMBINED_SUBJECT])

**Kierunek:** Kierunki architektoniczne (`architecture`)
- Suma punktów: 896.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 75.0 pkt (surowy: 75.0, współczynnik: 1.0, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 94.0 pkt (surowy: 94.0, współczynnik: 1.0, źródło: english_language [BASIC_FROM_EXTENDED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 142.5 pkt (surowy: 95.0, współczynnik: 1.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 135.0 pkt (surowy: 90.0, współczynnik: 1.5, źródło: mathematics [EXTENDED])
    - Egzamin z uzdolnien artystycznych (0-500): 450.0 pkt (surowy: 450.0, współczynnik: 1.0, źródło: art_exam)

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

**Kierunek:** Kierunki z przedmiotem X (`engineering-x`)
- Suma punktów: 701.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 39.0 pkt (surowy: 78.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 50.0 pkt (surowy: 100.0, współczynnik: 0.5, źródło: english_language [BILINGUAL_ADJUSTED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 207.5 pkt (surowy: 83.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 165.0 pkt (surowy: 66.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik X (przedmiot lub egzamin zawodowy): 240.0 pkt (surowy: 120.0, współczynnik: 2.0, źródło: vocational_exam [VOCATIONAL])

**Kierunek:** Kierunki z przedmiotem XG (`science-xg`)
- Suma punktów: 771.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 39.0 pkt (surowy: 78.0, współczynnik: 0.5, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 50.0 pkt (surowy: 100.0, współczynnik: 0.5, źródło: english_language [BILINGUAL_ADJUSTED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 207.5 pkt (surowy: 83.0, współczynnik: 2.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 165.0 pkt (surowy: 66.0, współczynnik: 2.5, źródło: mathematics [EXTENDED])
    - Najlepszy wynik XG (przedmiot lub egzamin zawodowy): 310.0 pkt (surowy: 155.0, współczynnik: 2.0, źródło: geography [COMBINED_SUBJECT])

**Kierunek:** Kierunki architektoniczne (`architecture`)
- Suma punktów: 801.50
- Rozbicie składników:
    - Jezyk polski (poziom podstawowy, z konwersja z rozszerzenia): 78.0 pkt (surowy: 78.0, współczynnik: 1.0, źródło: polish_language [BASIC])
    - Jezyk obcy nowozytny - najlepszy wynik: 100.0 pkt (surowy: 100.0, współczynnik: 1.0, źródło: english_language [BILINGUAL_ADJUSTED])
    - Matematyka - poziom podstawowy (z konwersja z rozszerzenia): 124.5 pkt (surowy: 83.0, współczynnik: 1.5, źródło: mathematics [BASIC_FROM_EXTENDED])
    - Matematyka - poziom rozszerzony: 99.0 pkt (surowy: 66.0, współczynnik: 1.5, źródło: mathematics [EXTENDED])
    - Egzamin z uzdolnien artystycznych (0-500): 400.0 pkt (surowy: 400.0, współczynnik: 1.0, źródło: art_exam)

