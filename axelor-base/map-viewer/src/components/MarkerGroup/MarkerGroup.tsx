import { useEffect, useMemo, useState } from "react";
import { LayerGroup, LayersControl, Marker } from "react-leaflet";
import L from "leaflet";

import type { MarkerPoint } from "../../utils";
import { MarkerPopup } from "../../components";
import { computeMapGroupData } from "../../api/map-group";

const ICON_ORIGINAL_SIZE = 24;
const ICON_SIZE = 30;

const MarkerGroup = ({ id, name }: { id: number; name: string }) => {
  const [config, setConfig] = useState<any>();

  useEffect(() => {
    computeMapGroupData(id)
      .then(setConfig)
      .catch(() => setConfig(null));
  }, [id]);

  const colorIcon = useMemo(
    () =>
      L.divIcon({
        className: "",
        html: `
        <svg xmlns="http://www.w3.org/2000/svg" width="${ICON_SIZE}" height="${ICON_SIZE}" viewBox="0 0 ${ICON_SIZE} ${ICON_SIZE}" fill="${
          config?.color ?? "#457896"
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
    [config?.color]
  );

  if (!Array.isArray(config?.data)) {
    return null;
  }

  return (
    <LayersControl.Overlay key={`${name}-${id}`} checked name={name}>
      <LayerGroup>
        {(config.data as MarkerPoint[]).map((m, idx) => (
          <Marker
            key={idx}
            position={[m.latitude, m.longitude]}
            icon={colorIcon}
          >
            <MarkerPopup
              {...m}
              model={config?.model}
              viewName={config?.viewName}
            />
          </Marker>
        ))}
      </LayerGroup>
    </LayersControl.Overlay>
  );
};

export default MarkerGroup;
