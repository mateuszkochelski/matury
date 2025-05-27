import { BACKEND_URL } from "./constants";
import CourseDegreeTable from "@/components/course-degree-table/CourseDegreeTable";

type FieldOfStudy = {
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

type FieldOfStudyData = {
  content: FieldOfStudy[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export default async function Home() {
  const response = await fetch(`${BACKEND_URL}/api/field_of_study`);
  const data: FieldOfStudyData = await response.json();
  const { content: fields, page: pageData } = data;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { number: pageNumber } = pageData;

  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start">
        <CourseDegreeTable data={fields} />
      </main>
    </div>
  );
}
