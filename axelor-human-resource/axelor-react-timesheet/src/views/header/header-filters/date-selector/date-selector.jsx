import { useCallback, useState } from "react";
import { clsx } from "@axelor/ui";
import { fetchTimesheets } from "../../../../services/api";
import { useEmployee, useTimesheet } from "../../../../hooks/store";
import styles from "./date-selector.module.css";
import {
  datetoString,
  getIncludedIntervals,
  getLeaveListInterval,
  getPublicHolidaysDate,
} from "../../../../services/utils";
import { useEffect } from "react";
import dayjs from "../../../../lib/dayjs";
import { DatePickerComponent } from "../../../../components/date-picker/date-picker";
import { TIMESHEET_FIELDS } from "../../../../constant";

export const DateSelector = ({ className }) => {
  const [employee, setEmployee] = useEmployee();
  const [, setTimesheet] = useTimesheet(null);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [publicHolidays, setPublicHolidays] = useState([]);
  const [interval, setInterval] = useState([]);
  const [excludedInterval, setExcludedInterval] = useState([]);
  const [year, setYear] = useState(dayjs().year());
  const openTodaysTimesheet = useCallback(async () => {
    setEmployee(employee);
  }, [employee]);

  const openTimesheet = useCallback(
    async (date) => {
      const res =
        (await fetchTimesheets({
          data: {
            _domain: `('${
              date.toISOString().split("T")[0]
            }' Between self.fromDate AND self.toDate) AND self.employee.id = ${
              employee.id
            } `,
          },
          fields: TIMESHEET_FIELDS,
          limit: 1,
          sortBy: ["-toDate"],
        })) || [];
      if (res?.data?.length) {
        setTimesheet(res.data[0]);
        setSelectedDate(date);
      } else {
        window.alert("No timesheet found for this date.");
      }
    },
    [employee]
  );

  const handleCalDateChange = async (date) => {
    const data = await getIncludedIntervals(date, employee);
    setInterval(data);
    setYear(dayjs(date).year());
  };

  useEffect(() => {
    (async () => {
      if (!employee) return;
      const data = await getIncludedIntervals(new Date(), employee);
      setInterval(data);
    })();
  }, [employee]);

  useEffect(() => {
    if (!employee) return;
    const fromDate = datetoString(dayjs().year(year).startOf("year"));
    const toDate = datetoString(dayjs().year(year).endOf("year"));
    (async () => {
      const dates = await getPublicHolidaysDate(employee, fromDate, toDate);
      setPublicHolidays(dates);
    })();
    (async () => {
      const dates = await getLeaveListInterval(employee, fromDate, toDate);
      setExcludedInterval(dates);
    })();
  }, [employee, year]);

  return (
    <div className={clsx(styles.container, className)}>
      <button className={styles.todayButton} onClick={openTodaysTimesheet}>
        Today
      </button>
      <DatePickerComponent
        selectedDate={selectedDate}
        onChange={openTimesheet}
        interval={interval}
        publicHolidays={publicHolidays}
        handleCalDateChange={handleCalDateChange}
        excludedInterval={excludedInterval}
      />
    </div>
  );
};
