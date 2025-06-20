import { BACKEND_URL } from "../../constants";
import CourseDegreeTable from "@/components/course-degree-table/CourseDegreeTable";
import { CustomBreadcrumb } from "@/components/custom-breadcrumb/CustomBradcrumb";
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

export default async function Home({ params }: { params: { id: string } }) {
  const departmentId = params.id;
  if (!departmentId) {
    notFound();
  }

  const departmentResponse = await fetch(`${BACKEND_URL}/api/department/${departmentId}`);
  const departmentData: Department = await departmentResponse.json();

  const fieldOfStudyResponse = await fetch(
    `${BACKEND_URL}/api/field_of_study/department/${departmentData.id}`,
  );
  const fieldOfStudyData: FieldOfStudyData = await fieldOfStudyResponse.json();
  const { content: fields } = fieldOfStudyData;

  return (
    <div className="min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <CustomBreadcrumb
        items={[
          { name: "Strona główna", href: "/" },
          {
            name: departmentData.university.name,
            href: `/university/${departmentData.university.id}`,
          },
          { name: departmentData.name },
        ]}
        className="mb-3"
      />
      <h1 className="mb-5">{departmentData.name}</h1>
      <h2>Kierunki na wydziale</h2>
      <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center">
        <main className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start">
          <CourseDegreeTable data={fields} />
        </main>
      </div>
    </div>
  );
}
