import { useContext } from "react";
import { TimesheetContext } from "../context/TimesheetContext";

export const useTSlines = () => {
  const { tslines } = useContext(TimesheetContext);
  return tslines;
};
