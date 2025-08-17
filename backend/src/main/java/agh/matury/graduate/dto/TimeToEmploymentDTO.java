package agh.matury.graduate.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class TimeToEmploymentDTO {
    private String kategoria; // np. "z doświadczeniem pracy", "bez doświadczenia", "ogółem"
    
    // Średni czas do podjęcia pierwszej pracy (w miesiącach)
    private BigDecimal czasDoPracyOgolem;
    private BigDecimal czasDoPracyZDoswiadczeniem;
    private BigDecimal czasDoPracyBezDoswiadczenia;
    
    // Średni czas do podjęcia pierwszej pracy na umowę o pracę (w miesiącach)
    private BigDecimal czasDoEtatuOgolem;
    private BigDecimal czasDoEtatuZDoswiadczeniem;
    private BigDecimal czasDoEtatuBezDoswiadczenia;
    
    // Kwintyle czasu do pracy
    private BigDecimal czasPracaQ1;
    private BigDecimal czasPracaQ2;
    private BigDecimal czasPracaQ3;
    private BigDecimal czasPracaQ4;
    
    // Kwintyle czasu do etatu
    private BigDecimal czasEtatQ1;
    private BigDecimal czasEtatQ2;
    private BigDecimal czasEtatQ3;
    private BigDecimal czasEtatQ4;

    public TimeToEmploymentDTO(String kategoria, BigDecimal czasPracy, BigDecimal czasEtatu,
                              BigDecimal czasPracaZDosw, BigDecimal czasPracaBezDosw,
                              BigDecimal czasEtatZDosw, BigDecimal czasEtatBezDosw,
                              BigDecimal czasPracaQ1, BigDecimal czasPracaQ2, BigDecimal czasPracaQ3, BigDecimal czasPracaQ4,
                              BigDecimal czasEtatQ1, BigDecimal czasEtatQ2, BigDecimal czasEtatQ3, BigDecimal czasEtatQ4) {
        this.kategoria = kategoria;
        this.czasDoPracyOgolem = czasPracy;
        this.czasDoEtatuOgolem = czasEtatu;
        this.czasDoPracyZDoswiadczeniem = czasPracaZDosw;
        this.czasDoPracyBezDoswiadczenia = czasPracaBezDosw;
        this.czasDoEtatuZDoswiadczeniem = czasEtatZDosw;
        this.czasDoEtatuBezDoswiadczenia = czasEtatBezDosw;
        this.czasPracaQ1 = czasPracaQ1;
        this.czasPracaQ2 = czasPracaQ2;
        this.czasPracaQ3 = czasPracaQ3;
        this.czasPracaQ4 = czasPracaQ4;
        this.czasEtatQ1 = czasEtatQ1;
        this.czasEtatQ2 = czasEtatQ2;
        this.czasEtatQ3 = czasEtatQ3;
        this.czasEtatQ4 = czasEtatQ4;
    }
}