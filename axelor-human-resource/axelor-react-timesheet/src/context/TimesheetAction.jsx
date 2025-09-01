import {
  SET_ERROR,
  SET_LOADING,
  SET_SAVED,
  UPDATE_COUNTS,
  UPDATE_TSLINES,
} from "./TimesheetActionTypes";

export const updateTSlines = (lines) => ({
  type: UPDATE_TSLINES,
  payload: lines,
});

export const updateCounts = (counts) => ({
  type: UPDATE_COUNTS,
  payload: counts,
});

export const setLoading = (isLoading) => ({
  type: SET_LOADING,
  payload: isLoading,
});

export const setSaved = (isSaved) => ({
  type: SET_SAVED,
  payload: isSaved,
});

export const setError = (payload) => ({
  type: SET_ERROR,
  payload,
});
