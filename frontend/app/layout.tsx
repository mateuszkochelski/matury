import "./globals.css";
import { Header } from "@/components/header/Header";
import { Geist, Geist_Mono } from "next/font/google";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} font-[family-name:var(--font-geist-sans)] antialiased`}
      >
        <div className="min-h-screen bg-background">
          <Header />
          <div className="container mx-auto px-4 pt-20 pb-6 flex flex-col gap-5">{children}</div>
        </div>
      </body>
    </html>
  );
}
