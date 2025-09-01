import { clsx } from "@axelor/ui";
import styles from "./badge.module.css";

// variant = "success" | "pending" | "error" | "default";

export const Badge = ({ variant, text, className }) => {
  return (
    <div className={clsx(styles.badge, styles[variant], className)}>
      <div className={styles.badgeIndicator}></div>
      <span>{text}</span>
    </div>
  );
};

export default Badge;
