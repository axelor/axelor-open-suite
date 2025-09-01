import { createRoot } from "react-dom/client";

import App from "./App.jsx";
import Providers from "./providers";

import "./index.css";

createRoot(document.getElementById("root")).render(
  <Providers>
    <App />
  </Providers>
);
