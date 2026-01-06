import { FiltersFormValues } from "./FiltersForm";
import { ValueOf } from "next/dist/shared/lib/constants";

const mapFrontendToBackendFilterNames = {
  degrees: "degrees",
  department_name: "department",
  "duration-max": "semestersTo",
  "duration-min": "semestersFrom",
  name: "name",
  "passRate-max": "passRateTo",
  "passRate-min": "passRateFrom",
  "avgIncome-max": "avgSalaryTo",
  "avgIncome-min": "avgSalaryFrom",
  "salary-max": "avgSalaryTo",
  "salary-min": "avgSalaryFrom",
  university_city: "city",
  university_name: "university",
};

export type TableSearchParams = {
  pageSize?: string;
  pageIndex?: string;
  hiddenColumns?: string;
  sortBy?: string;
  direction?: string;
  searchName?: string;
} & Omit<FiltersFormValues, "degrees" | "isFavouritesOnly"> & {
    degrees?: string;
  } & FetchDataIdsProp;

type FetchDataProps = {
  baseUrl: string;
  pageSize?: string;
  pageIndex?: string;
  sortBy?: string;
  direction?: string;
  filters?: object;
  ids?: string;
  searchName?: string;
};

export type FetchDataIdsProp = Pick<FetchDataProps, "ids">;

export async function fetchData({
  baseUrl,
  pageSize,
  pageIndex,
  sortBy,
  direction,
  filters,
  ids,
  searchName,
}: FetchDataProps): Promise<Response> {
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
  if (ids) {
    url.searchParams.set("ids", ids);
  }
  if (searchName) {
    url.searchParams.set("searchName", searchName);
  }
  if (filters) {
    Object.entries(filters).forEach(([key, value]) => {
      const backendKey =
        mapFrontendToBackendFilterNames[key as keyof typeof mapFrontendToBackendFilterNames];
      if (value !== undefined && value !== null && value !== "") {
        const paramValue = Array.isArray(value) ? value.join(",") : value;
        url.searchParams.set(
          backendKey,
          paramValue as ValueOf<typeof mapFrontendToBackendFilterNames>,
        );
      }
    });
  }

  return fetch(url);
}
