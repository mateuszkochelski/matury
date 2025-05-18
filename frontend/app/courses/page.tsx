'use client'

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Button } from "@/components/ui/button/button";
import { FloatingLabelInput } from "@/components/ui/floating-label-input";
import { Input } from "@/components/ui/input";
import { SplitButton } from "@/components/ui/split-button";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";

export default function Home() {
  const { error, data } = useQuery({
    queryKey: ["repoData"],
    queryFn: async () => {
      const response = await fetch("http://localhost:8080/api/university");
      return await response.json();
    },
  });

  useEffect(() => {
    console.log({data});
  }, [data])

  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start">
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink href="/">Home</BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbLink href="/components">Components</BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>Breadcrumb</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
        <Button>Button default</Button>
        <Button variant="destructive">Button destructive</Button>
        <Button variant="outline">Button outline</Button>
        <Button variant="secondary">Button secondary</Button>
        <Button variant="ghost">Button ghost</Button>
        <Button variant="link">Button link</Button>
        <SplitButton
          firstText="First button"
          secondText="Second button"
          secondVariant="secondary"
        />
        <Input />
        <FloatingLabelInput placeholder="Placeholder" />
      </main>
    </div>
  );
}
