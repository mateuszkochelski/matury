"use client";

import { useEffect, useState } from "react";
import { FieldOfStudy } from "../custom-table/types";
import { Button } from "../ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";

export default function FieldsCarousel({ fields }: { fields: FieldOfStudy[] }) {
  const [itemsPerPage, setItemsPerPage] = useState(4);

  useEffect(() => {
    const updateItems = () => {
      if (window.innerWidth < 640) {
        setItemsPerPage(1);
      } else if (window.innerWidth < 1024) {
        setItemsPerPage(2);
      } else {
        setItemsPerPage(4);
      }
    };
    updateItems();
    window.addEventListener("resize", updateItems);
    return () => window.removeEventListener("resize", updateItems);
  }, []);

  const [currentPage, setCurrentPage] = useState(0);
  const pagesCount = Math.ceil(fields.length / itemsPerPage);
  const nextPage = () => {
    setCurrentPage((prev) => (prev + 1) % pagesCount);
  };
  const prevPage = () => {
    setCurrentPage((prev) => (prev - 1 + pagesCount) % pagesCount);
  };

  return (
    <Card className="border-primary/20">
      <CardContent className="p-6">
        <div className="flex items-center justify-between mb-4">
          <Button
            variant="outline"
            size="sm"
            onClick={prevPage}
            className="border-primary/30 bg-transparent"
            disabled={currentPage == 0}
          >
            <ChevronLeft className="w-4 h-4" />
          </Button>

          <div className="flex gap-2">
            {[...Array(pagesCount).keys()].map((index) => (
              <div
                key={index}
                className={`w-2 h-2 rounded-full ${
                  index === currentPage ? "bg-primary" : "bg-primary/30"
                }`}
              />
            ))}
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={nextPage}
            className="border-primary/30 bg-transparent"
            disabled={currentPage == pagesCount - 1}
          >
            <ChevronRight className="w-4 h-4" />
          </Button>
        </div>

        <div className="grid sm:grid-cols-2 md:grid-cols-4 gap-4">
          {fields
            .slice(currentPage * itemsPerPage, currentPage * itemsPerPage + itemsPerPage)
            .map((field) => (
              <Card
                key={field.id}
                className="border-primary/10 hover:shadow-md transition-shadow flex flex-col justify-between"
              >
                <CardHeader className="pb-3">
                  <CardTitle className="text-base text-foreground">{field.name}</CardTitle>
                  <CardDescription className="text-sm">{field.university.name}</CardDescription>
                  <CardDescription className="text-sm">{field.department.name}</CardDescription>
                </CardHeader>
                <CardContent className="pt-0 space-y-3">
                  <a href={`/${field.university.id}/${field.department.id}/${field.id}`}>
                    <Button
                      size="sm"
                      className="w-full bg-primary hover:bg-primary/90 text-foreground cursor-pointer"
                    >
                      Sprawdź
                    </Button>
                  </a>
                </CardContent>
              </Card>
            ))}
        </div>
      </CardContent>
    </Card>
  );
}
