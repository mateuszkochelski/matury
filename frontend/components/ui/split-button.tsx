import { ButtonVariants } from "./button/variants";
import { Button } from "@/components/ui/button";

function SplitButton({
  firstText,
  firstVariant,
  secondText,
  secondVariant,
}: {
  firstText: string;
  firstVariant: ButtonVariants;
  secondText: string;
  secondVariant: ButtonVariants;
}) {
  return (
    <div className="inline-flex -space-x-px rounded-md shadow-xs rtl:space-x-reverse">
      <Button
        className="rounded-none shadow-none first:rounded-s-md last:rounded-e-md focus-visible:z-10"
        variant={firstVariant}
      >
        {firstText}
      </Button>
      <Button
        className="rounded-none shadow-none first:rounded-s-md last:rounded-e-md focus-visible:z-10"
        variant={secondVariant}
      >
        {secondText}
      </Button>
    </div>
  );
}

export { SplitButton };
