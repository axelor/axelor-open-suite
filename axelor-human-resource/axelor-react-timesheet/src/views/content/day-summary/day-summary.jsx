import dayjs from "../../../lib/dayjs";
import { DATE_FORMATS } from "../../../constant";
import {
  formatDays,
  formatHours,
  formatMinutes,
  parseDays,
  parseHours,
  parseMinutes,
} from "../../../services/utils";
import { WorkSummary } from "../../../components/work-summary/work-summary";
import ProgressBar from "../../../components/progress-bar/progress-bar";
import styles from "./day-summary.module.css";

export const DaySummary = ({
  date,
  info = {},
  isDayPreference = false,
  isMinutesPreference = false,
}) => {
  const leaveDuration = Number(info.leaveDuration || 0);

  let actualTime,
    expectedTime,
    diffTime,
    actual,
    expected,
    leave,
    diff,
    timeLabel;

  if (isDayPreference) {
    actualTime = Number(info.duration || 0);
    expectedTime = Number(info.weeklyPlanningDuration || 0);
    diffTime = actualTime + leaveDuration - expectedTime;

    actual = parseDays(String(actualTime));
    expected = parseDays(String(expectedTime));
    leave = parseDays(String(leaveDuration));
    diff = parseDays(String(diffTime));

    timeLabel = formatDays(actual.d);
  } else if (isMinutesPreference) {
    actualTime = Number(info.duration || 0);
    expectedTime = Number(info.weeklyPlanningHoursDuration || 0) * 60;
    diffTime = actualTime + leaveDuration - expectedTime;

    actual = parseMinutes(String(actualTime));
    expected = parseMinutes(String(expectedTime));
    leave = parseMinutes(String(leaveDuration));
    diff = parseMinutes(String(diffTime));

    timeLabel = formatMinutes(actual.m);
  } else {
    actualTime = Number(info.hoursDuration || 0);
    const weeklyHours = Number(info.weeklyPlanningHoursDuration || 0);
    const leaveFactor = leaveDuration === 0 ? 1 : leaveDuration;
    expectedTime = weeklyHours * leaveFactor;
    diffTime = actualTime + leaveDuration - expectedTime;

    actual = parseHours(String(actualTime));
    expected = parseHours(String(expectedTime));
    leave = parseHours(String(leaveDuration));
    diff = parseHours(String(diffTime));

    timeLabel = formatHours(actual.h, actual.m);
  }

  return (
    <div className={styles.daySummaryContainer}>
      <div className={styles.summaryHeader}>
        <ProgressBar
          actualHours={actual.h}
          actualMinutes={actual.m}
          expectedHours={expected.h}
          expectedMinutes={expected.m}
          actualDays={actual.d}
          expectedDays={expected.d}
          isDayPreference={isDayPreference}
          isMinutesPreference={isMinutesPreference}
          expectedTotalMinutes={expected.m}
          actualTotalMinutes={actual.m}
        />
        <div>
          <div className={styles.dateLabel}>
            {dayjs(date, DATE_FORMATS.YYYY_MM_DD).format("dddd, MMMM D, YYYY")}
          </div>
          <span className={styles.timeLabel}>{timeLabel}</span>
        </div>
      </div>
      <div className={styles.workSummaryContainer}>
        <WorkSummary
          actual={actual}
          expect={expected}
          leave={leave}
          diff={diff}
          diffTime={diffTime}
          isDayPreference={isDayPreference}
          isMinutesPreference={isMinutesPreference}
        />
      </div>
    </div>
  );
};
