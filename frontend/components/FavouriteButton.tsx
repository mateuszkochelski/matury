"use client";

import { favouritesAtom } from "@/lib/atoms";
import { useAtom } from "jotai";
import { HeartIcon } from "lucide-react";

export function FavouriteButton({ fieldId, size }: { fieldId: number; size: "small" | "large" }) {
  const [favourites, setFavourites] = useAtom(favouritesAtom);
  const isFavourite = favourites.includes(fieldId);

  const toggleFavourite = () => {
    if (isFavourite) {
      setFavourites((favourites) => favourites.filter((id) => id !== fieldId));
    } else {
      setFavourites((favourites) => [...favourites, fieldId]);
    }
  };

  return (
    <div className="flex justify-center w-fit">
      <div className="cursor-pointer" onClick={toggleFavourite}>
        <HeartIcon
          fill={isFavourite ? "#f008" : "transparent"}
          className={`transition-[fill] duration-100 ${size === "large" ? "size-8" : ""} ${isFavourite ? "" : "hover:fill-[#f004]"}`}
        />
      </div>
    </div>
  );
}
