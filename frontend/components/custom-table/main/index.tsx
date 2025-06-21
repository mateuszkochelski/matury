import { GenericTable, TableProps } from "../GenericTable";
import { FieldOfStudy } from "../types";
import { columns } from "./columns";

export const MainTable = (props: TableProps<FieldOfStudy>) => (
  <GenericTable columns={columns} {...props} />
);
