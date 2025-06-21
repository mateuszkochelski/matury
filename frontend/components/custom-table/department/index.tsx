import { GenericTable, TableProps } from "../GenericTable";
import { FieldOfStudy } from "../types";
import { columns } from "./columns";

// TODO: we do not need some of the data passed to this table
export const DepartmentTable = (props: TableProps<FieldOfStudy>) => (
  <GenericTable columns={columns} {...props} />
);
