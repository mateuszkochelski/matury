import { BACKEND_URL } from "../app/constants";
import { FieldOfStudy, FieldOfStudyData } from "@/components/course-degree-table/types";

type Department = {
  id: number;
  name: string;
  url: string;
  university: {
    id: number;
    name: string;
    acronym: string;
    city: string;
  };
};

export async function getDepartmentData(departmentId: string): Promise<{
  departmentData: Department,
  departmentFields: FieldOfStudy[]
}> {
  const [departmentResponse, fieldOfStudyResponse] = await Promise.all([
    fetch(`${BACKEND_URL}/api/department/${departmentId}`),
    fetch(`${BACKEND_URL}/api/field_of_study/department/${departmentId}`),
  ]);
  const [departmentData, fieldOfStudyData]: [Department, FieldOfStudyData] = await Promise.all([
    departmentResponse.json(),
    fieldOfStudyResponse.json(),
  ]);
  return {departmentData, departmentFields: fieldOfStudyData.content};
}
