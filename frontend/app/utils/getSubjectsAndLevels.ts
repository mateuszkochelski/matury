"use server";

import { BACKEND_URL } from "../constants";

export type ExamLevel = "BASIC" | "EXTENDED" | "BILINGUAL" | "SPECIAL";
export type SubjectAndLevel = { code: string; label: string; levels: ExamLevel[] };

export async function getSubjectsAndLevels(): Promise<SubjectAndLevel[]> {
  const response = await fetch(`${BACKEND_URL}/api/recruitment-calculator/subjects`, {
    headers: { Accept: "application/json" },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch subjects from backend: ${response.status}`);
  }

  const data = await response.json();
  return data as SubjectAndLevel[];
}
