import StoreProvider from "../hooks/store.jsx";

export default function Providers({ children }) {
  return <StoreProvider>{children}</StoreProvider>;
}
