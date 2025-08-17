package agh.matury.graduate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldOfStudyMonthlyIndexRepository extends JpaRepository<FieldOfStudyMonthlyIndex, Long> {

    @Query("SELECT f.miesiac, AVG(f.wwz), AVG(f.wwb) FROM FieldOfStudyMonthlyIndex f " +
           "WHERE (:uczelniaId IS NULL OR f.uczelniaId = :uczelniaId) " +
           "AND (:kierunekId IS NULL OR f.kierunekId = :kierunekId) " +
           "AND (:poziom IS NULL OR f.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR f.rokDyplomu = :rokDyplomu) " +
           "GROUP BY f.miesiac ORDER BY f.miesiac")
    List<Object[]> findMonthlySeries(
            @Param("uczelniaId") String uczelniaId,
            @Param("kierunekId") String kierunekId,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu
    );
}


