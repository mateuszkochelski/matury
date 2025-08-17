export type TableSearchParams = {
  pageSize?: string;
  pageIndex?: string;
  hiddenColumns?: string;
  sortBy?: string;
  direction?: string;
};

export async function fetchData(
  baseUrl: string,
  pageSize?: string,
  pageIndex?: string,
  sortBy?: string,
  direction?: string,
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
  return fetch(url);
}
