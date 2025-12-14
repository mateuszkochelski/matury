"use client";

import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { AddExamHandler } from "../ExamSelectionModal";
import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function Page2({
  selectedExam,
  selectedLevel,
  setSelectedLevel,
  onAddExam,
  onChangeSelectedExam,
}: {
  selectedExam: SubjectAndLevel;
  selectedLevel: string;
  setSelectedLevel: Dispatch<SetStateAction<string>>;
  onAddExam: AddExamHandler;
  onChangeSelectedExam: () => void;
}) {
  const [scoreInput, setScoreInput] = useState("");

  useEffect(() => {
    if (selectedExam.levels.length === 1) {
      setSelectedLevel(selectedExam.levels[0]);
    }
  }, [selectedExam, setSelectedLevel]);

  return (
    <div className="space-y-4">
      <div className="space-y-2 gap-1">
        <div className="flex items-center justify-between">
          <Label className="text-foreground">{selectedExam.label}</Label>
          <button
            onClick={onChangeSelectedExam}
            className="text-primary hover:text-primary/80 text-sm"
          >
            Change
          </button>
        </div>
        <div className="grid grid-cols-2 gap-2">
          {selectedExam.levels.map((level) => (
            <button
              key={level}
              onClick={() => setSelectedLevel(level)}
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

      <div className="space-y-2">
        <Label className="text-foreground">Score (0-100)</Label>
        <Input
          type="number"
          min="0"
          max="100"
          placeholder="Enter score"
          className="border-primary/30"
          value={scoreInput}
          onChange={(e) => setScoreInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && selectedLevel) {
              const score = Number.parseInt(scoreInput || "0") || 0;
              onAddExam(selectedExam, selectedLevel, score);
            }
          }}
        />
      </div>

      <Button
        onClick={() => {
          const score = Number.parseInt(scoreInput || "0") || 0;
          if (selectedLevel) onAddExam(selectedExam, selectedLevel, score);
        }}
        disabled={!selectedLevel}
        className="w-full bg-primary hover:bg-primary/90 text-foreground"
      >
        Add Exam
      </Button>
    </div>
  );
}
