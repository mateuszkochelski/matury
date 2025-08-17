package agh.matury.graduate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(4) // Uruchom po innych seederach
public class GraduateSeeder implements CommandLineRunner {

    private final GraduateRepository graduateRepository;

    // Mapowanie nazw kolumn z CSV na pola w modelu
    private final Map<String, String> columnMapping = createColumnMapping();

    @Override
    public void run(String... args) throws Exception {
        if (graduateRepository.count() > 0) {
            log.info("Graduates data already exists, skipping seeding");
            return;
        }

        log.info("Starting graduates data seeding...");
        seedGraduatesData();
        log.info("Graduates data seeding completed. Total records: {}", graduateRepository.count());
    }

    private void seedGraduatesData() {
        String csvFilePath = "graduates-major-dictionary.csv";
        Path path = Paths.get(csvFilePath);

        if (!Files.exists(path)) {
            log.warn("Graduates CSV file not found at: {}. Skipping graduates seeding.", csvFilePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isHeader = true;
            int processedRecords = 0;
            int batchSize = 1000;

            // Przygotuj mapę etykiet do mapowania wartości
            Map<String, String> variableToLabelMap = new HashMap<>();
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] values = parseCSVLine(line);
                if (values.length >= 2) {
                    String nazwaZmiennej = values[0].trim();
                    String etykieta = values[1].trim();
                    variableToLabelMap.put(nazwaZmiennej, etykieta);
                }
            }

            log.info("Loaded {} variable mappings from CSV", variableToLabelMap.size());
            log.info("Note: This seeder loads the data dictionary. Actual graduate data should be imported separately.");

        } catch (IOException e) {
            log.error("Error reading graduates CSV file: {}", e.getMessage());
        }
    }

    private String[] parseCSVLine(String line) {
        // Proste parsowanie CSV - dla bardziej złożonych przypadków użyj OpenCSV
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private Map<String, String> createColumnMapping() {
        Map<String, String> mapping = new HashMap<>();
        
        // Podstawowe mapowania
        mapping.put("P_ROKDYP", "rokDyplomu");
        mapping.put("P_KIERUNEK_ID", "kierunekId");
        mapping.put("P_POZIOM", "poziom");
        mapping.put("P_FORMA", "forma");
        mapping.put("P_N", "liczbaAbsolwentow");
        
        // Informacje o uczelni
        mapping.put("P_KIERUNEK_NAZWA", "nazwaKierunku");
        mapping.put("P_SPEC_NAZWA", "nazwaSpecjalnosci");
        mapping.put("P_UCZELNIA", "uczelniaId");
        mapping.put("P_NAZWA_UCZELNI", "nazwaUczelni");
        mapping.put("P_JEDN", "jednostkaId");
        mapping.put("P_NAZWA_JEDN", "nazwaJednostki");
        mapping.put("P_PROFIL", "profil");
        mapping.put("P_DZIEDZINA_ID", "dziedzinaId");
        mapping.put("P_DZIEDZINA", "dziedzina");
        mapping.put("P_WOJ", "wojewodztwo");
        
        // Wskaźniki zatrudnienia
        mapping.put("P_PROC_WZUS", "procWZus");
        mapping.put("P_PROC_POZAZUS", "procPozaZus");
        mapping.put("P_PROC_DOSW", "procDosw");
        
        // Zatrudnienie per rok
        mapping.put("P_CZY_PRACA_P1", "czyPracaP1");
        mapping.put("P_CZY_PRACA_P2", "czyPracaP2");
        mapping.put("P_CZY_PRACA_P3", "czyPracaP3");
        mapping.put("P_CZY_PRACA_P4", "czyPracaP4");
        mapping.put("P_CZY_PRACA_P5", "czyPracaP5");
        
        // Etat per rok
        mapping.put("P_CZY_ETAT_P1", "czyEtatP1");
        mapping.put("P_CZY_ETAT_P2", "czyEtatP2");
        mapping.put("P_CZY_ETAT_P3", "czyEtatP3");
        mapping.put("P_CZY_ETAT_P4", "czyEtatP4");
        mapping.put("P_CZY_ETAT_P5", "czyEtatP5");
        
        // Samozatrudnienie per rok
        mapping.put("P_CZY_SAMOZ_P1", "czySamozP1");
        mapping.put("P_CZY_SAMOZ_P2", "czySamozP2");
        mapping.put("P_CZY_SAMOZ_P3", "czySamozP3");
        mapping.put("P_CZY_SAMOZ_P4", "czySamozP4");
        mapping.put("P_CZY_SAMOZ_P5", "czySamozP5");
        
        // Bezrobocie per rok
        mapping.put("P_CZY_BEZR_P1", "czyBezrP1");
        mapping.put("P_CZY_BEZR_P2", "czyBezrP2");
        mapping.put("P_CZY_BEZR_P3", "czyBezrP3");
        mapping.put("P_CZY_BEZR_P4", "czyBezrP4");
        mapping.put("P_CZY_BEZR_P5", "czyBezrP5");
        
        // WWB per rok
        mapping.put("P_WWB_P1", "wwbP1");
        mapping.put("P_WWB_P2", "wwbP2");
        mapping.put("P_WWB_P3", "wwbP3");
        mapping.put("P_WWB_P4", "wwbP4");
        mapping.put("P_WWB_P5", "wwbP5");
        
        // Zarobki per rok
        mapping.put("P_E_ZAR_P1", "eZarP1");
        mapping.put("P_E_ZAR_P2", "eZarP2");
        mapping.put("P_E_ZAR_P3", "eZarP3");
        mapping.put("P_E_ZAR_P4", "eZarP4");
        mapping.put("P_E_ZAR_P5", "eZarP5");
        
        // WWZ per rok
        mapping.put("P_WWZ_P1", "wwzP1");
        mapping.put("P_WWZ_P2", "wwzP2");
        mapping.put("P_WWZ_P3", "wwzP3");
        mapping.put("P_WWZ_P4", "wwzP4");
        mapping.put("P_WWZ_P5", "wwzP5");
        
        // Zarobki etat per rok
        mapping.put("P_E_ZAR_ETAT_P1", "eZarEtatP1");
        mapping.put("P_E_ZAR_ETAT_P2", "eZarEtatP2");
        mapping.put("P_E_ZAR_ETAT_P3", "eZarEtatP3");
        mapping.put("P_E_ZAR_ETAT_P4", "eZarEtatP4");
        mapping.put("P_E_ZAR_ETAT_P5", "eZarEtatP5");
        
        // Czas do pracy
        mapping.put("P_CZAS_PRACA", "czasPraca");
        mapping.put("P_CZAS_ETAT", "czasEtat");
        
        // Geografia - liczby
        mapping.put("P_N_KMZ1_NAJW_MIASTA", "nKmz1NajwMiasta");
        mapping.put("P_N_KMZ2_MIASTA_POWIATOWE", "nKmz2MiastaPowiatowe");
        mapping.put("P_N_KMZ3_MNIEJSZE_MIEJSC", "nKmz3MniejszeMiejsc");
        
        // Geografia - zarobki
        mapping.put("P_E_ZAR_KMZ1", "eZarKmz1");
        mapping.put("P_E_ZAR_KMZ2", "eZarKmz2");
        mapping.put("P_E_ZAR_KMZ3", "eZarKmz3");
        
        // Geografia - WWZ
        mapping.put("P_WWZ_KMZ1", "wwzKmz1");
        mapping.put("P_WWZ_KMZ2", "wwzKmz2");
        mapping.put("P_WWZ_KMZ3", "wwzKmz3");
        
        // Kariera akademicka
        mapping.put("P_PROC_STUDIA", "procStudia");
        mapping.put("P_PROC_UKON", "procUkon");
        mapping.put("P_PROC_DYPLOM", "procDyplom");
        mapping.put("P_PROC_DOKTORANCKIE", "procDoktoranckie");
        mapping.put("P_PROC_DOKTORAT", "procDoktorat");
        
        // Studia II stopnia
        mapping.put("P_IF_2st_P1", "if2stP1");
        mapping.put("P_IF_2st_P2", "if2stP2");
        mapping.put("P_IF_2st_P3", "if2stP3");
        mapping.put("P_IF_2st_P4", "if2stP4");
        mapping.put("P_IF_2st_P5", "if2stP5");
        mapping.put("P_IF_2st_ucz", "if2stUcz");
        
        return mapping;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}