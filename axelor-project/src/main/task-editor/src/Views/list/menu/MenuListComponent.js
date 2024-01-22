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
import { Menu, MenuItem, Typography, Divider } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import { useTaskEditor } from '../Context';
import { translate } from '../../../utils';

const useStyles = makeStyles(theme => ({
  root: {
    display: 'flex',
  },
  paper: {
    marginRight: theme.spacing(2),
  },
  icon: {
    fill: '#6f7782',
    height: '1em',
    marginRight: 8,
    width: '1em',
  },
  typography: {
    color: '#151b26',
    fontSize: 14,
  },
  menuPaper: {
    border: '1px solid #d3d4d5',
  },
}));

export default function MenuListComponent({
  anchorEl,
  isFullScreen,
  onClose,
  onMenuTaskDelete,
  openPdfDialog,
  openSubtaskOfAlert,
  openDuplicateTaskDialog,
  openTaskToProjectDialog,
}) {
  const classes = useStyles();
  const { toggleTaskFullScreen } = useTaskEditor();
  const onDelete = e => {
    onMenuTaskDelete();
  };
  return (
    <div className={classes.root}>
      <div>
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={onClose}
          classes={{
            paper: classes.menuPaper,
          }}
          elevation={0}
          getContentAnchorEl={null}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'center',
          }}
        >
          {!isFullScreen && (
            <MenuItem
              onClick={() => {
                onClose();
                toggleTaskFullScreen(true);
              }}
            >
              <svg className={classes.icon} focusable="false" viewBox="0 0 32 32">
                <path d="M13.7,19.7L5.4,28H13c0.6,0,1,0.4,1,1s-0.4,1-1,1H3c-0.6,0-1-0.4-1-1V19c0-0.6,0.4-1,1-1s1,0.4,1,1v7.6l8.3-8.3c0.4-0.4,1-0.4,1.4,0S14.1,19.3,13.7,19.7z M29,2H19c-0.6,0-1,0.4-1,1s0.4,1,1,1h7.6l-8.3,8.3c-0.4,0.4-0.4,1,0,1.4c0.2,0.2,0.5,0.3,0.7,0.3s0.5-0.1,0.7-0.3L28,5.4V13c0,0.6,0.4,1,1,1s1-0.4,1-1V3C30,2.4,29.6,2,29,2z"></path>
              </svg>
              <Typography className={classes.typography}>{translate('Full screen')}</Typography>
            </MenuItem>
          )}
          <MenuItem
            onClick={() => {
              openSubtaskOfAlert();
              onClose();
            }}
          >
            <svg className={classes.icon} focusable="false" viewBox="0 0 32 32">
              <path d="M15,13c0-0.6-0.4-1-1-1H3c-0.6,0-1-0.4-1-1V7c0-0.6,0.4-1,1-1h11c0.6,0,1-0.4,1-1s-0.4-1-1-1H3C1.3,4,0,5.3,0,7v4c0,1.7,1.3,3,3,3h11C14.6,14,15,13.6,15,13z M29,18h-3v-6c0-2.2-1.8-4-4-4h-4.6l2.3-2.3c0.4-0.4,0.4-1,0-1.4s-1-0.4-1.4,0l-4,4c-0.4,0.4-0.4,1,0,1.4c0,0,0,0,0,0l4,4c0.4,0.4,1,0.4,1.4,0c0,0,0,0,0,0c0.4-0.4,0.4-1,0-1.4c0,0,0,0,0,0L17.4,10H22c1.1,0,2,0.9,2,2v6H9c-1.7,0-3,1.3-3,3v4c0,1.7,1.3,3,3,3h20c1.7,0,3-1.3,3-3v-4C32,19.3,30.7,18,29,18z M30,25c0,0.6-0.4,1-1,1H9c-0.6,0-1-0.4-1-1v-4c0-0.6,0.4-1,1-1h20c0.6,0,1,0.4,1,1V25z"></path>
            </svg>
            <Typography className={classes.typography}>{translate('Make a subtask of')}</Typography>
          </MenuItem>
          <Divider />
          <MenuItem
            onClick={() => {
              onClose();
              openDuplicateTaskDialog();
            }}
          >
            <Typography className={classes.typography}>{translate('Duplicate task')}</Typography>
          </MenuItem>
          <MenuItem
            onClick={() => {
              onClose();
              openPdfDialog();
            }}
          >
            <Typography className={classes.typography}>{translate('Print')}</Typography>
          </MenuItem>
          <MenuItem
            onClick={() => {
              onClose();
              openTaskToProjectDialog();
            }}
          >
            <Typography className={classes.typography}>{translate('Convert to project')}</Typography>
          </MenuItem>
          <Divider />
          <MenuItem
            onClick={e => {
              onDelete(e);
              onClose();
            }}
          >
            <Typography
              className={classes.typography}
              style={{
                color: '#ED4F5F',
              }}
            >
              {translate('Delete Task')}
            </Typography>
          </MenuItem>
        </Menu>
      </div>
    </div>
  );
}
