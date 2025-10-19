import { BACKEND_URL } from "../constants";
import { fetchData } from "@/components/custom-table/fetchData";

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

type University = {
  id: number;
  name: string;
  city: string;
  acronym: string;
  url: string;
  description: string;
  address: string;
  longitude: number;
  latitude: number;
};

type DepartmentData = {
  content: Department[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export async function getUniversityData(universityId: string): Promise<{
  universityData: University;
  departmentData: DepartmentData;
}> {
  const [universityResponse, departmentResponse] = await Promise.all([
    fetch(`${BACKEND_URL}/api/university/${universityId}`),
    // It's safe to assume that no university will have more than a 1000 departments
    fetchData({
      baseUrl: `${BACKEND_URL}/api/department/university/${universityId}`,
      pageSize: "1000",
      pageIndex: "0",
    }),
  ]);
  const [universityData, departmentData]: [University, DepartmentData] = await Promise.all([
    universityResponse.json(),
    departmentResponse.json(),
  ]);
  return { universityData, departmentData };
}
