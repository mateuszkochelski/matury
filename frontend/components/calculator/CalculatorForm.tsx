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

  const [scoringResults, setScoringResults] = useState<ScoringResults | null>(null);

  const handleAddExam = (exam: SubjectAndLevel, level: string, score: number) => {
    const newScore: ExamScore = {
      examCode: exam.code,
      examLabel: exam.label,
      level,
      score,
    };
    setExamScores((curr) => [...curr, newScore]);
    setShowModal(false);
  };

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
    <div>
      {/* Add Exam Button */}
      <div>
        <Button
          onClick={() => setShowModal(true)}
          className="bg-primary hover:bg-primary/90 text-foreground gap-2"
        >
          <Plus className="w-4 h-4" />
          Add Exam Score
        </Button>
      </div>

      {/* Modal for exam selection */}
      <ExamSelectionModal
        onAddExam={handleAddExam}
        showModal={showModal}
        setShowModal={setShowModal}
      />

      {/* Exam Scores Summary */}
      {examScores.length > 0 && (
        <div className="space-y-3">
          <Label className="text-foreground font-semibold">
            Selected Exams ({examScores.length})
          </Label>
          <div className="space-y-2">
            {examScores.map((exam, index) => (
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
        </div>
      )}

      {/* Calculate Button and Result */}
      {examScores.length > 0 && (
        <div className="flex flex-col gap-4 pt-4 border-t border-primary/20">
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
