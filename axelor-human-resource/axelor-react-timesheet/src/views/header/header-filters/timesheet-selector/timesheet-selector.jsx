import { useState, useEffect } from "react";
import { Box } from "@axelor/ui";
import Select from "react-select";
// eslint-disable-next-line no-unused-vars
import { components } from "react-select";

import Badge from "../../../../components/badge/badge.jsx";
import { getSelectionLabel, groupByYear } from "../../../../services/utils.js";
import { useTimesheet, useStore } from "../../../../hooks/store";

import "./timesheet-selector.css";

const formatGroupLabel = (data) => (
  <div className={"groupStyles"}>
    <span>{data.label}</span>
    <span className={"groupBadgeStyles"}>{data.options.length}</span>
  </div>
);

const CustomSingleValue = ({ ...props }) => {
  const { data } = props;
  return (
    <components.SingleValue {...props} className="selected-value">
      <span>{data.label}</span>
      <Badge
        className={"badge"}
        text={data.isCompleted ? "Completed" : "Pending"}
        variant={data.isCompleted ? "success" : "pending"}
      />
    </components.SingleValue>
  );
};

const CustomOption = (props) => {
  const isCompleted = props.data?.isCompleted || false;
  return (
    <components.Option {...props}>
      <div className={"optionList"}>
        <div> {props.children}</div>
        <Badge
          text={isCompleted ? "Completed" : "Pending"}
          variant={isCompleted ? "success" : "pending"}
        />
      </div>
    </components.Option>
  );
};
export const TimesheetSelector = () => {
  const [selectedOption, setSelectedOption] = useState(null);
  const [timesheet, setTimesheet] = useTimesheet();
  const [options, setOptions] = useState([]);
  const { state } = useStore();

  const loadOptions = () => {
    if (state.records) {
      const options = groupByYear(state.records);
      setOptions(options);
      return;
    }
  };

  const handleChange = (selectedOption) => {
    setSelectedOption(selectedOption);
    setTimesheet(selectedOption);
  };

  useEffect(() => {
    if (timesheet) {
      const label = getSelectionLabel(timesheet.fromDate, timesheet.toDate);
      setSelectedOption({ ...timesheet, label, value: timesheet?.id });
    } else {
      setSelectedOption(null);
    }
  }, [timesheet]);

  return (
    <Box className="selector-container">
      <Select
        cacheOptions
        onMenuOpen={loadOptions}
        options={options}
        onChange={handleChange}
        value={selectedOption}
        formatGroupLabel={formatGroupLabel}
        placeholder="Search timesheets..."
        components={{ Option: CustomOption, SingleValue: CustomSingleValue }}
        className="timesheet-selector"
        classNamePrefix="react-select"
      />
    </Box>
  );
};

export default TimesheetSelector;
