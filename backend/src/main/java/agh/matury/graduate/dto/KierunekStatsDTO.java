package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class KierunekStatsDTO {
    private String nazwaKierunku;
    private BigDecimal wwzTrzeciRok;        // Względny Wskaźnik Zarobków w 3. roku
    private BigDecimal zarobkiTrzeciRok;    // Średnie zarobki w 3. roku
    private Long liczbaAbsolwentow;         // Liczba absolwentów

    public KierunekStatsDTO(String nazwaKierunku, BigDecimal wwz, BigDecimal zarobki, Long liczbaAbsolwentow) {
        this.nazwaKierunku = nazwaKierunku;
        this.wwzTrzeciRok = wwz;
        this.zarobkiTrzeciRok = zarobki;
        this.liczbaAbsolwentow = liczbaAbsolwentow;
    }
}