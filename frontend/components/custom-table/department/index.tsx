"use client";

import { GenericTable, TableProps } from "../GenericTable";
import { FieldOfStudyExtended } from "../types";
import { columns } from "./columns";

const adjustedColumns = columns.map((column) =>
  column.id === "favourite" ? { ...column, enableColumnFilter: false } : column,
);

// TODO: we do not need some of the data passed to this table
export const DepartmentTable = (props: TableProps<FieldOfStudyExtended>) => {
  return <GenericTable columns={adjustedColumns} {...props} />;
};
