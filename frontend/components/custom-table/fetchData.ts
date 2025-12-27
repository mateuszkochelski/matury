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
} & Omit<FiltersFormValues, "degrees"> & {
    degrees?: string;
  };

type FetchDataProps = {
  baseUrl: string;
  pageSize?: string;
  pageIndex?: string;
  sortBy?: string;
  direction?: string;
  filters?: object;
};

export async function fetchData({
  baseUrl,
  pageSize,
  pageIndex,
  sortBy,
  direction,
  filters,
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
