"use client";

import { useEffect } from "react";

export default function UserHistoryTracker({ fieldId }: { fieldId: number }) {
  useEffect(() => {
    let ids = [];

    try {
      const stored = localStorage.getItem("userFieldsHistory");
      ids = stored ? JSON.parse(stored) : [];
    } catch (e) {
      console.error("localStorage parsing error:", e);
    }

    if (!Array.isArray(ids)) ids = [];

    if (ids.includes(fieldId)) {
      // If history already contains this id, move it to the end of array
      ids = ids.filter((id) => id !== fieldId);
    } else if (ids.length >= 10) {
      // Otherwise remove oldest entry and add new one to the end of array
      ids.shift();
    }
    ids.push(fieldId);

    localStorage.setItem("userFieldsHistory", JSON.stringify(ids));
  }, [fieldId]);

  return null;
}
