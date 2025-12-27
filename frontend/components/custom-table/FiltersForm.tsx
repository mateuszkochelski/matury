"use client";

import { useCallback } from "react";
import RangeInput from "../RangeInput";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "../ui/accordion";
import { Checkbox } from "../ui/checkbox";
import { FloatingLabelInput } from "../ui/floating-label-input";
import { DegreesObject } from "./types";
import { Button } from "@/components/ui/button";
import { type Table } from "@tanstack/react-table";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useForm, Controller, SubmitHandler, Control } from "react-hook-form";

export type FiltersFormValues = {
  degrees?: DegreesObject;
  department_name?: string;
  "duration-max"?: string;
  "duration-min"?: string;
  name?: string;
  "passRate-max"?: string;
  "passRate-min"?: string;
  "avgIncome-max"?: string;
  "avgIncome-min"?: string;
  "salary-max"?: string;
  "salary-min"?: string;
  university_city?: string;
  university_name?: string;
};

export function FiltersForm<T>({
  table,
  filters,
}: {
  table: Table<T>;
  filters?: FiltersFormValues;
}) {
  const {
    register,
    handleSubmit,
    // TODO: fix this once error handling is added or remove it entirely
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    formState: { errors },
    control,
  } = useForm<FiltersFormValues>({
    defaultValues: filters,
  });

  const searchParams = useSearchParams();

  const { replace } = useRouter();
  const pathname = usePathname();

  const handleUrlUpdate: SubmitHandler<FiltersFormValues> = useCallback(
    (updateParams: FiltersFormValues) => {
      const params = new URLSearchParams(searchParams.toString());

      const { degrees, ...otherParams } = updateParams;

      params.delete("degrees");
      Object.entries(degrees ?? {})
        .filter(([_, value]) => !!value)
        .forEach(([key]) => {
          params.append("degrees", key);
        });

      Object.entries(otherParams)
        .filter(([key, value]) => !!value || params.get(key) !== null)
        .forEach(([key, value]) => {
          if (!value) {
            return params.delete(key);
          }

          params.set(key, value);
        });

      replace(`${pathname}?${params.toString()}`);
    },
    [searchParams, pathname, replace],
  );

  return (
    <form className="space-y-3" onSubmit={handleSubmit(handleUrlUpdate)}>
      <Accordion type="multiple" className="w-full max-h-80 overflow-x-scroll mb-0">
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
                    {...register(column.id as keyof FiltersFormValues)}
                  />
                );
                break;
              case "number":
                Input = (
                  <RangeInput
                    register={{
                      function: register,
                      name: column.id,
                    }}
                  />
                );
                break;
              default:
                // custom filter components
                switch (column.id) {
                  case "degree":
                    Input = <DegreeComponent control={control} />;
                    break;
                  case "actions":
                    break; // TODO: implement me
                  default:
                    console.warn(
                      `Filter configuration missing for column "${column.id}". This column is marked as filterable, but no filter input is rendered. Please check both the column definition to ensure 'meta.filterType' is set correctly and the custom filter components.`,
                    );
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
      </Accordion>
      <div className="p-3 grid grid-cols-2 gap-2 border-t">
        <Button type="submit">Zastosuj</Button>
        <Button variant="destructive">Resetuj</Button>
      </div>
    </form>
  );
}

function DegreeComponent({
  control,
}: {
  /* eslint-disable @typescript-eslint/no-explicit-any */
  control: Control<FiltersFormValues, any, FiltersFormValues>;
}) {
  return (
    <>
      <div className="flex items-center space-x-2">
        <Controller
          name="degrees.bachelor"
          control={control}
          render={({ field }) => (
            <Checkbox id="bachelor" checked={!!field.value} onCheckedChange={field.onChange} />
          )}
        />
        <label
          htmlFor="bachelor"
          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
        >
          Licencjackie
        </label>
      </div>
      <div className="flex items-center space-x-2">
        <Controller
          name="degrees.engineer"
          control={control}
          render={({ field }) => (
            <Checkbox id="engineer" checked={!!field.value} onCheckedChange={field.onChange} />
          )}
        />
        <label
          htmlFor="engineer"
          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
        >
          Inżynierskie
        </label>
      </div>
      <div className="flex items-center space-x-2">
        <Controller
          name="degrees.master"
          control={control}
          render={({ field }) => (
            <Checkbox id="master" checked={!!field.value} onCheckedChange={field.onChange} />
          )}
        />
        <label
          htmlFor="master"
          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
        >
          Magisterskie
        </label>
      </div>
      <div className="flex items-center space-x-2">
        <Controller
          name="degrees.long_master"
          control={control}
          render={({ field }) => (
            <Checkbox id="long-master" checked={!!field.value} onCheckedChange={field.onChange} />
          )}
        />
        <label
          htmlFor="long-master"
          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
        >
          Jednolite Magisterskie
        </label>
      </div>
    </>
  );
}
