import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import Popover from "../popover/popover";
import { clsx } from "@axelor/ui";
import styles from "./action-button.module.css";

export const ActionButton = ({
  icon,
  onClick,
  className,
  title,
  placement = "end-bottom",
}) => {
  return (
    <Popover title={title} placement={placement}>
      <MaterialIcon
        onClick={onClick}
        icon={icon}
        className={clsx(styles.actionBtn, className)}
      />
    </Popover>
  );
};
