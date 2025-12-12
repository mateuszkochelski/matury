"use client";

import { useEffect, useState } from "react";
import ExamSelectionModal from "./ExamSelectionModal";
import { getAdmissionProbability } from "./utils/getAdmissionProbability";
import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Badge } from "@/components/ui/badge/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Plus, Trash2 } from "lucide-react";
import { useParams } from "next/navigation";

export type ExamScore = {
  examCode: string;
  examLabel: string;
  level: string;
  score: number;
};

type ScoringResults = {
  probability: number;
  points: number;
};

export default function CalculatorForm() {
  const { universityId, fieldId } = useParams<{ universityId: string; fieldId: string }>();
  const [examScores, setExamScores] = useState<ExamScore[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [showAllExams, setShowAllExams] = useState(false);
  const examsToShow = showAllExams ? examScores.length : 4;

  const [scoringResults, setScoringResults] = useState<ScoringResults | null>(null);

  useEffect(() => {
    const stored = localStorage.getItem("examScores");
    if (!stored) return;

    const parsed: ExamScore[] = JSON.parse(stored);
    setExamScores(parsed);
  }, []);

  useEffect(() => {
    if (examScores.length === 0) return;
    localStorage.setItem("examScores", JSON.stringify(examScores));
  }, [examScores]);

  const onAddExam = (exam: SubjectAndLevel, level: string, score: number) => {
    const newScore: ExamScore = {
      examCode: exam.code,
      examLabel: exam.label,
      level,
      score,
    };
    setExamScores((curr) => [...curr, newScore]);
    setShowModal(false);
  };

  const handleUpdateScore = (index: number, score: number) => {
    const updated = [...examScores];
    updated[index].score = Math.max(0, Math.min(100, score));
    setExamScores(updated);
    setScoringResults(null);
  };

  const handleRemoveScore = (index: number) => {
    setExamScores(examScores.filter((_, i) => i !== index));
    setScoringResults(null);
  };

  const calculateProbability = async () => {
    if (examScores.length === 0) return;

    // TODO: once the BE is corrected this logic should be refined
    const data = await getAdmissionProbability(universityId, fieldId, examScores);
    console.log({ data });
    // Calculate average score
    // const totalScore = examScores.reduce((sum, exam) => sum + exam.score, 0);
    // const averageScore = totalScore / examScores.length;

    // Weight calculation: Advanced and Bilingual exams are weighted higher
    const weightedScores = examScores.map((exam) => {
      let weight = 1;
      if (exam.level === "Advanced") weight = 1.15;
      if (exam.level === "Bilingual") weight = 1.2;
      return exam.score * weight;
    });

    const totalWeighted = weightedScores.reduce((sum, score) => sum + score, 0);
    const adjustedAverage = totalWeighted / examScores.length;

    // Convert to probability (0-100)
    const baseProbability = Math.min(100, (adjustedAverage / 100) * 110);
    setScoringResults({
      probability: Math.round(Math.max(5, baseProbability)),
      points: data.totalPoints ?? weightedScores,
    });
  };

  return (
    <div className="space-y-2">
      {/* Add Exam Button */}
      <div>
        <Button
          onClick={() => setShowModal(true)}
          className="bg-primary hover:bg-primary/90 text-foreground gap-2 w-full sm:w-auto"
        >
          <Plus className="w-4 h-4" />
          Add Exam Score
        </Button>
      </div>

      {/* Modal for exam selection */}
      <ExamSelectionModal onAddExam={onAddExam} showModal={showModal} setShowModal={setShowModal} />

      {/* Exam Scores Summary */}
      {examScores.length > 0 && (
        <div className="space-y-4">
          <Label className="text-foreground font-semibold">
            Selected Exams ({examScores.length})
          </Label>
          <div className="relative transition-all duration-300">
            <div className="space-y-2">
              {examScores.slice(0, examsToShow).map((exam, index) => (
                <div
                  key={index}
                  className="flex flex-col sm:flex-row sm:items-center gap-3 p-3 bg-primary/5 rounded-lg border border-primary/20"
                >
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium text-foreground">{exam.examLabel}</span>
                      <Badge variant="secondary" className="bg-accent/20 text-accent border-0">
                        {exam.level}
                      </Badge>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 sm:ml-auto">
                    <div className="flex items-center gap-2">
                      <span className="text-sm text-foreground/60">Score:</span>
                      <Input
                        type="number"
                        min="0"
                        max="100"
                        value={exam.score}
                        onChange={(e) =>
                          handleUpdateScore(index, Number.parseInt(e.target.value) || 0)
                        }
                        className="w-20 border-primary/30 text-center"
                      />
                      <span className="text-sm text-foreground/60">/100</span>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveScore(index)}
                      className="text-red-500 hover:text-red-600 hover:bg-red-50"
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
            {!showAllExams && examScores.length > 3 && (
              <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-b from-transparent to-white pointer-events-none" />
            )}
          </div>
          {examScores.length > 4 && (
            <Button
              variant="outline"
              onClick={() => setShowAllExams(!showAllExams)}
              className="w-full border-primary/30 text-primary hover:bg-primary/5 gap-2"
            >
              {/* // TODO: those should be emotes rather than svgs */}
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                {showAllExams ? (
                  <>
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-4.803m5.596-3.856a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0z"
                    />
                  </>
                ) : (
                  <>
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                    />
                  </>
                )}
              </svg>
              {showAllExams ? "Pokaż mniej" : "Pokaż wszystkie"}
            </Button>
          )}
        </div>
      )}

      {/* Calculate Button and Result */}
      {examScores.length > 0 && (
        <div className="flex flex-col gap-4 border-primary/20">
          <Button
            onClick={calculateProbability}
            className="bg-primary hover:bg-primary/90 text-foreground w-full sm:w-auto"
          >
            Oblicz Prawdopodobieństwo
          </Button>

          {scoringResults !== null && (
            <div className="space-y-4 p-4 bg-primary/5 rounded-lg border border-primary/20 sm:flex">
              {/* Points Scored Section */}
              <div className="space-y-2">
                <p className="text-sm text-foreground/60">Zdobyte Punkty</p>
                <div className="flex items-baseline gap-2">
                  <span className="text-4xl font-bold text-primary">{scoringResults.points}</span>
                  <span className="text-foreground/60">punktów</span>
                </div>
              </div>

              {/* Divider */}
              <div className="block border-primary/20 border-t sm:border-t-0 sm:border-l sm:self-stretch sm:mx-4" />

              {/* Probability Section */}
              <div className="space-y-2 sm:flex-1">
                <p className="text-sm text-foreground/60">Prawdopodobieństwo Przyjęcia</p>
                <div className="flex items-center gap-3">
                  <div className="flex-1 bg-primary/10 rounded-full h-4 overflow-hidden">
                    <div
                      className="bg-primary h-full transition-all"
                      style={{ width: `${scoringResults.probability}%` }}
                    />
                  </div>
                  <span className="text-3xl font-bold text-primary whitespace-nowrap">
                    {scoringResults.probability}%
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {examScores.length === 0 && (
        <div className="text-center py-8">
          <p className="text-foreground/60">
            No exams selected. Click "Add Exam Score" to get started.
          </p>
        </div>
      )}
    </div>
  );
}
