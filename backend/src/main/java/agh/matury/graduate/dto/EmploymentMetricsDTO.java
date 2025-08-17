package agh.matury.graduate.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class EmploymentMetricsDTO {
    private String wojewodztwo;
    
    // Średnia miesięczna liczba pracodawców
    private BigDecimal sredniaMiesięcznaLiczbaPracodawcow;
    private BigDecimal kwintyl1LiczbaPracodawcow;
    private BigDecimal kwintyl2LiczbaPracodawcow;
    private BigDecimal kwintyl3LiczbaPracodawcow;
    private BigDecimal kwintyl4LiczbaPracodawcow;
    
    // Średnia roczna liczba przypadków zakończenia pracy etatowej
    private BigDecimal sredniaNRocznychZakonczenEtatow;
    private BigDecimal koniecetatuRazNaIleLat;
    private BigDecimal kwintyl1ZakonczenEtatow;
    private BigDecimal kwintyl2ZakonczenEtatow;
    private BigDecimal kwintyl3ZakonczenEtatow;
    private BigDecimal kwintyl4ZakonczenEtatow;
    
    // Liczba absolwentów z doświadczeniem pracy na umowę o pracę
    private Long liczbaAbsolwentowZEtatem;

    public EmploymentMetricsDTO(String wojewodztwo, BigDecimal sredniaPracodawcy, 
                               BigDecimal q1Pracodawcy, BigDecimal q2Pracodawcy, BigDecimal q3Pracodawcy, BigDecimal q4Pracodawcy,
                               BigDecimal sredniaZakonczenia, BigDecimal razNaIleLat,
                               BigDecimal q1Zakonczenia, BigDecimal q2Zakonczenia, BigDecimal q3Zakonczenia, BigDecimal q4Zakonczenia,
                               Long liczbaEtatow) {
        this.wojewodztwo = wojewodztwo;
        this.sredniaMiesięcznaLiczbaPracodawcow = sredniaPracodawcy;
        this.kwintyl1LiczbaPracodawcow = q1Pracodawcy;
        this.kwintyl2LiczbaPracodawcow = q2Pracodawcy;
        this.kwintyl3LiczbaPracodawcow = q3Pracodawcy;
        this.kwintyl4LiczbaPracodawcow = q4Pracodawcy;
        this.sredniaNRocznychZakonczenEtatow = sredniaZakonczenia;
        this.koniecetatuRazNaIleLat = razNaIleLat;
        this.kwintyl1ZakonczenEtatow = q1Zakonczenia;
        this.kwintyl2ZakonczenEtatow = q2Zakonczenia;
        this.kwintyl3ZakonczenEtatow = q3Zakonczenia;
        this.kwintyl4ZakonczenEtatow = q4Zakonczenia;
        this.liczbaAbsolwentowZEtatem = liczbaEtatow;
    }
}