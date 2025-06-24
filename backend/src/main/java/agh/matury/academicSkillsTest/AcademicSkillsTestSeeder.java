package agh.matury.academicSkillsTest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AcademicSkillsTestSeeder implements CommandLineRunner {

    private final AcademicSkillsTestQuestionRepository questionRepository;

    public AcademicSkillsTestSeeder(AcademicSkillsTestQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (questionRepository.count() == 0) {
            seedQuestions();
        }
    }

    private void seedQuestions() {
        List<AcademicSkillsTestQuestion> questions = Arrays.asList(
            // Zdolności logiczno-matematyczne (pytania 1-4)
            new AcademicSkillsTestQuestion(
                "Łatwo przychodzi mi rozwiązywanie zadań matematycznych lub logicznych.",
                AcademicSkillCategory.LOGICAL_MATHEMATICAL, 1
            ),
            new AcademicSkillsTestQuestion(
                "Lubię łamigłówki, szarady i gry logiczne – chętnie podejmuję się ich rozwiązywania.",
                AcademicSkillCategory.LOGICAL_MATHEMATICAL, 2
            ),
            new AcademicSkillsTestQuestion(
                "Nauka przedmiotów ścisłych (matematyka, fizyka) nie sprawia mi trudności – szybko pojmuję nowe koncepcje.",
                AcademicSkillCategory.LOGICAL_MATHEMATICAL, 3
            ),
            new AcademicSkillsTestQuestion(
                "Potrafię wykonywać obliczenia w pamięci szybko i poprawnie.",
                AcademicSkillCategory.LOGICAL_MATHEMATICAL, 4
            ),
            
            // Zdolności językowe (pytania 5-8)
            new AcademicSkillsTestQuestion(
                "Mam bogate słownictwo i łatwo wyrażam swoje myśli słowami.",
                AcademicSkillCategory.LINGUISTIC, 5
            ),
            new AcademicSkillsTestQuestion(
                "Z łatwością uczę się języków obcych – gramatyka i wymowa nie stanowią dla mnie dużego wyzwania.",
                AcademicSkillCategory.LINGUISTIC, 6
            ),
            new AcademicSkillsTestQuestion(
                "Czytanie książek i pisanie przychodzi mi z lekkością i sprawia przyjemność.",
                AcademicSkillCategory.LINGUISTIC, 7
            ),
            new AcademicSkillsTestQuestion(
                "Potrafię klarownie wytłumaczyć skomplikowane zagadnienie innym osobom.",
                AcademicSkillCategory.LINGUISTIC, 8
            ),
            
            // Zdolności artystyczne (pytania 9-12)
            new AcademicSkillsTestQuestion(
                "Mam talent artystyczny lub twórczy, który rozwijam (np. rysuję, gram na instrumencie, tworzę rękodzieło).",
                AcademicSkillCategory.ARTISTIC, 9
            ),
            new AcademicSkillsTestQuestion(
                "Łatwo wyobrażam sobie różne kształty, obrazy lub sceny w myślach.",
                AcademicSkillCategory.ARTISTIC, 10
            ),
            new AcademicSkillsTestQuestion(
                "Mam dobre wyczucie estetyki – zwracam uwagę na kolory, formy, kompozycję i potrafię je dobrze dobrać.",
                AcademicSkillCategory.ARTISTIC, 11
            ),
            new AcademicSkillsTestQuestion(
                "Zajęcia kreatywne (rysowanie, malowanie, pisanie, gra na instrumencie) wychodzą mi naturalnie.",
                AcademicSkillCategory.ARTISTIC, 12
            ),
            
            // Zdolności techniczne (pytania 13-16)
            new AcademicSkillsTestQuestion(
                "Interesuję się techniką i działaniem urządzeń – lubię wiedzieć, jak zbudowane są maszyny, komputery itp.",
                AcademicSkillCategory.TECHNICAL, 13
            ),
            new AcademicSkillsTestQuestion(
                "Chętnie majsterkuję – np. rozkładam sprzęty na części, naprawiam lub konstruuję proste urządzenia.",
                AcademicSkillCategory.TECHNICAL, 14
            ),
            new AcademicSkillsTestQuestion(
                "Szybko opanowuję obsługę nowych narzędzi, maszyn czy programów.",
                AcademicSkillCategory.TECHNICAL, 15
            ),
            new AcademicSkillsTestQuestion(
                "Mam dobrą koordynację wzrokowo-ruchową i zdolności manualne potrzebne przy pracy technicznej.",
                AcademicSkillCategory.TECHNICAL, 16
            ),
            
            // Zdolności przyrodnicze (pytania 17-20)
            new AcademicSkillsTestQuestion(
                "Uwielbiam obserwować przyrodę – interesują mnie zwierzęta, rośliny i zjawiska naturalne.",
                AcademicSkillCategory.NATURAL_SCIENCES, 17
            ),
            new AcademicSkillsTestQuestion(
                "Z łatwością zapamiętuję ciekawostki z biologii, geografii czy chemii.",
                AcademicSkillCategory.NATURAL_SCIENCES, 18
            ),
            new AcademicSkillsTestQuestion(
                "Lubię wykonywać doświadczenia naukowe lub projekty badawcze z zakresu przyrody.",
                AcademicSkillCategory.NATURAL_SCIENCES, 19
            ),
            new AcademicSkillsTestQuestion(
                "Przedmioty przyrodnicze (biologia, geografia, chemia) należą do moich ulubionych lub najmocniejszych.",
                AcademicSkillCategory.NATURAL_SCIENCES, 20
            )
        );

        questionRepository.saveAll(questions);
        System.out.println("Zainicjalizowano " + questions.size() + " pytań testu uzdolnień kierunkowych.");
    }
} 