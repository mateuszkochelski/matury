"use client";

import { RowActions } from "../RowActions";
import { FieldOfStudy } from "../types";
import { ColumnDef } from "@tanstack/react-table";

export const columns: ColumnDef<FieldOfStudy>[] = [
  {
    header: "Stopień",
    accessorKey: "level",
    size: 100,
    enableColumnFilter: false, // TODO
  },
  {
    id: "duration",
    header: "Ilość semestrów",
    accessorKey: "duration",
    size: 120,
    meta: {
      filterType: "number",
    },
    cell: ({ row }) => <div className="text-center">{row.getValue("duration")}</div>,
  },
  {
    header: "Kierunek",
    accessorKey: "name",
    size: 180,
    meta: {
      filterType: "string",
    },
    enableHiding: false,
    cell: ({ row }) => <div className="font-medium">{row.getValue("name")}</div>,
  },
  {
    header: "Wydział",
    accessorKey: "department.name",
    size: 200,
    meta: {
      filterType: "string",
    },
  },
  {
    header: "Uczelnia",
    accessorKey: "university.name",
    size: 220,
    meta: {
      filterType: "string",
    },
  },
  {
    header: "Miejscowość",
    accessorKey: "university.city",
    size: 140,
    meta: {
      filterType: "string",
    },
  },
  {
    id: "passRate",
    header: "Średnia zdawalność",
    accessorKey: "university.id",
    size: 120,
    meta: {
      filterType: "number",
    },
    cell: ({ row }) => <div className="text-center">{`${row.getValue("passRate")}%`}</div>,
  },
  {
    id: "salary",
    header: "Śr. zarobki absolw.",
    accessorKey: "department.id",
    size: 120,
    meta: {
      filterType: "number",
    },
    cell: ({ row }) => (
      <div className="text-right">{`${Number(row.getValue("salary")) * 100} PLN`}</div>
    ),
  },
  {
    id: "actions",
    header: "Zapisz",
    cell: ({ row }) => <RowActions row={row} />,
    size: 60,
    enableHiding: false,
    enableColumnFilter: false, // TODO
  },
];
