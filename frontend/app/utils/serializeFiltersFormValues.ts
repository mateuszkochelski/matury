import { FiltersFormValues } from "@/components/custom-table/FiltersForm";
import { TableSearchParams } from "@/components/custom-table/fetchData";
import { possibleDegrees } from "@/components/custom-table/types";

export function serializeFiltersFormValues(rest: TableSearchParams) {
  const splitDegrees = (rest.degrees ?? "").toString().split(",").filter(Boolean);

  const filters: FiltersFormValues | undefined =
    Object.keys(rest).length > 0
      ? {
          ...rest,
          degrees: possibleDegrees.reduce(
            (acc, val) => {
              // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
              acc![val] = splitDegrees.includes(val);
              return acc;
            },
            {} as FiltersFormValues["degrees"],
          ),
          isFavouritesOnly: !!rest.ids,
        }
      : undefined;

  return filters;
}
