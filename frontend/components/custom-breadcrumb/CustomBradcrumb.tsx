"use client";

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";

type Crumb = {
  name: string;
  href?: string;
};

type CustomBreadcrumbProps = {
  items: Crumb[];
  className?: string;
};

export const CustomBreadcrumb: React.FC<CustomBreadcrumbProps> = ({ items, className }) => {
  return (
    <div className={`overflow-x-auto whitespace-nowrap ${className}`}>
      <Breadcrumb className="inline-flex items-center space-x-1 text-sm">
        <BreadcrumbList>
          {items.map((item, index) => (
            <span key={index} className="inline-flex items-center whitespace-nowrap">
              <BreadcrumbItem className="mr-2">
                {item.href ? (
                  <BreadcrumbLink href={item.href}>{item.name}</BreadcrumbLink>
                ) : (
                  <BreadcrumbPage>{item.name}</BreadcrumbPage>
                )}
              </BreadcrumbItem>
              {index < items.length - 1 && <BreadcrumbSeparator />}
            </span>
          ))}
        </BreadcrumbList>
      </Breadcrumb>
    </div>
  );
};
