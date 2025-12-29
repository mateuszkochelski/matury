"use client";

import { buttonVariantsAndSizes } from "@/components/ui/button";
import { favouritesAtom } from "@/lib/atoms";
import { cn } from "@/lib/utils";
import { useAtomValue } from "jotai";
import { Heart } from "lucide-react";
import Link from "next/link";

export const SYNC_FAVOURITES_VALUE = "sync";

export function LinkToFavourites() {
  const favourites = useAtomValue(favouritesAtom);
  return (
    <Link
      href={`/szukaj?ids=${favourites.toString()}`}
      aria-label="Zobacz polubione"
      className={cn(buttonVariantsAndSizes({ variant: "secondary", size: "sm" }))}
    >
      <Heart fill="#f008" className="w-5 h-5 md:w-4 md:h-4" />
      <span className="hidden md:inline ml-2">Ulubione</span>
    </Link>
  );
}
