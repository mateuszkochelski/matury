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

  if (!response.ok) {
    const errorBody = await response.text();
    console.error("Recruitment calculator API error:", {
      status: response.status,
      statusText: response.statusText,
      url: response.url,
      universityId,
      fieldOfStudyId,
      examScoresCount: examScores.length,
      errorBody,
    });
    return;
  }

  return await response.json();
}
