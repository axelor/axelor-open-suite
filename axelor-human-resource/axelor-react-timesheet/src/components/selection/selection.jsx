import {
  forwardRef,
  useCallback,
  useEffect,
  useMemo,
  useState,
  useRef,
} from "react";

import { clsx, Box, Select as AxSelect, useRefs, InputLabel } from "@axelor/ui";

import styles from "./selection.module.scss";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";

const EMPTY = [];

export const Select = forwardRef(function Select(props, ref) {
  const {
    label,
    autoFocus,
    readOnly,
    className,
    options,
    fetchOptions,
    onShowCreate,
    onShowSelect,
    onShowCreateAndSelect,
    onInputChange,
    onOpen,
    canSelect = true,
    openOnFocus = true,
    value = null,
    onChange,
    menuOptions,
    required,
    ...selectProps
  } = props;

  const [items, setItems] = useState([]);
  const [inputValue, setInputValue] = useState("");

  const [ready, setReady] = useState(!fetchOptions);
  const selectRef = useRefs(ref);
  const loadTimerRef = useRef();

  const loadOptions = useCallback(
    (inputValue) => {
      if (loadTimerRef.current) {
        clearTimeout(loadTimerRef.current);
      }

      loadTimerRef.current = setTimeout(
        async () => {
          if (fetchOptions) {
            const items = await fetchOptions(inputValue);
            loadTimerRef.current = undefined;
            setItems(items || []);
            setReady(true);
          }
        },
        inputValue ? 300 : 0
      );
    },
    [fetchOptions]
  );

  const handleOpen = useCallback(() => {
    if (onOpen) onOpen();
    if (fetchOptions && !inputValue && !loadTimerRef.current) {
      loadOptions("");
    }
  }, [fetchOptions, inputValue, loadOptions, onOpen]);

  const handleClose = useCallback(() => {
    setReady(false);
  }, []);

  const handleInputChange = useCallback(
    (text) => {
      setInputValue(text);
      if (onInputChange) onInputChange(text);
      if (fetchOptions) {
        if (text) {
          loadOptions(text);
        }
      }
    },
    [fetchOptions, loadOptions, onInputChange]
  );

  const handleChange = useCallback(
    (value) => {
      onChange?.(value);
    },
    [onChange]
  );

  useEffect(() => {
    clearTimeout(loadTimerRef.current);
  }, []);

  useEffect(() => {
    if (autoFocus && selectRef.current) {
      selectRef.current.focus();
    }
  }, [autoFocus, selectRef]);

  const currOptions = fetchOptions ? (ready ? items : EMPTY) : options;
  const hasOptions = currOptions.length > 0;

  const customOptions = useMemo(() => {
    const options = [];

    if (onShowSelect && hasOptions) {
      options.push({
        key: "select",
        title: (
          <Box d="flex" gap={6} alignItems="center">
            <MaterialIcon icon="search" className={styles.icon} />
            <em>{"Search more..."}</em>
          </Box>
        ),
        onClick: () => onShowSelect(inputValue),
      });
    } else if (selectProps.canShowNoResultOption && !hasOptions) {
      options.push({
        key: "no-result",
        title: (
          <Box d="flex" gap={6} alignItems="center">
            <em>{"No results found"}</em>
          </Box>
        ),
      });
    }

    const canShowCreateWithInput =
      selectProps.canCreateOnTheFly && onShowCreate && inputValue;
    const canShowCreateWithInputAndSelect =
      selectProps.canCreateOnTheFly && onShowCreateAndSelect && inputValue;

    if (canShowCreateWithInput) {
      options.push({
        key: "create-with-input",
        title: (
          <Box d="flex" gap={6} alignItems="center">
            <MaterialIcon icon="add" className={styles.icon} />
            <em>{(`Create "{0}"...`, inputValue)}</em>
          </Box>
        ),
        onClick: () => onShowCreate(inputValue),
      });
    } else if (onShowCreate) {
      options.push({
        key: "create",
        title: (
          <Box d="flex" gap={6} alignItems="center">
            <MaterialIcon icon="add" className={styles.icon} />
            <em>{"Create..."}</em>
          </Box>
        ),
        onClick: () => onShowCreate(""),
      });
    }

    if (canShowCreateWithInputAndSelect) {
      options.push({
        key: "create-and-select",
        title: (
          <Box d="flex" gap={6} alignItems="center">
            <MaterialIcon icon="add_task" className={styles.icon} />
            <em>{(`Create "{0}" and select...`, inputValue)}</em>
          </Box>
        ),
        onClick: () => onShowCreateAndSelect(inputValue),
      });
    }

    return options;
  }, [
    hasOptions,
    inputValue,
    selectProps.canCreateOnTheFly,
    onShowCreate,
    onShowCreateAndSelect,
    onShowSelect,
    selectProps.canShowNoResultOption,
  ]);

  return (
    <div className={styles.selectionContainer}>
      {label && <InputLabel className={styles.label}>{label}</InputLabel>}
      <AxSelect
        key={autoFocus ? "focused" : "normal"}
        clearOnBlur
        clearOnEscape
        {...selectProps}
        ref={selectRef}
        value={value}
        autoFocus={autoFocus}
        readOnly={readOnly || !canSelect}
        openOnFocus={openOnFocus}
        options={currOptions}
        customOptions={ready ? customOptions : EMPTY}
        onInputChange={handleInputChange}
        onOpen={handleOpen}
        onChange={handleChange}
        className={clsx(className, {
          [styles.readonly]: readOnly,
          [styles.required]: required,
        })}
        menuOptions={{
          maxWidth: 600,
          ...menuOptions,
        }}
        {...(fetchOptions && {
          onClose: handleClose,
        })}
      />
    </div>
  );
});
