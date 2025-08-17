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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(5) // Uruchom po GraduateSeeder
public class GraduateDataSeeder implements CommandLineRunner {

    private final GraduateRepository graduateRepository;
    private final FieldOfStudyMonthlyIndexRepository monthlyIndexRepository;

    private static final Map<String, String> WOJEWODZTWA_MAP = Stream.of(new String[][] {
        { "02", "dolnośląskie" },
        { "04", "kujawsko-pomorskie" },
        { "06", "lubelskie" },
        { "08", "lubuskie" },
        { "10", "łódzkie" },
        { "12", "małopolskie" },
        { "14", "mazowieckie" },
        { "16", "opolskie" },
        { "18", "podkarpackie" },
        { "20", "podlaskie" },
        { "22", "pomorskie" },
        { "24", "śląskie" },
        { "26", "świętokrzyskie" },
        { "28", "warmińsko-mazurskie" },
        { "30", "wielkopolskie" },
        { "32", "zachodniopomorskie" }
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    @Override
    public void run(String... args) throws Exception {
        if (graduateRepository.count() > 0) {
            log.info("Graduate data already exists, skipping real data seeding");
            return;
        }

        log.info("Starting real graduate data seeding...");
        
        // Sprawdź czy istnieje plik z rzeczywistymi danymi absolwentów
        String realDataCsvPath = "graduate-real-data.csv";
        Path realDataPath = Paths.get(realDataCsvPath);
        
        if (Files.exists(realDataPath)) {
            seedRealGraduateData(realDataPath);
        } else {
            log.info("Real graduate data file not found at: {}. Creating sample data.", realDataCsvPath);
            createSampleData();
        }
        
        log.info("Graduate data seeding completed. Total records: {}", graduateRepository.count());
    }

    private void seedRealGraduateData(Path csvPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line;
            boolean isHeader = true;
            int processedRecords = 0;
            int batchSize = 1000;
            List<Graduate> batch = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    Graduate graduate = parseGraduateFromCsv(line);
                    if (graduate != null) {
                        batch.add(graduate);
                        
                        if (batch.size() >= batchSize) {
                            graduateRepository.saveAll(batch);
                            processedRecords += batch.size();
                            batch.clear();
                            log.debug("Processed {} records so far", processedRecords);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error parsing line: {}, error: {}", line, e.getMessage());
                }
            }

            // Zapisz pozostałe rekordy
            if (!batch.isEmpty()) {
                graduateRepository.saveAll(batch);
                processedRecords += batch.size();
            }

            log.info("Successfully processed {} graduate records from CSV", processedRecords);

        } catch (IOException e) {
            log.error("Error reading graduate data CSV file: {}", e.getMessage());
        }
    }

    private Graduate parseGraduateFromCsv(String line) {
        String[] values = parseCSVLine(line);
        
        // Sprawdź czy linia ma minimum wymaganych kolumn (694 kolumny)
        if (values.length < 600) {
            return null;
        }

        try {
            return Graduate.builder()
                    .rokDyplomu(parseInteger(values[0]))
                    .kierunekId(cleanString(values[1]))
                    .poziom(cleanString(values[2]))
                    .forma(cleanString(values[3]))
                    .liczbaAbsolwentow(parseInteger(values[4]))
                    .procWZus(parseDecimal(values[7]))
                    .procPozaZus(parseDecimal(values[8]))
                    .procDosw(parseDecimal(values[9]))
                    .nazwaKierunku(cleanString(values[669]))
                    .nazwaSpecjalnosci(cleanString(values[670]))
                    .uczelniaId(cleanString(values[671]))
                    .nazwaUczelni(cleanString(values[672]))
                    .jednostkaId(cleanString(values[673]))
                    .nazwaJednostki(cleanString(values[674]))
                    .profil(cleanString(values[676]))
                    .dziedzina_id(cleanString(values[677]))
                    .dziedzina(cleanString(values[678]))
                    .wojewodztwo(WOJEWODZTWA_MAP.get(cleanString(values[686])))
                    .czasPraca(parseDecimal(values[21]))
                    .czasEtat(parseDecimal(values[29]))
                    .czasPracaDosw(parseDecimal(values[19]))
                    .czasPracaNdosw(parseDecimal(values[20]))
                    .czasEtatDosw(parseDecimal(values[27]))
                    .czasEtatNdosw(parseDecimal(values[28]))
                    .czasPracaQ1(parseDecimal(values[22]))
                    .czasPracaQ2(parseDecimal(values[23]))
                    .czasPracaQ3(parseDecimal(values[24]))
                    .czasPracaQ4(parseDecimal(values[25]))
                    .czasEtatQ1(parseDecimal(values[30]))
                    .czasEtatQ2(parseDecimal(values[31]))
                    .czasEtatQ3(parseDecimal(values[32]))
                    .czasEtatQ4(parseDecimal(values[33]))
                    .czyPracaP1(parseDecimal(values[109]))
                    .czyPracaP2(parseDecimal(values[112]))
                    .czyPracaP3(parseDecimal(values[115]))
                    .czyPracaP4(parseDecimal(values[118]))
                    .czyPracaP5(parseDecimal(values[121]))
                    .eZarP1(parseDecimal(values[304]))
                    .eZarP2(parseDecimal(values[307]))
                    .eZarP3(parseDecimal(values[310]))
                    .eZarP4(parseDecimal(values[313]))
                    .eZarP5(parseDecimal(values[316]))
                    .eZarEtatP1(parseDecimal(values[368]))
                    .eZarEtatP2(parseDecimal(values[371]))
                    .eZarEtatP3(parseDecimal(values[374]))
                    .eZarEtatP4(parseDecimal(values[377]))
                    .eZarEtatP5(parseDecimal(values[380]))
                    .wwzP1(parseDecimal(values[434]))
                    .wwzP2(parseDecimal(values[437]))
                    .wwzP3(parseDecimal(values[440]))
                    .wwzP4(parseDecimal(values[443]))
                    .wwzP5(parseDecimal(values[446]))
                    .wwbP1(parseDecimal(values[83]))
                    .wwbP2(parseDecimal(values[85]))
                    .wwbP3(parseDecimal(values[87]))
                    .wwbP4(parseDecimal(values[89]))
                    .wwbP5(parseDecimal(values[91]))
                    .czyBezrP1(parseDecimal(values[43]))
                    .czyBezrP2(parseDecimal(values[46]))
                    .czyBezrP3(parseDecimal(values[49]))
                    .czyBezrP4(parseDecimal(values[52]))
                    .czyBezrP5(parseDecimal(values[55]))
                    .if2st(parseDecimal(values[691]))
                    .if2stP1(parseDecimal(values[687]))
                    .if2stP2(parseDecimal(values[688]))
                    .if2stP3(parseDecimal(values[689]))
                    .if2stP4(parseDecimal(values[690]))
                    .if2stP5(parseDecimal(values[691]))
                    .if2stUcz(parseDecimal(values[693]))
                    .nCzyEtat(parseInteger(values[285]))
                    .eMiesNPracodawcow(parseDecimal(values[286]))
                    .eMiesNPracodawcowQ1(parseDecimal(values[287]))
                    .eMiesNPracodawcowQ2(parseDecimal(values[288]))
                    .eMiesNPracodawcowQ3(parseDecimal(values[289]))
                    .eMiesNPracodawcowQ4(parseDecimal(values[290]))
                    .eRocznaNKoncowetatow(parseDecimal(values[292]))
                    .koniecetatuRazNaIleLat(parseDecimal(values[293]))
                    .eRocznaNKoncowetatowQ1(parseDecimal(values[294]))
                    .eRocznaNKoncowetatowQ2(parseDecimal(values[295]))
                    .eRocznaNKoncowetatowQ3(parseDecimal(values[296]))
                    .eRocznaNKoncowetatowQ4(parseDecimal(values[297]))
                    .nKmz1NajwMiasta(parseInteger(values[452]))
                    .nKmz2MiastaPowiatowe(parseInteger(values[453]))
                    .nKmz3MniejszeMiejsc(parseInteger(values[454]))
                    .eZarKmz1(parseDecimal(values[462]))
                    .eZarKmz2(parseDecimal(values[463]))
                    .eZarKmz3(parseDecimal(values[464]))
                    .wwzKmz1(parseDecimal(values[465]))
                    .wwzKmz2(parseDecimal(values[466]))
                    .wwzKmz3(parseDecimal(values[467]))
                    .procStudia(parseDecimal(values[13]))
                    .procUkon(parseDecimal(values[15]))
                    .procDyplom(parseDecimal(values[16]))
                    .procDoktoranckie(parseDecimal(values[17]))
                    .procDoktorat(parseDecimal(values[18]))
                    .procMiesPraca(parseDecimal(values[198]))
                    .procMiesEtat(parseDecimal(values[228]))
                    .procMiesSamoz(parseDecimal(values[259]))
                    .meZar(parseDecimal(values[333]))
                    .meZarEtat(parseDecimal(values[397]))
                    .zarQ1(parseDecimal(values[334]))
                    .zarQ2(parseDecimal(values[335]))
                    .zarQ3(parseDecimal(values[336]))
                    .zarQ4(parseDecimal(values[337]))
                    .build();
        } catch (Exception e) {
            log.warn("Error creating Graduate object from line: {}, error: {}", line.substring(0, Math.min(line.length(), 200)), e.getMessage());
            return null;
        }
    }

    private void createSampleData() {
        log.info("Creating sample graduate data for testing...");
        
        List<Graduate> sampleGraduates = Arrays.asList(
                Graduate.builder()
                        .wojewodztwo("mazowieckie")
                        .poziom("I stopień")
                        .rokDyplomu(2020)
                        .forma("stacjonarne")
                        .liczbaAbsolwentow(1250)
                        .nazwaKierunku("Informatyka")
                        .nazwaUczelni("Politechnika Warszawska")
                        .dziedzina("nauki techniczne")
                        .profil("praktyczny")
                        .wwzP1(new BigDecimal("1.15"))
                        .wwzP2(new BigDecimal("1.22"))
                        .wwzP3(new BigDecimal("1.28"))
                        .wwzP4(new BigDecimal("1.35"))
                        .wwzP5(new BigDecimal("1.42"))
                        .wwbP1(new BigDecimal("0.85"))
                        .wwbP2(new BigDecimal("0.78"))
                        .wwbP3(new BigDecimal("0.72"))
                        .wwbP4(new BigDecimal("0.68"))
                        .wwbP5(new BigDecimal("0.65"))
                        .czyPracaP1(new BigDecimal("82.5"))
                        .czyPracaP2(new BigDecimal("87.2"))
                        .czyPracaP3(new BigDecimal("91.8"))
                        .czyPracaP4(new BigDecimal("94.1"))
                        .czyPracaP5(new BigDecimal("95.3"))
                        .eZarP1(new BigDecimal("6200.00"))
                        .eZarP2(new BigDecimal("7100.00"))
                        .eZarP3(new BigDecimal("8300.00"))
                        .eZarP4(new BigDecimal("9200.00"))
                        .eZarP5(new BigDecimal("10100.00"))
                        .czasPraca(new BigDecimal("2.1"))
                        .czasEtat(new BigDecimal("2.8"))
                        .if2stP1(new BigDecimal("38.5"))
                        .if2stP2(new BigDecimal("45.2"))
                        .if2stP3(new BigDecimal("48.7"))
                        .if2st(new BigDecimal("52.3"))
                        .eMiesNPracodawcow(new BigDecimal("1.3"))
                        .eRocznaNKoncowetatow(new BigDecimal("0.7"))
                        .koniecetatuRazNaIleLat(new BigDecimal("1.43"))
                        .nCzyEtat(980)
                        .build(),

                Graduate.builder()
                        .wojewodztwo("dolnośląskie")
                        .poziom("II stopień")
                        .rokDyplomu(2021)
                        .forma("stacjonarne")
                        .liczbaAbsolwentow(800)
                        .nazwaKierunku("Zarządzanie")
                        .nazwaUczelni("Uniwersytet Ekonomiczny we Wrocławiu")
                        .dziedzina("nauki ekonomiczne")
                        .profil("ogólnoakademicki")
                        .wwzP1(new BigDecimal("1.08"))
                        .wwzP2(new BigDecimal("1.12"))
                        .wwzP3(new BigDecimal("1.18"))
                        .wwzP4(new BigDecimal("1.23"))
                        .wwzP5(new BigDecimal("1.29"))
                        .wwbP1(new BigDecimal("0.92"))
                        .wwbP2(new BigDecimal("0.87"))
                        .wwbP3(new BigDecimal("0.83"))
                        .wwbP4(new BigDecimal("0.79"))
                        .wwbP5(new BigDecimal("0.75"))
                        .czyPracaP1(new BigDecimal("78.3"))
                        .czyPracaP2(new BigDecimal("83.1"))
                        .czyPracaP3(new BigDecimal("87.4"))
                        .czyPracaP4(new BigDecimal("89.7"))
                        .czyPracaP5(new BigDecimal("91.2"))
                        .eZarP1(new BigDecimal("4800.00"))
                        .eZarP2(new BigDecimal("5400.00"))
                        .eZarP3(new BigDecimal("6100.00"))
                        .eZarP4(new BigDecimal("6800.00"))
                        .eZarP5(new BigDecimal("7500.00"))
                        .czasPraca(new BigDecimal("3.8"))
                        .czasEtat(new BigDecimal("4.5"))
                        .if2st(new BigDecimal("0")) // II stopień nie kontynuuje
                        .eMiesNPracodawcow(new BigDecimal("1.1"))
                        .eRocznaNKoncowetatow(new BigDecimal("0.9"))
                        .koniecetatuRazNaIleLat(new BigDecimal("1.11"))
                        .nCzyEtat(620)
                        .build(),

                Graduate.builder()
                        .wojewodztwo("śląskie")
                        .poziom("I stopień")
                        .rokDyplomu(2019)
                        .forma("niestacjonarne")
                        .liczbaAbsolwentow(450)
                        .nazwaKierunku("Mechanika i budowa maszyn")
                        .nazwaUczelni("Politechnika Śląska")
                        .dziedzina("nauki techniczne")
                        .profil("praktyczny")
                        .wwzP1(new BigDecimal("1.12"))
                        .wwzP2(new BigDecimal("1.19"))
                        .wwzP3(new BigDecimal("1.25"))
                        .wwzP4(new BigDecimal("1.31"))
                        .wwzP5(new BigDecimal("1.37"))
                        .wwbP1(new BigDecimal("0.88"))
                        .wwbP2(new BigDecimal("0.82"))
                        .wwbP3(new BigDecimal("0.76"))
                        .wwbP4(new BigDecimal("0.71"))
                        .wwbP5(new BigDecimal("0.67"))
                        .czyPracaP1(new BigDecimal("79.1"))
                        .czyPracaP2(new BigDecimal("84.6"))
                        .czyPracaP3(new BigDecimal("88.9"))
                        .czyPracaP4(new BigDecimal("91.3"))
                        .czyPracaP5(new BigDecimal("93.1"))
                        .eZarP1(new BigDecimal("5200.00"))
                        .eZarP2(new BigDecimal("5900.00"))
                        .eZarP3(new BigDecimal("6700.00"))
                        .eZarP4(new BigDecimal("7400.00"))
                        .eZarP5(new BigDecimal("8200.00"))
                        .czasPraca(new BigDecimal("3.2"))
                        .czasEtat(new BigDecimal("3.9"))
                        .if2stP1(new BigDecimal("28.3"))
                        .if2stP2(new BigDecimal("33.7"))
                        .if2stP3(new BigDecimal("36.2"))
                        .if2st(new BigDecimal("41.5"))
                        .eMiesNPracodawcow(new BigDecimal("1.2"))
                        .eRocznaNKoncowetatow(new BigDecimal("0.8"))
                        .koniecetatuRazNaIleLat(new BigDecimal("1.25"))
                        .nCzyEtat(350)
                        .build()
        );

        graduateRepository.saveAll(sampleGraduates);

        // Sample monthly series for demo (months 1..12)
        for (int m = 1; m <= 12; m++) {
            monthlyIndexRepository.save(FieldOfStudyMonthlyIndex.builder()
                    .uczelniaId("PW")
                    .kierunekId("INF")
                    .poziom("I stopień")
                    .rokDyplomu(2020)
                    .miesiac(m)
                    .wwz(new BigDecimal("1." + (10 + m)))
                    .wwb(new BigDecimal("0." + (80 - Math.min(79, m))))
                    .build());
        }
        log.info("Created {} sample graduate records", sampleGraduates.size());
    }

    private String[] parseCSVLine(String line) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        // Trim leading/trailing whitespace and quotes from the whole line
        line = line.trim();
        if (line.startsWith("\"")) {
            line = line.substring(1);
        }
        if (line.endsWith("\"")) {
            line = line.substring(0, line.length() - 1);
        }

        // Split by the delimiter ";"
        String[] parts = line.split(";", -1);
        for(String part : parts) {
            values.add(part.replace("\"", "").trim());
        }

        return values.toArray(new String[0]);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "NULL".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            // Usuń cudzysłowy i spacje
            String cleanValue = value.replace("\"", "").replace(",", ".").trim();
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "NULL".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            String cleanValue = value.replace("\"", "").trim();
            return Integer.parseInt(cleanValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String cleanString(String value) {
        if (value == null || value.trim().isEmpty() || "NULL".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.replace("\"", "").trim();
    }
}