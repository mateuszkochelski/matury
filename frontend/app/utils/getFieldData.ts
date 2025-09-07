import { BACKEND_URL } from "../constants";
import { fetchData } from "@/components/custom-table/fetchData";
import { FieldOfStudyData } from "@/components/custom-table/types";

export type FieldOfStudy = {
  id: number;
  name: string;
  level: string;
  duration: number;
  language: string;
  university: {
    id: number;
    name: string;
    acronym: string;
    city: string;
  };
  department: {
    id: number;
    name: string;
  };
};

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
  departmentFields: FieldOfStudy[];
  thresholdData: ThresholdData;
}> {
  const [fieldResponse, thresholdResponse] = await Promise.all([
    fetch(`${BACKEND_URL}/api/field_of_study/${fieldId}`),
    // It's safe to assume that no field of study will have more than a 1000 threshold entries
    fetchData(`${BACKEND_URL}/api/threshold/fieldOfStudy/${fieldId}`, "1000", "0"),
  ]);
  const [fieldData, thresholdData]: [FieldOfStudy, ThresholdData] = await Promise.all([
    fieldResponse.json(),
    thresholdResponse.json(),
  ]);
  const [departmentFieldsResponse] = await Promise.all([
    fetchData(
      `${BACKEND_URL}/api/field_of_study/department/${fieldData.department.id}`,
      "1000",
      "0",
    ),
  ]);
  const [departmentFieldsData]: [FieldOfStudyData] = await Promise.all([
    departmentFieldsResponse.json(),
  ]);
  return { fieldData, departmentFields: departmentFieldsData.content, thresholdData };
}
