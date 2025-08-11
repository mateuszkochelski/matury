import { getDepartmentData } from "@/app/utils/getDepartmentData";
import { CustomBreadcrumb } from "@/components/custom-breadcrumb/CustomBradcrumb";
import { DepartmentTable } from "@/components/custom-table/department";
import { TableSearchParams } from "@/components/custom-table/fetchData";
import { notFound, redirect } from "next/navigation";

export default async function Page({
  params,
  searchParams,
}: {
  params: Promise<{ universityId: string; departmentId: string }>;
  searchParams?: Promise<TableSearchParams>;
}) {
  const { universityId, departmentId } = await params;

  if (!departmentId) {
    notFound();
  }

  const { pageSize, pageIndex, hiddenColumns } = (await searchParams) ?? {};

  const { departmentData, fieldOfStudyData } = await getDepartmentData(
    departmentId,
    pageSize,
    pageIndex,
  );
  const { number: pageNumber, totalElements, size } = fieldOfStudyData.page;

  // discourage user from providing artificial values
  if (departmentData.university.id !== Number(universityId)) {
    return redirect(`/${departmentData.university.id}/${departmentId}`);
  }

  return (
    <>
      <CustomBreadcrumb
        items={[
          { name: "Strona główna", href: "/" },
          {
            name: departmentData.university.name,
            href: `/${universityId}`,
          },
          { name: departmentData.name },
        ]}
      />
      <h1>{departmentData.name}</h1>
      <h2>Kierunki na wydziale</h2>
      <DepartmentTable
        data={fieldOfStudyData.content}
        pageNumber={pageNumber}
        pageSize={size}
        totalElements={totalElements}
        hiddenColumns={hiddenColumns?.split(",")}
      />
    </>
  );
}
