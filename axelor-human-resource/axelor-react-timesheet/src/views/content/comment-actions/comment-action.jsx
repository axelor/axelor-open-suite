import { useState, useEffect, useCallback, useMemo } from "react";
import { Box, Button, clsx } from "@axelor/ui";
import { ActionButton } from "../../../components/action-button/action-button";
import { DialogBox } from "../../../components/dialog-box/DialogBox";
import { Select } from "../../../components/selection/selection";
import { getTSSelectionOptions } from "../../../services/utils";
import { updateTimesheetLine } from "../../../services/api";
import { toast } from "sonner";
import { useTSlines } from "../../../hooks/useTSlines";
import { updateTSlines } from "../../../context/TimesheetAction";
import { useTsDispatch } from "../../../hooks/useTsDispatch";
import styles from "./comment-action.module.css";
import { TIME_LOGGING_PREFERENCES } from "../../../constant";

const CommentAction = ({
  tslines = [],
  popoverPlacement = "end-bottom",
  loggingPreference = TIME_LOGGING_PREFERENCES.HOURS,
}) => {
  const lines = useTSlines();
  const dispatch = useTsDispatch();
  const [open, setOpen] = useState(false);
  const [selectedTS, setSelectedTS] = useState(null);
  const [comments, setComments] = useState("");

  const isMultiline = tslines.length > 1;

  const commentCount = useMemo(
    () =>
      tslines.reduce((acc, curr) => acc + (curr.comments?.trim() ? 1 : 0), 0),
    [tslines]
  );

  const options = useMemo(
    () => getTSSelectionOptions(tslines, loggingPreference),
    [tslines, loggingPreference]
  );

  const getIndicatorClass = () => {
    return commentCount === tslines.length
      ? styles.commentIndicator
      : styles.pendingIndicator;
  };

  const handleOpen = useCallback(() => setOpen(true), []);

  const handleClose = useCallback(() => {
    setOpen(false);
    setSelectedTS(null);
    setComments("");
  }, []);

  const handleTSChange = useCallback((tsline) => {
    setSelectedTS(tsline);
    setComments(tsline.comments || "");
  }, []);

  const handleSave = useCallback(async () => {
    if (!selectedTS) return;

    try {
      const res = await updateTimesheetLine(selectedTS.id, {
        ...selectedTS,
        comments,
      });

      if (res.status === -1) {
        throw new Error(res.data.message);
      }

      const updatedLines = lines.map((line) =>
        line.id === selectedTS.id ? { ...res.data[0] } : line
      );

      dispatch(updateTSlines(updatedLines));
    } catch (error) {
      toast.error("Failed to update comment");
      console.error("Failed to update timesheet line:", error);
    } finally {
      handleClose();
    }
  }, [selectedTS, comments, lines, handleClose]);

  useEffect(() => {
    if (open && !isMultiline && tslines[0]) {
      setSelectedTS(tslines[0]);
      setComments(tslines[0].comments || "");
    }
  }, [open, isMultiline, tslines]);

  if (!tslines.length) return null;

  return (
    <>
      <ActionButton
        title="Add comment"
        icon="comment"
        className={clsx(styles.commentBtn, getIndicatorClass())}
        onClick={handleOpen}
        placement={popoverPlacement}
      />
      <DialogBox open={open} onClose={handleClose} fullscreen={false}>
        <Box className={styles.commentContainer}>
          {isMultiline && (
            <Select
              options={options}
              label="Timesheet lines"
              placeholder="Select timesheet lines..."
              optionLabel={(i) => i.label}
              optionKey={(i) => i.value}
              value={selectedTS}
              onChange={handleTSChange}
            />
          )}
          {selectedTS && (
            <>
              <textarea
                placeholder="Write a comment"
                className={styles.commentBox}
                value={comments}
                onChange={(e) => setComments(e.target.value)}
              />
              <Box className={styles.btnActions}>
                <Button onClick={handleSave}>Save</Button>
                <Button onClick={handleClose}>Cancel</Button>
              </Box>
            </>
          )}
        </Box>
      </DialogBox>
    </>
  );
};

export default CommentAction;
