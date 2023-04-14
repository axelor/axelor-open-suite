import React from 'react';
import { Dialog } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import { drawerWidth, mobileDrawerWidth } from '../constants';

const useStyles = makeStyles(theme => ({
  paper: {
    display: 'flex',
    alignItems: 'center',
  },
  main: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    width: drawerWidth,
    height: '100%',
    [theme.breakpoints.only('xs')]: {
      width: mobileDrawerWidth,
    },
  },
}));

export default function FullScreenDialog({ open, handleFullScreenClose, children, toolbar }) {
  const classes = useStyles();
  return (
    <Dialog
      fullScreen
      open={open}
      onClose={handleFullScreenClose}
      classes={{
        paper: classes.paper,
      }}
    >
      {toolbar}
      <div className={classes.main} onClick={handleFullScreenClose}>
        <div className={classes.content} onClick={e => e.stopPropagation()}>
          {children}
        </div>
      </div>
    </Dialog>
  );
}
