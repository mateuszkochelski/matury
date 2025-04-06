import * as React from "react";
import { buttonVariantsAndSizes } from "./variants";
import { cn } from "@/lib/utils";
import { Slot } from "@radix-ui/react-slot";
import { type VariantProps } from "class-variance-authority";

type Props = React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariantsAndSizes> & {
    asChild?: boolean;
  };

function Button({ className, variant, size, asChild = false, ...props }: Props) {
  const Comp = asChild ? Slot : "button";

  return (
    <Comp
      data-slot="button"
      className={cn(buttonVariantsAndSizes({ variant, size, className }))}
      {...props}
    />
  );
}

export { Button };
