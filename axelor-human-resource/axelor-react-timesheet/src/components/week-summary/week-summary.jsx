import { useMemo } from "react";
import { AutoPopup } from "../auto-popup/auto-popup";
import { useTimesheet } from "../../hooks/store";
import { useCounts } from "../../hooks/useCounts";
import {
  formatDays,
  formatHours,
  formatMinutes,
  formatSigned,
  parseDays,
  parseHours,
  parseMinutes,
} from "../../services/utils";
import { WorkSummary } from "../work-summary/work-summary";
import { TIME_LOGGING_PREFERENCES } from "../../constant";
import styles from "./week-summary.module.css";

export const WeekSummary = ({ week, active }) => {
  const counts = useCounts();
  const [timesheet] = useTimesheet();

  const isDayPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

  const isMinutesPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.MINUTES;

  const { actualTime, expectTime, leaveTime } = useMemo(() => {
    if (!counts) return { actualTime: 0, expectTime: 0, leaveTime: 0 };

    let actual = 0;
    let expected = 0;
    let leave = 0;

    const durationKey =
      isDayPreference || isMinutesPreference ? "duration" : "hoursDuration";
    const planningDurationKey = isDayPreference
      ? "weeklyPlanningDuration"
      : "weeklyPlanningHoursDuration";

    week.forEach((day) => {
      const item = counts[day] || {};

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
  }, [counts, week, isDayPreference, isMinutesPreference]);

  const diffTime = actualTime + leaveTime - expectTime;

  const parseValue = (value) => {
    if (isDayPreference) return parseDays(value);
    if (isMinutesPreference) return parseMinutes(value);
    return parseHours(value);
  };

  const actual = parseValue(actualTime);
  const expect = parseValue(expectTime);
  const diff = parseValue(diffTime);
  const leave = parseValue(leaveTime);

  const formatValue = (obj, totalValue) => {
    if (isDayPreference) return formatSigned(totalValue, formatDays(obj.d));
    if (isMinutesPreference)
      return formatSigned(totalValue, formatMinutes(obj.m));
    return formatSigned(totalValue, formatHours(obj.h, obj.m));
  };

  const formattedDiff = formatValue(diff, diffTime);
  const formattedActual = formatValue(actual, actualTime);

  return (
    <div className={styles.container}>
      <span className={styles.diffTime}>
        {(diff.h !== 0 || diff.m !== 0 || diff.d !== 0) && formattedDiff}
      </span>

      {active && (
        <div>
          <AutoPopup
            trigger={() => <span className={styles.weekSummary}>Summary</span>}
            contentStyle={{ padding: "20px" }}
          >
            <div>
              <div className={styles.summaryWrapper}>
                <WorkSummary
                  actual={actual}
                  expect={expect}
                  diff={diff}
                  leave={leave}
                  diffTime={diffTime}
                  isDayPreference={isDayPreference}
                  isMinutesPreference={isMinutesPreference}
                />
              </div>
              <div className={styles.summaryWrapper}>
                <div>
                  <span>Actual working time</span>
                  <span>{formattedActual}</span>
                </div>
              </div>
            </div>
          </AutoPopup>
        </div>
      )}
    </div>
  );
};

export default WeekSummary;
