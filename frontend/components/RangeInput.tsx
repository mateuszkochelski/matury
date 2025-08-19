import { useId } from "react";
import { Input } from "@/components/ui/input";
import {FieldValues, UseFormRegister, type RegisterOptions} from "react-hook-form"


export default function RangeInput({register}: {register?: {function: UseFormRegister<FieldValues>, name: string}}) {
  const id = useId();
  return (
    <div className="flex">
      <Input
        id={`${id}-1`}
        className="flex-1 rounded-e-none [-moz-appearance:_textfield] focus:z-10 [&::-webkit-inner-spin-button]:m-0 [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:m-0 [&::-webkit-outer-spin-button]:appearance-none w-auto"
        placeholder="From"
        type="number"
        aria-label="Min Value"
        {...(register ? register.function(`${register.name}-min`) : {})}
      />
      <Input
        id={`${id}-2`}
        className="-ms-px flex-1 rounded-s-none [-moz-appearance:_textfield] focus:z-10 [&::-webkit-inner-spin-button]:m-0 [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:m-0 [&::-webkit-outer-spin-button]:appearance-none w-auto"
        placeholder="To"
        type="number"
        aria-label="Max Value"
        {...(register ? register.function(`${register.name}-max`) : {})}
      />
    </div>
  );
}
