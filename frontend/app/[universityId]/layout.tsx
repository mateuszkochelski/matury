export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <main className="min-h-screen flex flex-col p-2 pb-20 gap-3 sm:gap-4 sm:p-8 md:p-16 lg:p-20 mx-auto max-w-[1000px]">
      {children}
    </main>
  );
}
