import { Box, Button } from "@axelor/ui";
import { capitalizeWord } from "@axelor/ui/grid";
import { useState } from "react";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";

import { HeaderFilters } from "./header-filters";
import SaveStatus from "../../components/save-status/save-status";
import { TimeSheetForm } from "../content/timesheet-form/timesheet-form";
import { DialogBox } from "../../components/dialog-box/DialogBox";
import { useTimesheet } from "../../hooks/store";
import { MOBILE_VIEW } from "../../constant";
import useResponsive from "../../hooks/useResponsive";

import "./index.css";

export default function Header() {
  const [timesheet] = useTimesheet();
  const [openForm, setOpenForm] = useState(false);

  const res = useResponsive();

  const { timeLoggingPreferenceSelect = "" } = timesheet || {};

  const isMobileView = MOBILE_VIEW.some((x) => res[x]);

  const handleFormClose = () => setOpenForm(false);

  return (
    <Box className="header-replica-container">
      <Box d="flex" gap="16" justifyContent="space-between" flexWrap="wrap">
        <Box d="flex" gap="16" flexWrap="wrap">
          <HeaderFilters />

          {!timesheet?.isCompleted && !isMobileView && (
            <Button className="add-btn" onClick={() => setOpenForm(true)}>
              <MaterialIcon icon="add" />
            </Button>
          )}

          {openForm && (
            <DialogBox
              open={openForm}
              fullscreen={false}
              onClose={handleFormClose}
              title="Add Time sheet Entry"
            >
              <TimeSheetForm
                date={timesheet.fromDate}
                onClose={handleFormClose}
              />
            </DialogBox>
          )}
        </Box>
        <Box d="flex" gap={4}>
          <SaveStatus />

          {timeLoggingPreferenceSelect && (
            <Box className="time-preference-container" p={2}>
              {capitalizeWord(timeLoggingPreferenceSelect)}
            </Box>
          )}
        </Box>
      </Box>
    </Box>
  );
}
