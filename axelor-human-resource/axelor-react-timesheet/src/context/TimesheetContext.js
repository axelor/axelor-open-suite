import { createContext } from "react";

export const INITIAL_STATE = {
  tslines: [],
  counts: {},
};

export const TimesheetContext = createContext(INITIAL_STATE);
