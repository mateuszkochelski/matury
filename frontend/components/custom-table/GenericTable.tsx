"use client";

import { useCallback, useEffect, useId, useRef, useState } from "react";
import RangeInput from "../RangeInput";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "../ui/accordion";
import { Checkbox } from "../ui/checkbox";
import { FloatingLabelInput } from "../ui/floating-label-input";
import { TableSearchParams } from "./fetchData";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
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
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
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
  CircleXIcon,
  Columns3Icon,
  FilterIcon,
  SearchIcon,
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
};

export type TableProps<T> = Omit<GenericTableProps<T>, "columns">;

export function GenericTable<T>({
  data = [],
  columns,
  pageNumber = 1,
  pageSize = 10,
  totalElements,
  hiddenColumns = [],
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
  const inputRef = useRef<HTMLInputElement>(null);

  const [sorting, setSorting] = useState<SortingState>([
    {
      id: "name",
      desc: false,
    },
  ]);

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onSortingChange: setSorting,
    enableSortingRemoval: false,
    getPaginationRowModel: getPaginationRowModel(),
    onPaginationChange: setPagination,
    manualPagination: true,
    onColumnFiltersChange: setColumnFilters,
    onColumnVisibilityChange: setColumnVisibility,
    getFilteredRowModel: getFilteredRowModel(),
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
  const { replace } = useRouter();

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
    },
    [searchParams, pathname, replace],
  );

  useEffect(() => {
    handleUrlUpdate([
      { param: "pageIndex", value: pagination.pageIndex },
      { param: "pageSize", value: pagination.pageSize },
    ]);
  }, [pagination, handleUrlUpdate]);

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
        <div className="flex items-center gap-3">
          {/* Filter by name or email */}
          <div className="relative">
            <Input
              id={`${id}-input`}
              ref={inputRef}
              className={cn(
                "peer ps-9",
                Boolean(table.getColumn("name")?.getFilterValue()) && "pe-9",
              )}
              value={(table.getColumn("name")?.getFilterValue() ?? "") as string}
              onChange={(e) => table.getColumn("name")?.setFilterValue(e.target.value)}
              placeholder="Wyszukaj kierunek po nazwie"
              type="text"
              aria-label="Wyszukaj kierunek po nazwie"
            />
            <div className="text-muted-foreground/80 pointer-events-none absolute inset-y-0 start-0 flex items-center justify-center ps-3 peer-disabled:opacity-50">
              <SearchIcon size={16} aria-hidden="true" />
            </div>
            {Boolean(table.getColumn("name")?.getFilterValue()) && (
              <button
                className="text-muted-foreground/80 hover:text-foreground focus-visible:border-ring focus-visible:ring-ring/50 absolute inset-y-0 end-0 flex h-full w-9 items-center justify-center rounded-e-md transition-[color,box-shadow] outline-none focus:z-10 focus-visible:ring-[3px] disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50"
                aria-label="Clear filter"
                onClick={() => {
                  table.getColumn("name")?.setFilterValue("");
                  if (inputRef.current) {
                    inputRef.current.focus();
                  }
                }}
              >
                <CircleXIcon size={16} aria-hidden="true" />
              </button>
            )}
          </div>
          {/* Toggle columns visibility */}
          <DropdownMenu onOpenChange={handleColumnVisibilityUpdate}>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">
                <Columns3Icon className="-ms-1 opacity-60" size={16} aria-hidden="true" />
                Widok
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start">
              <DropdownMenuLabel>Toggle columns</DropdownMenuLabel>
              {table
                .getAllColumns()
                .filter((column) => column.getCanHide())
                .map((column) => {
                  return (
                    <DropdownMenuCheckboxItem
                      key={column.id}
                      checked={column.getIsVisible()}
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
                {/* {selectedStatuses.length > 0 && (
                  <span className="bg-background text-muted-foreground/70 -me-1 inline-flex h-5 max-h-full items-center rounded border px-1 font-[inherit] text-[0.625rem] font-medium">
                    {selectedStatuses.length}
                  </span>
                )} */}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-72 p-0" align="start">
              <div className="space-y-3">
                <Accordion type="multiple" className="w-full">
                  {table
                    .getAllColumns()
                    .filter((column) => column.getCanFilter())
                    .map((column) => {
                      let Input;
                      switch (column.columnDef.meta?.filterType) {
                        case "string":
                          Input = (
                            <FloatingLabelInput
                              // TODO: "Wyszukaj uczelnia" zamiast "uczelnię"
                              placeholder={`Wyszukaj ${column.columnDef.header?.toString()}`}
                            />
                          );
                          break;
                        case "number":
                          Input = <RangeInput />;
                          break;
                        default:
                          break;
                      }

                      return (
                        <AccordionItem value={column.id} className="*:p-3" key={column.id}>
                          <AccordionTrigger className="py-2 hover:no-underline">
                            {column.columnDef.header?.toString()}
                          </AccordionTrigger>
                          <AccordionContent>{Input}</AccordionContent>
                        </AccordionItem>
                      );
                    })}
                  <AccordionItem value="item-3" className="*:p-3 !border-b">
                    <AccordionTrigger className="py-2 hover:no-underline">Degree</AccordionTrigger>
                    <AccordionContent className="flex flex-col gap-1.5">
                      <div className="flex items-center space-x-2">
                        <Checkbox id="bachelors" />
                        <label
                          htmlFor="terms"
                          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                        >
                          Bachelors
                        </label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Checkbox id="engineering" />
                        <label
                          htmlFor="terms"
                          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                        >
                          Engineering
                        </label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Checkbox id="masters" />
                        <label
                          htmlFor="terms"
                          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                        >
                          Masters
                        </label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Checkbox id="engineering-masters" />
                        <label
                          htmlFor="terms"
                          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                        >
                          Engineering Masters
                        </label>
                      </div>
                    </AccordionContent>
                  </AccordionItem>
                </Accordion>
                <div className="p-3 grid grid-cols-2 gap-2">
                  <Button>Save</Button>
                  <Button variant="secondary">Cancel</Button>
                </div>
              </div>
            </PopoverContent>
          </Popover>
        </div>
      </div>

      {/* Table */}
      <div className="bg-card/60 overflow-hidden rounded-md border">
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
                  No results.
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
            <SelectTrigger id={id} className="w-fit whitespace-nowrap">
              <SelectValue placeholder="Select number of results" />
            </SelectTrigger>
            <SelectContent className="[&_*[role=option]]:ps-2 [&_*[role=option]]:pe-8 [&_*[role=option]>span]:start-auto [&_*[role=option]>span]:end-2">
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
                  aria-label="Go to first page"
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
                  aria-label="Go to previous page"
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
                  aria-label="Go to next page"
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
                  aria-label="Go to last page"
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
