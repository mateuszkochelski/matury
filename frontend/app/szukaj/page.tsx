import { BACKEND_URL } from "../constants";
import { serializeFiltersFormValues } from "../utils/serializeFiltersFormValues";
import { fetchData, TableSearchParams } from "@/components/custom-table/fetchData";
import { MainTable } from "@/components/custom-table/main";
import { FieldOfStudyExtendedData } from "@/components/custom-table/types";

export default async function Home({
  searchParams,
}: {
  searchParams?: Promise<TableSearchParams>;
}) {
  const { pageSize, pageIndex, hiddenColumns, sortBy, direction, searchName, ...rest } =
    (await searchParams) ?? {};

  const response = await fetchData({
    baseUrl: `${BACKEND_URL}/api/field_of_study`,
    pageSize,
    pageIndex,
    sortBy,
    direction,
    filters: rest,
    ids: rest.ids,
    searchName,
  });

  const data: FieldOfStudyExtendedData = await response.json();
  const { content: fields, page: pageData, matched } = data;
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
        filters={serializeFiltersFormValues(rest)}
        searchNameValue={searchName}
        matched={matched}
      />
    </main>
  );
}
