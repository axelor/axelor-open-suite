import DatePicker from "react-datepicker";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { getDayClassName } from "../../services/utils";

import "react-datepicker/dist/react-datepicker.css";
import styles from "./date-picker.module.css";
import "./date-picker.css";

export const DatePickerComponent = ({
  className,
  selectedDate,
  onChange,
  interval,
  excludedInterval,
  publicHolidays,
  minDate,
  maxDate,
  handleCalDateChange = () => {},
}) => {
  return (
    <div className={className}>
      <DatePicker
        selected={selectedDate}
        onChange={onChange}
        calendarClassName="custom-datepicker"
        includeDateIntervals={interval}
        excludeDateIntervals={excludedInterval}
        excludeDates={publicHolidays}
        showMonthDropdown
        showYearDropdown
        showTwoColumnMonthYearPicker
        dropdownMode="select"
        dayClassName={getDayClassName}
        popperPlacement="bottom-end"
        customInput={
          <div className={styles.datePickerWrapper}>
            <MaterialIcon icon="today" className={styles.calendarIcon} />
          </div>
        }
        onMonthChange={handleCalDateChange}
        onYearChange={handleCalDateChange}
        minDate={minDate}
        maxDate={maxDate}
      />
    </div>
  );
};
