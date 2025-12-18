"use client";

import { useState } from "react";
import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Search } from "lucide-react";

export default function Page1({
  onSelectExam,
  examsData,
  isLoading,
}: {
  onSelectExam: (exam: SubjectAndLevel) => void;
  examsData: SubjectAndLevel[];
  isLoading: boolean;
}) {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredExams = examsData.filter((exam) =>
    exam.label.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  return (
    <div className="space-y-2">
      <Label className="text-foreground">Wyszukaj przedmiot</Label>
      <div className="relative">
        <Search className="absolute left-3 top-3 w-4 h-4 text-foreground/40" />
        <Input
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Wyszukaj z 32 przedmiotów..."
          className="pl-10 border-primary/30"
        />
      </div>

      <div className="space-y-2">
        <Label className="text-foreground">Wybierz przedmiot</Label>
        <div>
          <div className="max-h-64 overflow-y-auto space-y-1 border border-primary/20 rounded-lg p-2">
            <ExamsList exams={filteredExams} isLoading={isLoading} onSelectExam={onSelectExam} />
          </div>
        </div>
      </div>
    </div>
  );
}

function ExamsList({
  exams,
  isLoading,
  onSelectExam,
}: {
  exams: SubjectAndLevel[];
  isLoading: boolean;
  onSelectExam: (exam: SubjectAndLevel) => void;
}) {
  if (isLoading) {
    return Array.from({ length: 32 }).map((_, index) => (
      <div key={index} className="w-full px-3 py-2 rounded animate-pulse">
        <div className="flex items-center gap-2">
          <div className="h-4 bg-primary/20 rounded flex-1" />
          <div className="h-3 w-16 bg-primary/10 rounded" />
        </div>
      </div>
    ));
  }

  if (exams.length === 0) {
    return <div className="px-3 py-2 text-foreground/60 text-sm">Nie znaleziono matur</div>;
  }

  return exams.map((exam) => (
    <button
      key={exam.code}
      onClick={() => onSelectExam(exam)}
      className="w-full text-left px-3 py-2 rounded hover:bg-primary/10 transition-colors text-foreground hover:text-foreground"
    >
      <span className="font-medium">{exam.label}</span>
      <span className="text-foreground/60 text-xs ml-2">({exam.levels.length} poziomy)</span>
    </button>
  ));
}
