"use server";

import { readFile } from "fs/promises";
import path from "path";

export type ExamLevel = "BASIC" | "EXTENDED" | "BILINGUAL" | "SPECIAL";
export type SubjectAndLevel = { code: string; label: string; levels: ExamLevel[] };

const dirPath = "../backend/src/main/resources/recruitment";

export async function getSubjectsAndLevels(): Promise<SubjectAndLevel[]> {
  const subjectsLevelsPath = path.join(process.cwd(), dirPath, "subjects.json");

  const formulasPath = path.join(process.cwd(), dirPath, "formulas.json");

  try {
    const subjectsLevels = JSON.parse(await readFile(subjectsLevelsPath, "utf-8"));
    const formulas = JSON.parse(await readFile(formulasPath, "utf-8"));

    return formulas.subjects.map((subject) => ({
      levels: subjectsLevels[subject.code],
      ...subject,
    }));
  } catch (err) {
    throw new Error(`Failed to parse JSON at ${subjectsLevelsPath}: ${err}`);
  }
}
