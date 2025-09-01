import { useState } from "react";
import { clsx, Drawer } from "@axelor/ui";
import { TimesheetSelector } from "./timesheet-selector";
import { UserSelector } from "./employee-selector";
import { NavigationBar } from "./navigation-bar";
import { DateSelector } from "./date-selector";
import { useEmployee, useTimesheet, useUser } from "../../../hooks/store";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import Service from "../../../services/index";
import { TIMESHEET_MODEL } from "../../../services/api";
import { toast } from "sonner";
import useResponsive from "../../../hooks/useResponsive";
import { MOBILE_VIEW } from "../../../constant";
import { TimesheetDrawer } from "./timesheet-drawer/timesheet-drawer";
import Badge from "../../../components/badge/badge";
import styles from "./index.module.css";

export function HeaderFilters() {
  const user = useUser();
  const [employee] = useEmployee();
  const [timesheet, setTimesheet] = useTimesheet();
  const res = useResponsive();
  const [open, setOpen] = useState(false);

  const isMobileView = MOBILE_VIEW.some((x) => res[x]);
  const isCompleted = timesheet?.isCompleted;
  const canMarkComplete =
    employee && !isCompleted && employee?.managerUser?.id === user?.id;

  const handleMarkAsComplete = async () => {
    const res = await Service.action({
      action: "action-timesheet-method-complete",
      model: TIMESHEET_MODEL,
      data: {
        context: {
          user_id: user?.id,
          id: timesheet.id,
          employee,
        },
      },
    });
    if (res.data[0].reload) {
      setTimesheet({ ...timesheet, isCompleted: true });
      return toast.success("Marked as completed");
    } else {
      return toast.error(res?.data[0]?.info?.message);
    }
  };

  return (
    <div className={styles.headerContainer}>
      <div className={styles.headerUserSelection}>
        <UserSelector />
      </div>
      <div className={styles.headerFilters}>
        <TimesheetSelector />
        <NavigationBar />
        <DateSelector />
      </div>
      <div>
        <div className={styles.controlsWrapper}>
          {!isMobileView && canMarkComplete && (
            <button
              className={styles.markCompleteBtn}
              onClick={handleMarkAsComplete}
            >
              <MaterialIcon icon={"arrow_forward"} />
              <span className={styles.btnLabel}> Complete</span>
            </button>
          )}

          {!isMobileView && isCompleted && (
            <Badge
              text="Completed"
              variant={"success"}
              className={styles.completedBadge}
            />
          )}
          {isMobileView && (
            <>
              <button
                className={clsx(
                  styles.sendBtn,
                  isCompleted && styles.completeIndicator,
                  canMarkComplete && styles.markCompleteIndicator
                )}
                onClick={() => setOpen((prev) => !prev)}
              >
                <MaterialIcon icon={"send"} />
              </button>
              <Drawer open={open} onClose={() => setOpen(false)}>
                <TimesheetDrawer
                  handleBack={() => setOpen(false)}
                  handleComplete={handleMarkAsComplete}
                />
              </Drawer>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
