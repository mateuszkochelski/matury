"use client";

import { useEffect, useState } from "react";
import { HeartIcon } from "lucide-react";

export function FavoriteButton({ fieldId, size }: { fieldId: number; size: "small" | "large" }) {
  const [isFavorite, setIsFavorite] = useState(false);

  useEffect(() => {
    try {
      const stored = localStorage.getItem("favorites");
      const favorites: number[] = stored ? JSON.parse(stored) : [];
      setIsFavorite(favorites.includes(fieldId));
    } catch (err) {
      console.error("Failed to read favorites:", err);
    }
  }, [fieldId]);

  const toggleFavorite = () => {
    try {
      const stored = localStorage.getItem("favorites");
      const favorites: number[] = stored ? JSON.parse(stored) : [];

      let updatedFavorites;
      if (favorites.includes(fieldId)) {
        updatedFavorites = favorites.filter((id) => id !== fieldId);
        setIsFavorite(false);
      } else {
        updatedFavorites = [...favorites, fieldId];
        setIsFavorite(true);
      }

      localStorage.setItem("favorites", JSON.stringify(updatedFavorites));
    } catch (err) {
      console.error("Failed to update favorites:", err);
    }
  };

  return (
    <div className="flex justify-center w-fit">
      <div className="cursor-pointer group" onClick={toggleFavorite}>
        <HeartIcon
          fill={isFavorite ? "#f008" : "background"}
          className={`transition-[fill] duration-100 ${size === "large" ? "size-8" : ""} ${isFavorite ? "" : "group-hover:fill-[#f008]"}`}
        />
      </div>
    </div>
  );
}
