import { getDepartmentData } from "@/app/utils/getDepartmentData";
import { serializeFiltersFormValues } from "@/app/utils/serializeFiltersFormValues";
import { Breadcrumb } from "@/components/Breadcrumb";
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

  const { pageSize, pageIndex, hiddenColumns, ...rest } = (await searchParams) ?? {};

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
      <Breadcrumb
        items={[
          {
            name: departmentData.university.name,
            href: `/${departmentData.university.id}`,
          },
          { name: departmentData.name },
        ]}
      />
      <h1 className="text-foreground mb-4">{departmentData.name}</h1>
      <h2 className="text-foreground">Kierunki studiów</h2>
      <DepartmentTable
        data={fieldOfStudyData.content}
        pageNumber={pageNumber}
        pageSize={size}
        totalElements={totalElements}
        hiddenColumns={hiddenColumns?.split(",")}
        filters={serializeFiltersFormValues(rest)}
      />
    </>
  );
}
