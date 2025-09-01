import { clsx } from "@axelor/ui";
import { useEffect, useMemo, useState, useCallback } from "react";
import AutoSizer from "react-virtualized-auto-sizer";
import { FixedSizeList as List } from "react-window";
import { useEmployee, useStore, useTimesheet } from "../../../hooks/store";
import {
  fetchPublicHolidays,
  fetchTimesheetLines,
  fetchValidatedLeaves,
} from "../../../services/api";
import {
  createTimesheetRow,
  getDateRange,
  groupDatesByWeek,
} from "../../../services/utils";
import { SheetDurationCell } from "../sheet-duration-cell/sheet-duration-cell";
import WeekTabs from "../week-tabs/week-tabs";

import { DialogBox } from "../../../components/dialog-box/DialogBox";
import WeekDays from "../../../components/week-days/week-days";
import {
  TIMESHEET_LINE_FIELDS,
  TIME_LOGGING_PREFERENCES,
} from "../../../constant";
import { updateTSlines } from "../../../context/TimesheetAction";
import { useTsDispatch } from "../../../hooks/useTsDispatch";
import { useTSlines } from "../../../hooks/useTSlines";
import { TimeSheetForm } from "../timesheet-form/timesheet-form";
import styles from "./timesheet-table.module.css";

export function TimesheetTable() {
  const [selectedWeekIdx, setSelectedWeekIdx] = useState(0);
  const [holidays, setHolidays] = useState([]);
  const [leaveList, setLeaveList] = useState([]);
  const [hoveredDate, setHoveredDate] = useState(null);
  const [openForm, setOpenForm] = useState(false);
  const [rows, setRows] = useState([]);

  const [employee] = useEmployee();
  const [timesheet] = useTimesheet();
  const { state } = useStore();
  const { projects, projectTasks } = state;
  const tslines = useTSlines();
  const dispatch = useTsDispatch();

  const isCompleted = timesheet?.isCompleted;

  const weeks = useMemo(() => {
    let dateColumns = [];
    if (timesheet?.fromDate && timesheet?.toDate) {
      dateColumns = getDateRange(timesheet.fromDate, timesheet.toDate);
    }
    return groupDatesByWeek(dateColumns);
  }, [timesheet]);

  const isMonthView = weeks.length >= 2;

  const shownDates = useMemo(
    () => (isMonthView ? weeks[selectedWeekIdx] || [] : weeks?.[0]),
    [isMonthView, weeks, selectedWeekIdx]
  );
  const handleHoveredDate = useCallback((date) => setHoveredDate(date), []);

  const isDayPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

  const isMinutesPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.MINUTES;

  const durationKey =
    isDayPreference || isMinutesPreference ? "duration" : "hoursDuration";
  const planningDurationKey = isDayPreference
    ? "weeklyPlanningDuration"
    : "weeklyPlanningHoursDuration";

  const handleFormClose = useCallback(() => {
    setOpenForm(false);
    setHoveredDate(null);
  }, []);

  const Row = useCallback(
    ({ index, style }) => {
      const row = rows[index];
      return (
        <div key={row.id} style={style} className={styles.tr}>
          <div className={clsx(styles.rowNameCell, styles.td)}>
            <div className={styles.truncateText}>{row.label}</div>
          </div>
          {shownDates.map((date) => (
            <div key={date} className={clsx(styles.timesheetCell, styles.td)}>
              <div className={clsx(styles.timeEntryPlaceholder)}>
                <SheetDurationCell key={date} row={row} date={date} />
              </div>
            </div>
          ))}
        </div>
      );
    },
    [shownDates, rows]
  );

  useEffect(() => {
    (async () => {
      if (!shownDates?.length || !timesheet?.id) return;
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
  }, [shownDates, timesheet?.id]);

  useEffect(() => {
    (async () => {
      if (!employee) return;
      const dates = await fetchPublicHolidays(
        employee,
        timesheet.fromDate,
        timesheet.toDate
      );
      setHolidays(dates);
      setSelectedWeekIdx(0);
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
    const r = createTimesheetRow(projects, projectTasks, tslines);
    if (r?.length !== rows.length) {
      setRows(r);
    }
  }, [projects, projectTasks, tslines, rows?.length]);

  return (
    <div
      className={styles.timesheetTableScrollContainer}
      style={{
        height: weeks?.length === 1 ? "calc(100% + 80px)" : "100%",
      }}
    >
      <WeekTabs
        items={weeks}
        activeId={selectedWeekIdx}
        onClick={(id) => setSelectedWeekIdx(id)}
      />
      <div className={clsx(styles.timesheetTable, styles.stickyTable)}>
        <div className={clsx(styles.tr, styles.stickyHeaderRow)}>
          <div className={clsx(styles.td, styles.projectHeader)}>Projects</div>
          {shownDates.map((date) => (
            <div
              key={date}
              className={clsx(styles.stickyDate, styles.td)}
              onMouseEnter={() => handleHoveredDate(date)}
              onMouseLeave={() => !openForm && handleHoveredDate(null)}
            >
              <WeekDays
                date={date}
                isDayPreference={isDayPreference}
                isMinutesPreference={isMinutesPreference}
                holidays={holidays}
                leaveList={leaveList}
                isCompleted={isCompleted}
                durationKey={durationKey}
                planningDurationKey={planningDurationKey}
                hoveredDate={hoveredDate}
                handleOpenForm={() => setOpenForm(true)}
              />
            </div>
          ))}
        </div>
        <div className={styles.listWrapperContainer}>
          <AutoSizer>
            {({ height, width }) => (
              <List
                itemCount={rows.length}
                itemSize={50}
                height={height}
                width={width}
                className={styles.reactWindowList}
              >
                {Row}
              </List>
            )}
          </AutoSizer>
        </div>
        {openForm && (
          <DialogBox
            open={openForm}
            fullscreen={false}
            onClose={handleFormClose}
            title={"Add Time sheet Entry"}
          >
            <TimeSheetForm date={hoveredDate} onClose={handleFormClose} />
          </DialogBox>
        )}
      </div>
    </div>
  );
}
