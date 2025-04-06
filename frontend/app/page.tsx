import { Button } from "@/components/ui/button/button";

export default function Home() {
  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start">
        <Button>Button default</Button>
        <Button variant="destructive">Button destructive</Button>
        <Button variant="outline">Button outline</Button>
        <Button variant="secondary">Button secondary</Button>
        <Button variant="ghost">Button ghost</Button>
        <Button variant="link">Button link</Button>
      </main>
    </div>
  );
}
