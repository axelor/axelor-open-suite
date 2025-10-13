import { useMemo } from "react";
import { Popup } from "react-leaflet";

import { openAxelorView, type MarkerPoint } from "../../utils";
import LinkIcon from "../LinkIcon/LinkIcon";

interface MarkerPopup extends MarkerPoint {
  model: string;
}

const MarkerPopup = ({
  model,
  recordId,
  viewName = "",
  cardContent,
}: MarkerPopup) => {
  const isViewConfigured = useMemo(() => !!viewName, [viewName]);

  return (
    <Popup>
      <div
        onDoubleClick={
          isViewConfigured
            ? () => openAxelorView(viewName, recordId, model)
            : undefined
        }
      >
        <div dangerouslySetInnerHTML={{ __html: cardContent }} />
        {isViewConfigured && <LinkIcon />}
      </div>
    </Popup>
  );
};

export default MarkerPopup;
