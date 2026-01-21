import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Calculator, FileText, Search, TrendingUp } from "lucide-react";

export function FunctionalitySection() {
  const commonClassName = "w-6 md:w-8 h-6 md:h-8";
  const sections = [
    {
      icon: <Search className={`${commonClassName} text-primary`} />,
      title: "Wyszukiwarka",
      description: "Znajdź dokładnie te informacje które potrzebujesz dzięki rozbudowanym filtrom",
    },
    {
      icon: <Calculator className={`${commonClassName} text-orange-400`} />,
      title: "Kalkulator rekrutacyjny",
      description: "Sprawdź jaką masz szansę dostać się na wymarzony kierunek studiów",
    },
    {
      icon: <FileText className={`${commonClassName} text-accent`} />,
      title: "Dane historyczne",
      description: "Zobacz jak w ostatnich latach wyglądała rekrutacja na dany kierunek studiów",
    },
    {
      icon: <TrendingUp className={`${commonClassName} text-green-400`} />,
      title: "Dalsza kariera",
      description: "Sprawdź jak zarabiają absolwenci wybranych kierunków studiów",
    },
  ];

  return (
    <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6 md:gap-8">
      {sections.map((feature, index) => (
        <Card
          key={index}
          className="text-center border-primary/20 hover:shadow-lg transition-shadow"
        >
          <CardHeader>
            <div className="mx-auto mb-4 p-3 bg-background rounded-full w-fit">{feature.icon}</div>
            <CardTitle className="text-foreground text-lg">{feature.title}</CardTitle>
          </CardHeader>
          <CardContent>
            <CardDescription className="text-foreground/60">{feature.description}</CardDescription>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
