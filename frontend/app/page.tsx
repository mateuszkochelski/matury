import { BACKEND_URL } from "./constants";
import { fetchData, TableSearchParams } from "@/components/custom-table/fetchData";
import { MainTable } from "@/components/custom-table/main";
import { FieldOfStudyData } from "@/components/custom-table/types";

export default async function Home({
  searchParams,
}: {
  searchParams?: Promise<TableSearchParams>;
}) {
  const { pageSize, pageIndex, hiddenColumns, sortBy, direction } = (await searchParams) ?? {};

  const response = await fetchData(`${BACKEND_URL}/api/field_of_study`, pageSize, pageIndex, sortBy, direction);
  console.log({searchParams, response})
  const data: FieldOfStudyData = await response.json();
  const { content: fields, page: pageData } = data;
  const { number: pageNumber, totalElements, size } = pageData;

  return (
    <main className="min-h-screen p-2 pb-20 sm:p-8 md:p-16 lg:p-20 font-[family-name:var(--font-geist-sans)]">
      <MainTable
        data={fields}
        pageNumber={pageNumber}
        pageSize={size}
        totalElements={totalElements}
        hiddenColumns={hiddenColumns?.split(",")}
        sortBy={sortBy}
        direction={direction}
      />
    </main>
  );
}
