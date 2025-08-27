import { Popup } from "react-leaflet";

import { openAxelorView, type MarkerPoint } from "../../utils";

const MarkerPopup = ({ marker }: { marker: MarkerPoint }) => {
  return (
    <Popup>
      <div
        onDoubleClick={
          marker.view != null ? () => openAxelorView(marker.view) : undefined
        }
      >
        <strong>{marker.title}</strong>
        <br />
        {Object.entries(marker.fields).map(([key, value]) => (
          <div key={key}>
            <strong>{key.charAt(0).toUpperCase() + key.slice(1)}: </strong>
            {value ?? "N/A"}
          </div>
        ))}
      </div>
    </Popup>
  );
};

export default MarkerPopup;
