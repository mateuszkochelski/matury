"use client";

import React, { useMemo, useState, lazy, Suspense } from "react";
import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Calculator, FileText, Search, TrendingUp } from "lucide-react";
import { useRouter } from "next/navigation";

const ExamSelectionModal = lazy(() => import("./calculator/ExamSelectionModal"));

export function FunctionalitySection() {
  const router = useRouter();
  const [showModal, setShowModal] = useState(false);

  const onAddExam = (exam: SubjectAndLevel, level: string, score: number) => {
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

  const sections = useMemo(() => {
    const commonClassName = "w-6 md:w-8 h-6 md:h-8";
    return [
      {
        icon: <Search className={`${commonClassName} text-primary`} />,
        title: "Wyszukiwarka",
        description: "Znajdź dokładnie te rzeczy których potrzebujesz za pomocą różnych kryteriów",
        onClick: () => router.push("/szukaj"),
      },
      {
        icon: <Calculator className={`${commonClassName} text-orange-400`} />,
        title: "Kalkulator rekrutacyjny",
        description: "Sprawdź jaką masz szansę na udaną rekrutację na ulubionej uczelni",
        onClick: () => setShowModal(true),
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
  }, [router]);

  return (
    <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6 md:gap-8">
      {sections.map((feature, index) => (
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

      <Suspense fallback={null}>
        <ExamSelectionModal
          onAddExam={onAddExam}
          showModal={showModal}
          setShowModal={setShowModal}
          shouldShowConfirmationPage
        />
      </Suspense>
    </div>
  );
}
