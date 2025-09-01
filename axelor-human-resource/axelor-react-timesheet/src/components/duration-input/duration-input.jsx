import { useCallback, useEffect, useRef } from "react";
import styles from "./duration-input.module.css";

export function DurationInput({
  duration,
  setDuration,
  isDayPreference = false,
  isMinutesPreference = false,
}) {
  const inputRefs = useRef([]);

  const handleChange = useCallback(
    (e) => {
      let { value } = e.target;
      const { name } = e.target;

      if (isDayPreference || isMinutesPreference) {
        const regex = /^\d{0,3}(\.\d{0,2})?$/;

        if (!regex.test(value)) return;
        setDuration((prev) => ({ ...prev, [name]: value }));
      } else {
        let numValue = Number(value);
        if (isNaN(numValue)) return;

        if (numValue > 2) {
          inputRefs.current[1]?.focus();
        }
        if (name === "hour" && numValue > 24) {
          numValue = Number(numValue.toString().slice(-1));
        }
        if (name === "min" && numValue >= 60) {
          numValue = Number(numValue.toString().slice(-1));
        }
        setDuration((prev) => ({ ...prev, [name]: numValue }));
      }
    },
    [isDayPreference, isMinutesPreference, setDuration]
  );

  const handleCellKeyDown = useCallback(
    (e) => {
      if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown"].includes(e.key)) {
        e.preventDefault();
      }

      const focusedInput = e.target.name;

      if (e.key === "ArrowLeft" || e.key === "ArrowRight") {
        if (focusedInput === "hour" && e.key === "ArrowRight") {
          inputRefs.current[1]?.focus();
        } else if (focusedInput === "min" && e.key === "ArrowLeft") {
          inputRefs.current[0]?.focus();
        }
        return;
      }

      if (e.key === "ArrowUp" || e.key === "ArrowDown") {
        if (focusedInput === "hour") {
          const diff = e.key === "ArrowUp" ? 1 : -1;
          const newHour = Math.max(0, duration.hour + diff);
          const updatedValue = { ...duration, hour: newHour };
          setDuration(updatedValue);
        } else if (focusedInput === "min") {
          if (isMinutesPreference) {
            const diff = e.key === "ArrowUp" ? 1 : -1;
            const currentMin = parseFloat(duration.min);

            let newMin = Math.max(0, currentMin + diff);

            const updatedValue = { ...duration, hour: 0, min: newMin };

            setDuration(updatedValue);
          } else {
            const diff = e.key === "ArrowUp" ? 5 : -5;
            let newMin = duration.min + diff;
            let newHour = duration.hour;

            if (newMin >= 60) {
              newHour += Math.floor(newMin / 60);
              newMin = newMin % 60;
            } else if (newMin < 0 && newHour > 0) {
              newHour -= 1;
              newMin = 60 + newMin;
            } else if (newMin < 0) {
              newMin = 0;
            }

            const updatedValue = { ...duration, hour: newHour, min: newMin };
            setDuration(updatedValue);
          }
        } else if (focusedInput === "day") {
          const diff = e.key === "ArrowUp" ? 1 : -1;
          const newDay = Math.max(
            0,
            parseFloat((parseFloat(duration.day) || 0) + diff).toFixed(2)
          );
          const updatedValue = { ...duration, day: newDay };
          setDuration(updatedValue);
        }
      }
    },
    [duration, isMinutesPreference]
  );

  useEffect(() => {
    if (inputRefs.current?.[0]) {
      inputRefs.current[0].focus();
    }
  }, []);

  return (
    <>
      {/* TODO: Create a seperate component or use the  mobile input one*/}
      {isDayPreference ? (
        <>
          <input
            ref={(el) => {
              inputRefs.current[0] = el;
            }}
            name="day"
            className={styles.durationInput}
            style={{
              width: 50,
            }}
            value={duration.day}
            onChange={handleChange}
            onKeyDown={handleCellKeyDown}
            autoComplete="off"
          />
          <span>d</span>
        </>
      ) : isMinutesPreference ? (
        <>
          <input
            ref={(el) => {
              inputRefs.current[0] = el;
            }}
            name="min"
            className={styles.durationInput}
            style={{
              width: 50,
            }}
            value={duration.min}
            onChange={handleChange}
            onKeyDown={handleCellKeyDown}
            autoComplete="off"
          />
          <span>m</span>
        </>
      ) : (
        <>
          <input
            ref={(el) => {
              inputRefs.current[0] = el;
            }}
            name="hour"
            className={styles.durationInput}
            value={duration.hour}
            onKeyDown={handleCellKeyDown}
            onChange={handleChange}
            autoComplete="off"
          />
          <span>h</span>
          <input
            ref={(el) => {
              inputRefs.current[1] = el;
            }}
            name="min"
            className={styles.durationInput}
            value={duration.min}
            onChange={handleChange}
            onKeyDown={handleCellKeyDown}
            autoComplete="off"
          />
        </>
      )}
    </>
  );
}

export default DurationInput;
