import React from "react";
import { Box } from "@axelor/ui";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { CircularProgress } from "@axelor/ui";

import { useSaveIndicator } from "../../hooks/useSaveIndicator";

const SaveStatus = () => {
  const { isLoading, isSaved, isError } = useSaveIndicator();

  const statusConfig = {
    loading: {
      show: isLoading,
      icon: <CircularProgress size={12} indeterminate={true} />,
      text: "Saving...",
      color: "inherit",
    },
    saved: {
      show: isSaved,
      icon: <MaterialIcon fontSize={16} icon="cloud_done" />,
      text: "Saved",
      color: "primary",
    },
    error: {
      show: isError,
      icon: <MaterialIcon fontSize={16} icon="cloud_off" />,
      text: "Error",
      color: "danger",
    },
  };

  const activeStatus = Object.values(statusConfig).find((s) => s.show);

  if (!activeStatus) return null;

  return (
    <Box d="flex" m="2" alignItems="center" gap={1}>
      <Box d="flex" gap={4} color={activeStatus.color} style={{ fontSize: 14 }}>
        {activeStatus.icon}
        {activeStatus.text}
      </Box>
    </Box>
  );
};

export default SaveStatus;
