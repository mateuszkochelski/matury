import { GenericTable, TableProps } from "../GenericTable";
import { FieldOfStudyExtended } from "../types";
import { columns } from "./columns";

// TODO: we do not need some of the data passed to this table
export const DepartmentTable = (props: TableProps<FieldOfStudyExtended>) => (
  <GenericTable columns={columns} {...props} />
);
