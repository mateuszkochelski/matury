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
  const response = await fetch(`${BACKEND_URL}/api/recruitment-calculator/calculate`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify({ universityId, fieldOfStudyId, examResults: normalizedExamScores }),
  });

  console.log({ body: JSON.stringify({ universityId, fieldOfStudyId, examResults: normalizedExamScores }), response });

  if (!response.ok) {
    // noop
    return;
  }

  const data = await response.json();
  return data;
}
