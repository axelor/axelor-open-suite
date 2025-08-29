import { useMemo } from "react";
import { Marker } from "react-leaflet";
import L from "leaflet";

import type { MarkerPoint } from "../../utils";
import { MarkerPopup } from "../../components";

const ICON_ORIGINAL_SIZE = 24;
const ICON_SIZE = 30;

const MarkerGroup = ({
  markers,
  color,
}: {
  markers: MarkerPoint[];
  color?: string;
}) => {
  const colorIcon = useMemo(
    () =>
      L.divIcon({
        className: "",
        html: `
        <svg xmlns="http://www.w3.org/2000/svg" width="${ICON_SIZE}" height="${ICON_SIZE}" viewBox="0 0 ${ICON_SIZE} ${ICON_SIZE}" fill="${
          color ?? "#457896"
        }" stroke="currentColor"  stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-map-pin-icon lucide-map-pin">
            <g transform="scale(${ICON_SIZE / ICON_ORIGINAL_SIZE}, ${
          ICON_SIZE / ICON_ORIGINAL_SIZE
        })">
                <path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/>
                <circle cx="12" cy="10" r="${
                  (ICON_SIZE * 3) / ICON_ORIGINAL_SIZE
                }" fill="white"/>
            </g>
        </svg>
    `,
        iconSize: [ICON_SIZE, ICON_SIZE],
        iconAnchor: [ICON_SIZE / 2, ICON_SIZE],
      }),
    [color]
  );

  return markers.map((m, idx) => (
    <Marker key={idx} position={[m.latitude, m.longitude]} icon={colorIcon}>
      <MarkerPopup marker={m} />
    </Marker>
  ));
};

export default MarkerGroup;
