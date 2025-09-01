import { memo } from "react";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { clsx } from "@axelor/ui";

import styles from "./arrow-button.module.css";

export const ArrowButton = memo(({ direction, onClick, className = "" }) => (
  <MaterialIcon
    icon={`keyboard_arrow_${direction}`}
    className={clsx(styles.arrowIcon, className)}
    onClick={onClick}
  />
));
