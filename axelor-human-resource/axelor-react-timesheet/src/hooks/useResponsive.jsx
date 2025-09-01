import { useCallback, useEffect, useMemo, useState } from "react";

export function useMediaQuery(query) {
  const media = useMemo(() => window.matchMedia(query), [query]);
  const [state, setState] = useState(media.matches);
  const handleChange = useCallback(() => setState(media.matches), [media]);

  useEffect(() => {
    media.addEventListener("change", handleChange);
    return () => {
      media.removeEventListener("change", handleChange);
    };
  }, [handleChange, media]);

  return state;
}

export function useResponsive() {
  const xs = useMediaQuery("(max-width: 639.98px)");
  const sm = useMediaQuery("(min-width: 640px) and (max-width: 767.98px)");
  const md = useMediaQuery("(min-width: 768px) and (max-width: 1023.98px)");
  const lg = useMediaQuery("(min-width: 1024px) and (max-width: 1279.98px)");
  const xl = useMediaQuery("(min-width: 1280px) and (max-width: 1535.98px)");
  const xxl = useMediaQuery("(min-width: 1536px)");
  return {
    xs,
    sm,
    md,
    lg,
    xl,
    xxl,
  };
}

export default useResponsive;
