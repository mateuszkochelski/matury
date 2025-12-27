import { BACKEND_URL } from "../constants";
import { fetchData } from "@/components/custom-table/fetchData";
import { FieldOfStudy } from "@/components/custom-table/types";

export type Threshold = {
  id: number;
  year: number;
  phase: number;
  admissionLimit: number | null;
  admissions: number | null;
  threshold: number;
  specialRequirements: string | null;
  fieldOfStudy: {
    id: number;
    name: string;
  };
};

type ThresholdData = {
  content: Threshold[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

type GraduateData = {
  fieldOfStudyId: number;
  avgIncome: number;
  incomeAfterYear1: number;
  incomeAfterYear2: number;
  incomeAfterYear3: number;
  incomeAfterYear4: number;
  incomeAfterYear5: number;
  passRate: number;
};

export async function getFieldData(fieldId: string): Promise<{
  fieldData: FieldOfStudy;
  graduateData: GraduateData;
  recoFields: FieldOfStudy[];
  thresholdData: ThresholdData;
}> {
  const [fieldResponse, thresholdResponse, graduateResponse] = await Promise.all([
    fetch(`${BACKEND_URL}/api/field_of_study/${fieldId}`),
    // It's safe to assume that no field of study will have more than a 1000 threshold entries
    fetchData({
      baseUrl: `${BACKEND_URL}/api/threshold/fieldOfStudy/${fieldId}`,
      pageSize: "1000",
      pageIndex: "0",
    }),
    fetch(`${BACKEND_URL}/api/field_of_study/${fieldId}/graduate`),
  ]);
  const [fieldData, thresholdData]: [FieldOfStudy, ThresholdData] = await Promise.all([
    fieldResponse.json(),
    thresholdResponse.json(),
  ]);
  // default values if request fails (not found)
  let graduateData: GraduateData = {
    fieldOfStudyId: fieldData.id,
    avgIncome: 0,
    incomeAfterYear1: 0,
    incomeAfterYear2: 0,
    incomeAfterYear3: 0,
    incomeAfterYear4: 0,
    incomeAfterYear5: 0,
    passRate: 0,
  };
  if (graduateResponse.ok) {
    graduateData = await graduateResponse.json();
  }
  const recoResponse = await fetch(`${BACKEND_URL}/api/recommendation/field/${fieldId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ k: 16 }),
  });
  const recoData: FieldOfStudy[] = await recoResponse.json();
  return { fieldData, graduateData, recoFields: recoData, thresholdData };
}
