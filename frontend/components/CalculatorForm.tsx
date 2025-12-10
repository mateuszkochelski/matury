"use client";

import { useEffect, useState } from "react";
import { getSubjectsAndLevels, SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Badge } from "@/components/ui/badge/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Plus, Trash2, Search, X } from "lucide-react";

export type ExamScore = {
  examCode: string;
  examLabel: string;
  level: string;
  score: number;
};

export default function CalculatorForm() {
  const [examScores, setExamScores] = useState<ExamScore[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [examsData, setExamsData] = useState<SubjectAndLevel[]>([]);
  const filteredExams = examsData.filter((exam) =>
    exam.label.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const [selectedExam, setSelectedExam] = useState<SubjectAndLevel | null>(null);
  const [selectedLevel, setSelectedLevel] = useState<string>("");
  const [probability, setProbability] = useState<number | null>(null);

  const handleAddExam = (exam: SubjectAndLevel, level: string, score: number) => {
    const newScore: ExamScore = {
      examCode: exam.code,
      examLabel: exam.label,
      level,
      score,
    };
    setExamScores([...examScores, newScore]);
    setSelectedExam(null);
    setSelectedLevel("");

    setSearchQuery("");
    setShowModal(false);
  };

  useEffect(() => {
    async function initializeExamsData() {
      setExamsData(await getSubjectsAndLevels());
    }
    initializeExamsData();
  }, []);

  useEffect(() => {
    const stored = localStorage.getItem("examScores");
    if (!stored) return;

    const parsed: ExamScore[] = JSON.parse(stored);
    setExamScores(parsed);
  }, []);

  useEffect(() => {
    localStorage.setItem("examScores", JSON.stringify(examScores));
  }, [examScores]);

  useEffect(() => {
    if (selectedExam?.levels.length === 1) setSelectedLevel(selectedExam.levels[0]);
  }, [selectedExam]);

  const handleUpdateScore = (index: number, score: number) => {
    const updated = [...examScores];
    updated[index].score = Math.max(0, Math.min(100, score));
    setExamScores(updated);
    setProbability(null);
  };

  const handleRemoveScore = (index: number) => {
    setExamScores(examScores.filter((_, i) => i !== index));
    setProbability(null);
  };

  const calculateProbability = () => {
    if (examScores.length === 0) return;

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
    setProbability(Math.round(Math.max(5, baseProbability)));
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
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md border-primary/20">
            <CardHeader className="flex flex-row items-center justify-between space-y-0">
              <CardTitle className="text-foreground">Add Exam Score</CardTitle>
              <button
                onClick={() => {
                  setShowModal(false);
                  setSelectedExam(null);
                  setSelectedLevel("");

                  console.log("halo2");
                  setSearchQuery("");
                }}
                className="text-foreground/60 hover:text-foreground"
              >
                <X className="w-5 h-5" />
              </button>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Search Exams */}
              <div className="space-y-2">
                <Label className="text-foreground">Search Exams</Label>
                <div className="relative">
                  <Search className="absolute left-3 top-3 w-4 h-4 text-foreground/40" />
                  <Input
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search 32 exams..."
                    className="pl-10 border-primary/30"
                  />
                </div>
              </div>

              {/* Exam Selection */}
              {!selectedExam ? (
                <div className="space-y-2">
                  <Label className="text-foreground">Select Exam</Label>
                  <div className="max-h-64 overflow-y-auto space-y-1 border border-primary/20 rounded-lg p-2">
                    {filteredExams.length > 0 ? (
                      filteredExams.map((exam) => (
                        <button
                          key={exam.code}
                          onClick={() => setSelectedExam(exam)}
                          className="w-full text-left px-3 py-2 rounded hover:bg-primary/10 transition-colors text-foreground hover:text-foreground"
                        >
                          <span className="font-medium">{exam.label}</span>
                          <span className="text-foreground/60 text-xs ml-2">
                            ({exam.levels.length} levels)
                          </span>
                        </button>
                      ))
                    ) : (
                      <div className="px-3 py-2 text-foreground/60 text-sm">No exams found</div>
                    )}
                  </div>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Level Selection */}
                  <div className="space-y-2 gap-1">
                    <div className="flex items-center justify-between">
                      <Label className="text-foreground">{selectedExam.label}</Label>
                      <button
                        onClick={() => {
                          setSelectedExam(null);
                          setSelectedLevel("");

                          console.log("halo3");
                        }}
                        className="text-primary hover:text-primary/80 text-sm"
                      >
                        Change
                      </button>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      {selectedExam.levels.map((level) => (
                        <button
                          key={level}
                          onClick={() => {
                            setSelectedLevel(level);
                            console.log("halo4");
                          }}
                          className={`px-4 py-2 rounded border-2 transition-colors font-medium ${
                            selectedLevel === level
                              ? "bg-primary text-foreground border-primary"
                              : "border-primary/30 text-foreground hover:border-primary/60"
                          }`}
                        >
                          {level}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Score Input */}
                  <div className="space-y-2">
                    <Label className="text-foreground">Score (0-100)</Label>
                    <Input
                      type="number"
                      min="0"
                      max="100"
                      placeholder="Enter score"
                      className="border-primary/30"
                      onKeyDown={(e) => {
                        if (e.key === "Enter" && selectedLevel) {
                          const score = Number.parseInt((e.target as HTMLInputElement).value) || 0;
                          handleAddExam(selectedExam, selectedLevel, score);
                        }
                      }}
                      onChange={() => {}}
                    />
                  </div>

                  {/* Add Button */}
                  <Button
                    onClick={() => {
                      const input = document.querySelector(
                        "input[placeholder='Enter score']",
                      ) as HTMLInputElement;
                      const score = Number.parseInt(input.value || "0");
                      if (selectedLevel) {
                        handleAddExam(selectedExam, selectedLevel, score);
                      }
                    }}
                    disabled={!selectedLevel}
                    className="w-full bg-primary hover:bg-primary/90 text-foreground"
                  >
                    Add Exam
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}

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
        <div className="flex flex-col sm:flex-row sm:items-center gap-4 pt-4 border-t border-primary/20">
          <Button
            onClick={calculateProbability}
            className="bg-primary hover:bg-primary/90 text-foreground"
          >
            Calculate Probability
          </Button>

          {probability !== null && (
            <div className="flex items-center gap-3">
              <div className="flex-1">
                <p className="text-sm text-foreground/60 mb-1">Admission Probability</p>
                <div className="flex items-center gap-2">
                  <div className="flex-1 bg-primary/10 rounded-full h-3 overflow-hidden">
                    <div
                      className="bg-primary h-full transition-all"
                      style={{ width: `${probability}%` }}
                    />
                  </div>
                  <span className="text-xl font-bold text-primary whitespace-nowrap">
                    {probability}%
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
