import { Badge, Box } from "@axelor/ui";

import { HeaderFilters } from "./header-filters";
import { HeaderTitle } from "./header-title";
import { capitalizeWord } from "@axelor/ui/grid";
import { useTimesheet } from "../../hooks/store";

import "./index.css";

export default function Header() {
  const [timesheet] = useTimesheet();

  const { timeLoggingPreferenceSelect = "" } = timesheet || {};

  return (
    <Box className="header-replica-container">
      <HeaderTitle />
      <Box d="flex" justifyContent="space-between" flexWrap="wrap">
        <HeaderFilters />

        {timeLoggingPreferenceSelect && (
          <Box className="time-preference-container" p={2}>
            {capitalizeWord(timeLoggingPreferenceSelect)}
          </Box>
        )}
      </Box>
    </Box>
  );
}
