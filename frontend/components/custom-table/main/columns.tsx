"use client";

import { FavouriteButton } from "@/components/FavouriteButton";
import { FieldOfStudyExtended } from "../types";
import { ColumnDef } from "@tanstack/react-table";
import Link from "next/link";

export const columns: ColumnDef<FieldOfStudyExtended>[] = [
  {
    id: "degree",
    header: "Stopień",
    accessorKey: "level",
    size: 100,
    cell: ({ row }) => (row.getValue("degree") as string).replaceAll("_", " "),
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
    cell: ({ row }) => (
      <Link
        href={`/${row.original.university.id}/${row.original.department.id}/${row.original.id}`}
        className="font-semibold underline"
      >
        {row.getValue("name")}
      </Link>
    ),
  },
  {
    header: "Wydział",
    accessorKey: "department.name",
    size: 200,
    meta: {
      filterType: "string",
    },
    cell: ({ row }) => (
      <Link
        href={`/${row.original.university.id}/${row.original.department.id}`}
        className="underline"
      >
        {row.getValue("department_name")}
      </Link>
    ),
  },
  {
    header: "Uczelnia",
    accessorKey: "university.name",
    size: 220,
    meta: {
      filterType: "string",
    },
    cell: ({ row }) => (
      <Link href={`/${row.original.university.id}`} className="underline">
        {row.getValue("university_name")}
      </Link>
    ),
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
    accessorKey: "passRate",
    size: 120,
    meta: {
      filterType: "number",
    },
    cell: ({ row }) => (
      <div>{row.getValue("passRate") ? `${row.getValue("passRate")} %` : "-"}</div>
    ),
    enableSorting: false,
  },
  {
    id: "avgIncome",
    header: "Śr. zarobki absolw.",
    accessorKey: "avgIncome",
    size: 120,
    meta: {
      filterType: "number",
    },
    cell: ({ row }) => (
      <div>{row.getValue("avgIncome") ? `${Math.round(row.getValue("avgIncome"))} zł` : "-"}</div>
    ),
    enableSorting: false,
  },
  {
    id: "favourite",
    header: "Zapisz",
    cell: ({ row }) => <FavouriteButton fieldId={row.original.id} size="small" />,
    size: 60,
    meta: {
      filterType: "favourite",
    },
    accessorFn: (row) => {
      const stored = localStorage.getItem("favourites");
      const favourites: number[] = stored ? JSON.parse(stored) : [];

      return favourites.includes(row.id)
    }
  },
];
