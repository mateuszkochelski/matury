package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldOfStudyDetailDTO {

    private String uczelniaId;
    private String kierunekId;
    private String poziom;
    private Integer rokDyplomu;

    // Rozkład zarobków (ogółem i etat)
    private BigDecimal eZarP1;
    private BigDecimal eZarP2;
    private BigDecimal eZarP3;
    private BigDecimal eZarP4;
    private BigDecimal eZarP5;

    private BigDecimal eZarEtatP1;
    private BigDecimal eZarEtatP2;
    private BigDecimal eZarEtatP3;
    private BigDecimal eZarEtatP4;
    private BigDecimal eZarEtatP5;

    private BigDecimal meZar;
    private BigDecimal meZarEtat;
    private BigDecimal zarQ1;
    private BigDecimal zarQ2;
    private BigDecimal zarQ3;
    private BigDecimal zarQ4;

    // Indeksy względne roczne
    private BigDecimal wwzP1;
    private BigDecimal wwzP2;
    private BigDecimal wwzP3;
    private BigDecimal wwzP4;
    private BigDecimal wwzP5;

    private BigDecimal wwbP1;
    private BigDecimal wwbP2;
    private BigDecimal wwbP3;
    private BigDecimal wwbP4;
    private BigDecimal wwbP5;

    // Czas do pracy
    private BigDecimal czasPraca;
    private BigDecimal czasPracaQ1;
    private BigDecimal czasPracaQ2;
    private BigDecimal czasPracaQ3;
    private BigDecimal czasPracaQ4;

    private BigDecimal czasEtat;
    private BigDecimal czasEtatQ1;
    private BigDecimal czasEtatQ2;
    private BigDecimal czasEtatQ3;
    private BigDecimal czasEtatQ4;

    // Stabilność (procent miesięcy)
    private BigDecimal procMiesPraca;
    private BigDecimal procMiesEtat;
    private BigDecimal procMiesSamoz;

    // Serie miesięczne WWZ/WWB (miesiąc -> wartość)
    private List<Integer> miesiace;
    private List<BigDecimal> wwzMies;
    private List<BigDecimal> wwbMies;
}


