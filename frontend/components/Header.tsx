import { Button } from "@/components/ui/button";
import { Menu } from "lucide-react";
import Link from "next/link";

export function Header() {
  return (
    <header className="border-b border-primary/20 bg-white/80 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-primary to-accent rounded-full flex items-center justify-center">
            <span className="text-xl">Logo</span>
          </div>
          <h1 className="text-xl font-bold text-foreground">nazwa_strony</h1>
        </Link>
        <nav className="hidden md:flex items-center gap-6">
          <Link href="/" className="text-foreground/70 hover:text-foreground transition-colors">
            Link1
          </Link>
          <Link href="/" className="text-foreground/70 hover:text-foreground transition-colors">
            Link2
          </Link>
          <Link href="/" className="text-foreground/70 hover:text-foreground transition-colors">
            Link3
          </Link>
          <Button
            variant="outline"
            className="border-primary text-primary hover:bg-primary/10 bg-transparent"
          >
            Button
          </Button>
        </nav>
        <Button variant="ghost" size="sm" className="md:hidden">
          <Menu className="w-5 h-5" />
        </Button>
      </div>
    </header>
  );
}
