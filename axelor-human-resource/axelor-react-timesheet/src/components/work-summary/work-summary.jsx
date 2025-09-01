import { Box, clsx } from "@axelor/ui";

import {
  formatHours,
  formatDays,
  formatSigned,
  formatMinutes,
} from "../../services/utils";
import styles from "./work-summary.module.css";

export const WorkSummary = ({
  actual,
  leave,
  expect,
  diff,
  diffTime = 0,
  isDayPreference,
  isMinutesPreference,
}) => {
  const formatValue = (obj, totalValue = 0, signed = false) => {
    if (isDayPreference)
      return signed
        ? formatSigned(totalValue, formatDays(obj.d))
        : formatDays(obj.d);
    if (isMinutesPreference)
      return signed
        ? formatSigned(totalValue, formatMinutes(obj.m))
        : formatMinutes(obj.m);
    return signed
      ? formatSigned(totalValue, formatHours(obj.h, obj.m))
      : formatHours(obj.h, obj.m);
  };

  const formattedActual = formatValue(actual);
  const formattedLeave = formatValue(leave);
  const formattedExpect = formatValue(expect);
  const formattedDiff = formatValue(diff, diffTime, true);

  return (
    <div>
      <Box className={styles.workDescContainer}>
        <span>Work</span>
        <span>{formattedActual}</span>

        <span className={styles.positiveIndicator}>Leave</span>
        <span>{formattedLeave}</span>

        <span className={styles.NegativeIndicator}>Expected</span>
        <span>{formattedExpect}</span>

        <span className={clsx(styles.difference, styles.equalIndicator)}>
          Difference
        </span>
        <span className={styles.difference}>{formattedDiff}</span>
      </Box>
    </div>
  );
};
