import { GenericTable, TableProps } from "../GenericTable";
import { FieldOfStudyExtended } from "../types";
import { columns } from "./columns";

export const MainTable = (props: TableProps<FieldOfStudyExtended>) => (
  <GenericTable columns={columns} {...props} />
);
