import { memo, useMemo } from "react";
import {
  formatDays,
  formatHours,
  formatMinutes,
  parseDays,
  parseHours,
  parseMinutes,
} from "../../services/utils";
import styles from "./work-hour-ratio.module.css";

const WorkHourRatio = memo(
  ({
    title,
    date,
    counts,
    isDayPreference = false,
    isMinutesPreference = false,
  }) => {
    const data = counts?.[date] || {};
    const duration = data.duration || "0";
    const hoursDuration = data.hoursDuration || "0.00";
    const weeklyPlanningDuration = Number(data.weeklyPlanningDuration) || 0;
    const weeklyPlanningHoursDuration =
      (Number(data.weeklyPlanningHoursDuration) || 0) *
      (isMinutesPreference ? 60 : 1);
    const leaveDuration = Number(data.leaveDuration) || 1;

    const actual = isDayPreference
      ? parseDays(duration?.toString())
      : isMinutesPreference
      ? parseMinutes(duration?.toString())
      : parseHours(hoursDuration?.toString());

    const expected = useMemo(() => {
      return isDayPreference
        ? parseDays((weeklyPlanningDuration * leaveDuration).toString())
        : isMinutesPreference
        ? parseMinutes((weeklyPlanningHoursDuration * leaveDuration).toString())
        : parseHours((weeklyPlanningHoursDuration * leaveDuration).toString());
    }, [
      isDayPreference,
      isMinutesPreference,
      weeklyPlanningDuration,
      weeklyPlanningHoursDuration,
      leaveDuration,
    ]);

    const ratioDisplay = useMemo(
      () =>
        isDayPreference
          ? `${formatDays(actual.d)} / ${formatDays(expected.d)}`
          : isMinutesPreference
          ? `${formatMinutes(actual.m)} / ${formatMinutes(expected.m)}`
          : `${formatHours(actual.h, actual.m)} / ${formatHours(
              expected.h,
              expected.m
            )}`,
      [isDayPreference, isMinutesPreference, actual, expected]
    );

    return (
      <div className={styles.container}>
        <span>{title}</span>
        <div className={styles.workHourWrapper}>{ratioDisplay}</div>
      </div>
    );
  }
);

export default WorkHourRatio;
