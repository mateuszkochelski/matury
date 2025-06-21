import { FieldOfStudyData } from "@/components/course-degree-table/types";
import { BACKEND_URL } from "./constants";
import CourseDegreeTable from "@/components/course-degree-table/CourseDegreeTable";

export type TableSearchParams = {
  pageSize?: string;
  pageIndex?: string;
  hiddenColumns?: string;
}

export default async function Home(props: {
  searchParams?: Promise<TableSearchParams>;
}) {
  const searchParams = await props.searchParams ?? {};
  const { pageSize, pageIndex, hiddenColumns } = searchParams;

  const apiUrl = new URL(`${BACKEND_URL}/api/field_of_study`);
  if (pageSize) {
    apiUrl.searchParams.set("size", pageSize);
  }
  if (pageIndex) {
    apiUrl.searchParams.set("page", pageIndex);
  }

  const response = await fetch(apiUrl);
  const data: FieldOfStudyData = await response.json();
  const { content: fields, page: pageData } = data;
  const { number: pageNumber, totalElements, size } = pageData;

  return (
    <main className="min-h-screen p-2 pb-20 sm:p-8 md:p-16 lg:p-20 font-[family-name:var(--font-geist-sans)]">
      <CourseDegreeTable
        data={fields}
        pageNumber={pageNumber}
        pageSize={size}
        totalElements={totalElements}
        hiddenColumns={hiddenColumns?.split(",")}
      />
    </main>
  );
}
