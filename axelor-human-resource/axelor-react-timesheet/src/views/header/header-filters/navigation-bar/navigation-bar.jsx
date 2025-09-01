import { useMemo } from "react";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { Button } from "@axelor/ui";

import styles from "./navigation.module.css";
import { useStore } from "../../../../hooks/store";
import { fetchTimesheetOptions } from "../../../../services/api.js";

export const NavigationBar = () => {
  const { state, setState } = useStore();
  const { page, records, timesheet, employee } = state;

  const isNextDisable = useMemo(() => {
    return (
      page?.limit + page?.offset >= page?.total &&
      records?.findIndex((r) => r.id === timesheet?.id) === records?.length - 1
    );
  }, [page, records?.length, timesheet?.id]);

  const isPreviousDisable = useMemo(
    () =>
      !timesheet?.id || records?.findIndex((r) => r.id === timesheet?.id) === 0,
    [records, timesheet]
  );

  const onPrevious = () => {
    const index = records?.findIndex((r) => r.id === timesheet?.id);
    if (records[index - 1]) {
      setState((prev) => ({
        ...prev,
        timesheet: prev.records[index - 1],
      }));
    }
  };

  const onNext = async () => {
    const index = records?.findIndex((r) => r.id === timesheet?.id);
    if (!records?.[index + 1] || !records?.length) {
      const res = await fetchTimesheetOptions(employee, {
        ...(page || {}),
        offset: (page?.offset || 0) + (page?.limit || 0),
        limit: 10,
      });
      if (!res?.data) return;

      const updatedRecords = res.data.some((i) => i.id === timesheet.id)
        ? [...records.filter((i) => i.id !== timesheet.id), ...res.data]
        : records;

      setState((prev) => ({
        ...prev,
        records: updatedRecords,
        page: {
          offset: res.offset,
          total: res.total,
          limit: 10,
        },
        timesheet: res?.data?.[0],
      }));
    } else {
      setState((prev) => ({
        ...prev,
        timesheet: prev.records[index + 1],
      }));
    }
  };

  return (
    <div className={styles.container}>
      <Button
        title="Previous timesheet"
        onClick={onPrevious}
        disabled={isPreviousDisable}
      >
        <MaterialIcon icon="keyboard_arrow_left" />
      </Button>
      <Button title="Next timesheet" onClick={onNext} disabled={isNextDisable}>
        <MaterialIcon icon="keyboard_arrow_right" />
      </Button>
    </div>
  );
};
