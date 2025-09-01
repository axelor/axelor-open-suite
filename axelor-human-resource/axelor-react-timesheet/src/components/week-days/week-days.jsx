import { Button, clsx } from "@axelor/ui";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { useTimesheet } from "../../hooks/store";
import { useCounts } from "../../hooks/useCounts";
import {
  getDateStatus,
  isOnLeave,
  parseDays,
  parseHours,
  parseMinutes,
} from "../../services/utils";
import { DayHeaderCard } from "../../views/content/day-header-card/day-header-card";
import { DaySummary } from "../../views/content/day-summary/day-summary";
import { AutoPopup } from "../auto-popup/auto-popup";
import styles from "./week-days.module.css";

const WeekDays = ({
  date,
  isDayPreference,
  isMinutesPreference,
  holidays,
  leaveList,
  isCompleted,
  durationKey,
  planningDurationKey,
  hoveredDate,
  handleOpenForm,
}) => {
  const [timesheet] = useTimesheet();
  const counts = useCounts();

  const actual = isDayPreference
    ? parseDays(counts?.[date]?.[durationKey]?.toString() || "0")
    : isMinutesPreference
    ? parseMinutes(counts?.[date]?.[durationKey]?.toString() || "0.00")
    : parseHours(counts?.[date]?.[durationKey]?.toString() || "0.00");

  const weeklyPlanningDurationValue =
    Number(counts?.[date]?.[planningDurationKey] ?? 0) *
    (isMinutesPreference ? 60 : 1);

  const leaveDuration =
    Number(counts?.[date]?.leaveDuration) === 0
      ? 1
      : Number(counts?.[date]?.leaveDuration);

  const expected = isDayPreference
    ? parseDays((weeklyPlanningDurationValue * leaveDuration).toString())
    : isMinutesPreference
    ? parseMinutes((weeklyPlanningDurationValue * leaveDuration).toString())
    : parseHours(
        (weeklyPlanningDurationValue * leaveDuration)?.toString() || "0.00"
      );

  const holiday = holidays?.find((holiday) => holiday.date === date);
  const leave = isOnLeave(date, leaveList);
  const status = getDateStatus(date, timesheet, holiday, leave);

  return (
    <>
      <DayHeaderCard
        status={status}
        date={date}
        actualHours={actual.h}
        actualMinutes={actual.m}
        expectedHours={expected.h}
        expectedMinutes={expected.m}
        {...(isDayPreference && {
          expectedDays: expected.d,
          actualDays: actual.d,
        })}
        isDayPreference={isDayPreference}
        {...(isMinutesPreference && {
          expectedTotalMinutes: expected.m,
          actualTotalMinutes: actual.m,
        })}
        isMinutesPreference={isMinutesPreference}
      />
      {hoveredDate === date && !status.disable && (
        <div
          className={clsx(
            styles.headerContainer,
            isCompleted && styles.removeBackdrop
          )}
        >
          <div className={styles.headerWrapper}>
            {!isCompleted && (
              <Button className={styles.headerAddBtn} onClick={handleOpenForm}>
                <MaterialIcon icon="add" />
                <span>Add time entry</span>
              </Button>
            )}
            <AutoPopup
              trigger={() => <span className={styles.summary}>Summary</span>}
            >
              <div className={styles.daySummaryWrapper1}>
                <DaySummary
                  date={date}
                  info={counts?.[date]}
                  isDayPreference={isDayPreference}
                  isMinutesPreference={isMinutesPreference}
                />
              </div>
            </AutoPopup>
          </div>
        </div>
      )}
    </>
  );
};

export default WeekDays;
