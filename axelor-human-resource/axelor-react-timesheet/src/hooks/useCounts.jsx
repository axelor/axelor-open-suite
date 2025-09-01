import { useContext } from "react";
import { TimesheetContext } from "../context/TimesheetContext";

export const useCounts = () => {
  const { counts } = useContext(TimesheetContext);
  return counts;
};
