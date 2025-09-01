import React from "react";
import { Box } from "@axelor/ui";
import { TimesheetTable } from "./timesheet-table";
import { TimesheetCompact } from "./timesheet-compact/timesheet-compact";
import { MOBILE_VIEW } from "../../constant";
import { useCounts } from "../../hooks/useCounts";
import { useTimesheet } from "../../hooks/store";
import { useEffect } from "react";
import { getCounts } from "../../services/api";
import { updateCounts } from "../../context/TimesheetAction";
import { useTsDispatch } from "../../hooks/useTsDispatch";
import useResponsive from "../../hooks/useResponsive";
import "./index.css";

export default function Content() {
  const res = useResponsive();
  const isMobileView = MOBILE_VIEW.some((x) => res[x]);
  const dispatch = useTsDispatch();
  const counts = useCounts();
  const [timesheet] = useTimesheet();

  useEffect(() => {
    (async () => {
      if (!timesheet?.id) return;
      const counts = await getCounts(timesheet?.id);
      dispatch(updateCounts(counts));
    })();
  }, [timesheet?.id, dispatch]);

  return (
    <Box d={"flex"} flexDirection={"column"} flex={"1 1 auto"}>
      {isMobileView ? (
        <div className="mobile-content">
          <TimesheetCompact counts={counts} />
        </div>
      ) : (
        <div className="desktop-content">
          <TimesheetTable counts={counts} />
        </div>
      )}
    </Box>
  );
}
