import { Popup } from "react-leaflet";

import { openAxelorView, type MarkerPoint } from "../../utils";

interface MarkerPopup extends MarkerPoint {
  model: string;
}

const MarkerPopup = ({
  model,
  recordId,
  viewName,
  cardContent,
}: MarkerPopup) => {
  return (
    <Popup>
      <div
        onDoubleClick={
          viewName != null
            ? () => openAxelorView(viewName, recordId, model)
            : undefined
        }
        dangerouslySetInnerHTML={{ __html: cardContent }}
      />
    </Popup>
  );
};

export default MarkerPopup;
