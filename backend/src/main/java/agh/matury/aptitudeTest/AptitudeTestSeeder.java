package agh.matury.aptitudeTest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AptitudeTestSeeder implements CommandLineRunner {

    private final AptitudeTestQuestionRepository questionRepository;

    public AptitudeTestSeeder(AptitudeTestQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (questionRepository.count() == 0) {
            seedQuestions();
        }
    }

    private void seedQuestions() {
        List<AptitudeTestQuestion> questions = Arrays.asList(
            new AptitudeTestQuestion(
                "Preferuję pracę fizyczną lub z użyciem narzędzi – np. majsterkowanie, praca na świeżym powietrzu.",
                HollandCategory.REALISTIC, 1
            ),
            new AptitudeTestQuestion(
                "Lubię dociekać, jak coś działa – często zadaję pytania w stylu \"dlaczego?\" i analizuję mechanizmy zjawisk.",
                HollandCategory.INVESTIGATIVE, 2
            ),
            new AptitudeTestQuestion(
                "Cenię możliwość wyrażania siebie w pracy – mam bogatą wyobraźnię i twórcze pomysły, rutynowa praca mnie nuży.",
                HollandCategory.ARTISTIC, 3
            ),
            new AptitudeTestQuestion(
                "Pomaganie innym sprawia mi satysfakcję – lubię pracować z ludźmi i wspierać ich w rozwiązywaniu problemów.",
                HollandCategory.SOCIAL, 4
            ),
            new AptitudeTestQuestion(
                "Mam żyłkę lidera – chętnie przekonuję innych do swoich pomysłów, wyznaczam ambitne cele i rywalizacja mnie motywuje.",
                HollandCategory.ENTERPRISING, 5
            ),
            new AptitudeTestQuestion(
                "Lubię, gdy wszystko jest dobrze zorganizowane – cenię ład, procedury i jasno określone zasady w miejscu pracy.",
                HollandCategory.CONVENTIONAL, 6
            ),
            new AptitudeTestQuestion(
                "Z przyjemnością majsterkuję i naprawiam różne przedmioty w wolnym czasie.",
                HollandCategory.REALISTIC, 7
            ),
            new AptitudeTestQuestion(
                "Interesuje mnie naukowe wyjaśnianie świata – czytam popularnonaukowe artykuły, eksperymentuję, analizuję dane.",
                HollandCategory.INVESTIGATIVE, 8
            ),
            new AptitudeTestQuestion(
                "Mam artystyczną naturę – ciągnie mnie do sztuki (muzyka, plastyka, literatura) i nowych, niebanalnych pomysłów.",
                HollandCategory.ARTISTIC, 9
            ),
            new AptitudeTestQuestion(
                "Łatwo nawiązuję bliskie relacje z ludźmi – jestem empatyczny i potrafię uważnie słuchać.",
                HollandCategory.SOCIAL, 10
            ),
            new AptitudeTestQuestion(
                "Lubię podejmować decyzje i brać za nie odpowiedzialność – dobrze się czuję w roli przewodzenia grupie.",
                HollandCategory.ENTERPRISING, 11
            ),
            new AptitudeTestQuestion(
                "Zwracam uwagę na szczegóły – jestem skrupulatny w pracy z dokumentami, liczbami czy danymi.",
                HollandCategory.CONVENTIONAL, 12
            ),
            new AptitudeTestQuestion(
                "Ciężka, fizyczna praca może być satysfakcjonująca i nie boję się wysiłku.",
                HollandCategory.REALISTIC, 13
            ),
            new AptitudeTestQuestion(
                "Rozwiązywanie złożonych problemów daje mi radość – np. zagadki logiczne, trudne pytania badawcze.",
                HollandCategory.INVESTIGATIVE, 14
            ),
            new AptitudeTestQuestion(
                "Cenię kreatywność ponad schematy – wolę improwizować i tworzyć coś nowego, niż trzymać się utartych ścieżek.",
                HollandCategory.ARTISTIC, 15
            ),
            new AptitudeTestQuestion(
                "Chciałbym pracować dla dobra innych – ważne jest dla mnie poczucie, że moja praca pomaga ludziom lub społeczeństwu.",
                HollandCategory.SOCIAL, 16
            ),
            new AptitudeTestQuestion(
                "Pieniądze, sukces i awans są dla mnie ważnymi motywatorami w wyborze ścieżki kariery.",
                HollandCategory.ENTERPRISING, 17
            ),
            new AptitudeTestQuestion(
                "Lepiej czuję się, wykonując zadania według ustalonego planu niż improwizując na bieżąco.",
                HollandCategory.CONVENTIONAL, 18
            ),
            new AptitudeTestQuestion(
                "Wolę pracę praktyczną niż siedzenie przy biurku – np. ruch w terenie zamiast pracy czysto koncepcyjnej.",
                HollandCategory.REALISTIC, 19
            ),
            new AptitudeTestQuestion(
                "Dobrze odnajduję się w dyskusjach naukowych i intelektualnych na różne tematy.",
                HollandCategory.INVESTIGATIVE, 20
            )
        );

        questionRepository.saveAll(questions);
        System.out.println("Zainicjalizowano " + questions.size() + " pytań testu predyspozycji zawodowych.");
    }
} 