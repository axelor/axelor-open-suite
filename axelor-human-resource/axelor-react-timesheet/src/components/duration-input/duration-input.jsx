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

  const handleHourKeyDown = useCallback((e) => {
    if (e.code === "Space") inputRefs.current[1]?.focus();
  }, []);

  const handleMinKeyDown = useCallback(
    (e) => {
      if (e.code === "Backspace" && duration.min == 0) {
        e.preventDefault();
        inputRefs.current[0]?.focus();
      }
    },
    [duration.min]
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
            onKeyDown={handleHourKeyDown}
            onChange={handleChange}
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
            onKeyDown={handleMinKeyDown}
          />
        </>
      )}
    </>
  );
}

export default DurationInput;
