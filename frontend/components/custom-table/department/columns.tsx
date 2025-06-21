"use client";

import { columns as courseDegreeColumns } from "../main/columns";

const toOmit = ["department.name", "university.name", "university.city"];

export const columns = courseDegreeColumns.filter(
  // @ts-expect-error this property normally is not accessed
  (column) => !toOmit.includes((column.accessorKey ?? "") as string),
);
