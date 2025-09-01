import { BottomSheet } from "react-spring-bottom-sheet";
import { Button } from "@axelor/ui";
import Picker from "react-mobile-picker";
import "react-spring-bottom-sheet/dist/style.css";

import styles from "./mobile-time-picker.module.css";

export const MobileTimePicker = ({
  open,
  handleClose,
  value,
  handleChange,
  onSave,
  showCancel = true,
}) => {
  const timePickerOptions = {
    hour: Array.from({ length: 24 }, (_, i) => i.toString()),
    min: Array.from({ length: 12 }, (_, i) => (i * 5).toString()),
  };

  return (
    <BottomSheet
      open={open}
      onDismiss={handleClose}
      snapPoints={({ maxHeight }) => [maxHeight * 0.6]}
      header={
        <div className={styles.bottomSheetHeader}>
          {showCancel && (
            <Button className={styles.cancelBtn} onClick={handleClose}>
              Cancel
            </Button>
          )}
          <h3>Duration</h3>
        </div>
      }
    >
      <div className={styles.timePickerContainer}>
        <Picker
          value={value}
          onChange={handleChange}
          wheelMode="natural"
          height={160}
        >
          <Picker.Column name="hour">
            {timePickerOptions.hour.map((option) => (
              <Picker.Item
                key={option}
                value={option}
                className={styles.pickerItem}
              >
                {option.padStart(2, "0")}
              </Picker.Item>
            ))}
          </Picker.Column>

          <div className={styles.pickerSeparator}>h</div>

          <Picker.Column name="min">
            {timePickerOptions.min.map((option) => (
              <Picker.Item
                key={option}
                value={option}
                className={styles.pickerItem}
              >
                {option.padStart(2, "0")}
              </Picker.Item>
            ))}
          </Picker.Column>
        </Picker>
        <div className={styles.pickerActions}>
          <button className={styles.confirmBtn} onClick={onSave}>
            Confirm
          </button>
        </div>
      </div>
    </BottomSheet>
  );
};
