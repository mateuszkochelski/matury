"use client";

import { SetStateAction, useState, Dispatch, useEffect } from "react";
import Page1 from "./modal-pages/Page1";
import Page2 from "./modal-pages/Page2";
import Page3 from "./modal-pages/Page3";
import { getSubjectsAndLevels, SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { X } from "lucide-react";

export type AddExamHandler = (exam: SubjectAndLevel, level: string, score: number) => void;

type ExamSelectionModalProps = {
  onAddExam: AddExamHandler;
  showModal: boolean;
  setShowModal: Dispatch<SetStateAction<boolean>>;
  shouldShowConfirmationPage?: boolean;
};

export default function ExamSelectionModal({
  onAddExam,
  showModal,
  setShowModal,
  shouldShowConfirmationPage,
}: ExamSelectionModalProps) {
  const [selectedExam, setSelectedExam] = useState<SubjectAndLevel | null>(null);
  const [selectedLevel, setSelectedLevel] = useState<string>("");
  const [showConfirmationPage, setShowConfirmationPage] = useState(false);
  const [examsData, setExamsData] = useState<SubjectAndLevel[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const resetState = () => {
    setSelectedExam(null);
    setSelectedLevel("");
    setShowConfirmationPage(false);
  };

  const handleAddExam: AddExamHandler = (exam, level, score) => {
    onAddExam(exam, level, score);

    if (!shouldShowConfirmationPage) {
      resetState();
    }
    setShowConfirmationPage(!!shouldShowConfirmationPage);
  };

  const handleClose = () => {
    setShowModal(false);
    resetState();
  };

  const handleChangeSelectedExam = () => {
    setSelectedExam(null);
    setSelectedLevel("");
  };

  useEffect(() => {
    const initializeExamsData = async () => {
      setExamsData(await getSubjectsAndLevels());
      setIsLoading(false);
    };
    initializeExamsData();
  }, []);

  if (!showModal) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-md border-primary/20">
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <CardTitle className="text-foreground">
            {showConfirmationPage ? "Egzamin dodany" : "Add Exam Score"}
          </CardTitle>
          <button onClick={handleClose} className="text-foreground/60 hover:text-foreground">
            <X className="w-5 h-5" />
          </button>
        </CardHeader>
        <CardContent className="space-y-4">
          {!selectedExam && !showConfirmationPage && (
            <Page1
              onSelectExam={(exam) => setSelectedExam(exam)}
              examsData={examsData}
              isLoading={isLoading}
            />
          )}

          {selectedExam && !showConfirmationPage && (
            <Page2
              selectedExam={selectedExam}
              selectedLevel={selectedLevel}
              setSelectedLevel={setSelectedLevel}
              onAddExam={handleAddExam}
              onChangeSelectedExam={handleChangeSelectedExam}
            />
          )}

          {showConfirmationPage && (
            <Page3
              selectedExam={selectedExam}
              selectedLevel={selectedLevel}
              onAddAnother={() => resetState()}
              onClose={handleClose}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
