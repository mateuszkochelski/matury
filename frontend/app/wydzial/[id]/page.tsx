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

export default async function Home({ params }: { params: Promise<{ id: string }> }) {
  const departmentId = (await params).id;
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
    <main className="min-h-screen flex flex-col p-2 pb-20 gap-3 sm:gap-4 sm:p-8 md:p-16 lg:p-20 font-[family-name:var(--font-geist-sans)]">
      <CustomBreadcrumb
        items={[
          { name: "Strona główna", href: "/" },
          {
            name: departmentData.university.name,
            href: `/university/${departmentData.university.id}`,
          },
          { name: departmentData.name },
        ]}
      />
      <h1>{departmentData.name}</h1>
      <h2>Kierunki na wydziale</h2>
      <CourseDegreeTable data={fields} />
    </main>
  );
}
