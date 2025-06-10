import { BACKEND_URL } from "../constants";
import CourseDegreeTable from "@/components/course-degree-table/CourseDegreeTable";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbSeparator,
  BreadcrumbLink,
  BreadcrumbPage,
  BreadcrumbList,
} from "@/components/ui/breadcrumb";
import { notFound } from "next/navigation";

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
  const departmentResponse = await fetch(`${BACKEND_URL}/api/department/1`);
  const departmentData: Department = await departmentResponse.json();

  const fieldOfStudyResponse = await fetch(
    `${BACKEND_URL}/api/field_of_study/department/${departmentData.id}`,
  );
  const fieldOfStudyData: FieldOfStudyData = await fieldOfStudyResponse.json();
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { content: fields, page: pageData } = fieldOfStudyData;

  return (
    <div className="min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <Breadcrumb className="mb-5">
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/">Strona główna</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink href={`/university/${departmentData.university.id}`}>
              {departmentData.university.name}
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage>{departmentData.name}</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
      <h1 className="mb-2">{departmentData.name}</h1>
      <h3>Kierunki na wydziale</h3>
      <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center">
        <main className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start">
          <CourseDegreeTable data={fields} />
        </main>
      </div>
    </div>
  );
}
