"use client";

import { FieldOfStudyExtended } from "./types";
import { Button } from "@/components/ui/button";
import { Row } from "@tanstack/react-table";
import { HeartIcon } from "lucide-react";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export function RowActions({ row }: { row: Row<FieldOfStudyExtended> }) {
  return (
    <div className="flex justify-center">
      <Button variant="ghost" size="icon">
        <HeartIcon fill="#f008" />
      </Button>
    </div>
  );
}
