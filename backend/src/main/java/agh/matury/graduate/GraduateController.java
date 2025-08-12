package agh.matury.graduate;

import agh.matury.graduate.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/graduates")
@RequiredArgsConstructor
@Tag(name = "Graduates", description = "API do zarządzania danymi absolwentów")
@CrossOrigin(origins = "*")
public class GraduateController {

    private final GraduateService graduateService;

    // ==================== FILTRY ====================

    @GetMapping("/filtry/wojewodztwa")
    @Operation(summary = "Pobierz wszystkie województwa", description = "Zwraca listę wszystkich województw dostępnych w bazie danych")
    public ResponseEntity<List<String>> getAllWojewodztwa() {
        return ResponseEntity.ok(graduateService.getAllWojewodztwa());
    }

    @GetMapping("/filtry/poziomy")
    @Operation(summary = "Pobierz wszystkie poziomy studiów", description = "Zwraca listę wszystkich poziomów studiów (I stopień, II stopień, jednolite itp.)")
    public ResponseEntity<List<String>> getAllPoziomy() {
        return ResponseEntity.ok(graduateService.getAllPoziomy());
    }

    @GetMapping("/filtry/lata")
    @Operation(summary = "Pobierz wszystkie lata ukończenia", description = "Zwraca listę wszystkich lat ukończenia studiów")
    public ResponseEntity<List<Integer>> getAllRokiDyplomu() {
        return ResponseEntity.ok(graduateService.getAllRokiDyplomu());
    }

    @GetMapping("/filtry/dziedziny")
    @Operation(summary = "Pobierz wszystkie dziedziny nauki", description = "Zwraca listę wszystkich dziedzin nauki")
    public ResponseEntity<List<String>> getAllDziedziny() {
        return ResponseEntity.ok(graduateService.getAllDziedziny());
    }

    // ==================== WSKAŹNIKI ZATRUDNIENIA ====================

    @GetMapping("/statystyki/zatrudnienie/wojewodztwa")
    @Operation(summary = "Statystyki zatrudnienia per województwo", 
               description = "Średni Względny Wskaźnik Zatrudnienia wśród absolwentów per województwo per rok po studiach")
    public ResponseEntity<List<EmploymentStatsDTO>> getEmploymentStatsByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<EmploymentStatsDTO> stats = graduateService.getEmploymentStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statystyki/zatrudnienie/wojewodztwa/zaawansowane")
    @Operation(summary = "Zaawansowane statystyki zatrudnienia per województwo",
               description = "Zwraca zaawansowane statystyki (średnia, mediana, odch. std, min, max) dla wskaźników zatrudnienia.")
    public ResponseEntity<List<AdvancedEmploymentStatsDTO>> getAdvancedEmploymentStatsByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {

        List<AdvancedEmploymentStatsDTO> stats = graduateService.getAdvancedEmploymentStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== WSKAŹNIKI BEZROBOCIA ====================

    @GetMapping("/statystyki/bezrobocie/wojewodztwa")
    @Operation(summary = "Statystyki bezrobocia per województwo", 
               description = "Procent absolwentów doświadczających bezrobocia per województwo per rok po studiach")
    public ResponseEntity<List<UnemploymentStatsDTO>> getUnemploymentStatsByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<UnemploymentStatsDTO> stats = graduateService.getUnemploymentStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statystyki/bezrobocie/wwb/wojewodztwa")
    @Operation(summary = "Względny Wskaźnik Bezrobocia per województwo", 
               description = "Średni Względny Wskaźnik Bezrobocia wśród absolwentów per województwo per rok po studiach")
    public ResponseEntity<List<UnemploymentStatsDTO>> getRelativeUnemploymentIndexByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<UnemploymentStatsDTO> stats = graduateService.getRelativeUnemploymentIndexByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== WSKAŹNIKI ZAROBKÓW ====================

    @GetMapping("/statystyki/zarobki/wojewodztwa")
    @Operation(summary = "Statystyki zarobków per województwo", 
               description = "Średnie miesięczne wynagrodzenie absolwentów per województwo per rok po studiach")
    public ResponseEntity<List<SalaryStatsDTO>> getSalaryStatsByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<SalaryStatsDTO> stats = graduateService.getSalaryStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statystyki/zarobki/wwz/wojewodztwa")
    @Operation(summary = "Względny Wskaźnik Zarobków per województwo", 
               description = "Średni Względny Wskaźnik Zarobków wśród absolwentów per województwo per rok po studiach")
    public ResponseEntity<List<SalaryStatsDTO>> getRelativeSalaryIndexByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<SalaryStatsDTO> stats = graduateService.getRelativeSalaryIndexByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== WSKAŹNIKI GEOGRAFICZNE ====================

    @GetMapping("/statystyki/geograficzne")
    @Operation(summary = "Statystyki geograficzne", 
               description = "Średnie zarobki i wskaźniki per kategoria miejscowości (największe miasta, miasta powiatowe, mniejsze miejscowości)")
    public ResponseEntity<GeographicStatsDTO> getGeographicStats(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        GeographicStatsDTO stats = graduateService.getGeographicStats(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== WSKAŹNIKI KARIERY AKADEMICKIEJ ====================

    @GetMapping("/statystyki/kariera-akademicka/wojewodztwa")
    @Operation(summary = "Statystyki kariery akademickiej per województwo", 
               description = "Procent absolwentów kontynuujących edukację per województwo")
    public ResponseEntity<List<AcademicCareerStatsDTO>> getAcademicCareerStatsByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<AcademicCareerStatsDTO> stats = graduateService.getAcademicCareerStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== AGREGACJE PER DZIEDZINA NAUKI ====================

    @GetMapping("/statystyki/dziedziny")
    @Operation(summary = "Statystyki per dziedzina nauki", 
               description = "Względny Wskaźnik Zarobków, Bezrobocia i zatrudnienie w trzecim roku per dziedzina nauki")
    public ResponseEntity<List<DziedzinaStatsDTO>> getStatsByDziedzina(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<DziedzinaStatsDTO> stats = graduateService.getStatsByDziedzina(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== AGREGACJE PER UCZELNIA ====================

    @GetMapping("/statystyki/uczelnie")
    @Operation(summary = "Statystyki per uczelnia", 
               description = "Względny Wskaźnik Zarobków, Bezrobocia i zatrudnienie w trzecim roku per uczelnia")
    public ResponseEntity<List<UczelniaStatsDTO>> getStatsByUczelnia(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<UczelniaStatsDTO> stats = graduateService.getStatsByUczelnia(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== TOP KIERUNKI ====================

    @GetMapping("/ranking/kierunki")
    @Operation(summary = "Top kierunki pod względem zarobków", 
               description = "Ranking kierunków studiów pod względem Względnego Wskaźnika Zarobków w trzecim roku po studiach")
    public ResponseEntity<List<KierunekStatsDTO>> getTopKierunkiByWwz(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu,
            @Parameter(description = "Minimalna liczba absolwentów (domyślnie 10)") @RequestParam(required = false) Long minCount) {
        
        List<KierunekStatsDTO> stats = graduateService.getTopKierunkiByWwz(wojewodztwo, poziom, rokDyplomu, minCount);
        return ResponseEntity.ok(stats);
    }

    // ==================== PODSTAWOWE CRUD ====================

    @GetMapping
    @Operation(summary = "Pobierz wszystkich absolwentów", description = "Zwraca listę wszystkich absolwentów w bazie danych")
    public ResponseEntity<List<Graduate>> getAllGraduates() {
        return ResponseEntity.ok(graduateService.getAllGraduates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz absolwenta po ID", description = "Zwraca dane absolwenta o podanym ID")
    public ResponseEntity<Graduate> getGraduateById(@PathVariable Long id) {
        Graduate graduate = graduateService.getGraduateById(id);
        if (graduate != null) {
            return ResponseEntity.ok(graduate);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Dodaj nowego absolwenta", description = "Tworzy nowy rekord absolwenta w bazie danych")
    public ResponseEntity<Graduate> createGraduate(@RequestBody Graduate graduate) {
        Graduate savedGraduate = graduateService.saveGraduate(graduate);
        return ResponseEntity.ok(savedGraduate);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj absolwenta", description = "Aktualizuje dane absolwenta o podanym ID")
    public ResponseEntity<Graduate> updateGraduate(@PathVariable Long id, @RequestBody Graduate graduate) {
        Graduate existingGraduate = graduateService.getGraduateById(id);
        if (existingGraduate != null) {
            graduate.setId(id);
            Graduate updatedGraduate = graduateService.saveGraduate(graduate);
            return ResponseEntity.ok(updatedGraduate);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń absolwenta", description = "Usuwa absolwenta o podanym ID z bazy danych")
    public ResponseEntity<Void> deleteGraduate(@PathVariable Long id) {
        Graduate existingGraduate = graduateService.getGraduateById(id);
        if (existingGraduate != null) {
            graduateService.deleteGraduate(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== WSKAŹNIKI PRACODAWCÓW I ROTACJI ZATRUDNIENIA ====================

    @GetMapping("/statystyki/zatrudnienie/pracodawcy/wojewodztwa")
    @Operation(summary = "Statystyki pracodawców i rotacji zatrudnienia per województwo", 
               description = "Średnia miesięczna liczba pracodawców i średnia roczna liczba zakończeń etatów per województwo")
    public ResponseEntity<List<EmploymentMetricsDTO>> getEmploymentMetricsByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<EmploymentMetricsDTO> stats = graduateService.getEmploymentMetricsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== CZAS DO PODJĘCIA PRACY ====================

    @GetMapping("/statystyki/czas-do-pracy")
    @Operation(summary = "Czas do podjęcia pierwszej pracy po studiach", 
               description = "Średni czas (w miesiącach) od uzyskania dyplomu do podjęcia pierwszej pracy i pierwszej pracy na umowę o pracę")
    public ResponseEntity<List<TimeToEmploymentDTO>> getTimeToEmploymentStats(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<TimeToEmploymentDTO> stats = graduateService.getTimeToEmploymentStats(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== KONTYNUACJA STUDIÓW PO I STOPNIU ====================

    @GetMapping("/statystyki/kontynuacja-studiow/wojewodztwa")
    @Operation(summary = "Kontynuacja studiów II stopnia po I stopniu per województwo", 
               description = "Procent absolwentów I stopnia kontynuujących studia II stopnia per rok i województwo")
    public ResponseEntity<List<ContinuationStudiesDTO>> getContinuationStudiesByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<ContinuationStudiesDTO> stats = graduateService.getContinuationStudiesByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    // ==================== PODSTAWOWE INFORMACJE O ABSOLWENTACH ====================

    @GetMapping("/statystyki/podstawowe-info")
    @Operation(summary = "Podstawowe informacje o absolwentach", 
               description = "Rok ukończenia studiów, stopień studiów, forma studiów, liczba absolwentów per województwo")
    public ResponseEntity<List<BasicGraduateInfoDTO>> getBasicGraduateInfo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<BasicGraduateInfoDTO> stats = graduateService.getBasicGraduateInfo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statystyki/podsumowanie/wojewodztwa")
    @Operation(summary = "Podsumowanie absolwentów per województwo", 
               description = "Zagregowane dane o liczbie absolwentów, zakresie lat i liczbie kierunków per województwo")
    public ResponseEntity<List<BasicGraduateInfoDTO>> getBasicGraduateSummaryByWojewodztwo(
            @Parameter(description = "Filtr województwa (opcjonalny)") @RequestParam(required = false) String wojewodztwo,
            @Parameter(description = "Filtr poziomu studiów (opcjonalny)") @RequestParam(required = false) String poziom,
            @Parameter(description = "Filtr roku dyplomu (opcjonalny)") @RequestParam(required = false) Integer rokDyplomu) {
        
        List<BasicGraduateInfoDTO> stats = graduateService.getBasicGraduateSummaryByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        return ResponseEntity.ok(stats);
    }
}