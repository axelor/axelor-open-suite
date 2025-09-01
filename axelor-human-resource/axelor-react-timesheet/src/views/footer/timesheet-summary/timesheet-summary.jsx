import { useMemo } from "react";
import { Box } from "@axelor/ui";
import { useTimesheet } from "../../../hooks/store";
import { useCounts } from "../../../hooks/useCounts";
import {
  formatDays,
  formatHours,
  formatMinutes,
  formatSigned,
  getSelectionLabel,
  parseDays,
  parseHours,
  parseMinutes,
} from "../../../services/utils";
import { WorkSummary } from "../../../components/work-summary/work-summary";
import { TIME_LOGGING_PREFERENCES } from "../../../constant";
import styles from "./timesheet-summary.module.css";

const TimeSheetSummary = ({ isExpanded, showTitle }) => {
  const [timesheet] = useTimesheet();
  const counts = useCounts();
  const isDayPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

  const isMinutesPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.MINUTES;

  const parseValue = (val) => {
    if (isDayPreference) return parseDays(val);
    if (isMinutesPreference) return parseMinutes(val);
    return parseHours(val);
  };

  const formatValue = (obj, totalVal = 0, signed = false) => {
    if (isDayPreference)
      return signed
        ? formatSigned(totalVal, formatDays(obj.d))
        : formatDays(obj.d);
    if (isMinutesPreference)
      return signed
        ? formatSigned(totalVal, formatMinutes(obj.m))
        : formatMinutes(obj.m);
    return signed
      ? formatSigned(totalVal, formatHours(obj.h, obj.m))
      : formatHours(obj.h, obj.m);
  };

  const { actualTime, expectTime, leaveTime } = useMemo(() => {
    if (!counts) {
      return { actualTime: 0, expectTime: 0, leaveTime: 0 };
    }
    let actual = 0;
    let expected = 0;
    let leave = 0;

    const durationKey =
      isDayPreference || isMinutesPreference ? "duration" : "hoursDuration";
    const planningDurationKey = isDayPreference
      ? "weeklyPlanningDuration"
      : "weeklyPlanningHoursDuration";

    Object.values(counts).forEach((item) => {
      const duration = Number(item[durationKey] || 0);
      const planningDuration =
        Number(item[planningDurationKey] || 0) * (isMinutesPreference ? 60 : 1);
      const leaveDuration = Number(item.leaveDuration || 0);

      const adjustedPlanningDuration =
        planningDuration * (leaveDuration === 0 ? 1 : leaveDuration);

      actual += duration;
      leave += leaveDuration;
      expected += adjustedPlanningDuration;
    });

    return { actualTime: actual, expectTime: expected, leaveTime: leave };
  }, [counts, isDayPreference, isMinutesPreference]);

  const diffTime = actualTime + leaveTime - expectTime;

  const actualDuration = parseValue(actualTime);
  const expectedDuration = parseValue(expectTime);
  const leaveDuration = parseValue(leaveTime);
  const diffDuration = parseValue(diffTime);

  const formattedActual = formatValue(actualDuration);
  const formattedExpect = formatValue(expectedDuration);
  const formattedDiff = formatValue(diffDuration, diffTime, true);
  if (!timesheet) {
    return <Box className={styles.summaryContainer}>No data</Box>;
  }

  return (
    <Box className={styles.summaryContainer}>
      {isExpanded && (
        <Box>
          {showTitle && (
            <div className={styles.timesheetLabel}>
              <span>Timesheet summary </span>
              <span>
                {getSelectionLabel(timesheet.fromDate, timesheet.toDate)}
              </span>
            </div>
          )}
          <span className={styles.label}>Working time</span>
        </Box>
      )}

      <Box className={styles.summaryInfo}>
        <Box className={styles.summaryInfoWrapper}>
          <Box d="flex" gap={8}>
            <span className={styles.actualTime}>{formattedActual}</span>
            <span>/</span>
            <span>{formattedExpect}</span>
          </Box>

          <Box className={styles.seperator}></Box>

          <Box className={styles.diff}>
            <span>Difference: </span>
            <span>{formattedDiff}</span>
          </Box>
        </Box>

        {isExpanded && (
          <>
            <div className={styles.separator}></div>
            <WorkSummary
              actual={actualDuration}
              expect={expectedDuration}
              leave={leaveDuration}
              diff={diffDuration}
              diffTime={diffTime}
              isDayPreference={isDayPreference}
              isMinutesPreference={isMinutesPreference}
            />
          </>
        )}
      </Box>
    </Box>
  );
};

export default TimeSheetSummary;
