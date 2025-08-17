package agh.matury.graduate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "field_of_study_monthly_index")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldOfStudyMonthlyIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Klucze identyfikujące program/kierunek w danym roku
    @Column(name = "uczelnia_id")
    private String uczelniaId;

    @Column(name = "kierunek_id")
    private String kierunekId;

    @Column(name = "poziom")
    private String poziom;

    @Column(name = "rok_dyplomu")
    private Integer rokDyplomu;

    // Numer miesiąca po dyplomie (1..N)
    @Column(name = "miesiac")
    private Integer miesiac;

    // Względny Wskaźnik Zarobków i Bezrobocia w danym miesiącu
    @Column(name = "wwz", precision = 8, scale = 4)
    private BigDecimal wwz;

    @Column(name = "wwb", precision = 8, scale = 4)
    private BigDecimal wwb;
}


