import React from 'react';

export function useDebounce(cb, duration) {
  const timer = React.useRef(null);

  const clearTimer = () => timer.current && clearTimeout(timer.current);
  const setTimer = cb => (timer.current = setTimeout(cb, duration));

  React.useEffect(() => {
    return () => clearTimer();
  }, []);

  return (...args) => {
    clearTimer();
    setTimer(() => cb(...args));
  };
}

export function useFocusRef({ autoFocus }) {
  const inputRef = React.useRef();

  React.useEffect(() => {
    autoFocus === true && inputRef.current.focus();
  }, [autoFocus, inputRef]);

  return inputRef;
}
