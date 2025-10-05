import { FiltersFormValues } from "./FiltersForm";

export const mapFrontendToBackendFilterNames = { // TODO: duplicate code
  degrees: "degrees",
  department_name: "department",
  "duration-max": "semestersTo",
  "duration-min": "semestersFrom",
  name: "name",
  "passRate-max": "passRateTo",
  "passRate-min": "passRateFrom",
  "salary-max": "avgSalaryTo",
  "salary-min": "avgSalaryFrom",
  university_city: "city",
  university_name: "university"
}

export type TableSearchParams = {
  pageSize?: string;
  pageIndex?: string;
  hiddenColumns?: string;
  sortBy?: string;
  direction?: string;
} & Omit<FiltersFormValues, "degrees"> & {
  degrees?: string;
};

export async function fetchData(
  baseUrl: string,
  pageSize?: string,
  pageIndex?: string,
  sortBy?: string,
  direction?: string,
  filters?: object,
): Promise<Response> {
  const url = new URL(baseUrl);
  if (pageSize) {
    url.searchParams.set("size", pageSize);
  }
  if (pageIndex) {
    url.searchParams.set("page", pageIndex);
  }
  if (sortBy) {
    url.searchParams.set("sort", sortBy.replaceAll("_", "."));
  }
  if (direction) {
    url.searchParams.set("direction", direction);
  }
  console.log({filters})
  if (filters) {
    Object.entries(filters).forEach(([key, value]) => {
      const backendKey = mapFrontendToBackendFilterNames[key as keyof typeof mapFrontendToBackendFilterNames];
      if (value !== undefined && value !== null && value !== "") {
        console.log(value)
        url.searchParams.set(backendKey, value);
      }
    });
  }
  console.log({url})
  return fetch(url);
}
