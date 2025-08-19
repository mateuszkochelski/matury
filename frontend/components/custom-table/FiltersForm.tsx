"use client";
import { useForm } from 'react-hook-form';
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
  type Table,
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



export function FitlersForm<T>({table}: {table: Table<T>}) {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm();

    return (
        <form className="space-y-3" onSubmit={handleSubmit((data) => console.log(data))}>
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
                              placeholder={`Wyszukaj ${column.columnDef.header?.toString().replace("Uczelnia", "Uczelnię")}`}
                              {...register(column.id)}
                            />
                          );
                          break;
                        case "number":
                            Input = <RangeInput
                            register={{
                                function: register,
                                name: column.id
                            }}
                          />;
                          break;
                        default:
                            // custom filter components
                            switch (column.id) {
                                case "degree":
                                    Input = (
                                        <>
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
                                        </>
                                    )
                                    break;
                                case "actions":
                                    break; // TODO: implement me
                                default:
                                    console.warn(
                                        `Filter configuration missing for column "${column.id}". This column is marked as filterable, but no filter input is rendered. Please check both the column definition to ensure 'meta.filterType' is set correctly and the custom filter components.`
                                    )
                                    break;
                            }

                      }

                      return (
                        <AccordionItem value={column.id} className="*:p-3" key={column.id}>
                          <AccordionTrigger className="py-2 hover:no-underline">
                            {column.columnDef.header?.toString()}
                          </AccordionTrigger>
                          <AccordionContent className="flex flex-col gap-1.5">{Input}</AccordionContent>
                        </AccordionItem>
                      );
                    })}
                  {/* <AccordionItem value="item-3" className="*:p-3 !border-b">
                    <AccordionTrigger className="py-2 hover:no-underline">Degree</AccordionTrigger>
                    <AccordionContent className="flex flex-col gap-1.5">
                      
                    </AccordionContent>
                  </AccordionItem> */}
                </Accordion>
                <div className="p-3 grid grid-cols-2 gap-2">
                  <Button
                    type="submit"
                  >Save</Button>
                  <Button variant="secondary">Cancel</Button>
                </div>
              </form>
    )
}