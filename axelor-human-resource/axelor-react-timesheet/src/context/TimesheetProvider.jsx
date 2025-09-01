import React, { useReducer } from "react";
import { INITIAL_STATE, TimesheetContext } from "./TimesheetContext";
import Reducer from "./TimesheetReducer";

export const TimesheetContextProvider = ({ children }) => {
  const [state, dispatch] = useReducer(Reducer, INITIAL_STATE);

  return (
    <TimesheetContext.Provider
      value={{
        tslines: state.tslines,
        counts: state.counts,
        dispatch,
      }}
    >
      {children}
    </TimesheetContext.Provider>
  );
};
