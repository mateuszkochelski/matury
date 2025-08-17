package agh.matury.graduate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GraduateRepository extends JpaRepository<Graduate, Long> {

    // Podstawowe filtry
    List<Graduate> findByWojewodztwo(String wojewodztwo);
    List<Graduate> findByPoziom(String poziom);
    List<Graduate> findByRokDyplomu(Integer rokDyplomu);
    List<Graduate> findByNazwaUczelni(String nazwaUczelni);
    List<Graduate> findByDziedzina(String dziedzina);

    // Agregacje dla wskaźników zatrudnienia per województwo
    @Query("SELECT g.wojewodztwo, AVG(g.czyPracaP1), AVG(g.czyPracaP2), AVG(g.czyPracaP3), AVG(g.czyPracaP4), AVG(g.czyPracaP5) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findEmploymentStatsByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje dla wskaźników zatrudnienia per rok po studiach
    @Query("SELECT AVG(g.czyPracaP1) as p1, AVG(g.czyPracaP2) as p2, AVG(g.czyPracaP3) as p3, " +
           "AVG(g.czyPracaP4) as p4, AVG(g.czyPracaP5) as p5 " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu)")
    List<Object[]> findEmploymentByYear(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje dla wskaźników bezrobocia per województwo
    @Query("SELECT g.wojewodztwo, AVG(g.czyBezrP1), AVG(g.czyBezrP2), AVG(g.czyBezrP3), AVG(g.czyBezrP4), AVG(g.czyBezrP5) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findUnemploymentStatsByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje dla Względnego Wskaźnika Bezrobocia per województwo
    @Query("SELECT g.wojewodztwo, AVG(g.wwbP1), AVG(g.wwbP2), AVG(g.wwbP3), AVG(g.wwbP4), AVG(g.wwbP5) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findRelativeUnemploymentIndexByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje dla wskaźników zarobków per województwo
    @Query("SELECT g.wojewodztwo, AVG(g.eZarP1), AVG(g.eZarP2), AVG(g.eZarP3), AVG(g.eZarP4), AVG(g.eZarP5) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findSalaryStatsByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje dla Względnego Wskaźnika Zarobków per województwo
    @Query("SELECT g.wojewodztwo, AVG(g.wwzP1), AVG(g.wwzP2), AVG(g.wwzP3), AVG(g.wwzP4), AVG(g.wwzP5) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findRelativeSalaryIndexByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Dynamiczne zapytanie dla statystyk geograficznych
    @Query("SELECT " +
           "AVG(g.eZarKmz1), AVG(g.eZarKmz2), AVG(g.eZarKmz3), " +
           "AVG(g.wwzKmz1), AVG(g.wwzKmz2), AVG(g.wwzKmz3) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu)")
    List<Object[]> findGeographicStats(@Param("wojewodztwo") String wojewodztwo,
                                       @Param("poziom") String poziom,
                                       @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje dla wskaźników kariery akademickiej per województwo
    @Query("SELECT g.wojewodztwo, AVG(g.procStudia), AVG(g.procUkon), AVG(g.procDyplom), " +
           "AVG(g.procDoktoranckie), AVG(g.procDoktorat) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findAcademicCareerStatsByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje per dziedzina nauki
    @Query("SELECT g.dziedzina, AVG(g.wwzP3), AVG(g.wwbP3), AVG(g.czyPracaP3) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.dziedzina " +
           "ORDER BY AVG(g.wwzP3) DESC")
    List<Object[]> findStatsByDziedzina(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // Agregacje per uczelnia
    @Query("SELECT g.nazwaUczelni, AVG(g.wwzP3), AVG(g.wwbP3), AVG(g.czyPracaP3), COUNT(g) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.nazwaUczelni " +
           "ORDER BY AVG(g.wwzP3) DESC")
    List<Object[]> findStatsByUczelnia(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    @Query("SELECT g.uczelniaId, g.nazwaUczelni, AVG(g.wwzP3), AVG(g.wwbP3), AVG(g.czyEtatP3), SUM(g.liczbaAbsolwentow) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.uczelniaId, g.nazwaUczelni " +
           "HAVING SUM(g.liczbaAbsolwentow) > 10 " +
           "ORDER BY g.nazwaUczelni")
    List<Object[]> getStatsByUczelnia(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu
    );

    @Query("SELECT g.nazwaUczelni, g.nazwaKierunku, AVG(g.wwzP3), AVG(g.wwbP3), AVG(g.czyEtatP3), SUM(g.liczbaAbsolwentow) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.nazwaUczelni, g.nazwaKierunku " +
           "HAVING SUM(g.liczbaAbsolwentow) > :minCount " +
           "ORDER BY g.nazwaUczelni, g.nazwaKierunku")
    List<Object[]> getStatsByUczelniaAndKierunek(
        @Param("wojewodztwo") String wojewodztwo,
        @Param("poziom") String poziom,
        @Param("rokDyplomu") Integer rokDyplomu,
        @Param("minCount") Long minCount
    );

    // ==================== TOP KIERUNKI ====================

    @Query("SELECT g.nazwaKierunku, AVG(g.wwzP3), AVG(g.eZarP3), COUNT(g) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.nazwaKierunku " +
           "HAVING COUNT(g) >= :minCount " +
           "ORDER BY AVG(g.wwzP3) DESC")
    List<Object[]> findTopKierunkiByWwz(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu,
            @Param("minCount") Long minCount);

    // Unikalne wartości dla filtrów
    @Query("SELECT DISTINCT g.wojewodztwo FROM Graduate g ORDER BY g.wojewodztwo")
    List<String> findAllWojewodztwa();

    @Query("SELECT DISTINCT g.poziom FROM Graduate g ORDER BY g.poziom")
    List<String> findAllPoziomy();

    @Query("SELECT DISTINCT g.rokDyplomu FROM Graduate g ORDER BY g.rokDyplomu DESC")
    List<Integer> findAllRokiDyplomu();

    @Query("SELECT DISTINCT g.dziedzina_id FROM Graduate g ORDER BY g.dziedzina_id")
    List<String> findAllDziedziny();

    // ==================== WSKAŹNIKI PRACODAWCÓW I ROTACJI ZATRUDNIENIA ====================
    
    @Query("SELECT g.wojewodztwo, AVG(g.eMiesNPracodawcow), AVG(g.eMiesNPracodawcowQ1), AVG(g.eMiesNPracodawcowQ2), " +
           "AVG(g.eMiesNPracodawcowQ3), AVG(g.eMiesNPracodawcowQ4), " +
           "AVG(g.eRocznaNKoncowetatow), AVG(g.koniecetatuRazNaIleLat), " +
           "AVG(g.eRocznaNKoncowetatowQ1), AVG(g.eRocznaNKoncowetatowQ2), AVG(g.eRocznaNKoncowetatowQ3), AVG(g.eRocznaNKoncowetatowQ4), " +
           "SUM(g.nCzyEtat) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findEmploymentMetricsByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // ==================== CZAS DO PODJĘCIA PRACY ====================
    
    @Query("SELECT AVG(g.czasPraca), AVG(g.czasEtat), " +
           "AVG(g.czasPracaDosw), AVG(g.czasPracaNdosw), " +
           "AVG(g.czasEtatDosw), AVG(g.czasEtatNdosw) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu)")
    List<Object[]> findTimeToEmploymentStats(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    @Query("SELECT AVG(g.czasPracaQ1), AVG(g.czasPracaQ2), AVG(g.czasPracaQ3), AVG(g.czasPracaQ4), " +
           "AVG(g.czasEtatQ1), AVG(g.czasEtatQ2), AVG(g.czasEtatQ3), AVG(g.czasEtatQ4) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu)")
    List<Object[]> findTimeToEmploymentQuintiles(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // ==================== KONTYNUACJA STUDIÓW PO I STOPNIU ====================
    
    @Query("SELECT g.wojewodztwo, AVG(g.if2stP1), AVG(g.if2stP2), AVG(g.if2stP3), AVG(g.if2stP4), AVG(g.if2stP5), " +
           "AVG(g.if2st), AVG(g.if2stUcz) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY g.wojewodztwo")
    List<Object[]> findContinuationStudiesByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    // ==================== PODSTAWOWE INFORMACJE O ABSOLWENTACH ====================
    
    @Query("SELECT g.wojewodztwo, g.rokDyplomu, g.poziom, g.forma, SUM(g.liczbaAbsolwentow), COUNT(g) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo, g.rokDyplomu, g.poziom, g.forma " +
           "ORDER BY g.wojewodztwo, g.rokDyplomu DESC, g.poziom, g.forma")
    List<Object[]> findBasicGraduateInfo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    @Query("SELECT g.wojewodztwo, COUNT(g), SUM(g.liczbaAbsolwentow), " +
           "AVG(CAST(g.rokDyplomu AS FLOAT)), MIN(g.rokDyplomu), MAX(g.rokDyplomu) " +
           "FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu) " +
           "GROUP BY g.wojewodztwo " +
           "ORDER BY SUM(g.liczbaAbsolwentow) DESC")
    List<Object[]> findBasicGraduateSummaryByWojewodztwo(
            @Param("wojewodztwo") String wojewodztwo,
            @Param("poziom") String poziom,
            @Param("rokDyplomu") Integer rokDyplomu);

    @Query("SELECT g.wojewodztwo, g.czyPracaP1, g.czyPracaP2, g.czyPracaP3, g.czyPracaP4, g.czyPracaP5 FROM Graduate g " +
           "WHERE (:wojewodztwo IS NULL OR g.wojewodztwo = :wojewodztwo) " +
           "AND (:poziom IS NULL OR g.poziom = :poziom) " +
           "AND (:rokDyplomu IS NULL OR g.rokDyplomu = :rokDyplomu)")
    List<Object[]> findRawEmploymentData(@Param("wojewodztwo") String wojewodztwo,
                                         @Param("poziom") String poziom,
                                         @Param("rokDyplomu") Integer rokDyplomu);
}