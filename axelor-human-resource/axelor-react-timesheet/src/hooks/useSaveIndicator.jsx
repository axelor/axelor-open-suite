import { useCallback, useContext } from "react";

import { setLoading, setSaved, setError } from "../context/TimesheetAction";
import { TimesheetContext } from "../context/TimesheetContext";

export const useSaveIndicator = (
  savedDuration = 1000,
  errorDuration = 1000
) => {
  const { isLoading, isSaved, isError, dispatch } =
    useContext(TimesheetContext);

  const scheduleReset = useCallback(
    (action, duration) => {
      if (duration > 0) {
        setTimeout(() => dispatch(action(false)), duration);
      }
    },
    [dispatch]
  );

  const startSaving = useCallback(() => {
    dispatch(setLoading(true));
    dispatch(setSaved(false));
    dispatch(setError(false));
  }, [dispatch]);

  const finishSaving = useCallback(() => {
    dispatch(setLoading(false));
    dispatch(setSaved(true));
    scheduleReset(setSaved, savedDuration);
  }, [dispatch, savedDuration, scheduleReset]);

  const errorSaving = useCallback(() => {
    dispatch(setLoading(false));
    dispatch(setSaved(false));
    dispatch(setError(true));
    scheduleReset(setError, errorDuration);
  }, [dispatch, errorDuration, scheduleReset]);

  const reset = useCallback(() => {
    dispatch(setLoading(false));
    dispatch(setSaved(false));
    dispatch(setError(false));
  }, [dispatch]);

  return {
    isLoading,
    isSaved,
    isError,
    startSaving,
    finishSaving,
    errorSaving,
    reset,
  };
};
