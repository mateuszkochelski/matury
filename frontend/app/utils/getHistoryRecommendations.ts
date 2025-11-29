"use server";

import { BACKEND_URL } from "../constants";
import { FieldOfStudy } from "@/components/custom-table/types";

export async function getHistoryRecommendations(fieldIds: number[]) {
  const res = await fetch(`${BACKEND_URL}/api/recommendation/history`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ fieldIds, k: 16 }),
  });

  if (res.ok) {
    const fields: FieldOfStudy[] = await res.json();
    return fields;
  }

  return [];
}
