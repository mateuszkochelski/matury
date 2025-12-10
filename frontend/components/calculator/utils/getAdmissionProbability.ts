"use server";

import { BACKEND_URL } from "../../../app/constants";

type ExamScore = {
  examCode: string;
  examLabel: string;
  level: string;
  score: number;
};

export async function getAdmissionProbability(
  universityId: string,
  fieldOfStudyId: string,
  examScores: ExamScore[],
) {
  const normalizedExamScores = examScores.map((score) => ({
    subjectCode: score.examCode,
    level: score.level,
    score: score.score,
  }));
  const respone = await fetch(`${BACKEND_URL}/api/recruitment-calculator/calculate`, {
    method: "POST",
    body: JSON.stringify({ universityId, fieldOfStudyId, examResults: normalizedExamScores }),
  });

  if (!respone.ok) {
    // noop
  }
  const data = await respone.json()
}
