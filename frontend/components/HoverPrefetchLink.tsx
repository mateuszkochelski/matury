"use client";

import { useState } from "react";
import Link from "next/link";

export function HoverPrefetchLink({
  href,
  className,
  children,
}: {
  href: string;
  className: string;
  children: React.ReactNode;
}) {
  const [active, setActive] = useState(false);

  return (
    <Link
      href={href}
      className={className}
      prefetch={active ? null : false}
      onMouseEnter={() => setActive(true)}
    >
      {children}
    </Link>
  );
}
