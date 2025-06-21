import CourseDegreeTable from "@/components/course-degree-table/CourseDegreeTable";
import { CustomBreadcrumb } from "@/components/custom-breadcrumb/CustomBradcrumb";
import { notFound } from "next/navigation";
import { getDepartmentData } from "@/utils/getDepartmentData";

export default async function Home({ params }: { params: Promise<{ id: string }> }) {
  const departmentId = (await params).id;
  if (!departmentId) {
    notFound();
  }

  const {departmentData, departmentFields} = await getDepartmentData(departmentId);

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
      <CourseDegreeTable data={departmentFields} />
    </main>
  );
}
