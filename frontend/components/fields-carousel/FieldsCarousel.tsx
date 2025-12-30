"use client";

import { useRef, useEffect, useState } from "react";
import { FavouriteButton } from "../FavouriteButton";
import { FieldOfStudy } from "../custom-table/types";
import { Button } from "../ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";

export default function FieldsCarousel({ fields }: { fields: FieldOfStudy[] }) {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const checkScroll = () => {
    if (scrollContainerRef.current) {
      const { scrollLeft, scrollWidth, clientWidth } = scrollContainerRef.current;
      setCanScrollLeft(scrollLeft > 0);
      setCanScrollRight(scrollLeft < scrollWidth - clientWidth - 10);
    }
  };

  useEffect(() => {
    checkScroll();
    const container = scrollContainerRef.current;
    if (container) {
      container.addEventListener("scroll", checkScroll);
      window.addEventListener("resize", checkScroll);
      return () => {
        container.removeEventListener("scroll", checkScroll);
        window.removeEventListener("resize", checkScroll);
      };
    }
  }, [fields]);

  const scroll = (direction: "left" | "right") => {
    if (scrollContainerRef.current) {
      // sm breakpoint - page padding
      const isSm = scrollContainerRef.current.clientWidth >= 600 - 32;
      // Cards' width + gap
      const scrollAmount = (isSm ? 320 : 216) + 16;

      scrollContainerRef.current.scrollBy({
        left: direction === "left" ? -scrollAmount : scrollAmount,
        behavior: "smooth",
      });
    }
  };

  return (
    <div className="relative group">
      {/* Left Navigation Button */}
      <Button
        variant="ghost"
        size="icon"
        onClick={() => scroll("left")}
        className={`${canScrollLeft ? "absolute" : "hidden"} left-0 top-1/2 -translate-y-1/2 z-10 bg-white/70 hover:bg-white/90 rounded-full shadow-md transition-all`}
        aria-label="Scroll left"
      >
        <ChevronLeft className="w-6 h-6 text-foreground" />
      </Button>

      {/* Scrollable Container */}
      <div ref={scrollContainerRef} className="overflow-x-auto no-scrollbar scroll-smooth">
        <div className="flex gap-4 pb-2">
          {fields.map((field) => (
            <div key={field.id} className="inline-block flex-shrink-0 w-54 sm:w-80">
              <Card className="border-primary/10 hover:shadow-lg transition-shadow duration-300 flex flex-col h-full relative">
                <div className="absolute top-3 right-3 z-5">
                  <FavouriteButton fieldId={field.id} size="small" />
                </div>

                <CardHeader className="py-3 flex-grow">
                  <CardTitle className="text-base sm:text-lg font-semibold text-foreground line-clamp-2 mr-3">
                    {field.name}
                  </CardTitle>
                  <CardDescription className="text-xs sm:text-sm text-foreground/70 mt-1">
                    {field.university.name}
                  </CardDescription>
                  <CardDescription className="text-xs sm:text-sm text-foreground/60">
                    {field.department.name}
                  </CardDescription>
                </CardHeader>
                <CardContent className="pt-0 space-y-3">
                  <a href={`/${field.university.id}/${field.department.id}/${field.id}`}>
                    <Button
                      size="sm"
                      className="w-full bg-primary hover:bg-primary/90 text-foreground cursor-pointer transition-colors text-sm"
                    >
                      Sprawdź
                    </Button>
                  </a>
                </CardContent>
              </Card>
            </div>
          ))}
        </div>
      </div>

      {/* Right Navigation Button */}
      <Button
        variant="ghost"
        size="icon"
        onClick={() => scroll("right")}
        className={`${canScrollRight ? "absolute" : "hidden"} right-0 top-1/2 -translate-y-1/2 z-10 bg-white/70 hover:bg-white/90 rounded-full shadow-md transition-all`}
        aria-label="Scroll right"
      >
        <ChevronRight className="w-6 h-6 text-foreground" />
      </Button>
    </div>
  );
}
