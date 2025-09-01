import React, { useState, useEffect } from "react";
import { clsx } from "@axelor/ui";

import { ArrowButton } from "../arrow-button/arrow-button";
import { useDebounce } from "../../hooks/useDebounce";
import styles from "./stepper-input.module.css";

export function StepperInput({ value, onChange, name, displayValue }) {
  const [inputValue, setInputValue] = useState((value ?? 0).toString());
  const [isEditing, setIsEditing] = useState(false);

  const debouncedOnChange = useDebounce((newVal) => {
    onChange?.(parseFloat(newVal));
  }, 500);

  const handleInputChange = (e) => {
    const value = e.target.value;
    const isValid = /^\d{0,3}(\.\d{0,2})?$/.test(value);

    if (isValid) {
      setInputValue(value);
      if (value !== "" && value !== ".") {
        debouncedOnChange(value);
      }
    }
  };

  const handleChange = (value) => {
    const currentNum = parseFloat(inputValue) || 0;
    const newValue = Math.round((currentNum + value) * 100) / 100;
    const finalValue = Math.max(0, newValue).toString();

    setInputValue(finalValue);
    debouncedOnChange(finalValue);
  };

  const handleIncrement = () => handleChange(1);
  const handleDecrement = () => handleChange(-1);

  const enableEdit = () => {
    setIsEditing((prev) => !prev);
  };

  useEffect(() => {
    setInputValue((value ?? 0).toString());
  }, [value]);

  return (
    <div className={styles.container} onClick={enableEdit}>
      <div className={styles.content}>
        {isEditing && (
          <ArrowButton
            direction="up"
            onClick={(e) => {
              e.stopPropagation();
              handleIncrement();
            }}
            className={styles.arrowButton}
          />
        )}

        <div
          className={clsx(
            styles.inputWrapper,
            isEditing ? styles.activeInputWrapper : styles.inactiveInputWrapper
          )}
          onClick={() => {
            isEditing && enableEdit();
          }}
        >
          <input
            name={name}
            className={styles.durationInput}
            value={inputValue}
            onChange={handleInputChange}
            autoComplete="off"
            readOnly={!isEditing}
            tabIndex={isEditing ? 0 : -1}
          />
          <span>{displayValue}</span>
        </div>

        {isEditing && (
          <ArrowButton
            direction="down"
            onClick={(e) => {
              e.stopPropagation();
              handleDecrement();
            }}
            className={styles.arrowButton}
          />
        )}
      </div>
    </div>
  );
}

export default StepperInput;
