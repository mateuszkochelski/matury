import { useId } from "react";
import { Input } from "@/components/ui/input";
import { UseFormRegister } from "react-hook-form";
import { FiltersFormValues } from "./custom-table/FiltersForm";

export default function RangeInput({
  register,
}: {
  register?: { function: UseFormRegister<FiltersFormValues>; name: string };
}) {
  const id = useId();
  return (
    <div className="flex">
      <Input
        id={`${id}-1`}
        className="flex-1 rounded-e-none [-moz-appearance:_textfield] focus:z-10 [&::-webkit-inner-spin-button]:m-0 [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:m-0 [&::-webkit-outer-spin-button]:appearance-none w-auto"
        placeholder="Od"
        type="number"
        aria-label="Minimalna wartość"
        // @ts-ignore
        {...(register ? register.function(`${register.name}-min`) : {})}
        />
      <Input
        id={`${id}-2`}
        className="-ms-px flex-1 rounded-s-none [-moz-appearance:_textfield] focus:z-10 [&::-webkit-inner-spin-button]:m-0 [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:m-0 [&::-webkit-outer-spin-button]:appearance-none w-auto"
        placeholder="Do"
        type="number"
        aria-label="Maksymalna wartość"
        // @ts-ignore
        {...(register ? register.function(`${register.name}-max`) : {})}
      />
    </div>
  );
}
