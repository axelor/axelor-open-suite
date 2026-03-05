import { useMemo } from "react";
import { MapView } from "./components";
import "./App.css";

function App() {
  const mapViewId = useMemo(() => {
    const params = new URLSearchParams(window.location.search);

    return parseInt(params.get("mapId") ?? "", 10);
  }, []);

  return (
    <div style={{ height: "100vh", width: "100vw" }}>
      <MapView id={mapViewId} />
    </div>
  );
}

export default App;
