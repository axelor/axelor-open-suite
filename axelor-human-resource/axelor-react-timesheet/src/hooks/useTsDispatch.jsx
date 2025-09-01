import { useContext } from "react";
import { TimesheetContext } from "../context/TimesheetContext";

export const useTsDispatch = () => {
  const { dispatch } = useContext(TimesheetContext);
  return dispatch;
};
