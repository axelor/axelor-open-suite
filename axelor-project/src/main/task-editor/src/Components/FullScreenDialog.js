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
