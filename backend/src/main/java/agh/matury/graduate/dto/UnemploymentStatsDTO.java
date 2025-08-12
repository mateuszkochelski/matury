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
public class UnemploymentStatsDTO {
    private String wojewodztwo;
    private BigDecimal bezrobociePierwszyRok;
    private BigDecimal bezrobocieDrugiRok;
    private BigDecimal bezrobocieTrzeciRok;
    private BigDecimal bezrobocieCzwartyRok;
    private BigDecimal bezrobociePiatyRok;
    private BigDecimal srednieBezrobocie;
    
    // Względny Wskaźnik Bezrobocia
    private BigDecimal wwbPierwszyRok;
    private BigDecimal wwbDrugiRok;
    private BigDecimal wwbTrzeciRok;
    private BigDecimal wwbCzwartyRok;
    private BigDecimal wwbPiatyRok;
    private BigDecimal sredniaWwb;

    public UnemploymentStatsDTO(String wojewodztwo, BigDecimal bezr1, BigDecimal bezr2, 
                               BigDecimal bezr3, BigDecimal bezr4, BigDecimal bezr5) {
        this.wojewodztwo = wojewodztwo;
        this.bezrobociePierwszyRok = bezr1;
        this.bezrobocieDrugiRok = bezr2;
        this.bezrobocieTrzeciRok = bezr3;
        this.bezrobocieCzwartyRok = bezr4;
        this.bezrobociePiatyRok = bezr5;
        
        // Oblicz średnie bezrobocie
        if (bezr1 != null && bezr2 != null && bezr3 != null && bezr4 != null && bezr5 != null) {
            this.srednieBezrobocie = bezr1.add(bezr2).add(bezr3).add(bezr4).add(bezr5)
                .divide(BigDecimal.valueOf(5), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    // Konstruktor dla WWB
    public UnemploymentStatsDTO(String wojewodztwo, BigDecimal wwb1, BigDecimal wwb2,
                               BigDecimal wwb3, BigDecimal wwb4, BigDecimal wwb5, boolean isWwb) {
        this.wojewodztwo = wojewodztwo;
        this.wwbPierwszyRok = wwb1;
        this.wwbDrugiRok = wwb2;
        this.wwbTrzeciRok = wwb3;
        this.wwbCzwartyRok = wwb4;
        this.wwbPiatyRok = wwb5;
        
        // Oblicz średnią WWB
        if (wwb1 != null && wwb2 != null && wwb3 != null && wwb4 != null && wwb5 != null) {
            this.sredniaWwb = wwb1.add(wwb2).add(wwb3).add(wwb4).add(wwb5)
                .divide(BigDecimal.valueOf(5), 4, BigDecimal.ROUND_HALF_UP);
        }
    }
}