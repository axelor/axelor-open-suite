import { useState, useCallback } from 'react';

export default function useOnOff(initState = false) {
  const [state, set] = useState(initState);

  const on = useCallback(() => {
    set(true);
  }, []);
  const off = useCallback(() => {
    set(false);
  }, []);

  return [state, on, off];
}
