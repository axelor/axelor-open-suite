import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { useEmployee, useTimesheet, useUser } from "../../../../hooks/store";
import TimeSheetSummary from "../../../footer/timesheet-summary/timesheet-summary";
import dayjs from "../../../../lib/dayjs";
import Badge from "../../../../components/badge/badge";
import { DATE_FORMATS } from "../../../../constant";
import styles from "./timesheet-drawer.module.css";

export const TimesheetDrawer = ({ handleBack, handleComplete }) => {
  const user = useUser();
  const [employee] = useEmployee();
  const [timesheet] = useTimesheet();

  return (
    <div className={styles.drawerContainer}>
      <div className={styles.drawerHeader}>
        <button className={styles.backBtn} onClick={handleBack}>
          <MaterialIcon icon="keyboard_arrow_left" />
          <span>Back</span>
        </button>
        <span>Timesheet </span>
      </div>
      <div className={styles.drawerContent}>
        <div className={styles.dateRange}>
          <span>
            {dayjs(timesheet.fromDate, DATE_FORMATS.YYYY_MM_DD).format(
              "DD/MM/YYYY"
            )}
          </span>
          <span>-</span>
          <span>
            {dayjs(timesheet.toDate, DATE_FORMATS.YYYY_MM_DD).format(
              "DD/MM/YYYY"
            )}
          </span>
        </div>
        <div className={styles.badgeWrapper}>
          {timesheet?.isCompleted ? (
            <Badge
              variant={"success"}
              text={"Completed"}
              className={styles.inProgressLabel}
            />
          ) : (
            <Badge
              variant={"success"}
              text={"In Progress"}
              className={styles.inProgressLabel}
            />
          )}
        </div>
        <TimeSheetSummary isExpanded={true} />
      </div>
      <div className={styles.buttonContainer}>
        {employee?.managerUser?.id === user?.id && !timesheet?.isCompleted && (
          <button className={styles.markCompleteBtn} onClick={handleComplete}>
            <MaterialIcon icon={"arrow_forward"} />
            <span>Complete</span>
          </button>
        )}
      </div>
    </div>
  );
};
