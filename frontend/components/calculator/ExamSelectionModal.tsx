"use client";

import { SetStateAction, useEffect, useState, Dispatch } from "react";
import { getSubjectsAndLevels, SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Search, X } from "lucide-react";

type ExamSelectionModalProps = {
  handleAddExam: (exam: SubjectAndLevel, level: string, score: number) => void;
  showModal: boolean;
  setShowModal: Dispatch<SetStateAction<boolean>>;
};

export default function ExamSelectionModal({
  handleAddExam,
  showModal,
  setShowModal,
}: ExamSelectionModalProps) {
  const [searchQuery, setSearchQuery] = useState("");

  const [selectedExam, setSelectedExam] = useState<SubjectAndLevel | null>(null);
  const [selectedLevel, setSelectedLevel] = useState<string>("");

  const [examsData, setExamsData] = useState<SubjectAndLevel[]>([]);
  const filteredExams = examsData.filter((exam) =>
    exam.label.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  useEffect(() => {
    async function initializeExamsData() {
      setExamsData(await getSubjectsAndLevels());
    }
    initializeExamsData();
  }, []);

  useEffect(() => {
    if (selectedExam?.levels.length === 1) setSelectedLevel(selectedExam.levels[0]);
  }, [selectedExam]);

  return (
    showModal && (
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

                        setSelectedExam(null);
                        setSelectedLevel("");
                        setSearchQuery("");
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

                        setSelectedExam(null);
                        setSelectedLevel("");
                        setSearchQuery("");
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
    )
  );
}
