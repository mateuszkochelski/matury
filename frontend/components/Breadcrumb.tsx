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
    <nav className="flex items-center space-x-1 text-sm text-foreground/70">
      <Link href="/" className="flex items-center hover:text-foreground transition-colors">
        <Home className="w-4 h-4" />
      </Link>
      {items.map((item, index) => (
        <div key={index} className="flex items-center space-x-1">
          <ChevronRight className="w-4 h-4" />
          {item.href ? (
            <Link href={item.href} className="hover:text-foreground transition-colors">
              {item.name}
            </Link>
          ) : (
            <span className="text-foreground">{item.name}</span>
          )}
        </div>
      ))}
    </nav>
  );
}
