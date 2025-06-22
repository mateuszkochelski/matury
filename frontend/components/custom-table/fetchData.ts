export type TableSearchParams = {
  pageSize?: string;
  pageIndex?: string;
  hiddenColumns?: string;
};

export async function fetchData(
  baseUrl: string,
  pageSize?: string,
  pageIndex?: string,
): Promise<Response> {
  const url = new URL(baseUrl);
  if (pageSize) {
    url.searchParams.set("size", pageSize);
  }
  if (pageIndex) {
    url.searchParams.set("page", pageIndex);
  }
  return fetch(url);
}
