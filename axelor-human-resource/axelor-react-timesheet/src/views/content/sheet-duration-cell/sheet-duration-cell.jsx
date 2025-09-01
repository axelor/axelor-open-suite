import { useState, useRef, useEffect, useCallback, memo, useMemo } from "react";
import { clsx } from "@axelor/ui";
import { toast } from "sonner";
import {
  formatDays,
  formatHours,
  formatMinutes,
  getDateStatus,
  getLoggingPreference,
  getTsLines,
  hasSameProject,
  hasSameProjectTask,
  isDailyLimitExceed,
  isOnLeave,
  parseDays,
  parseHours,
  parseMinutes,
  toHour,
} from "../../../services/utils";
import CommentAction from "../comment-actions/comment-action";
import { ActionButton } from "../../../components/action-button/action-button";
import { ClickManager } from "../../../lib/click-manager";
import {
  editTimesheetLine,
  fetchTimesheetLines,
  removeDurations,
  updateToInvoice,
  updateTSDuration,
} from "../../../services/api";
import { useTimesheet } from "../../../hooks/store";
import { DialogBox } from "../../../components/dialog-box/DialogBox";
import {
  TIME_LOGGING_PREFERENCES,
  TIMESHEET_LINE_FIELDS,
} from "../../../constant";
import { ArrowButton } from "../../../components/arrow-button/arrow-button";
import { useDebounce } from "../../../hooks/useDebounce";
import { useTSlines } from "../../../hooks/useTSlines";
import { useCounts } from "../../../hooks/useCounts";
import { updateCounts, updateTSlines } from "../../../context/TimesheetAction";
import { useTsDispatch } from "../../../hooks/useTsDispatch";
import styles from "./sheet-duration-cell.module.css";
import { useSaveIndicator } from "../../../hooks/useSaveIndicator";

export const SheetDurationCell = memo(({ date, row, holidays, leaveList }) => {
  const cellRef = useRef(null);
  const [timesheet] = useTimesheet();

  const holiday = holidays?.find((holiday) => holiday.date === date);
  const leave = isOnLeave(date, leaveList);
  const isCompleted = timesheet?.isCompleted;
  const { disable } = getDateStatus(date, timesheet, holiday, leave);

  const [cellFocused, setCellFocused] = useState(false);

  const handleCellBlur = (e) => {
    if (!e.currentTarget.contains(e.relatedTarget)) {
      setCellFocused(false);
    }
  };

  const handleCellKeyDown = (e) => {
    if (e.key === "Tab") {
      setCellFocused(false);
      return;
    }
  };

  return (
    <div
      className={styles.wrapper}
      tabIndex={disable || isCompleted ? -1 : 0}
      ref={cellRef}
      onFocus={() => setCellFocused(true)}
      onBlur={handleCellBlur}
      data-focused={cellFocused}
      onKeyDown={handleCellKeyDown}
    >
      <TimeSheetCell
        row={row}
        date={date}
        cellFocused={cellFocused}
        isReadOnly={disable || isCompleted}
      />
    </div>
  );
});

export const TimeSheetCell = ({ row, date, cellFocused, isReadOnly }) => {
  const tslines = useTSlines();
  const counts = useCounts();
  const dispatch = useTsDispatch();

  const [lines, setLines] = useState([]);

  const hourInputRef = useRef(null);
  const minInputRef = useRef(null);
  const dayInputRef = useRef(null);
  const durationRef = useRef(null);

  const [timesheet] = useTimesheet();
  const [open, setOpen] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const [duration, setDuration] = useState({ day: 0, hour: 0, min: 0 });
  const [focusedInput, setFocusedInput] = useState("hour");
  const [editable, setEditable] = useState(false);
  const [isInvoice, setIsInvoice] = useState(false);
  const [isCommentExist, setIsCommentExist] = useState(false);

  const { startSaving, finishSaving, errorSaving } = useSaveIndicator();

  const isDayPreference =
    timesheet.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

  const isMinutesPreference =
    timesheet.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.MINUTES;

  const loggingPreference = getLoggingPreference({
    isDayPreference,
    isMinutesPreference,
  });

  const durationKey =
    isDayPreference || isMinutesPreference ? "duration" : "hoursDuration";

  const planningDurationKey = isDayPreference
    ? "weeklyPlanningDuration"
    : "weeklyPlanningHoursDuration";

  const debouncedUpdate = useDebounce((newValue) => {
    handleUpdate?.(newValue);
  }, 500);

  const { totalHourDuration, totalDuration } = lines.reduce(
    (acc, curr) => {
      acc.totalHourDuration += Number(curr.hoursDuration || "0.0");
      acc.totalDuration += Number(curr.duration || "0");
      return acc;
    },
    { totalHourDuration: 0, totalDuration: 0 }
  );

  const hasTimeSpent = useMemo(
    () => row?.invoicingType === 1 && lines.find((i) => i?.product),
    [row, lines]
  );

  const activateEditable = () => {
    setEditable(true);
  };

  const updateDuration = useCallback(
    (updated) => {
      setDuration(updated);

      let value;

      if (isDayPreference) {
        value = updated.day;
      } else if (isMinutesPreference) {
        value = updated.min;
      } else {
        value = toHour(updated);
      }

      debouncedUpdate(value);
    },
    [isDayPreference, isMinutesPreference, debouncedUpdate]
  );

  const incrementDay = (val) => {
    const rawValue = (parseFloat(duration.day) || 0) + val;
    const newDay = Math.max(0, parseFloat(rawValue.toFixed(2)));
    updateDuration({ ...duration, day: newDay });
  };

  const incrementHour = (val) => {
    let newHour = duration.hour + val;
    if (newHour < 0) newHour = 0;
    if (newHour > 24) newHour = 24;
    updateDuration({ ...duration, hour: newHour });
  };

  const incrementMinute = (val) => {
    const { hour, min } = duration;

    if (isMinutesPreference) {
      let newMin = parseFloat(min || 0) + val;
      if (isNaN(newMin)) newMin = 0;

      newMin = Math.max(0, newMin);

      updateDuration({ ...duration, min: newMin });
    } else {
      let newMin = min + val;
      let newHour = hour;

      if (newMin >= 60) {
        newHour += Math.floor(newMin / 60);
        newMin = newMin % 60;
      } else if (newMin < 0 && newHour > 0) {
        newHour -= 1;
        newMin = 60 + newMin;
      } else if (newMin < 0) {
        newMin = 0;
      }

      updateDuration({ ...duration, hour: newHour, min: newMin });
    }
  };

  const handleCellKeyDown = useCallback(
    (e) => {
      if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown"].includes(e.key)) {
        e.preventDefault();
      }

      if (e.key === "ArrowLeft" || e.key === "ArrowRight") {
        requestAnimationFrame(() => {
          if (focusedInput === "hour" && e.key === "ArrowRight") {
            minInputRef.current?.focus();
            setFocusedInput("min");
          } else if (focusedInput === "min" && e.key === "ArrowLeft") {
            hourInputRef.current?.focus();
            setFocusedInput("hour");
          }
        });
        return;
      }

      if (e.key === "ArrowUp" || e.key === "ArrowDown") {
        if (focusedInput === "hour") {
          const diff = e.key === "ArrowUp" ? 1 : -1;
          const newHour = Math.max(0, duration.hour + diff);
          const updatedValue = { ...duration, hour: newHour };
          setDuration(updatedValue);
          debouncedUpdate(toHour(updatedValue));
        } else if (focusedInput === "min") {
          if (isMinutesPreference) {
            const diff = e.key === "ArrowUp" ? 1 : -1;
            const currentMin = parseFloat(duration.min);

            let newMin = Math.max(0, currentMin + diff);

            const updatedValue = { ...duration, hour: 0, min: newMin };

            setDuration(updatedValue);
            debouncedUpdate(updatedValue.min);
          } else {
            const diff = e.key === "ArrowUp" ? 5 : -5;
            let newMin = duration.min + diff;
            let newHour = duration.hour;

            if (newMin >= 60) {
              newHour += Math.floor(newMin / 60);
              newMin = newMin % 60;
            } else if (newMin < 0 && newHour > 0) {
              newHour -= 1;
              newMin = 60 + newMin;
            } else if (newMin < 0) {
              newMin = 0;
            }

            const updatedValue = { ...duration, hour: newHour, min: newMin };
            setDuration(updatedValue);
            debouncedUpdate(toHour(updatedValue));
          }
        } else if (focusedInput === "day") {
          const diff = e.key === "ArrowUp" ? 1 : -1;
          const newDay = Math.max(
            0,
            parseFloat((parseFloat(duration.day) || 0) + diff).toFixed(2)
          );
          const updatedValue = { ...duration, day: newDay };
          setDuration(updatedValue);
          debouncedUpdate(updatedValue.day);
        }
      }
    },
    [focusedInput, duration, debouncedUpdate, isMinutesPreference]
  );

  const handleFocus = (field) => () => setFocusedInput(field);

  const handleInputChange = (e) => {
    const { value, name } = e.target;

    if (name === "day") {
      const isValid = /^(\d*\.?\d*)$/.test(value);
      if (!isValid) return;

      setDuration((prev) => ({ ...prev, day: value }));

      const num = parseFloat(value);
      if (!isNaN(num)) {
        debouncedUpdate(num);
      }
      return;
    }
    if (name === "min" && isMinutesPreference) {
      const isValid = /^(\d{0,3}(\.\d*)?)$/.test(value);
      if (!isValid) return;

      setDuration((prev) => ({ ...prev, min: value }));

      const num = parseFloat(value);
      if (!isNaN(num)) {
        updateDuration({ ...duration, min: num });
      }
      return;
    }

    let num = Number(value);
    if (isNaN(num)) return;
    if (name === "hour" && num > 24) num = 24;
    if (name === "min" && num >= 60) num = 59;

    updateDuration({ ...duration, [name]: num });
  };

  const handleClickOutside = useCallback(() => {
    setEditable(false);
  }, []);

  const editTimesheet = useCallback(async () => {
    await editTimesheetLine({
      timesheetId: timesheet?.id,
      date,
      projectId: row?.isProject ? row?.id : row.project?.id,
      projectTaskId: row?.isProject ? null : row?.id,
    });
  }, [row, timesheet?.id, date]);

  const updateTimeSheetLines = useCallback(
    async (date, duration, row) => {
      const updatedTimesheetLinesRes = await fetchTimesheetLines({
        data: {
          _domain: `self.timesheet.id = ${
            timesheet.id
          } AND self.date = '${date}' AND self.project.id = ${
            (row?.isProject ? row?.id : row.project?.id) ?? null
          }${
            !row?.isProject
              ? ` AND self.projectTask.id = ${row.id ?? null}`
              : ` AND self.projectTask = ${null}`
          }`,
        },
        fields: TIMESHEET_LINE_FIELDS,
      });
      const updatedTimesheetLines = updatedTimesheetLinesRes?.data;

      const updatedCounts = {
        ...counts,
        [date]: { ...counts?.[date], [durationKey]: duration },
      };

      const filtered = (tslines || []).filter(
        (t) =>
          !(
            t.timesheet?.id === timesheet?.id &&
            t.date === date &&
            hasSameProject(t, row) &&
            hasSameProjectTask(t, row)
          )
      );

      dispatch(updateCounts(updatedCounts));
      dispatch(
        updateTSlines([...(filtered || []), ...(updatedTimesheetLines || [])])
      );
    },
    [timesheet?.id, durationKey, counts, tslines]
  );

  const handleUpdate = useCallback(
    async (total) => {
      startSaving();

      try {
        let durationDiff;
        if (isDayPreference || isMinutesPreference) {
          durationDiff = total - totalDuration;
        } else {
          durationDiff = total - totalHourDuration;
        }

        const payload = {
          timesheetId: timesheet.id,
          projectId: row.isProject ? row.id : row.project?.id,
          projectTaskId: row.isProject ? null : row.id,
          date,
          [durationKey]: total,
          toInvoice: row?.invoicingType === 1,
        };

        const res = await updateTSDuration(payload);
        if (res.codeStatus !== 200) {
          toast.error("Failed to update timesheet entry!");
          errorSaving();
          return;
        }

        if (
          isDailyLimitExceed(
            counts?.[date],
            durationDiff,
            durationKey,
            planningDurationKey,
            isMinutesPreference
          ) &&
          durationDiff > 0
        ) {
          toast.warning("Daily limit exceeded!");
        }

        const baseDuration = Number(counts?.[date]?.[durationKey] || 0);
        const newTotal = baseDuration + durationDiff;
        await updateTimeSheetLines(date, newTotal, row);

        finishSaving();
      } catch (err) {
        console.error("Update Error >>>", err);
        toast.error("An error occurred while updating the timesheet.");
        errorSaving();
      }
    },
    [
      counts,
      durationKey,
      planningDurationKey,
      timesheet?.id,
      date,
      isDayPreference,
      isMinutesPreference,
      row,
      totalDuration,
      totalHourDuration,
      updateTimeSheetLines,
      startSaving,
      finishSaving,
      errorSaving,
    ]
  );

  const handleDelete = useCallback(
    async (date, row, duration) => {
      const payload = {
        date,
        timesheetId: timesheet?.id,
        projectId: row?.isProject ? row.id : row.project?.id,
        projectTaskId: row?.isProject ? null : row.id,
      };

      const res = await removeDurations(payload);
      if (res.codeStatus !== 200) {
        return toast.error("Failed to delete.");
      }

      const baseDuration = Number(counts?.[date]?.[durationKey] || 0);
      const deletedDuration = isDayPreference
        ? duration.day
        : isMinutesPreference
        ? duration.min
        : toHour(duration);
      const newTotal = Math.max(0, baseDuration - deletedDuration);

      await updateTimeSheetLines(date, newTotal, row);

      toast.success("Successfully deleted!");
    },
    [
      counts,
      durationKey,
      isDayPreference,
      isMinutesPreference,
      timesheet?.id,
      updateTimeSheetLines,
    ]
  );

  const deleteItems = useCallback(async () => {
    setOpen(false);
    await handleDelete?.(date, row, duration);

    setDuration({ day: 0, hour: 0, min: 0 });
  }, [duration, date, row, handleDelete]);

  const handleUpdateToInvoice = useCallback(async () => {
    const newIsInvoice = !isInvoice;
    setIsInvoice(newIsInvoice);

    const res = await updateToInvoice({
      date,
      timesheetId: timesheet?.id,
      projectId: row?.isProject ? row.id : row.project?.id,
      projectTaskId: row?.isProject ? null : row.id,
      toInvoice: newIsInvoice,
    });

    if (res.status === -1) {
      console.error("Failed to update to invoice");
      return;
    }

    toast.success(
      res.count === 1 ? "Successfully updated." : `${res.count} lines updated.`
    );
  }, [date, timesheet?.id, row, isInvoice]);

  useEffect(() => {
    if (isDayPreference) {
      setDuration({ day: totalDuration, hour: 0, min: 0 });
    } else {
      const d = isMinutesPreference
        ? parseMinutes(totalDuration)
        : parseHours(totalHourDuration);
      setDuration({ day: 0, hour: d.h, min: d.m });
    }
  }, [isDayPreference, isMinutesPreference, totalDuration, totalHourDuration]);

  useEffect(() => {
    if (!cellFocused) return;

    const focusInput = (ref, type) => {
      ref.current?.focus();
      setFocusedInput(type);
    };

    const timer = setTimeout(() => {
      if (isDayPreference) {
        focusInput(dayInputRef, "day");
      } else if (isMinutesPreference) {
        focusInput(minInputRef, "min");
      } else {
        focusInput(hourInputRef, "hour");
      }
    }, 0);

    return () => clearTimeout(timer);
  }, [cellFocused, isDayPreference, isMinutesPreference]);

  useEffect(() => {
    return ClickManager.register(durationRef, handleClickOutside);
  }, [handleClickOutside]);

  useEffect(() => {
    const l = getTsLines(tslines, row, date);
    setIsCommentExist(l.some((item) => item.comments?.trim()));
    setLines(l);
  }, [tslines, row, date]);

  useEffect(() => {
    setIsInvoice(lines.every((i) => (i.product ? i.toInvoice : true)));
  }, [lines]);

  const renderArrowButtons = (direction) => {
    const isUp = direction === "up";
    const dayHandler = () => incrementDay(isUp ? 1 : -1);
    const hourHandler = () => incrementHour(isUp ? 1 : -1);
    const minHandler = () => {
      const increment = isMinutesPreference ? 1 : 5;
      incrementMinute(isUp ? increment : -increment);
    };

    return (
      <div className={styles.btnWrapper}>
        {isDayPreference ? (
          <ArrowButton direction={direction} onClick={dayHandler} />
        ) : isMinutesPreference ? (
          <ArrowButton direction={direction} onClick={minHandler} />
        ) : (
          <>
            <ArrowButton direction={direction} onClick={hourHandler} />
            <ArrowButton direction={direction} onClick={minHandler} />
          </>
        )}
      </div>
    );
  };

  if (isReadOnly) {
    return (
      <DisableCell
        totalHourDuration={totalHourDuration}
        hasLines={lines.length}
        isDayPreference={isDayPreference}
        isMinutesPreference={isMinutesPreference}
        totalDuration={totalDuration}
      />
    );
  }

  return (
    <div
      className={clsx(
        isCommentExist && styles.commentIndicator,
        lines.length > 1 && styles.multiLineIndicator
      )}
    >
      <div
        className={styles.cellContainer}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        onKeyDown={handleCellKeyDown}
      >
        {isHovered && renderArrowButtons("up")}

        <div
          className={clsx(
            styles.duration,
            cellFocused && styles.wrapperOutline
          )}
          ref={durationRef}
          onClick={activateEditable}
        >
          {isDayPreference ? (
            <>
              <input
                ref={dayInputRef}
                name="day"
                className={styles.durationInput}
                value={duration.day}
                onChange={handleInputChange}
                onFocus={handleFocus("day")}
                tabIndex={-1}
                style={{ width: 40, textAlign: "center" }}
                aria-label="Days"
                autoComplete="off"
              />
              <span>d</span>
            </>
          ) : isMinutesPreference ? (
            <>
              <input
                ref={minInputRef}
                name="min"
                className={styles.durationInput}
                value={duration.min}
                onChange={handleInputChange}
                onFocus={handleFocus("min")}
                tabIndex={-1}
                style={{ width: 40, textAlign: "center" }}
                aria-label="Minutes"
                autoComplete="off"
              />
              <span>m</span>
            </>
          ) : (
            <>
              <input
                ref={hourInputRef}
                name="hour"
                className={styles.durationInput}
                value={duration.hour}
                onChange={handleInputChange}
                onFocus={handleFocus("hour")}
                tabIndex={-1}
                style={{ width: 30, textAlign: "center" }}
                aria-label="Hours"
                autoComplete="off"
              />
              <span>h</span>
              <input
                ref={minInputRef}
                name="min"
                className={styles.durationInput}
                value={duration.min}
                onChange={handleInputChange}
                onFocus={handleFocus("min")}
                tabIndex={-1}
                style={{ width: 30, textAlign: "center" }}
                aria-label="Minutes"
                autoComplete="off"
              />
            </>
          )}
        </div>

        {isHovered && renderArrowButtons("down")}

        {(isHovered || editable) && lines?.length > 0 && (
          <div
            className={clsx(
              styles.actionWrapper,
              editable && styles.activateAction
            )}
          >
            {hasTimeSpent && (
              <ActionButton
                title={
                  isInvoice ? 'Disable "To Invoice"' : 'Enable "To Invoice"'
                }
                icon="article"
                onClick={handleUpdateToInvoice}
                className={clsx(
                  styles.invoiceBtn,
                  isInvoice && styles.activateToInvoice
                )}
              />
            )}
            <ActionButton
              title="Edit Timesheet"
              icon="edit"
              onClick={editTimesheet}
              className={styles.editBtn}
            />
            <CommentAction
              tslines={lines}
              loggingPreference={loggingPreference}
            />
            <ActionButton
              title="Delete Items"
              icon="delete"
              onClick={() => setOpen(true)}
              className={styles.deleteBtn}
            />
          </div>
        )}
        <DialogBox
          title="Delete"
          open={open}
          message={`Are you want delete ${
            lines?.length > 1 ? `${lines.length} lines?` : "line?"
          } `}
          onSave={deleteItems}
          onClose={() => setOpen(false)}
          fullscreen={false}
          addFooter={true}
        />
      </div>
    </div>
  );
};

const DisableCell = ({
  isDayPreference,
  isMinutesPreference,
  totalHourDuration,
  totalDuration,
  hasLines,
}) => {
  const duration = useMemo(() => {
    if (isDayPreference) {
      return parseDays(totalDuration);
    } else if (isMinutesPreference) {
      return parseMinutes(totalDuration);
    }
    return parseHours(totalHourDuration);
  }, [isDayPreference, isMinutesPreference, totalDuration, totalHourDuration]);

  return (
    <div className={clsx(styles.disableCell, hasLines && styles.hasLines)}>
      {isDayPreference ? (
        <span>{formatDays(duration.d)}</span>
      ) : isMinutesPreference ? (
        <span>{formatMinutes(duration.m)}</span>
      ) : (
        <span>{formatHours(duration.h, duration.m)}</span>
      )}
    </div>
  );
};
