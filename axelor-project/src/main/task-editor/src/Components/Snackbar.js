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
import { Snackbar, Button } from '@material-ui/core';
import MuiAlert from '@material-ui/lab/Alert';
import { makeStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';

import { translate } from '../utils';

function Alert(props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />;
}

const useStyles = makeStyles(theme => ({
  root: {
    width: '100%',
    '& > * + *': {
      marginTop: theme.spacing(2),
    },
  },
  actionButton: {
    textTransform: 'none',
  },
  close: {
    padding: 0,
    marginLeft: theme.spacing(1),
  },
}));

export default function Snackbars(props) {
  const classes = useStyles();
  const {
    open,
    severity = 'success',
    closeSnackbar,
    message,
    isAction = false,
    onActionClick,
    autoHideDuration = 1000,
    clickAway = false,
  } = props;
  const handleClose = (event, reason) => {
    if (reason === 'clickaway') {
      !clickAway && closeSnackbar();
      return;
    }
    closeSnackbar();
  };

  return (
    <div className={classes.root}>
      <Snackbar
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        open={open}
        autoHideDuration={autoHideDuration}
        onClose={handleClose}
      >
        <React.Fragment>
          <Alert
            onClose={handleClose}
            severity={severity}
            className={classes.alert}
            action={
              isAction && (
                <React.Fragment>
                  <Button
                    size="small"
                    variant="outlined"
                    color="inherit"
                    className={classes.actionButton}
                    onClick={() => {
                      onActionClick();
                      handleClose();
                    }}
                  >
                    {translate('TaskEditor.undo')}
                  </Button>
                  {clickAway && (
                    <IconButton aria-label="close" color="inherit" className={classes.close} onClick={handleClose}>
                      <CloseIcon />
                    </IconButton>
                  )}
                </React.Fragment>
              )
            }
          >
            {translate(message)}
          </Alert>
        </React.Fragment>
      </Snackbar>
    </div>
  );
}
