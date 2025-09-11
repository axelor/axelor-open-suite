import { useState, useEffect } from "react";
import { LayersControl, MapContainer, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

import { getUserGeolocalisation, type Position } from "../../utils";
import { MarkerGroup } from "../../components";
import { fetchMapView } from "../../api/map-view";

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const MapView = ({ id }: { id: number }) => {
  const [position, setPosition] = useState<Position>([48.8566, 2.3522]);
  const [mapView, setMapView] = useState<any>();

  useEffect(() => {
    getUserGeolocalisation(setPosition);
  }, []);

  useEffect(() => {
    if (!isNaN(id)) {
      fetchMapView(id)
        .then(({ data }) => setMapView(data?.[0]))
        .catch(() => setMapView(null));
    }
  }, [id]);

  useEffect(() => {
    const container = L.DomUtil.get("map") as any;
    if (container != null) {
      container._leaflet_id = null;
    }
  }, []);

  if (!mapView) return null;

  return (
    <MapContainer
      id="map"
      center={position}
      zoom={3}
      style={{ height: "100vh", width: "100%" }}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://osm.org/copyright">OpenStreetMap</a> contributors'
      />
      <LayersControl position="topright">
        {mapView?.mapGroupList?.map((_m: any, idx: number) => (
          <MarkerGroup key={idx} id={_m.id} name={_m.name} />
        ))}
      </LayersControl>
    </MapContainer>
  );
};

export default MapView;
