/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
