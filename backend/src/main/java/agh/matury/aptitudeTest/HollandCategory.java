package agh.matury.aptitudeTest;

import java.util.List;

public enum HollandCategory {
    REALISTIC("Realistyczny (praktyczny)", 
             "Osoby preferujące konkretne, manualne zadania i pracę z narzędziami lub maszynami. Zwykle są praktyczne i wytrwałe, gorzej czują się w zadaniach wymagających ciągłych kontaktów społecznych.",
             List.of("inżynieria (mechaniczna, elektryczna, budownictwo)", "logistyka", "informatyka techniczna", "wojskowość", "wychowanie fizyczne")),
    
    INVESTIGATIVE("Badawczy (analityczny)", 
                 "Osoby dociekliwe, ceniące wiedzę i naukę, zadające pytania i lubiące analizować problemy. Dobrze radzą sobie z teorią i abstrakcją, nieco gorzej z przewodzeniem innym.",
                 List.of("nauki ścisłe (matematyka, fizyka, chemia)", "informatyka (teoretyczna, data science)", "nauki przyrodnicze (biologia, geografia, medycyna laboratoryjna)", "ekonomia analityczna")),
    
    ARTISTIC("Artystyczny (kreatywny)", 
            "Osoby o bogatej wyobraźni, ceniące kreatywność i ekspresję pomysłów. Unikają rutyny i sztywnej organizacji czasu, wolą swobodę twórczą.",
            List.of("sztuki piękne (malarstwo, muzyka, aktorstwo)", "architektura", "design (grafika, moda)", "filologia polska lub kreatywnego pisania", "dziennikarstwo artystyczne", "fotografia")),
    
    SOCIAL("Społeczny (pomocny)", 
          "Osoby empatyczne, lubiące prace z ludźmi i utrzymywanie relacji. Ważna jest dla nich współpraca i pomaganie innym; unikają zadań czysto technicznych lub wymagających wysiłku fizycznego.",
          List.of("psychologia", "pedagogika", "praca socjalna", "kierunki medyczne (pielęgniarstwo, medycyna)", "socjologia", "zarządzanie zasobami ludzkimi")),
    
    ENTERPRISING("Przedsiębiorczy (lider)", 
                "Osoby pewne siebie, nastawione na osiąganie sukcesu, władzę i wpływ. Wykazują zdolności przywódcze, organizacyjne i przekonywania innych; motywuje je rywalizacja i zysk.",
                List.of("zarządzanie", "biznes i przedsiębiorczość", "marketing", "prawo", "ekonomia", "MBA")),
    
    CONVENTIONAL("Konwencjonalny (urzędowy)", 
                "Osoby zorganizowane, dokładne, lubiące pracować z danymi, liczbami, procedurami. Dobrze czują się w zadaniach wymagających porządku i powtarzalności, unikają zaś bardzo nieprzewidywalnych, kreatywnych wyzwań.",
                List.of("rachunkowość i finanse", "administracja", "bankowość", "księgowość", "analityka danych", "bibliologia/archiwistyka", "inżynieria przemysłowa (o charakterze procesowym)"));

    private final String displayName;
    private final String description;
    private final List<String> suggestedFieldsOfStudy;

    HollandCategory(String displayName, String description, List<String> suggestedFieldsOfStudy) {
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