package agh.matury.graduate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "graduates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Graduate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Podstawowe informacje o absolwencie
    @Column(name = "rok_dyplomu")
    private Integer rokDyplomu;

    @Column(name = "kierunek_id")
    private String kierunekId;

    @Column(name = "poziom")
    private String poziom;

    @Column(name = "forma")
    private String forma;

    @Column(name = "liczba_absolwentow")
    private Integer liczbaAbsolwentow;

    // Informacje o uczelni i lokalizacji
    @Column(name = "nazwa_kierunku")
    private String nazwaKierunku;

    @Column(name = "nazwa_specjalnosci")
    private String nazwaSpecjalnosci;

    @Column(name = "uczelnia_id")
    private String uczelniaId;

    @Column(name = "nazwa_uczelni")
    private String nazwaUczelni;

    @Column(name = "jednostka_id")
    private String jednostkaId;

    @Column(name = "nazwa_jednostki")
    private String nazwaJednostki;

    @Column(name = "profil")
    private String profil;

    @Column(name = "dziedzina_id")
    private String dziedzina_id;

    @Column(name = "dziedzina")
    private String dziedzina;

    @Column(name = "wojewodztwo")
    private String wojewodztwo;

    // Wskaźniki zatrudnienia - podstawowe
    @Column(name = "proc_w_zus", precision = 5, scale = 2)
    private BigDecimal procWZus;

    @Column(name = "proc_poza_zus", precision = 5, scale = 2)
    private BigDecimal procPozaZus;

    @Column(name = "proc_dosw", precision = 5, scale = 2)
    private BigDecimal procDosw;

    // Wskaźniki zatrudnienia per rok
    @Column(name = "czy_praca_p1", precision = 5, scale = 2)
    private BigDecimal czyPracaP1;

    @Column(name = "czy_praca_p2", precision = 5, scale = 2)
    private BigDecimal czyPracaP2;

    @Column(name = "czy_praca_p3", precision = 5, scale = 2)
    private BigDecimal czyPracaP3;

    @Column(name = "czy_praca_p4", precision = 5, scale = 2)
    private BigDecimal czyPracaP4;

    @Column(name = "czy_praca_p5", precision = 5, scale = 2)
    private BigDecimal czyPracaP5;

    // Wskaźniki zatrudnienia na umowę o pracę per rok
    @Column(name = "czy_etat_p1", precision = 5, scale = 2)
    private BigDecimal czyEtatP1;

    @Column(name = "czy_etat_p2", precision = 5, scale = 2)
    private BigDecimal czyEtatP2;

    @Column(name = "czy_etat_p3", precision = 5, scale = 2)
    private BigDecimal czyEtatP3;

    @Column(name = "czy_etat_p4", precision = 5, scale = 2)
    private BigDecimal czyEtatP4;

    @Column(name = "czy_etat_p5", precision = 5, scale = 2)
    private BigDecimal czyEtatP5;

    // Wskaźniki samozatrudnienia per rok
    @Column(name = "czy_samoz_p1", precision = 5, scale = 2)
    private BigDecimal czySamozP1;

    @Column(name = "czy_samoz_p2", precision = 5, scale = 2)
    private BigDecimal czySamozP2;

    @Column(name = "czy_samoz_p3", precision = 5, scale = 2)
    private BigDecimal czySamozP3;

    @Column(name = "czy_samoz_p4", precision = 5, scale = 2)
    private BigDecimal czySamozP4;

    @Column(name = "czy_samoz_p5", precision = 5, scale = 2)
    private BigDecimal czySamozP5;

    // Wskaźniki bezrobocia per rok
    @Column(name = "czy_bezr_p1", precision = 5, scale = 2)
    private BigDecimal czyBezrP1;

    @Column(name = "czy_bezr_p2", precision = 5, scale = 2)
    private BigDecimal czyBezrP2;

    @Column(name = "czy_bezr_p3", precision = 5, scale = 2)
    private BigDecimal czyBezrP3;

    @Column(name = "czy_bezr_p4", precision = 5, scale = 2)
    private BigDecimal czyBezrP4;

    @Column(name = "czy_bezr_p5", precision = 5, scale = 2)
    private BigDecimal czyBezrP5;

    // Względny Wskaźnik Bezrobocia per rok
    @Column(name = "wwb_p1", precision = 8, scale = 4)
    private BigDecimal wwbP1;

    @Column(name = "wwb_p2", precision = 8, scale = 4)
    private BigDecimal wwbP2;

    @Column(name = "wwb_p3", precision = 8, scale = 4)
    private BigDecimal wwbP3;

    @Column(name = "wwb_p4", precision = 8, scale = 4)
    private BigDecimal wwbP4;

    @Column(name = "wwb_p5", precision = 8, scale = 4)
    private BigDecimal wwbP5;

    // Średnie zarobki per rok
    @Column(name = "e_zar_p1", precision = 10, scale = 2)
    private BigDecimal eZarP1;

    @Column(name = "e_zar_p2", precision = 10, scale = 2)
    private BigDecimal eZarP2;

    @Column(name = "e_zar_p3", precision = 10, scale = 2)
    private BigDecimal eZarP3;

    @Column(name = "e_zar_p4", precision = 10, scale = 2)
    private BigDecimal eZarP4;

    @Column(name = "e_zar_p5", precision = 10, scale = 2)
    private BigDecimal eZarP5;

    // Względny Wskaźnik Zarobków per rok
    @Column(name = "wwz_p1", precision = 8, scale = 4)
    private BigDecimal wwzP1;

    @Column(name = "wwz_p2", precision = 8, scale = 4)
    private BigDecimal wwzP2;

    @Column(name = "wwz_p3", precision = 8, scale = 4)
    private BigDecimal wwzP3;

    @Column(name = "wwz_p4", precision = 8, scale = 4)
    private BigDecimal wwzP4;

    @Column(name = "wwz_p5", precision = 8, scale = 4)
    private BigDecimal wwzP5;

    // Średnie zarobki z umowy o pracę per rok
    @Column(name = "e_zar_etat_p1", precision = 10, scale = 2)
    private BigDecimal eZarEtatP1;

    @Column(name = "e_zar_etat_p2", precision = 10, scale = 2)
    private BigDecimal eZarEtatP2;

    @Column(name = "e_zar_etat_p3", precision = 10, scale = 2)
    private BigDecimal eZarEtatP3;

    @Column(name = "e_zar_etat_p4", precision = 10, scale = 2)
    private BigDecimal eZarEtatP4;

    @Column(name = "e_zar_etat_p5", precision = 10, scale = 2)
    private BigDecimal eZarEtatP5;

    // Czas do znalezienia pracy
    @Column(name = "czas_praca", precision = 6, scale = 2)
    private BigDecimal czasPraca;

    @Column(name = "czas_etat", precision = 6, scale = 2)
    private BigDecimal czasEtat;

    // Czas do pracy z/bez doświadczenia
    @Column(name = "czas_praca_dosw", precision = 6, scale = 2)
    private BigDecimal czasPracaDosw;

    @Column(name = "czas_praca_ndosw", precision = 6, scale = 2)
    private BigDecimal czasPracaNdosw;

    @Column(name = "czas_etat_dosw", precision = 6, scale = 2)
    private BigDecimal czasEtatDosw;

    @Column(name = "czas_etat_ndosw", precision = 6, scale = 2)
    private BigDecimal czasEtatNdosw;

    // Kwintyle czasu do pracy
    @Column(name = "czas_praca_q1", precision = 6, scale = 2)
    private BigDecimal czasPracaQ1;

    @Column(name = "czas_praca_q2", precision = 6, scale = 2)
    private BigDecimal czasPracaQ2;

    @Column(name = "czas_praca_q3", precision = 6, scale = 2)
    private BigDecimal czasPracaQ3;

    @Column(name = "czas_praca_q4", precision = 6, scale = 2)
    private BigDecimal czasPracaQ4;

    // Kwintyle czasu do etatu
    @Column(name = "czas_etat_q1", precision = 6, scale = 2)
    private BigDecimal czasEtatQ1;

    @Column(name = "czas_etat_q2", precision = 6, scale = 2)
    private BigDecimal czasEtatQ2;

    @Column(name = "czas_etat_q3", precision = 6, scale = 2)
    private BigDecimal czasEtatQ3;

    @Column(name = "czas_etat_q4", precision = 6, scale = 2)
    private BigDecimal czasEtatQ4;

    // Kontynuacja studiów II stopnia - ogółem
    @Column(name = "if_2st", precision = 5, scale = 2)
    private BigDecimal if2st;

    // Wskaźniki geograficzne - kategorie miejscowości
    @Column(name = "n_kmz1_najw_miasta")
    private Integer nKmz1NajwMiasta;

    @Column(name = "n_kmz2_miasta_powiatowe")
    private Integer nKmz2MiastaPowiatowe;

    @Column(name = "n_kmz3_mniejsze_miejsc")
    private Integer nKmz3MniejszeMiejsc;

    // Wskaźniki zarobków per kategoria miejscowości
    @Column(name = "e_zar_kmz1", precision = 10, scale = 2)
    private BigDecimal eZarKmz1;

    @Column(name = "e_zar_kmz2", precision = 10, scale = 2)
    private BigDecimal eZarKmz2;

    @Column(name = "e_zar_kmz3", precision = 10, scale = 2)
    private BigDecimal eZarKmz3;

    // Względny Wskaźnik Zarobków per kategoria miejscowości
    @Column(name = "wwz_kmz1", precision = 8, scale = 4)
    private BigDecimal wwzKmz1;

    @Column(name = "wwz_kmz2", precision = 8, scale = 4)
    private BigDecimal wwzKmz2;

    @Column(name = "wwz_kmz3", precision = 8, scale = 4)
    private BigDecimal wwzKmz3;

    // Wskaźniki kariery akademickiej
    @Column(name = "proc_studia", precision = 5, scale = 2)
    private BigDecimal procStudia;

    @Column(name = "proc_ukon", precision = 5, scale = 2)
    private BigDecimal procUkon;

    @Column(name = "proc_dyplom", precision = 5, scale = 2)
    private BigDecimal procDyplom;

    @Column(name = "proc_doktoranckie", precision = 5, scale = 2)
    private BigDecimal procDoktoranckie;

    @Column(name = "proc_doktorat", precision = 5, scale = 2)
    private BigDecimal procDoktorat;

    // Wskaźniki kontynuacji studiów II stopnia per rok
    @Column(name = "if_2st_p1", precision = 5, scale = 2)
    private BigDecimal if2stP1;

    @Column(name = "if_2st_p2", precision = 5, scale = 2)
    private BigDecimal if2stP2;

    @Column(name = "if_2st_p3", precision = 5, scale = 2)
    private BigDecimal if2stP3;

    @Column(name = "if_2st_p4", precision = 5, scale = 2)
    private BigDecimal if2stP4;

    @Column(name = "if_2st_p5", precision = 5, scale = 2)
    private BigDecimal if2stP5;

    @Column(name = "if_2st_ucz", precision = 5, scale = 2)
    private BigDecimal if2stUcz;

    // Wskaźniki pracodawców
    @Column(name = "n_czy_etat")
    private Integer nCzyEtat;

    @Column(name = "e_mies_n_pracodawcow", precision = 6, scale = 2)
    private BigDecimal eMiesNPracodawcow;

    @Column(name = "e_mies_n_pracodawcow_q1", precision = 6, scale = 2)
    private BigDecimal eMiesNPracodawcowQ1;

    @Column(name = "e_mies_n_pracodawcow_q2", precision = 6, scale = 2)
    private BigDecimal eMiesNPracodawcowQ2;

    @Column(name = "e_mies_n_pracodawcow_q3", precision = 6, scale = 2)
    private BigDecimal eMiesNPracodawcowQ3;

    @Column(name = "e_mies_n_pracodawcow_q4", precision = 6, scale = 2)
    private BigDecimal eMiesNPracodawcowQ4;

    // Wskaźniki rotacji zatrudnienia
    @Column(name = "e_roczna_n_koncowetatow", precision = 6, scale = 2)
    private BigDecimal eRocznaNKoncowetatow;

    @Column(name = "koniecetatu_raz_na_ile_lat", precision = 6, scale = 2)
    private BigDecimal koniecetatuRazNaIleLat;

    @Column(name = "e_roczna_n_koncowetatow_q1", precision = 6, scale = 2)
    private BigDecimal eRocznaNKoncowetatowQ1;

    @Column(name = "e_roczna_n_koncowetatow_q2", precision = 6, scale = 2)
    private BigDecimal eRocznaNKoncowetatowQ2;

    @Column(name = "e_roczna_n_koncowetatow_q3", precision = 6, scale = 2)
    private BigDecimal eRocznaNKoncowetatowQ3;

    @Column(name = "e_roczna_n_koncowetatow_q4", precision = 6, scale = 2)
    private BigDecimal eRocznaNKoncowetatowQ4;
}