"use client";

import { GOOGLE_MAPS_API_KEY } from "@/app/constants";
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api";

export type Coords = {
  lat: number;
  lng: number;
};

export default function Map({ center }: { center?: Coords }) {
  if (!GOOGLE_MAPS_API_KEY || !center) {
    return (
      <div className="w-full min-h-[100px] flex items-center justify-center bg-gray-100 text-gray-700 border border-gray-300 rounded">
        Unable to load the map
      </div>
    );
  } else {
    return (
      <LoadScript googleMapsApiKey={GOOGLE_MAPS_API_KEY}>
        <GoogleMap mapContainerClassName="w-full min-h-[400px] rounded" center={center} zoom={12}>
          <Marker position={center} />
        </GoogleMap>
      </LoadScript>
    );
  }
}
