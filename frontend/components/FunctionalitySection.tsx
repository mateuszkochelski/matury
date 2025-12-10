"use client";

import { useState } from "react";
import ExamSelectionModal from "./calculator/ExamSelectionModal";
import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Calculator, FileText, Search, TrendingUp } from "lucide-react";
import { useRouter } from "next/navigation";

export function FunctionalitySection() {
  const router = useRouter();

  const [showModal, setShowModal] = useState(false);

  const onAddExam = (exam: SubjectAndLevel, level: string, score: number) => {
    setShowModal(false);

    let toSave = [];
    const currentlyStored = localStorage.getItem("examScores");
    if (currentlyStored) {
      toSave = JSON.parse(currentlyStored);
    }

    toSave.push({
      examCode: exam.code,
      examLabel: exam.label,
      level,
      score,
    });

    localStorage.setItem("examScores", JSON.stringify(toSave));
  };

  return (
    <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6 md:gap-8">
      {[
        {
          icon: <Search className="w-6 md:w-8 h-6 md:h-8 text-primary" />,
          title: "Wyszukiwarka",
          description:
            "Znajdź dokładnie te rzeczy których potrzebujesz za pomocą różnych kryteriów",
          onClick: () => router.push("/szukaj"),
        },
        {
          icon: <FileText className="w-6 md:w-8 h-6 md:h-8 text-orange-400" />,
          title: "Dane historyczne",
          description:
            "Zobacz jak w ostatnich latach wyglądała rekrutacja na dany kierunek studiów",
        },
        {
          icon: <Calculator className="w-6 md:w-8 h-6 md:h-8 text-accent" />,
          title: "Kalkulator rekrutacyjny",
          description: "Sprawdź jaką masz szansę na udaną rekrutację na ulubionej uczelni",
          onClick: () => setShowModal(true),
        },
        {
          icon: <TrendingUp className="w-6 md:w-8 h-6 md:h-8 text-green-400" />,
          title: "Dalsza kariera",
          description: "Sprawdź jak zarabiają absolwenci wybranych kierunków studiów",
        },
      ].map((feature, index) => (
        <Card
          key={index}
          className="text-center border-primary/20 hover:shadow-lg transition-shadow"
          onClick={feature.onClick}
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
      <ExamSelectionModal onAddExam={onAddExam} showModal={showModal} setShowModal={setShowModal} />
    </div>
  );
}
