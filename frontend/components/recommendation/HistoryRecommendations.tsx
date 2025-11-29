"use client";

import { useEffect, useState } from "react";
import { CLIENT_BACKEND_URL } from "../../app/constants";
import { FieldOfStudy } from "../custom-table/types";
import FieldsCarousel from "../fields-carousel/FieldsCarousel";

export default function HistoryRecommedations() {
  const [recoFields, setRecoFields] = useState<FieldOfStudy[]>([]);

  useEffect(() => {
    const loadRecommendations = async () => {
      try {
        const stored = localStorage.getItem("userFieldsHistory");
        const ids = stored ? JSON.parse(stored) : [];

        const res = await fetch(`${CLIENT_BACKEND_URL}/api/recommendation/history`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ fieldIds: ids, k: 16 }),
        });

        if (res.ok) {
          const fields: FieldOfStudy[] = await res.json();
          setRecoFields(fields);
        }
      } catch (e) {
        console.error("Unable to fetch recommendations for user:", e);
      }
    };

    loadRecommendations();
  }, []);

  return (
    <div className="container mx-auto px-4">
      {recoFields.length > 0 ? (
        <>
          <div className="text-center mb-8 md:mb-16">
            <h2 className="h2 text-foreground mb-4">Sprawdź te kierunki!</h2>
            <p className="text-base md:text-lg text-foreground/70 max-w-2xl mx-auto">
              Wygenerowne na podstawie twojej historii przeglądania na naszej stronie
            </p>
          </div>
          <FieldsCarousel fields={recoFields} />
        </>
      ) : (
        <div className="text-center">
          <h2 className="h2 text-foreground mb-4">Zastanawiamy się czego szukasz</h2>
          <p className="text-base md:text-lg text-foreground/70 max-w-2xl mx-auto">
            Tutaj pojawią się rekomendacje kierunków studiów na podstawie twojej aktywności na
            naszej stronie. Nie ma czasu do stracenia!
          </p>
        </div>
      )}
    </div>
  );
}
