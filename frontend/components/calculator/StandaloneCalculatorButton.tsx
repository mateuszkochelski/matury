"use client";

import React, { useState, lazy, Suspense } from "react";
import { Button } from "../ui/button";
import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";

const ExamSelectionModal = lazy(() => import("./ExamSelectionModal"));

export function StandaloneCalculatorButton() {
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

  return (
    <>
      <Button
        size="lg"
        className="bg-white hover:bg-white/70 w-full sm:w-auto cursor-pointer"
        onClick={() => setShowModal(true)}
      >
        Wpisz wyniki matur
      </Button>
      <Suspense fallback={null}>
        <ExamSelectionModal
          onAddExam={onAddExam}
          showModal={showModal}
          setShowModal={setShowModal}
          shouldShowConfirmationPage
        />
      </Suspense>
    </>
  );
}
