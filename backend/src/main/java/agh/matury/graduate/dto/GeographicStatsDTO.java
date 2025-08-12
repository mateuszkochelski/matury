package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class GeographicStatsDTO {
    // Średnie zarobki per kategoria miejscowości
    private BigDecimal zarobkiNajwiekszeMiasta;
    private BigDecimal zarobkiMiastaPowiatowe;
    private BigDecimal zarobkiMniejszeMiejscowosci;
    
    // Względny Wskaźnik Zarobków per kategoria miejscowości
    private BigDecimal wwzNajwiekszeMiasta;
    private BigDecimal wwzMiastaPowiatowe;
    private BigDecimal wwzMniejszeMiejscowosci;

    public GeographicStatsDTO(BigDecimal zarKmz1, BigDecimal zarKmz2, BigDecimal zarKmz3,
                             BigDecimal wwzKmz1, BigDecimal wwzKmz2, BigDecimal wwzKmz3) {
        this.zarobkiNajwiekszeMiasta = zarKmz1;
        this.zarobkiMiastaPowiatowe = zarKmz2;
        this.zarobkiMniejszeMiejscowosci = zarKmz3;
        this.wwzNajwiekszeMiasta = wwzKmz1;
        this.wwzMiastaPowiatowe = wwzKmz2;
        this.wwzMniejszeMiejscowosci = wwzKmz3;
    }
}