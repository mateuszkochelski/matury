import { GOOGLE_MAPS_API_KEY } from "@/app/constants";
import { GoogleMapsEmbed } from "@next/third-parties/google";

export default function Map({ query }: { query?: string }) {
  if (!GOOGLE_MAPS_API_KEY || !query) {
    return (
      <div className="w-full min-h-[100px] flex items-center justify-center bg-card text-gray-700 border border-gray-300 rounded">
        Unable to load the map
      </div>
    );
  } else {
    return (
      <GoogleMapsEmbed
        apiKey={GOOGLE_MAPS_API_KEY}
        height={400}
        mode="place"
        width="100%"
        language="pl"
        q={query}
        zoom="13"
        // rounded-md from tailwind
        style="border-radius: calc(var(--radius) /* 0.25rem = 4px */ - 2px)"
      />
    );
  }
}
