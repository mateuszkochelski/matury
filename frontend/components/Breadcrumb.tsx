import { Fragment } from "react";
import { ChevronRight, Home } from "lucide-react";
import Link from "next/link";

type BreadcrumbItem = {
  name: string;
  href?: string;
};

type BreadcrumbProps = {
  items: BreadcrumbItem[];
};

export function Breadcrumb({ items }: BreadcrumbProps) {
  return (
    <nav className="flex items-center space-x-2 text-sm text-foreground/70 overflow-scroll [scrollbar-width:none]">
      <Link
        href="/"
        className="flex items-center hover:text-foreground transition-colors"
        title="Strona Główna"
      >
        <Home size={16} />
      </Link>
      <div>
        <ChevronRight size={16} />
      </div>
      <Link href="/szukaj" className="hover:text-foreground transition-colors whitespace-nowrap">
        Wyniki Wyszukiwania
      </Link>
      {items.map((item) => (
        <Fragment key={`${item.name}${item.href}`}>
          <div>
            <ChevronRight size={16} />
          </div>
          {item.href ? (
            <Link
              href={item.href}
              className="hover:text-foreground transition-colors whitespace-nowrap"
            >
              {item.name}
            </Link>
          ) : (
            <span className="text-foreground">{item.name}</span>
          )}
        </Fragment>
      ))}
    </nav>
  );
}
