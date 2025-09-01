import { useCallback, useState, useEffect } from "react";
import { Button, Input, InputLabel } from "@axelor/ui";
import { Select } from "../../../components/selection";
import styles from "./timesheet-form.module.css";
import {
  fetchActivity,
  fetchProject,
  fetchProjectTask,
  fetchTimesheetLineById,
  handleAddTSline,
} from "../../../services/api";
import DurationInput from "../../../components/duration-input/duration-input";
import { DatePickerComponent } from "../../../components/date-picker/date-picker";
import { datetoString, splitIntHourMin, toHour } from "../../../services/utils";
import { useConfigs, useEmployee, useTimesheet } from "../../../hooks/store";
import { toast } from "sonner";
import { INVOICING_TYPE, TIME_LOGGING_PREFERENCES } from "../../../constant";
import { useTSlines } from "../../../hooks/useTSlines";
import { useCounts } from "../../../hooks/useCounts";
import { useTsDispatch } from "../../../hooks/useTsDispatch";
import { updateCounts, updateTSlines } from "../../../context/TimesheetAction";

export const TimeSheetForm = ({ line = {}, date, onClose }) => {
  const tslines = useTSlines();
  const counts = useCounts();
  const dispatch = useTsDispatch();
  const [timesheet] = useTimesheet();
  const [employee] = useEmployee();
  const { configs } = useConfigs();

  const [tsline, setTSline] = useState({});
  const [duration, setDuration] = useState({ day: 0, hour: 0, min: 0 });

  const isDayPreference =
    timesheet.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.DAYS;

  const isMinutesPreference =
    timesheet?.timeLoggingPreferenceSelect === TIME_LOGGING_PREFERENCES.MINUTES;

  const showActivity = configs?.enableActivity || false;

  const handleChange = useCallback((name, value) => {
    setTSline((prev) => ({ ...prev, [name]: value }));
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();

    if (showActivity && tsline.toInvoice && !tsline.product) {
      return toast.error("Activity is Required");
    }

    const durationValue = isDayPreference
      ? duration.day
      : isMinutesPreference
      ? duration.min
      : toHour(duration);

    if (
      (isDayPreference && durationValue <= 0) ||
      (!isDayPreference && durationValue < 0.08)
    ) {
      toast.error(
        isDayPreference
          ? "Duration must be at least 1 day"
          : "Duration must be at least 5 minutes"
      );
      return;
    }

    const record = {
      projectId: tsline?.project?.id,
      projectTaskId: tsline?.projectTask?.id,
      product: tsline?.product?.id,
      timesheetId: timesheet.id,
      date: tsline.date,
      comments: tsline?.comments,
      toInvoice: showActivity ? tsline?.toInvoice : false,
      ...(isDayPreference || isMinutesPreference
        ? { duration: durationValue }
        : { hoursDuration: durationValue }),
    };

    try {
      const data = await handleAddTSline(record);
      const createdLineId = data.timesheetLineId;
      const createdTSLine = await fetchTimesheetLineById(createdLineId);

      const { date, duration, hoursDuration } = createdTSLine;
      dispatch(updateTSlines([...tslines, createdTSLine]));

      const updatedCounts = {
        ...counts,
        [date]: {
          ...counts?.[date],
          hoursDuration:
            Number(counts?.[date]?.hoursDuration || 0) + Number(hoursDuration),
          duration: Number(counts?.[date]?.duration || 0) + Number(duration),
        },
      };
      dispatch(updateCounts(updatedCounts));

      toast.success("Time sheet line created successfully");
    } catch (error) {
      console.error("Error while saving timesheet line:", error);
      toast.error("Failed to create time sheet line");
    } finally {
      onClose();
    }
  };

  useEffect(() => {
    setTSline({ ...(line || {}), date });

    if (isDayPreference) {
      setDuration({ day: line?.duration || 0, hour: 0, min: 0 });
    } else if (isMinutesPreference) {
      setDuration({ day: 0, hour: 0, min: line?.duration || 0 });
    } else {
      setDuration(splitIntHourMin(line?.hoursDuration));
    }
  }, [
    date,
    line.id,
    line?.hoursDuration,
    line?.duration,
    isDayPreference,
    isMinutesPreference,
  ]);

  useEffect(() => {
    if (!showActivity) return;

    if (tsline?.projectTask?.invoicingType === INVOICING_TYPE.TIME_SPENT) {
      setTSline((prev) => ({ ...prev, toInvoice: true }));
    }
  }, [tsline?.projectTask, showActivity]);

  return (
    <form className={styles.container} onSubmit={handleSave}>
      <div className={styles.wrapper}>
        <Select
          label="Project"
          className={styles.selection}
          value={tsline?.project}
          fetchOptions={() => fetchProject(employee)}
          optionLabel={(i) => i.fullName}
          optionKey={(i) => i.id}
          onChange={(value) => handleChange("project", value ?? undefined)}
        />
        <Select
          label="Task"
          className={styles.selection}
          value={tsline?.projectTask}
          optionLabel={(i) => i.fullName || i.name}
          fetchOptions={async () => await fetchProjectTask(tsline.project?.id)}
          optionKey={(i) => i.id}
          onChange={(value) => handleChange("projectTask", value)}
        />
        {showActivity && (
          <Select
            required={tsline?.toInvoice && !tsline.product}
            label="Activity"
            className={styles.selection}
            fetchOptions={fetchActivity}
            value={tsline?.product}
            optionLabel={(i) => i.fullName}
            optionKey={(i) => i.id}
            onChange={(value) => handleChange("product", value)}
          />
        )}
        <div className={styles.durationInputContainer}>
          <InputLabel className={styles.label}>Duration</InputLabel>
          <div className={styles.durationInputWrapper}>
            <DurationInput
              duration={duration}
              setDuration={setDuration}
              isDayPreference={isDayPreference}
              isMinutesPreference={isMinutesPreference}
            />
          </div>
        </div>

        <div>
          <InputLabel className={styles.label}>Description</InputLabel>
          <textarea
            value={tsline.comments || ""}
            className={styles.description}
            onChange={(e) => handleChange("comments", e.target.value)}
          />
          {showActivity && (
            <div className={styles.checkboxContainer}>
              <Input
                checked={tsline.toInvoice || false}
                name="toInvoice"
                type="checkbox"
                className={styles.checkbox}
                onChange={(e) => handleChange("toInvoice", e.target.checked)}
              />
              <InputLabel className={styles.label}>Invoice</InputLabel>
            </div>
          )}
        </div>

        <div className={styles.dateContainer}>
          <InputLabel className={styles.label}>Date</InputLabel>
          <div className={styles.dateWrapper}>
            <Input
              type="date"
              className={styles.dateField}
              value={tsline?.date || ""}
              readOnly={true}
            />
            <DatePickerComponent
              selectedDate={new Date(date)}
              onChange={(e) => handleChange("date", datetoString(e))}
              className={styles.calender}
              minDate={new Date(timesheet.fromDate)}
              maxDate={new Date(timesheet.toDate)}
            />
          </div>
        </div>
      </div>

      <div className={styles.actionBtn}>
        <Button type="submit" className={styles.saveBtn}>
          Save
        </Button>
        <Button className={styles.closeBtn} onClick={onClose}>
          Close
        </Button>
      </div>
    </form>
  );
};
