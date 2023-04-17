import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core';

import { translate } from '../utils';

function ConfirmationDialog({ open, onClose, onConfirm, title, content, disabled }) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="alert-dialog-title"
      aria-describedby="alert-dialog-description"
    >
      <DialogTitle id="alert-dialog-title">{translate(title)}</DialogTitle>
      <DialogContent>
        <DialogContentText id="alert-dialog-description">{translate(content)}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary" size="small">
          {translate('Cancel')}
        </Button>
        <Button onClick={onConfirm} color="primary" size="small" autoFocus disabled={disabled}>
          {translate('Confirm')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

ConfirmationDialog.defaultProps = {
  onClose: () => {},
  onConfirm: () => {},
  open: false,
  title: translate('TaskEditor.deleteRecord'),
  content: translate('TaskEditor.deleteRecordConfirm'),
  disabled: false,
};

export default ConfirmationDialog;
