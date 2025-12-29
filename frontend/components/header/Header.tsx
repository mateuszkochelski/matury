import { LinkToFavourites } from "./LinkToFavourites";
import { buttonVariantsAndSizes } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Search } from "lucide-react";
import Link from "next/link";

export function Header() {
  return (
    <header className="fixed top-0 left-0 right-0 z-50 border-b border-primary/10 bg-white backdrop-blur-md">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-3 hover:opacity-90 transition-opacity">
          <div className="w-9 h-9 bg-gradient-to-br from-primary to-accent rounded-full flex items-center justify-center shadow-sm">
            <span className="text-xl">🐧</span>
          </div>
          <span className="text-xl font-bold tracking-tight text-foreground">matury.eu</span>
        </Link>

        <nav className="flex items-center gap-2 sm:gap-4">
          <Link
            href="/szukaj"
            aria-label="Szukaj"
            className={cn(buttonVariantsAndSizes({ variant: "default", size: "sm" }))}
          >
            <Search className="w-5 h-5 md:w-4 md:h-4" />
            <span className="hidden md:inline ml-2">Szukaj</span>
          </Link>

          <LinkToFavourites />
        </nav>
      </div>
    </header>
  );
}
