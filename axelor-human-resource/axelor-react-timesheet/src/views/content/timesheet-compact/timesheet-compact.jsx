import { useState, useEffect, useMemo } from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import { Button } from "@axelor/ui";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";

import { useEmployee, useStore, useTimesheet } from "../../../hooks/store";
import {
  getDateRange,
  getDateStatus,
  isOnLeave,
  parseHours,
  parseDays,
  groupDatesByWeek,
  sortBy,
  parseMinutes,
} from "../../../services/utils";
import {
  fetchPublicHolidays,
  fetchValidatedLeaves,
  fetchTimesheetLines,
} from "../../../services/api";
import dayjs from "../../../lib/dayjs";
import { DayHeaderCard } from "../day-header-card/day-header-card";
import { DayTSLines } from "./day-tslines/day-tslines";
import WorkHourRatio from "../../../components/work-hour-ratio/work-hour-ratio";
import { DialogBox } from "../../../components/dialog-box/DialogBox";
import { TimeSheetForm } from "../timesheet-form/timesheet-form";
import { BottomSheet } from "react-spring-bottom-sheet";
import { DaySummary } from "../day-summary/day-summary";
import {
  DATE_FORMATS,
  TIME_LOGGING_PREFERENCES,
  TIMESHEET_LINE_FIELDS,
} from "../../../constant";
import { useTSlines } from "../../../hooks/useTSlines";
import { useCounts } from "../../../hooks/useCounts";
import styles from "./timesheet-compact.module.css";
import { useTsDispatch } from "../../../hooks/useTsDispatch";
import { updateTSlines } from "../../../context/TimesheetAction";

export const TimesheetCompact = () => {
  const [selectedWeekIdx, setSelectedWeekIdx] = useState(0);
  const [holidays, setHolidays] = useState([]);
  const [leaveList, setLeaveList] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [openForm, setOpenForm] = useState(false);
  const [openDaySummary, setOpenDaySummary] = useState(false);

  const timesheetLines = useTSlines();
  const counts = useCounts();
  const dispatch = useTsDispatch();

  const [employee] = useEmployee();
  const [timesheet] = useTimesheet();
  const { state } = useStore();
  const { projects, projectTasks } = state;

  const isDayPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

  const isMinutesPreference =
    timesheet.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.MINUTES;

  const durationKey =
    isDayPreference || isMinutesPreference ? "duration" : "hoursDuration";

  const planningDurationKey = isDayPreference
    ? "weeklyPlanningDuration"
    : "weeklyPlanningHoursDuration";

  const weeks = useMemo(() => {
    let dateColumns = [];
    if (timesheet?.fromDate && timesheet?.toDate) {
      dateColumns = getDateRange(timesheet.fromDate, timesheet.toDate);
    }
    return groupDatesByWeek(dateColumns);
  }, [timesheet]);

  const isMonthView = weeks.length > 1;
  const shownDates = useMemo(
    () => (isMonthView ? weeks[selectedWeekIdx] || [] : weeks[0] || []),
    [isMonthView, weeks, selectedWeekIdx]
  );

  const dateStatus = useMemo(() => {
    if (!selectedDate) return null;
    const holiday = holidays?.find((h) => h.date === selectedDate);
    const leave = isOnLeave(selectedDate, leaveList);
    return getDateStatus(selectedDate, timesheet, holiday, leave);
  }, [selectedDate, holidays, leaveList, timesheet]);

  const rows = useMemo(() => {
    const { tsProjects, tsProjectTasks, extra } = (timesheetLines || [])
      .filter((line) => line.date === selectedDate)
      .reduce(
        (acc, t) => {
          if (t.project) {
            if (t.projectTask) {
              acc.tsProjectTasks.push({
                ...t.projectTask,
                project: t.project,
                invoicingType:
                  t?.invoicingType || t?.["projectTask.invoicingType"],
              });
            } else {
              acc.tsProjects.push(t.project);
            }
          } else {
            acc.extra.push(t);
          }
          return acc;
        },
        { tsProjects: [], tsProjectTasks: [], extra: [] }
      );

    const sections = [
      ...Array.from(
        new Map([...projects, ...tsProjects].map((p) => [p?.id, p])).values()
      ).map((p) => ({
        ...p,
        isProject: true,
        label: p.fullName,
      })),
      ...Array.from(
        new Map(
          [...projectTasks, ...tsProjectTasks].map((t) => [t?.id, t])
        ).values()
      ).map((t) => ({
        ...t,
        isProject: false,
        label: `${t?.project?.fullName || ""}${t?.project ? " | " : ""}${
          t?.fullName || ""
        }`,
        invoicingType: t?.invoicingType || t?.["projectTask.invoicingType"],
      })),
      ...(extra.length
        ? [
            {
              isProject: false,
              label: "Miscellaneous",
            },
          ]
        : []),
    ];
    return sortBy(sections);
  }, [projects, projectTasks, timesheetLines, selectedDate]);

  const handleFormClose = () => setOpenForm(false);
  const handleOpenDaySummary = () => setOpenDaySummary(true);

  useEffect(() => {
    (async () => {
      if (!employee) return;
      const dates = await fetchPublicHolidays(
        employee,
        timesheet.fromDate,
        timesheet.toDate
      );
      setHolidays(dates);
    })();
  }, [employee, timesheet]);

  useEffect(() => {
    (async () => {
      if (!employee) return;
      const dates = await fetchValidatedLeaves(
        employee,
        timesheet.fromDate,
        timesheet.toDate
      );
      setLeaveList(dates);
    })();
  }, [employee, timesheet]);

  useEffect(() => {
    if (timesheet?.fromDate) {
      setSelectedDate(timesheet.fromDate);
    }
  }, [timesheet]);

  useEffect(() => {
    (async () => {
      if (!timesheet?.id || !shownDates?.length) return;
      const timesheetLines = await fetchTimesheetLines({
        data: {
          _domain: `self.timesheet.id = ${
            timesheet.id
          } AND self.date IN (${shownDates
            .map((date) => `'${date}'`)
            .join(",")})`,
        },
        fields: TIMESHEET_LINE_FIELDS,
      });
      dispatch(updateTSlines(timesheetLines?.data || []));
    })();
  }, [timesheet?.id, shownDates]);

  useEffect(() => {
    if (shownDates) {
      const day = selectedDate ? dayjs(selectedDate).day() : 0;
      setSelectedDate(shownDates[day === 0 ? day : day - 1]);
    }
  }, [shownDates]);

  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        <div className={styles.dayHeader}>
          <Swiper
            modules={[Navigation]}
            spaceBetween={0}
            slidesPerView={1}
            className={styles.swiper}
            onSlideChange={(swiper) => setSelectedWeekIdx(swiper.realIndex)}
          >
            {weeks.map((weekItems, index) => (
              <SwiperSlide key={index} className={styles.swiperSlide}>
                {weekItems.map((date) => {
                  const actual = isDayPreference
                    ? parseDays(
                        counts?.[date]?.[durationKey]?.toString() || "0"
                      )
                    : isMinutesPreference
                    ? parseMinutes(
                        counts?.[date]?.[durationKey]?.toString() || "0.00"
                      )
                    : parseHours(
                        counts?.[date]?.[durationKey]?.toString() || "0.00"
                      );

                  const weeklyPlanningDurationValue =
                    Number(counts?.[date]?.[planningDurationKey] ?? 0) *
                    (isMinutesPreference ? 60 : 1);

                  const leaveDuration =
                    Number(counts?.[date]?.leaveDuration) === 0
                      ? 1
                      : Number(counts?.[date]?.leaveDuration);

                  const expectedRaw =
                    weeklyPlanningDurationValue * leaveDuration;

                  const expected = isDayPreference
                    ? parseDays(expectedRaw.toString())
                    : isMinutesPreference
                    ? parseMinutes(expectedRaw.toString())
                    : parseHours(expectedRaw.toString());

                  return (
                    <div
                      key={date}
                      className={`${styles.slide} ${
                        selectedDate === date ? styles.selectedSlide : ""
                      }`}
                      onClick={() => setSelectedDate(date)}
                    >
                      <DayHeaderCard
                        date={date}
                        actualHours={actual.h}
                        actualMinutes={actual.m}
                        expectedHours={expected.h}
                        expectedMinutes={expected.m}
                        {...(isDayPreference && {
                          actualDays: actual.d,
                          expectedDays: expected.d,
                        })}
                        isDayPreference={isDayPreference}
                        {...(isMinutesPreference && {
                          expectedTotalMinutes: expected.m,
                          actualTotalMinutes: actual.m,
                        })}
                        isMinutesPreference={isMinutesPreference}
                      />
                    </div>
                  );
                })}
              </SwiperSlide>
            ))}
          </Swiper>
        </div>

        <div className={styles.tsLinesContainer}>
          <div
            className={styles.dayInfoContainer}
            onClick={handleOpenDaySummary}
          >
            <h3 className={styles.dateTitle}>
              {dayjs(selectedDate, DATE_FORMATS.YYYY_MM_DD).format(
                "dddd, MMMM D, YYYY"
              )}
            </h3>
            {!dateStatus?.disable && (
              <WorkHourRatio
                title="Working Time"
                counts={counts}
                date={selectedDate}
                isDayPreference={isDayPreference}
                isMinutesPreference={isMinutesPreference}
              />
            )}
          </div>

          {dateStatus?.disable ? (
            <div className={styles.emptyState}>
              <MaterialIcon icon="schedule" className={styles.emptyIcon} />
              <p>{dateStatus.message}</p>
            </div>
          ) : (
            <>
              <DayTSLines
                rows={rows}
                date={selectedDate}
                timesheetLines={timesheetLines}
              />
              {!timesheet?.isCompleted && (
                <Button
                  className={styles.headerAddBtn}
                  onClick={() => setOpenForm(true)}
                >
                  <MaterialIcon icon="add" />
                  <span>Add New time entry</span>
                </Button>
              )}
            </>
          )}

          {openForm && (
            <DialogBox
              open={openForm}
              fullscreen={false}
              onClose={handleFormClose}
              title="Add Time sheet Entry"
            >
              <TimeSheetForm date={selectedDate} onClose={handleFormClose} />
            </DialogBox>
          )}

          <BottomSheet
            open={openDaySummary}
            onDismiss={() => setOpenDaySummary(false)}
            snapPoints={({ maxHeight }) => [maxHeight * 0.6]}
          >
            <DaySummary
              date={selectedDate}
              info={counts?.[selectedDate]}
              isDayPreference={isDayPreference}
              isMinutesPreference={isMinutesPreference}
            />
          </BottomSheet>
        </div>
      </div>
    </div>
  );
};
