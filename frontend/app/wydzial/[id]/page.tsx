import { CustomBreadcrumb } from "@/components/custom-breadcrumb/CustomBradcrumb";
import { notFound } from "next/navigation";
import { getDepartmentData } from "@/utils/getDepartmentData";
import { DepartmentTable } from "@/components/custom-table/department";
import { TableSearchParams } from "@/components/custom-table/fetchData";

export default async function Home({ params, searchParams }: { params: Promise<{ id: string }>, searchParams?: Promise<TableSearchParams> }) {
  const departmentId = (await params).id;
  if (!departmentId) {
    notFound();
  }

  const { pageSize, pageIndex, hiddenColumns } = await searchParams ?? {};

  const {departmentData, fieldOfStudyData} = await getDepartmentData(departmentId, pageSize, pageIndex);
  const { number: pageNumber, totalElements, size } = fieldOfStudyData.page;

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
      <DepartmentTable data={fieldOfStudyData.content}
        pageNumber={pageNumber}
        pageSize={size}
        totalElements={totalElements}
        hiddenColumns={hiddenColumns?.split(",")} />
    </main>
  );
}
