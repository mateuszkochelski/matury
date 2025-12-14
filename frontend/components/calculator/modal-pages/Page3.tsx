"use client";

import { SubjectAndLevel } from "@/app/utils/getSubjectsAndLevels";
import { Button } from "@/components/ui/button";
import { CheckCircle, Plus } from "lucide-react";

export default function Page3({
  selectedExam,
  selectedLevel,
  onAddAnother,
  onClose,
}: {
  selectedExam: SubjectAndLevel | null;
  selectedLevel: string;
  onAddAnother: () => void;
  onClose: () => void;
}) {
  return (
    <div className="space-y-6 py-4">
      <div className="flex flex-col items-center text-center space-y-3">
        <div className="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center">
          <CheckCircle className="w-10 h-10 text-primary" />
        </div>
        <div className="space-y-1">
          <h3 className="text-lg font-semibold text-foreground">Egzamin został dodany!</h3>
          {!!selectedExam && (
            <p className="text-sm text-foreground/60">
              {selectedExam.label} ({selectedLevel}) został pomyślnie dodany do listy.
            </p>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <Button
          onClick={onAddAnother}
          className="w-full bg-primary hover:bg-primary/90 text-foreground gap-2"
        >
          <Plus className="w-4 h-4" />
          Dodaj kolejny egzamin
        </Button>
        <Button
          onClick={onClose}
          variant="outline"
          className="w-full border-primary/30 text-foreground hover:bg-primary/5 bg-transparent"
        >
          Zamknij
        </Button>
      </div>
    </div>
  );
}
