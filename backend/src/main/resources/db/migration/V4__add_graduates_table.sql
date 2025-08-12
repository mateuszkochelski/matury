-- Tabela absolwentów z danymi o zatrudnieniu, zarobkach i karierze akademickiej
CREATE TABLE graduates (
    id BIGSERIAL PRIMARY KEY,
    
    -- Podstawowe informacje o absolwencie
    rok_dyplomu INTEGER,
    kierunek_id VARCHAR(50),
    poziom VARCHAR(50),
    forma VARCHAR(50),
    liczba_absolwentow INTEGER,
    
    -- Informacje o uczelni i lokalizacji
    nazwa_kierunku TEXT,
    nazwa_specjalnosci TEXT,
    uczelnia_id VARCHAR(50),
    nazwa_uczelni TEXT,
    jednostka_id VARCHAR(50),
    nazwa_jednostki TEXT,
    profil VARCHAR(10),
    dziedzina_id VARCHAR(50),
    dziedzina TEXT,
    wojewodztwo VARCHAR(50),
    
    -- Wskaźniki zatrudnienia - podstawowe
    proc_w_zus DECIMAL(5,2),
    proc_poza_zus DECIMAL(5,2),
    proc_dosw DECIMAL(5,2),
    
    -- Wskaźniki zatrudnienia per rok
    czy_praca_p1 DECIMAL(5,2),
    czy_praca_p2 DECIMAL(5,2),
    czy_praca_p3 DECIMAL(5,2),
    czy_praca_p4 DECIMAL(5,2),
    czy_praca_p5 DECIMAL(5,2),
    
    -- Wskaźniki zatrudnienia na umowę o pracę per rok
    czy_etat_p1 DECIMAL(5,2),
    czy_etat_p2 DECIMAL(5,2),
    czy_etat_p3 DECIMAL(5,2),
    czy_etat_p4 DECIMAL(5,2),
    czy_etat_p5 DECIMAL(5,2),
    
    -- Wskaźniki samozatrudnienia per rok
    czy_samoz_p1 DECIMAL(5,2),
    czy_samoz_p2 DECIMAL(5,2),
    czy_samoz_p3 DECIMAL(5,2),
    czy_samoz_p4 DECIMAL(5,2),
    czy_samoz_p5 DECIMAL(5,2),
    
    -- Wskaźniki bezrobocia per rok
    czy_bezr_p1 DECIMAL(5,2),
    czy_bezr_p2 DECIMAL(5,2),
    czy_bezr_p3 DECIMAL(5,2),
    czy_bezr_p4 DECIMAL(5,2),
    czy_bezr_p5 DECIMAL(5,2),
    
    -- Względny Wskaźnik Bezrobocia per rok
    wwb_p1 DECIMAL(8,4),
    wwb_p2 DECIMAL(8,4),
    wwb_p3 DECIMAL(8,4),
    wwb_p4 DECIMAL(8,4),
    wwb_p5 DECIMAL(8,4),
    
    -- Średnie zarobki per rok
    e_zar_p1 DECIMAL(10,2),
    e_zar_p2 DECIMAL(10,2),
    e_zar_p3 DECIMAL(10,2),
    e_zar_p4 DECIMAL(10,2),
    e_zar_p5 DECIMAL(10,2),
    
    -- Względny Wskaźnik Zarobków per rok
    wwz_p1 DECIMAL(8,4),
    wwz_p2 DECIMAL(8,4),
    wwz_p3 DECIMAL(8,4),
    wwz_p4 DECIMAL(8,4),
    wwz_p5 DECIMAL(8,4),
    
    -- Średnie zarobki z umowy o pracę per rok
    e_zar_etat_p1 DECIMAL(10,2),
    e_zar_etat_p2 DECIMAL(10,2),
    e_zar_etat_p3 DECIMAL(10,2),
    e_zar_etat_p4 DECIMAL(10,2),
    e_zar_etat_p5 DECIMAL(10,2),
    
    -- Czas do znalezienia pracy
    czas_praca DECIMAL(6,2),
    czas_etat DECIMAL(6,2),
    
    -- Czas do pracy z/bez doświadczenia
    czas_praca_dosw DECIMAL(6,2),
    czas_praca_ndosw DECIMAL(6,2),
    czas_etat_dosw DECIMAL(6,2),
    czas_etat_ndosw DECIMAL(6,2),
    
    -- Kwintyle czasu do pracy
    czas_praca_q1 DECIMAL(6,2),
    czas_praca_q2 DECIMAL(6,2),
    czas_praca_q3 DECIMAL(6,2),
    czas_praca_q4 DECIMAL(6,2),
    
    -- Kwintyle czasu do etatu
    czas_etat_q1 DECIMAL(6,2),
    czas_etat_q2 DECIMAL(6,2),
    czas_etat_q3 DECIMAL(6,2),
    czas_etat_q4 DECIMAL(6,2),
    
    -- Wskaźniki geograficzne - kategorie miejscowości
    n_kmz1_najw_miasta INTEGER,
    n_kmz2_miasta_powiatowe INTEGER,
    n_kmz3_mniejsze_miejsc INTEGER,
    
    -- Wskaźniki zarobków per kategoria miejscowości
    e_zar_kmz1 DECIMAL(10,2),
    e_zar_kmz2 DECIMAL(10,2),
    e_zar_kmz3 DECIMAL(10,2),
    
    -- Względny Wskaźnik Zarobków per kategoria miejscowości
    wwz_kmz1 DECIMAL(8,4),
    wwz_kmz2 DECIMAL(8,4),
    wwz_kmz3 DECIMAL(8,4),
    
    -- Wskaźniki kariery akademickiej
    proc_studia DECIMAL(5,2),
    proc_ukon DECIMAL(5,2),
    proc_dyplom DECIMAL(5,2),
    proc_doktoranckie DECIMAL(5,2),
    proc_doktorat DECIMAL(5,2),
    
    -- Kontynuacja studiów II stopnia - ogółem
    if_2st DECIMAL(5,2),
    
    -- Wskaźniki kontynuacji studiów II stopnia per rok
    if_2st_p1 DECIMAL(5,2),
    if_2st_p2 DECIMAL(5,2),
    if_2st_p3 DECIMAL(5,2),
    if_2st_p4 DECIMAL(5,2),
    if_2st_p5 DECIMAL(5,2),
    if_2st_ucz DECIMAL(5,2),
    
    -- Wskaźniki pracodawców
    n_czy_etat INTEGER,
    e_mies_n_pracodawcow DECIMAL(6,2),
    e_mies_n_pracodawcow_q1 DECIMAL(6,2),
    e_mies_n_pracodawcow_q2 DECIMAL(6,2),
    e_mies_n_pracodawcow_q3 DECIMAL(6,2),
    e_mies_n_pracodawcow_q4 DECIMAL(6,2),
    
    -- Wskaźniki rotacji zatrudnienia
    e_roczna_n_koncowetatow DECIMAL(6,2),
    koniecetatu_raz_na_ile_lat DECIMAL(6,2),
    e_roczna_n_koncowetatow_q1 DECIMAL(6,2),
    e_roczna_n_koncowetatow_q2 DECIMAL(6,2),
    e_roczna_n_koncowetatow_q3 DECIMAL(6,2),
    e_roczna_n_koncowetatow_q4 DECIMAL(6,2)
);

-- Indeksy dla poprawienia wydajności zapytań
CREATE INDEX idx_graduates_wojewodztwo ON graduates(wojewodztwo);
CREATE INDEX idx_graduates_poziom ON graduates(poziom);
CREATE INDEX idx_graduates_rok_dyplomu ON graduates(rok_dyplomu);
CREATE INDEX idx_graduates_dziedzina ON graduates(dziedzina);
CREATE INDEX idx_graduates_uczelnia ON graduates(nazwa_uczelni);
CREATE INDEX idx_graduates_kierunek ON graduates(nazwa_kierunku);

-- Indeksy kompozytowe dla popularnych kombinacji filtrów
CREATE INDEX idx_graduates_woj_poziom ON graduates(wojewodztwo, poziom);
CREATE INDEX idx_graduates_woj_rok ON graduates(wojewodztwo, rok_dyplomu);
CREATE INDEX idx_graduates_poziom_rok ON graduates(poziom, rok_dyplomu);
CREATE INDEX idx_graduates_woj_poziom_rok ON graduates(wojewodztwo, poziom, rok_dyplomu);

-- Komentarze do tabeli
COMMENT ON TABLE graduates IS 'Dane absolwentów z informacjami o zatrudnieniu, zarobkach i karierze akademickiej';
COMMENT ON COLUMN graduates.wwz_p3 IS 'Względny Wskaźnik Zarobków w trzecim roku po ukończeniu studiów';
COMMENT ON COLUMN graduates.wwb_p3 IS 'Względny Wskaźnik Bezrobocia w trzecim roku po ukończeniu studiów';
COMMENT ON COLUMN graduates.czy_praca_p3 IS 'Procent zatrudnionych w trzecim roku po ukończeniu studiów';