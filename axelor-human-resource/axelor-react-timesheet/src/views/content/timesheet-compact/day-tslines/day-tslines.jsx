import { useState, useCallback, useEffect, useMemo } from "react";
import { clsx } from "@axelor/ui";
import {
  formatHours,
  parseHours,
  getTsLines,
  isDailyLimitExceed,
  toHour,
  hasSameProject,
  hasSameProjectTask,
  getLoggingPreference,
} from "../../../../services/utils";
import { ActionButton } from "../../../../components/action-button/action-button";
import CommentAction from "../../comment-actions/comment-action";
import { useTimesheet } from "../../../../hooks/store";
import {
  editTimesheetLine,
  fetchTimesheetLines,
  removeDurations,
  updateTSDuration,
  updateToInvoice,
} from "../../../../services/api";
import { DialogBox } from "../../../../components/dialog-box/DialogBox";
import {
  TIME_LOGGING_PREFERENCES,
  TIMESHEET_LINE_FIELDS,
} from "../../../../constant";
import { CardTitle } from "../../../../components/card-title/card-title";
import { MobileTimePicker } from "../../../../components/mobile-time-picker/mobile-time-picker";
import StepperInput from "../../../../components/stepper-input/stepper-input";
import { ArrowButton } from "../../../../components/arrow-button/arrow-button";
import { useTSlines } from "../../../../hooks/useTSlines";
import { useCounts } from "../../../../hooks/useCounts";
import { useTsDispatch } from "../../../../hooks/useTsDispatch";
import {
  updateCounts,
  updateTSlines,
} from "../../../../context/TimesheetAction";
import { toast } from "sonner";
import { useSaveIndicator } from "../../../../hooks/useSaveIndicator";
import styles from "./day-tslines.module.css";

export const DayTSLines = ({ rows, date, timesheetLines }) => {
  return (
    <div className={styles.container}>
      <div className={styles.tslinesList}>
        {rows.map((row, index) => {
          const lines = getTsLines(timesheetLines, row, date);
          return <TSLineCard key={index} lines={lines} row={row} date={date} />;
        })}
      </div>
    </div>
  );
};

export const TSLineCard = ({ lines, row, date }) => {
  const [open, setOpen] = useState(false);
  const [timesheet] = useTimesheet();
  const [duration, setDuration] = useState({ hour: 0, min: 0, day: 0 });
  const [isInvoice, setIsInvoice] = useState(false);
  const [isActive, setIsActive] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [pickerValue, setPickerValue] = useState({
    hour: "0",
    min: "0",
  });
  const tslines = useTSlines();
  const counts = useCounts();
  const dispatch = useTsDispatch();

  const { startSaving, finishSaving, errorSaving } = useSaveIndicator();

  const hasMultipleLines = lines.length > 1;
  const isCompleted = timesheet?.isCompleted;

  const isDayPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

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

  const hasTimeSpent = useMemo(
    () => row.invoicingType === 1 && lines.find((i) => i?.product),
    [row, lines]
  );

  const updateTimeSheetLines = useCallback(
    async (date, total) => {
      const updatedTimesheetLinesRes = await fetchTimesheetLines({
        data: {
          _domain: `self.timesheet.id = ${
            timesheet.id
          } AND self.date = '${date}' AND self.project.id = ${
            (row?.isProject ? row.id : row.project?.id) ?? null
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
        [date]: { ...counts?.[date], [durationKey]: total },
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
    [row, timesheet, durationKey, duration, counts, tslines]
  );

  const handleUpdate = useCallback(
    async (valueToUpdate) => {
      startSaving();

      const oldValue = isDayPreference
        ? duration.day
        : isMinutesPreference
        ? duration.min
        : toHour(duration);
      const updatedValue =
        isDayPreference || isMinutesPreference
          ? valueToUpdate
          : // New Hour Duration
            valueToUpdate.hour + valueToUpdate.min / 60;
      const durationDiff = updatedValue - oldValue;

      try {
        const res = await updateTSDuration({
          timesheetId: timesheet.id,
          projectId: row.isProject ? row.id : row.project?.id,
          projectTaskId: row.isProject ? null : row.id,
          date,
          [durationKey]: updatedValue,
          toInvoice: row?.invoicingType === 1,
        });

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

        const newTotal =
          Number(counts?.[date]?.[durationKey] || 0) - oldValue + updatedValue;
        await updateTimeSheetLines(date, newTotal);

        if (isDayPreference) {
          setDuration({ ...duration, day: updatedValue });
        } else if (isMinutesPreference) {
          setDuration({ ...duration, min: updatedValue });
        } else {
          const actual = parseHours(updatedValue);

          setDuration({ hour: actual.h, min: actual.m, day: 0 });
        }
        finishSaving();
      } catch (err) {
        console.error(err);
        toast.error("Failed to update timesheet entry!");
        errorSaving();
      }
    },
    [
      isDayPreference,
      isMinutesPreference,
      duration,
      timesheet,
      row,
      date,
      durationKey,
      counts,
      planningDurationKey,
      updateTimeSheetLines,
      startSaving,
      finishSaving,
      errorSaving,
    ]
  );

  const handleCloseTimePicker = () => setShowTimePicker(false);

  const handleTimePickerConfirm = useCallback(async () => {
    const newHour = parseInt(pickerValue.hour) || 0;
    const newMin = parseInt(pickerValue.min) || 0;
    await handleUpdate({ hour: newHour, min: newMin });
    setShowTimePicker(false);
  }, [pickerValue, handleUpdate]);

  const handleInputChange = useCallback(
    async (value) => {
      const val = Number(value) || 0;
      await handleUpdate(val);
    },
    [handleUpdate]
  );

  const handleDurationClick = useCallback(
    (e) => {
      e.stopPropagation();
      if (isCompleted) return;
      setPickerValue({
        hour: duration.hour.toString(),
        min: duration.min.toString().padStart(2, "0"),
      });
      setShowTimePicker(true);
    },
    [duration, isCompleted]
  );

  const editTimesheet = useCallback(
    async (e) => {
      e.stopPropagation();
      await editTimesheetLine({
        timesheetId: timesheet?.id,
        date,
        projectId: row.project?.id,
        projectTaskId: row.projectTask?.id,
      });
    },
    [row, date, timesheet]
  );

  const handleUpdateToInvoice = useCallback(
    async (e) => {
      e.stopPropagation();
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
        res.count === 1
          ? "Successfully updated."
          : `${res.count} lines updated successfully.`
      );
    },
    [date, timesheet?.id, row, isInvoice]
  );

  const deleteItems = useCallback(async () => {
    setOpen(false);

    const res = await removeDurations({
      date,
      timesheetId: timesheet?.id,
      projectId: row?.isProject ? row.id : row.project?.id,
      projectTaskId: row?.isProject ? null : row.id,
    });

    if (res.codeStatus !== 200) {
      return toast.error("Failed to delete.");
    }

    const oldValue = isDayPreference
      ? duration.day
      : isMinutesPreference
      ? duration.min
      : toHour(duration);
    const currentCount = counts?.[date]?.[durationKey] || 0;
    const newTotal = Number(currentCount) - Number(oldValue);

    await updateTimeSheetLines(date, newTotal);
    setDuration({ hour: 0, min: 0, day: 0 });
    setIsActive(false);
    toast.success("Successfully deleted!");
  }, [
    date,
    row,
    timesheet,
    isDayPreference,
    isMinutesPreference,
    duration,
    counts,
    durationKey,
    updateTimeSheetLines,
  ]);

  useEffect(() => {
    if (lines?.length) {
      if (isDayPreference || isMinutesPreference) {
        const totalDuration = lines.reduce(
          (acc, curr) => acc + Number(curr.duration || 0),
          0
        );
        setDuration(
          isDayPreference
            ? { day: totalDuration, hour: 0, min: 0 }
            : { day: 0, hour: 0, min: totalDuration }
        );
      } else {
        const totalHourDuration = lines.reduce(
          (acc, curr) => acc + Number(curr.hoursDuration || 0),
          0
        );
        const actual = parseHours(totalHourDuration);
        setDuration({ hour: actual.h, min: actual.m, day: 0 });
      }
      setIsInvoice(lines.every((i) => (i.product ? i.toInvoice : true)));
    }
  }, [lines, isDayPreference, isMinutesPreference]);

  return (
    <div
      className={clsx(
        styles.tslineCard,
        isActive && styles.active,
        hasMultipleLines && styles.multiLineIndicator
      )}
    >
      <div className={styles.cardHeader}>
        <div className={styles.projectInfo}>
          <CardTitle row={row} />
          <ArrowButton
            direction="down"
            onClick={() => setIsActive(!isActive && !isCompleted)}
            className={clsx(
              styles.dropdownArrow,
              isActive && styles.activeArrow
            )}
          />
        </div>
      </div>
      <div className={styles.seperator}></div>
      <div
        className={styles.duration}
        onClick={(e) => {
          if (!(isDayPreference || isMinutesPreference)) {
            handleDurationClick(e);
          }
        }}
      >
        {isDayPreference ? (
          <StepperInput
            value={duration.day}
            onChange={handleInputChange}
            name="day"
            displayValue="d"
          />
        ) : isMinutesPreference ? (
          <StepperInput
            value={duration.min}
            onChange={handleInputChange}
            name="min"
            displayValue="m"
          />
        ) : (
          formatHours(duration.hour, duration.min)
        )}
      </div>

      {lines?.length > 0 && (
        <div className={clsx(styles.actionWrapper)}>
          {hasTimeSpent && (
            <ActionButton
              title={isInvoice ? 'Disable "To Invoice"' : 'Enable "To Invoice"'}
              icon="article"
              onClick={handleUpdateToInvoice}
              placement="top"
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
            placement="top"
          />
          <CommentAction
            tslines={lines}
            popoverPlacement="top"
            loggingPreference={loggingPreference}
          />
          <ActionButton
            title="Delete Items"
            icon="delete"
            onClick={() => setOpen(true)}
            className={styles.deleteBtn}
            placement="top"
          />
        </div>
      )}

      {showTimePicker && (
        <MobileTimePicker
          open={showTimePicker}
          handleClose={handleCloseTimePicker}
          value={pickerValue}
          handleChange={setPickerValue}
          onSave={handleTimePickerConfirm}
        />
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
  );
};
