import { createContext } from "react";

export const INITIAL_STATE = {
  tslines: [],
  counts: {},
  isLoading: false,
  isSaved: false,
  isError: false,
};

export const TimesheetContext = createContext(INITIAL_STATE);
