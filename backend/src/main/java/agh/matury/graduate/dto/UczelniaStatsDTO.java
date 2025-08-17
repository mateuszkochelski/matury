package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class UczelniaStatsDTO {
    private String nazwaUczelni;
    private BigDecimal wwzTrzeciRok;           // Względny Wskaźnik Zarobków w 3. roku
    private BigDecimal wwbTrzeciRok;           // Względny Wskaźnik Bezrobocia w 3. roku
    private BigDecimal zatrudnienieTrzeciRok;  // Procent zatrudnionych w 3. roku
    private Long liczbaAbsolwentow;            // Liczba absolwentów

    public UczelniaStatsDTO(String nazwaUczelni, BigDecimal wwz, BigDecimal wwb, 
                           BigDecimal zatrudnienie, Long liczbaAbsolwentow) {
        this.nazwaUczelni = nazwaUczelni;
        this.wwzTrzeciRok = wwz;
        this.wwbTrzeciRok = wwb;
        this.zatrudnienieTrzeciRok = zatrudnienie;
        this.liczbaAbsolwentow = liczbaAbsolwentow;
    }
}