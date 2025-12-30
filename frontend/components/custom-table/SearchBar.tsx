"use client";

import { useState, useEffect, useRef } from "react";
import { MatchedType } from "./types";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import {
  SearchIcon,
  CircleXIcon,
  InfoIcon,
  GraduationCap,
  SearchXIcon,
  University,
  Landmark,
} from "lucide-react";
import { useRouter } from "next/navigation";

const matchedMapping = {
  field: {
    icon: GraduationCap,
    popoverText: "Znaleziono kierunek studiów",
  },
  department: { icon: Landmark, popoverText: "Znaleziono wydział" },
  university: {
    icon: University,
    popoverText: "Znaleziono uczelnię",
  },
  none: {
    icon: SearchXIcon,
    popoverText: "Brak wyników wyszukiwania",
  },
};

export function SearchBar({
  matched,
  searchNameValue,
}: {
  matched?: MatchedType;
  searchNameValue?: string;
}) {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);
  const isInitialMount = useRef(true);
  const [query, setQuery] = useState(searchNameValue ?? "");

  useEffect(() => {
    if (isInitialMount.current) {
      isInitialMount.current = false;
      return;
    }

    const prefetchTimer = setTimeout(() => {
      const url = `/szukaj?searchName=${encodeURIComponent(query)}`;
      router.prefetch(url);
    }, 250);

    const navigateTimer = setTimeout(() => {
      const url = `/szukaj?searchName=${encodeURIComponent(query)}`;
      router.replace(url, { scroll: false });
    }, 600);

    return () => {
      clearTimeout(prefetchTimer);
      clearTimeout(navigateTimer);
    };
  }, [query, router]);

  const handleClear = () => {
    setQuery("");
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  const getMatchedIcon = () => {
    if (matched === undefined) return <SearchIcon size={16} aria-hidden />;
    const IconComponent = matchedMapping[matched].icon;
    return <IconComponent size={16} aria-hidden />;
  };

  return (
    <div className="flex items-center gap-2">
      <div className="relative">
        <Input
          ref={inputRef}
          className={cn("peer ps-9", "w-62", query && "pe-9", "bg-card")}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Szukaj..."
          type="text"
          aria-label="Szukaj"
        />
        <div className="text-muted-foreground/80 pointer-events-none absolute inset-y-0 start-0 flex items-center justify-center ps-3 peer-disabled:opacity-50">
          {getMatchedIcon()}
        </div>
        {query && (
          <button
            className="text-muted-foreground/80 hover:text-foreground focus-visible:border-ring focus-visible:ring-ring/50 absolute inset-y-0 end-0 flex h-full w-9 items-center justify-center rounded-e-md transition-[color,box-shadow] outline-none focus:z-10 focus-visible:ring-[3px] disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50"
            aria-label="Wyczyść wyszukiwanie"
            onClick={handleClear}
          >
            <CircleXIcon size={16} aria-hidden="true" />
          </button>
        )}
      </div>
      <Popover>
        <PopoverTrigger asChild>
          <button
            className="cursor-pointer hover:text-foreground text-muted-foreground/80 transition-colors"
            aria-label="Informacja o wyszukiwaniu"
          >
            <InfoIcon size={16} />
          </button>
        </PopoverTrigger>
        <PopoverContent className="w-72 text-sm" align="end">
          <div className="space-y-3">
            <h3 className="font-semibold text-base">Jak działa wyszukiwanie?</h3>
            <div className="space-y-2">
              {Object.entries(matchedMapping).map(([key, value]) => {
                return (
                  <div key={key} className="flex items-center gap-2">
                    <value.icon size={16} />
                    <span>{value.popoverText}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}
