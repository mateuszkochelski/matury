import { BACKEND_URL } from "../constants";
import { fetchData } from "@/components/custom-table/fetchData";
import { FieldOfStudyData } from "@/components/custom-table/types";

export type Department = {
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

export async function getDepartmentData(
  departmentId: string,
  pageSize?: string,
  pageIndex?: string,
): Promise<{
  departmentData: Department;
  fieldOfStudyData: FieldOfStudyData;
}> {
  const [departmentResponse, fieldOfStudyResponse] = await Promise.all([
    fetch(`${BACKEND_URL}/api/department/${departmentId}`),
    fetchData({
      baseUrl: `${BACKEND_URL}/api/field_of_study/department/${departmentId}`,
      pageSize,
      pageIndex,
    }),
  ]);
  const [departmentData, fieldOfStudyData]: [Department, FieldOfStudyData] = await Promise.all([
    departmentResponse.json(),
    fieldOfStudyResponse.json(),
  ]);
  return { departmentData, fieldOfStudyData };
}
