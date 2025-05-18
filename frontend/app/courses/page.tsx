"use client";

import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";

export default function Home() {
  const { data } = useQuery({
    queryKey: ["repoData"],
    queryFn: async () => {
      const response = await fetch("http://localhost:8080/api/university");
      return (await response.json()) as object;
    },
  });

  useEffect(() => {
    console.log({ data });
  }, [data]);

  return (
    <div>
      <span>Check the console for data</span>
    </div>
  );
}
