import { clsx } from "@axelor/ui";
import styles from "./progress-bar.module.css";
import { formatDays, formatHours, formatMinutes } from "../../services/utils";
import useResponsive from "../../hooks/useResponsive";
import { MOBILE_VIEW } from "../../constant";

export const ProgressBar = ({
  actualHours = 7,
  actualMinutes = 0,
  expectedHours = 7,
  expectedMinutes = 0,
  actualDays = 1,
  expectedDays = 1,
  actualTotalMinutes = 1,
  expectedTotalMinutes = 420,
  isDayPreference = false,
  isMinutesPreference = false,
  initialWidth = 100,
}) => {
  const res = useResponsive();
  const isMobileView = MOBILE_VIEW.some((x) => res[x]);

  const actualTotal = isDayPreference
    ? actualDays
    : isMinutesPreference
    ? actualTotalMinutes
    : actualHours * 60 + actualMinutes;

  const expectedTotal = isDayPreference
    ? expectedDays
    : isMinutesPreference
    ? expectedTotalMinutes
    : expectedHours * 60 + expectedMinutes;

  if (!actualTotal && !expectedTotal) return null;

  const percent = Math.min((actualTotal / expectedTotal) * 100, 100);

  // TODO: Test and use "actualTotal" and "expectedDays" for isDayPreference
  const diff = isDayPreference
    ? actualDays - expectedDays
    : isMinutesPreference
    ? actualTotal - expectedTotal
    : (actualTotal - expectedTotal) / 60;

  const extraPoints = Math.min(Math.max(Math.floor(diff), 0), 3);

  const progressLabel = isDayPreference
    ? `${formatDays(actualDays)} / ${formatDays(expectedDays)}`
    : isMinutesPreference
    ? `${formatMinutes(actualTotal)} / ${formatMinutes(expectedTotal)}`
    : `${formatHours(actualHours, actualMinutes)} / ${formatHours(
        expectedHours,
        expectedMinutes
      )}`;

  if (isMobileView) {
    return (
      <div
        className={styles.mobileContainer}
        style={{ outlineWidth: `${extraPoints * 1.5}px` }}
      >
        <div
          className={styles.mobileWrapper}
          style={{ width: `${percent}%`, height: `${percent}%` }}
        />
      </div>
    );
  }

  return (
    <>
      <div className={styles.container} style={{ width: `${initialWidth}px` }}>
        <div className={clsx(styles.wrapper, styles.progressLine)}>
          <div className={styles.progress} style={{ width: `${percent}%` }} />
        </div>

        {extraPoints > 0 &&
          Array.from({ length: extraPoints }).map((_, i) => (
            <span key={i} className={styles.points} />
          ))}
      </div>

      <div className={styles.progressInfo}>{progressLabel}</div>
    </>
  );
};

export default ProgressBar;
