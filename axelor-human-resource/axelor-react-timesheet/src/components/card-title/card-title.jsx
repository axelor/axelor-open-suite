import styles from "./card-title.module.css";

export const CardTitle = ({ row }) => {
  return (
    <div className={styles.titleWrapper}>
      {row.isProject ? (
        <h4 className={styles.projectName}>{row.fullName}</h4>
      ) : (
        <div>
          <h4 className={styles.projectName}>
            {row.project?.fullName || row.label}
          </h4>
          <p className={styles.taskName}> {row?.fullName}</p>
        </div>
      )}
    </div>
  );
};
