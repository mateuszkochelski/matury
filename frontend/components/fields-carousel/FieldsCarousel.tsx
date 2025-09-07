"use client";

import { useState } from "react";
import { FieldOfStudy } from "../custom-table/types";
import { Button } from "../ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";

export default function FieldsCarousel({
  fields,
  itemsPerPage,
}: {
  fields: FieldOfStudy[];
  itemsPerPage: number;
}) {
  const [currentPage, setCurrentPage] = useState(0);
  const pagesCount = Math.ceil(fields.length / itemsPerPage);
  const pagesArr = Array.from({ length: pagesCount }, (_, i) => i);
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
          >
            <ChevronLeft className="w-4 h-4" />
          </Button>

          <div className="flex gap-2">
            {pagesArr.map((_, index) => (
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
          >
            <ChevronRight className="w-4 h-4" />
          </Button>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
          {fields.slice(currentPage * itemsPerPage, currentPage * itemsPerPage + 4).map((field) => (
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
