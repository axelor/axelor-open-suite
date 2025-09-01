import { useState } from "react";
import { Box, Button, clsx } from "@axelor/ui";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";

import Popover from "../../components/popover/popover";
import TimeSheetSummary from "./timesheet-summary/timesheet-summary";
import styles from "./footer.module.css";

export default function Footer() {
  const [openSummary, setOpenSummary] = useState(false);
  const handleToogleFooter = () => setOpenSummary((prev) => !prev);

  return (
    <Box
      className={clsx(
        styles.footerContainer,
        openSummary && styles.openSummary
      )}
    >
      <Popover title={"Open/Close summary"} placement="top-start">
        <Button
          style={{
            transform: openSummary ? "rotate(180deg)" : "rotate(0deg)",
          }}
          className={styles.openSummaryBtn}
          onClick={handleToogleFooter}
        >
          <MaterialIcon icon="keyboard_arrow_down" />
        </Button>
      </Popover>
      <TimeSheetSummary isExpanded={openSummary} showTitle={true} />
    </Box>
  );
}
