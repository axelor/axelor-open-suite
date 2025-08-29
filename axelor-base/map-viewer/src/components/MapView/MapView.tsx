import { useState, useEffect, useCallback } from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

import {
  getUserGeolocalisation,
  type MarkerPoint,
  type Position,
} from "../../utils";
import { markers } from "../../demo/data";
import { MarkerGroup } from "../../components";

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const MapView = () => {
  const [position, setPosition] = useState<Position>([48.8566, 2.3522]);
  const [apiMarkers, setApiMarkers] = useState<MarkerPoint[]>([]);

  useEffect(() => {
    getUserGeolocalisation(setPosition);
  }, []);

  const fetchMapData = useCallback(() => {
    setApiMarkers(markers);
  }, []);

  useEffect(() => {
    fetchMapData();
  }, [fetchMapData]);

  useEffect(() => {
    const container = L.DomUtil.get("map") as any;
    if (container != null) {
      container._leaflet_id = null;
    }
  }, []);

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
      <MarkerGroup markers={apiMarkers} />
    </MapContainer>
  );
};

export default MapView;
