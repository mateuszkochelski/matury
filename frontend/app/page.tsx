import { FunctionalitySection } from "@/components/FunctionalitySection";
import { StandaloneCalculatorButton } from "@/components/calculator/StandaloneCalculatorButton";
import HistoryRecommedations from "@/components/recommendation/HistoryRecommendations";
import { Button } from "@/components/ui/button";
import Link from "next/link";

export default async function Home() {
  return (
    <>
      <section className="container mx-auto px-4 py-8 md:py-12 lg:py-16">
        <div className="grid lg:grid-cols-2 gap-8 lg:gap-12 items-center">
          <div className="space-y-6">
            <h1 className="h1 text-foreground leading-tight">
              Znajdź idealny dla Ciebie
              <span className="text-primary block">Kierunek studiów</span>
            </h1>
            <p className="text-base md:text-lg text-foreground/70 leading-relaxed">
              Z łatwością odkryj najlepsze uczelnie w Polsce. Eksploruj wydziały oraz kierunki
              studiów. Porównaj swoje wyniki z matur. Sprwadź zdawanalność kierunlku oraz zarobki
              absolwentów. Możesz zrobić to wszystko w jednym miejscu.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link href="szukaj">
                <Button size="lg" className="w-full sm:w-auto cursor-pointer">
                  Przejdź do wyszukiwarki
                </Button>
              </Link>
            </div>
          </div>

          <div className="relative">
            <div className="bg-gradient-to-br from-primary/30 to-accent/30 rounded-3xl p-6 md:p-8 relative overflow-hidden">
              <div className="absolute top-4 right-4 w-16 md:w-20 h-16 md:h-20 bg-white/30 rounded-full"></div>
              <div className="absolute bottom-4 left-4 w-8 md:w-12 h-8 md:h-12 bg-white/20 rounded-full"></div>
              <div className="text-center">
                <div className="text-4xl md:text-6xl lg:text-8xl mb-4">🐧</div>
                <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-4 mx-auto max-w-xs">
                  <p className="text-sm text-foreground font-medium">
                    „Twoja przyszłość zaczyna się od świadomego wyboru kierunku studiów.”
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="py-8 md:py-16 md:px-8 bg-white">
        <div className="container mx-auto px-4">
          <div className="text-center mb-8 md:mb-16">
            <h2 className="h2 text-foreground mb-4">Mamy wszystko czego potrzebujesz</h2>
            <p className="text-base md:text-lg text-foreground/70 max-w-2xl mx-auto">
              Decyzje, które podejmujesz wymagają dokładnej analizy
            </p>
          </div>

          <FunctionalitySection />
        </div>
      </section>

      <section className="bg-secondary py-8 md:py-16">
        <div className="container mx-auto px-4 text-center">
          <div className="max-w-3xl mx-auto">
            <div className="text-4xl md:text-6xl mb-6">🐧</div>
            <h2 className="h2 text-foreground mb-4">Gotowy obliczyć swoje szanse?</h2>
            <p className="text-md md:text-lg text-foreground/80 mb-8">
              Wpisz swoje wyniki maturalne i przekonaj się, czy dostaniesz się na wymarzony kierunek
            </p>
            <div className="flex justify-center">
              <StandaloneCalculatorButton />
            </div>
          </div>
        </div>
      </section>

      <section className="py-8 md:py-16 md:px-8 bg-white">
        <HistoryRecommedations />
      </section>

      <footer className="bg-foreground text-white py-8 md:py-12">
        <div className="container mx-auto px-4 text-center">
          <div className="max-w-3xl mx-auto">
            <h2 className="h2 mb-4">O nas</h2>
            <p className="text-md md:text-lg mb-8">
              <a href="https://github.com/mateuszkochelski/matury">Link do Repozytorium</a>
            </p>
          </div>
        </div>
        <div className="border-t border-white/20 mt-8 pt-8 text-center text-sm text-white/70">
          <p>&copy; 2025 matury.eu Wszystkie prawa zastrzeżone</p>
        </div>
      </footer>
    </>
  );
}
