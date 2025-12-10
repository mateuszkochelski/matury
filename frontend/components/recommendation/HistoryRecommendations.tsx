"use client";

import { useEffect, useState } from "react";
import { FieldOfStudy } from "../custom-table/types";
import FieldsCarousel from "../fields-carousel/FieldsCarousel";
import { getHistoryRecommendations } from "@/app/utils/getHistoryRecommendations";

export default function HistoryRecommedations() {
  const [recoFields, setRecoFields] = useState<FieldOfStudy[]>([]);

  useEffect(() => {
    const loadRecommendations = async () => {
      try {
        const stored = localStorage.getItem("userFieldsHistory");
        const ids: number[] = stored ? JSON.parse(stored) : [];

        const fields = await getHistoryRecommendations(ids);
        setRecoFields(fields);
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
              Dobrane na podstawie Twojej aktywności
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
