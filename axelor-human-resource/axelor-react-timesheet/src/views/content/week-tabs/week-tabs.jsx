import { clsx } from "@axelor/ui";
import styles from "./week-tabs.module.css";
import { formatWeekLabel } from "../../../services/utils";
import WeekSummary from "../../../components/week-summary/week-summary";

export const WeekTabs = ({ items, activeId, onClick }) => {
  if (items.length < 2) {
    return null;
  }
  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        {items.map((item, i) => (
          <div
            key={i}
            className={clsx(styles.tabs, i === activeId && styles.active)}
            onClick={() => onClick(i)}
          >
            <div className={styles.weekLabel}>{formatWeekLabel(item)}</div>
            <WeekSummary week={item} active={i === activeId} />
          </div>
        ))}
      </div>
    </div>
  );
};

export default WeekTabs;
