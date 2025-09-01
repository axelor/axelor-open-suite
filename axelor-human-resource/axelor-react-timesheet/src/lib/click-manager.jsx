export const ClickManager = (() => {
  const listeners = new Map();
  let isInitialized = false;

  const init = () => {
    if (isInitialized) return;

    document.addEventListener("mousedown", (event) => {
      listeners.forEach((callback, ref) => {
        if (ref.current && !ref.current.contains(event.target)) {
          callback(event);
        }
      });
    });

    isInitialized = true;
  };

  return {
    register: (ref, callback) => {
      init();
      listeners.set(ref, callback);
      return () => listeners.delete(ref);
    },
  };
})();
