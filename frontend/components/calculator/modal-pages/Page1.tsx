"use client";

import { useEffect, useState } from "react";
import { getSubjectsAndLevels, SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Search } from "lucide-react";

export default function Page1({ onSelectExam }: { onSelectExam: (exam: SubjectAndLevel) => void }) {
  const [searchQuery, setSearchQuery] = useState("");
  const [examsData, setExamsData] = useState<SubjectAndLevel[]>([]);

  useEffect(() => {
    async function initializeExamsData() {
      setExamsData(await getSubjectsAndLevels());
    }
    initializeExamsData();
  }, []);

  const filteredExams = examsData.filter((exam) =>
    exam.label.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  return (
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

      <div className="space-y-2">
        <Label className="text-foreground">Select Exam</Label>
        <div className="max-h-64 overflow-y-auto space-y-1 border border-primary/20 rounded-lg p-2">
          {filteredExams.length > 0 ? (
            filteredExams.map((exam) => (
              <button
                key={exam.code}
                onClick={() => onSelectExam(exam)}
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
    </div>
  );
}
