"use client";

import { useCallback, useEffect, useId, useState } from "react";
import { FiltersFormValues, FiltersForm } from "./FiltersForm";
import { SearchBar } from "./SearchBar";
import { TableSearchParams } from "./fetchData";
import { MatchedType } from "./types";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Label } from "@/components/ui/label";
import { Pagination, PaginationContent, PaginationItem } from "@/components/ui/pagination";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { cn } from "@/lib/utils";
import {
  ColumnDef,
  ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  getFacetedUniqueValues,
  getPaginationRowModel,
  PaginationState,
  SortingState,
  useReactTable,
  VisibilityState,
} from "@tanstack/react-table";
import {
  ChevronDownIcon,
  ChevronFirstIcon,
  ChevronLastIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ChevronUpIcon,
  Columns3Icon,
  FilterIcon,
} from "lucide-react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";

type UrlUpdateParam = { param: keyof TableSearchParams; value?: string | number | string[] };

type GenericTableProps<T> = {
  data?: T[];
  columns: ColumnDef<T, unknown>[];
  pageNumber?: number;
  pageSize?: number;
  totalElements?: number;
  hiddenColumns?: string[];
  sortBy?: string;
  direction?: string;
  filters?: FiltersFormValues;
  searchNameValue?: string;
  matched?: MatchedType;
};

export type TableProps<T> = Omit<GenericTableProps<T>, "columns">;

export function GenericTable<T>({
  data = [],
  columns,
  pageNumber = 1,
  pageSize = 10,
  totalElements,
  hiddenColumns = [],
  sortBy = "name",
  direction = "asc",
  filters,
  searchNameValue,
  matched,
}: GenericTableProps<T>) {
  const id = useId();
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(
    Object.fromEntries(hiddenColumns.map((column) => [column, false])),
  );
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: pageNumber,
    pageSize: pageSize,
  });

  const [sorting, setSorting] = useState<SortingState>([
    {
      id: sortBy,
      desc: direction === "desc",
    },
  ]);

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    onSortingChange: setSorting,
    enableSortingRemoval: false,
    manualSorting: true,
    getPaginationRowModel: getPaginationRowModel(),
    onPaginationChange: setPagination,
    manualPagination: true,
    onColumnFiltersChange: setColumnFilters,
    onColumnVisibilityChange: setColumnVisibility,
    manualFiltering: true,
    getFacetedUniqueValues: getFacetedUniqueValues(),
    state: {
      sorting,
      pagination,
      columnFilters,
      columnVisibility,
    },
    rowCount: totalElements ?? data.length,
  });

  const searchParams = useSearchParams();
  const pathname = usePathname();
  const { replace, prefetch } = useRouter();

  const handleUrlUpdate = useCallback(
    (updateParams: UrlUpdateParam[]) => {
      const params = new URLSearchParams(searchParams.toString());

      for (const { value, param } of updateParams) {
        const stringValue = value?.toString();

        if (params.get(param) === stringValue) continue;

        if (stringValue !== undefined) {
          params.set(param, stringValue);
        } else {
          params.delete(param);
        }
      }

      replace(`${pathname}?${params.toString()}`);

      // prefetching logic
      const currentPage = params.get("pageIndex");
      if (currentPage === null) return;

      const nextPageIndex: number = Number(currentPage) + 1;

      const prefetchParams = new URLSearchParams(params.toString());
      prefetchParams.set("pageIndex", nextPageIndex.toString());

      prefetch(`${pathname}?${prefetchParams.toString()}`);
    },
    [searchParams, replace, pathname, prefetch],
  );

  useEffect(() => {
    handleUrlUpdate([
      { param: "pageIndex", value: pagination.pageIndex },
      { param: "pageSize", value: pagination.pageSize },
      { param: "sortBy", value: sorting[0].id },
      { param: "direction", value: sorting[0].desc ? "desc" : "asc" },
    ]);
  }, [pagination, handleUrlUpdate, sorting]);

  const handleColumnVisibilityUpdate = (open: boolean) => {
    // we only want to update after the dropdown is closed
    if (open) return;

    const hiddenColumns = table.getAllColumns().filter((column) => !column.getIsVisible());
    // we do not want to add an empty searchParam
    const value = hiddenColumns.map((column) => column.id).join(",") || undefined;
    handleUrlUpdate([{ param: "hiddenColumns", value }]);
  };

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-col sm:flex-row items-center gap-4">
          <SearchBar searchNameValue={searchNameValue} matched={matched} />

          <div className="grid grid-cols-2 gap-2 w-full">
            {/* Toggle columns visibility */}
            <DropdownMenu onOpenChange={handleColumnVisibilityUpdate}>
              <DropdownMenuTrigger asChild>
                <Button variant="outline">
                  <Columns3Icon className="-ms-1 opacity-60" size={16} aria-hidden="true" />
                  Widok
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="start" className="bg-card">
                <DropdownMenuLabel>Widoczność kolumn</DropdownMenuLabel>
                {table
                  .getAllColumns()
                  .filter((column) => column.getCanHide())
                  .map((column) => {
                    return (
                      <DropdownMenuCheckboxItem
                        key={column.id}
                        checked={column.getIsVisible()}
                        // values such as 0 or "" should be default or not present
                        onCheckedChange={(value) => column.toggleVisibility(!!value)}
                        onSelect={(event) => event.preventDefault()}
                      >
                        {column.columnDef.header?.toString()}
                      </DropdownMenuCheckboxItem>
                    );
                  })}
              </DropdownMenuContent>
            </DropdownMenu>
            {/* Filter by status */}
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline">
                  <FilterIcon className="-ms-1 opacity-60" size={16} aria-hidden="true" />
                  Filtry
                  {/* // TODO: this logic does not take degrees into account */}
                  {Object.values(filters ?? {}).filter((v) => !!v).length > 0 && (
                    <span className="bg-background text-muted-foreground/70 -me-1 inline-flex h-5 max-h-full items-center rounded border px-1 font-[inherit] text-[0.625rem] font-medium">
                      {Object.values(filters ?? {}).filter((v) => !!v).length}
                    </span>
                  )}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-72 p-0 bg-card" align="start">
                <FiltersForm table={table} filters={filters} />
              </PopoverContent>
            </Popover>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-table overflow-hidden rounded-md border">
        <Table className="table-fixed">
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id} className="hover:bg-transparent">
                {headerGroup.headers.map((header) => {
                  let headerContent;
                  if (header.isPlaceholder) {
                    headerContent = null;
                  } else if (header.column.getCanSort()) {
                    headerContent = (
                      <div
                        className={cn(
                          "flex h-full cursor-pointer items-center justify-between gap-2 select-none",
                        )}
                        onClick={header.column.getToggleSortingHandler()}
                        onKeyDown={(e) => {
                          if (e.key === "Enter" || e.key === " ") {
                            e.preventDefault();
                            header.column.getToggleSortingHandler()?.(e);
                          }
                        }}
                        tabIndex={0}
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {{
                          asc: (
                            <ChevronUpIcon
                              className="shrink-0 opacity-60"
                              size={16}
                              aria-hidden="true"
                            />
                          ),
                          desc: (
                            <ChevronDownIcon
                              className="shrink-0 opacity-60"
                              size={16}
                              aria-hidden="true"
                            />
                          ),
                        }[header.column.getIsSorted() as string] ?? null}
                      </div>
                    );
                  } else {
                    headerContent = flexRender(header.column.columnDef.header, header.getContext());
                  }

                  return (
                    <TableHead
                      key={header.id}
                      style={{ width: `${header.getSize()}px` }}
                      className="h-11"
                    >
                      {headerContent}
                    </TableHead>
                  );
                })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {table.getRowModel().rows.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}>
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id} className="last:py-0">
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={columns.length} className="h-24">
                  Brak wyników
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between gap-2 sm:gap-8">
        {/* Results per page */}
        <div className="flex items-center gap-3">
          <Label htmlFor={id} className="max-sm:sr-only">
            Kierunki na stronę
          </Label>
          <Select
            value={table.getState().pagination.pageSize.toString()}
            onValueChange={(value) => {
              table.setPageSize(Number(value));
            }}
          >
            <SelectTrigger id={id} className="w-fit whitespace-nowrap bg-card">
              <SelectValue placeholder="Wybierz liczbę wyników" />
            </SelectTrigger>
            <SelectContent className="[&_*[role=option]]:ps-2 [&_*[role=option]]:pe-8 [&_*[role=option]>span]:start-auto [&_*[role=option]>span]:end-2 bg-card">
              {[5, 10, 25, 50].map((pageSize) => (
                <SelectItem key={pageSize} value={pageSize.toString()}>
                  {pageSize}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        {/* Page number information */}
        <div className="text-muted-foreground flex grow justify-end text-sm whitespace-nowrap">
          <p className="text-muted-foreground text-sm whitespace-nowrap" aria-live="polite">
            <span className="text-foreground">
              {table.getState().pagination.pageIndex * table.getState().pagination.pageSize + 1}-
              {Math.min(
                Math.max(
                  table.getState().pagination.pageIndex * table.getState().pagination.pageSize +
                    table.getState().pagination.pageSize,
                  0,
                ),
                table.getRowCount(),
              )}
            </span>
            <span className="hidden sm:inline">
              {" "}
              z <span className="text-foreground">{table.getRowCount().toString()}</span>
            </span>
          </p>
        </div>

        {/* Pagination buttons */}
        <div>
          <Pagination>
            <PaginationContent>
              {/* First page button */}
              <PaginationItem>
                <Button
                  size="icon"
                  variant="outline"
                  className="disabled:pointer-events-none disabled:opacity-50"
                  onClick={() => table.firstPage()}
                  disabled={!table.getCanPreviousPage()}
                  aria-label="Pierwsza strona"
                >
                  <ChevronFirstIcon size={16} aria-hidden="true" />
                </Button>
              </PaginationItem>
              {/* Previous page button */}
              <PaginationItem>
                <Button
                  size="icon"
                  variant="outline"
                  className="disabled:pointer-events-none disabled:opacity-50"
                  onClick={() => table.previousPage()}
                  disabled={!table.getCanPreviousPage()}
                  aria-label="Poprzednia strona"
                >
                  <ChevronLeftIcon size={16} aria-hidden="true" />
                </Button>
              </PaginationItem>
              {/* Next page button */}
              <PaginationItem>
                <Button
                  size="icon"
                  variant="outline"
                  className="disabled:pointer-events-none disabled:opacity-50"
                  onClick={() => table.nextPage()}
                  disabled={!table.getCanNextPage()}
                  aria-label="Następna strona"
                >
                  <ChevronRightIcon size={16} aria-hidden="true" />
                </Button>
              </PaginationItem>
              {/* Last page button */}
              <PaginationItem>
                <Button
                  size="icon"
                  variant="outline"
                  className="disabled:pointer-events-none disabled:opacity-50"
                  onClick={() => table.lastPage()}
                  disabled={!table.getCanNextPage()}
                  aria-label="Ostatnia strona"
                >
                  <ChevronLastIcon size={16} aria-hidden="true" />
                </Button>
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      </div>
    </div>
  );
}
