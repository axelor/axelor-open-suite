import ProgressBar from "../../../components/progress-bar/progress-bar";
import { clsx } from "@axelor/ui";
import styles from "./day-header-card.module.css";
import { getDayNameAndNumber } from "../../../services/utils";
import Popover from "../../../components/popover/popover";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import useResponsive from "../../../hooks/useResponsive";
import { DESKTOP_VIEW } from "../../../constant";

export function DayHeaderCard({
  status = {},
  date,
  actualHours = 7,
  actualMinutes = 0,
  expectedHours = 7,
  expectedMinutes = 0,
  isDayPreference = false,
  expectedDays = 1,
  actualDays = 1,
  isMinutesPreference,
  actualTotalMinutes = 1,
  expectedTotalMinutes = 420,
}) {
  const isToday = new Date(date).toDateString() === new Date().toDateString();
  const res = useResponsive();
  const isDesktopView = DESKTOP_VIEW.some((x) => res[x]);

  const [day, num] = getDayNameAndNumber(date, { compactView: !isDesktopView });

  return (
    <div className={styles.container}>
      <div
        className={clsx(
          styles.dayHeaderCard,
          status.isWeekend && isDesktopView && styles.dayHeaderCardWeekend,
          status.disable && styles.dayHeaderCardDisable
        )}
      >
        <div
          className={clsx(
            styles.dayHeaderTitle,
            (status?.holiday || status?.leave) && styles.holiday
          )}
        >
          <Popover title={status.message}>
            <div className={styles.headerTitle}>
              <span className={clsx(isToday && styles.currentDate)}>
                {day} {num}
              </span>
              {status.outsidePeriod && (
                <MaterialIcon icon="warning" className={styles.headerWarning} />
              )}
            </div>
          </Popover>
        </div>
        {!status.disable && (
          <ProgressBar
            actualHours={actualHours}
            actualMinutes={actualMinutes}
            expectedHours={expectedHours}
            expectedMinutes={expectedMinutes}
            initialWidth={!isDesktopView ? 80 : 100}
            isDayPreference={isDayPreference}
            {...(isDayPreference && { expectedDays })}
            {...(isDayPreference && { actualDays })}
            isMinutesPreference={isMinutesPreference}
            {...(isMinutesPreference && { expectedTotalMinutes })}
            {...(isMinutesPreference && { actualTotalMinutes })}
          />
        )}
      </div>
    </div>
  );
}
