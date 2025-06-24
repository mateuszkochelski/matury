package agh.matury.academicSkillsTest;

import java.util.List;

public enum AcademicSkillCategory {
    LOGICAL_MATHEMATICAL("Zdolności logiczno-matematyczne", 
                        "Talent do myślenia analitycznego, rozumowania logicznego i operowania liczbami. Osoby o wysokich wynikach wykazują łatwość w rozwiązywaniu problemów abstrakcyjnych, dostrzeganiu wzorców i zależności przyczynowo-skutkowych.",
                        List.of("matematyka", "fizyka", "informatyka (algorytmy, sztuczna inteligencja)", "ekonomia", "inżynieria (np. automatyka, elektrotechnika)")),
    
    LINGUISTIC("Zdolności językowe (werbalne)", 
              "Zdolność do efektywnego posługiwania się językiem – zarówno w mowie, jak i piśmie. Osoby te cechuje bogate słownictwo, łatwość w nauce języków, dobre rozumienie tekstu i jasne formułowanie myśli.",
              List.of("filologia polska lub obca", "lingwistyka", "dziennikarstwo", "prawo", "komunikacja społeczna", "literaturoznawstwo", "logopedia")),
    
    ARTISTIC("Zdolności artystyczne (kreatywne)", 
            "Uzdolnienia w zakresie sztuk wizualnych, muzycznych lub ogólnie pojętej kreatywności. Osoby z wysokim wynikiem mają rozwiniętą wyobraźnię przestrzenną i estetyczną, potrafią tworzyć oryginalne prace artystyczne.",
            List.of("akademia sztuk pięknych (malarstwo, rzeźba)", "projektowanie graficzne", "architektura i urbanistyka", "szkoła filmowa (reżyseria, scenografia)", "muzyka (akademia muzyczna)", "design przemysłowy")),
    
    TECHNICAL("Zdolności techniczne", 
             "Zdolności manualno-techniczne – umiejętność rozumienia zasad działania urządzeń, majsterkowania i stosowania wiedzy w praktyce. Często łączą się tu talenty praktyczne z logicznym myśleniem.",
             List.of("mechanika i budowa maszyn", "mechatronika", "elektronika", "robotyka", "informatyka (administracja sieci, hardware)", "inżynieria produkcji", "budownictwo")),
    
    NATURAL_SCIENCES("Zdolności przyrodnicze", 
                    "Zainteresowania i zdolności w obszarze nauk przyrodniczych – biologia, chemia, geografia, ekologia itp. Osoby te są wrażliwe na otaczający świat natury, mają łatwość w przyswajaniu wiedzy o organizmach i zjawiskach przyrodniczych.",
                    List.of("biologia", "biotechnologia", "ochrona środowiska", "rolnictwo", "leśnictwo", "geografia", "geologia", "medycyna lub weterynaria"));

    private final String displayName;
    private final String description;
    private final List<String> suggestedFieldsOfStudy;

    AcademicSkillCategory(String displayName, String description, List<String> suggestedFieldsOfStudy) {
        this.displayName = displayName;
        this.description = description;
        this.suggestedFieldsOfStudy = suggestedFieldsOfStudy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSuggestedFieldsOfStudy() {
        return suggestedFieldsOfStudy;
    }
} 