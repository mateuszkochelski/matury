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

export async function getFieldData(fieldId: string): Promise<{
  fieldData: FieldOfStudy;
  recoFields: FieldOfStudy[];
  thresholdData: ThresholdData;
}> {
  const [fieldResponse, thresholdResponse] = await Promise.all([
    fetch(`${BACKEND_URL}/api/field_of_study/${fieldId}`),
    // It's safe to assume that no field of study will have more than a 1000 threshold entries
    fetchData({
      baseUrl: `${BACKEND_URL}/api/threshold/fieldOfStudy/${fieldId}`,
      pageSize: "1000",
      pageIndex: "0",
    }),
  ]);
  const [fieldData, thresholdData]: [FieldOfStudy, ThresholdData] = await Promise.all([
    fieldResponse.json(),
    thresholdResponse.json(),
  ]);
  const recoUrl = new URL(`${BACKEND_URL}/api/recommendation/field/${fieldId}`);
  recoUrl.searchParams.set("k", "16");
  const recoResponse = await fetch(recoUrl);
  const recoData: FieldOfStudy[] = await recoResponse.json();
  return { fieldData, recoFields: recoData, thresholdData };
}
