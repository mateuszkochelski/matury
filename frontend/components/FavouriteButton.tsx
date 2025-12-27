"use client";

import { useEffect, useState } from "react";
import { getFavourites } from "@/app/utils/getFavourites";
import { HeartIcon } from "lucide-react";

export function FavouriteButton({ fieldId, size }: { fieldId: number; size: "small" | "large" }) {
  const [isFavourite, setIsFavourite] = useState(false);

  useEffect(() => {
    try {
      const favourites = getFavourites();
      setIsFavourite(favourites.includes(fieldId));
    } catch (err) {
      console.error("Failed to read favourites:", err);
    }
  }, [fieldId]);

  const toggleFavourite = () => {
    try {
      const favourites = getFavourites();

      let updatedFavourites;
      if (favourites.includes(fieldId)) {
        updatedFavourites = favourites.filter((id) => id !== fieldId);
        setIsFavourite(false);
      } else {
        favourites.push(fieldId);
        updatedFavourites = favourites;
        setIsFavourite(true);
      }

      localStorage.setItem("favourites", JSON.stringify(updatedFavourites));
    } catch (err) {
      console.error("Failed to update favourites:", err);
    }
  };

  return (
    <div className="flex justify-center w-fit">
      <div className="cursor-pointer group" onClick={toggleFavourite}>
        <HeartIcon
          fill={isFavourite ? "#f008" : "background"}
          className={`transition-[fill] duration-100 ${size === "large" ? "size-8" : ""} ${isFavourite ? "" : "group-hover:fill-[#f004]"}`}
        />
      </div>
    </div>
  );
}
