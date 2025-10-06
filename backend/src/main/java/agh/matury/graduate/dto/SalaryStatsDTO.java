package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStatsDTO {
    private String wojewodztwo;
    
    // Średnie zarobki per rok
    private BigDecimal zarobkiPierwszyRok;
    private BigDecimal zarobkiDrugiRok;
    private BigDecimal zarobkiTrzeciRok;
    private BigDecimal zarobkiCzwartyRok;
    private BigDecimal zarobkiPiatyRok;
    private BigDecimal srednieZarobki;
    
    // Względny Wskaźnik Zarobków
    private BigDecimal wwzPierwszyRok;
    private BigDecimal wwzDrugiRok;
    private BigDecimal wwzTrzeciRok;
    private BigDecimal wwzCzwartyRok;
    private BigDecimal wwzPiatyRok;
    private BigDecimal sredniaWwz;

    public SalaryStatsDTO(String wojewodztwo, BigDecimal zar1, BigDecimal zar2,
                         BigDecimal zar3, BigDecimal zar4, BigDecimal zar5) {
        this.wojewodztwo = wojewodztwo;
        this.zarobkiPierwszyRok = zar1;
        this.zarobkiDrugiRok = zar2;
        this.zarobkiTrzeciRok = zar3;
        this.zarobkiCzwartyRok = zar4;
        this.zarobkiPiatyRok = zar5;
        
        // Oblicz średnie zarobki
        if (zar1 != null && zar2 != null && zar3 != null && zar4 != null && zar5 != null) {
            this.srednieZarobki = zar1.add(zar2).add(zar3).add(zar4).add(zar5)
                .divide(BigDecimal.valueOf(5), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    // Konstruktor dla WWZ
    public SalaryStatsDTO(String wojewodztwo, BigDecimal wwz1, BigDecimal wwz2,
                         BigDecimal wwz3, BigDecimal wwz4, BigDecimal wwz5, boolean isWwz) {
        this.wojewodztwo = wojewodztwo;
        this.wwzPierwszyRok = wwz1;
        this.wwzDrugiRok = wwz2;
        this.wwzTrzeciRok = wwz3;
        this.wwzCzwartyRok = wwz4;
        this.wwzPiatyRok = wwz5;
        
        // Oblicz średnią WWZ
        if (wwz1 != null && wwz2 != null && wwz3 != null && wwz4 != null && wwz5 != null) {
            this.sredniaWwz = wwz1.add(wwz2).add(wwz3).add(wwz4).add(wwz5)
                .divide(BigDecimal.valueOf(5), 4, BigDecimal.ROUND_HALF_UP);
        }
    }
}