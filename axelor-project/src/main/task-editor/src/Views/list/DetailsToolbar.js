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
import React, { useState } from 'react';
import classnames from 'classnames';
import { IconButton, Button } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import DoneIcon from '@material-ui/icons/Done';
import MenuListComponent from './menu/MenuListComponent';
import { TOOLBAR_HEIGHT, mobileDrawerWidth } from '../../constants';
import { translate } from '../../utils';

const useStyles = makeStyles(theme => ({
  drawerHeader: {
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0, 1),
    ...theme.mixins.toolbar,
    position: 'fixed',
    justifyContent: 'space-between',
    width: '50%',
    height: TOOLBAR_HEIGHT,
    boxSizing: 'border-box',
    minHeight: `${TOOLBAR_HEIGHT}px !important`,
    boxShadow: theme.shadows[4],
  },
  fullScreenToolbar: {
    zIndex: 100,
    background: 'white',
    width: '50%',
    [theme.breakpoints.only('xs')]: {
      width: mobileDrawerWidth,
    },
  },
  markComplete: {
    textTransform: 'none',
    color: '#6F7782',
    '&:hover': {
      color: '#00bf9c',
      borderColor: '#00bf9c',
    },
  },
  toolbarIcon: {
    fill: '#6f7782',
    height: 16,
    width: 16,
  },
  toolbarIconButton: {
    padding: 10,
  },
}));

const getSubTaskIcon = () => {
  return (
    <svg
      focusable="false"
      viewBox="0 0 32 32"
      style={{
        fill: '#6f7782',
        height: 16,
        width: 16,
      }}
    >
      <path d="M25,20c-2.4,0-4.4,1.7-4.9,4H11c-3.9,0-7-3.1-7-7v-5h16.1c0.5,2.3,2.5,4,4.9,4c2.8,0,5-2.2,5-5s-2.2-5-5-5c-2.4,0-4.4,1.7-4.9,4H4V3c0-0.6-0.4-1-1-1S2,2.4,2,3v14c0,5,4,9,9,9h9.1c0.5,2.3,2.5,4,4.9,4c2.8,0,5-2.2,5-5S27.8,20,25,20z M25,8c1.7,0,3,1.3,3,3s-1.3,3-3,3s-3-1.3-3-3S23.3,8,25,8z M25,28c-1.7,0-3-1.3-3-3s1.3-3,3-3s3,1.3,3,3S26.7,28,25,28z"></path>
    </svg>
  );
};
function DetailsToolbar({
  isFullScreen,
  toggleStatus,
  isCompleted,
  attachmentList,
  selectFile,
  addNewSubtask,
  subTaskCount,
  copyToClipboard,
  handleClose,
  onMenuTaskDelete,
  openPdfDialog,
  openSubtaskOfAlert,
  openDuplicateTaskDialog,
  openTaskToProjectDialog,
}) {
  const classes = useStyles();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleToggle = e => {
    e.stopPropagation();
    setAnchorEl(e.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };
  const upload = () => {
    document.getElementById('inputFile').click();
  };
  return (
    <div className={classnames(classes.drawerHeader, classes.fullScreenToolbar)}>
      <div>
        <Button
          variant="outlined"
          size="small"
          className={classes.markComplete}
          startIcon={<DoneIcon />}
          onClick={toggleStatus}
          style={{
            color: isCompleted ? 'white' : '#6F7782',
            background: isCompleted ? '#00bf9c' : 'white',
          }}
        >
          {isCompleted ? translate('Completed') : translate('Mark Complete')}
        </Button>
      </div>
      <div>
        <IconButton onClick={upload} className={classes.toolbarIconButton}>
          <svg className={classes.toolbarIcon} focusable="false" viewBox="0 0 32 32">
            <path d="M19,32c-3.9,0-7-3.1-7-7V10c0-2.2,1.8-4,4-4s4,1.8,4,4v9c0,0.6-0.4,1-1,1s-1-0.4-1-1v-9c0-1.1-0.9-2-2-2s-2,0.9-2,2v15c0,2.8,2.2,5,5,5s5-2.2,5-5V10c0-4.4-3.6-8-8-8s-8,3.6-8,8v5c0,0.6-0.4,1-1,1s-1-0.4-1-1v-5C6,4.5,10.5,0,16,0s10,4.5,10,10v15C26,28.9,22.9,32,19,32z"></path>
          </svg>
          {attachmentList && attachmentList.length > 0 && (
            <span style={{ fontSize: 10.5, marginBottom: 12 }}>{attachmentList.length}</span>
          )}
          <input id="inputFile" type="file" name="file" multiple onChange={selectFile} style={{ display: 'none' }} />
        </IconButton>
        <IconButton onClick={() => addNewSubtask()} className={classes.toolbarIconButton}>
          {getSubTaskIcon()}
          {subTaskCount ? <span style={{ fontSize: 10.5, marginBottom: 12 }}>{subTaskCount}</span> : null}
        </IconButton>
        <IconButton onClick={() => copyToClipboard()} className={classes.toolbarIconButton}>
          <svg className={classes.toolbarIcon} focusable="false" viewBox="0 0 32 32">
            <path d="M9,32c-2.3,0-4.6-0.9-6.4-2.6c-3.5-3.5-3.5-9.2,0-12.7l4-4c0.4-0.4,1-0.4,1.4,0c0.4,0.4,0.4,1,0,1.4l-4,4c-2.7,2.7-2.7,7.2,0,9.9s7.2,2.7,9.9,0l4-4c2.7-2.7,2.7-7.2,0-9.9c-0.8-0.8-1.8-1.4-2.9-1.7c-0.5-0.2-0.8-0.7-0.7-1.3c0.2-0.5,0.7-0.8,1.3-0.7c1.4,0.4,2.7,1.2,3.7,2.2c3.5,3.5,3.5,9.2,0,12.7l-4,4C13.6,31.1,11.3,32,9,32z M16.6,21.6c-0.1,0-0.2,0-0.3,0c-1.4-0.4-2.7-1.2-3.7-2.2c-1.7-1.7-2.6-4-2.6-6.4s0.9-4.7,2.6-6.4l4-4c3.5-3.5,9.2-3.5,12.7,0s3.5,9.2,0,12.7l-4,4c-0.4,0.4-1,0.4-1.4,0s-0.4-1,0-1.4l4-4c2.7-2.7,2.7-7.2,0-9.9S20.7,1.3,18,4l-4,4c-1.3,1.4-2,3.1-2,5s0.7,3.6,2.1,5c0.8,0.8,1.8,1.4,2.9,1.7c0.5,0.2,0.8,0.7,0.7,1.3C17.5,21.4,17.1,21.6,16.6,21.6z"></path>
          </svg>
        </IconButton>
        <IconButton onClick={handleToggle} className={classes.toolbarIconButton}>
          <svg className={classes.toolbarIcon} focusable="false" viewBox="0 0 32 32">
            <path d="M16,13c1.7,0,3,1.3,3,3s-1.3,3-3,3s-3-1.3-3-3S14.3,13,16,13z M3,13c1.7,0,3,1.3,3,3s-1.3,3-3,3s-3-1.3-3-3S1.3,13,3,13z M29,13c1.7,0,3,1.3,3,3s-1.3,3-3,3s-3-1.3-3-3S27.3,13,29,13z"></path>
          </svg>
        </IconButton>
        <IconButton onClick={handleClose} className={classes.toolbarIconButton}>
          <svg className={classes.toolbarIcon} focusable="false" viewBox="0 0 32 32">
            {isFullScreen ? (
              <path d="M18.1,16L27,7.1c0.6-0.6,0.6-1.5,0-2.1s-1.5-0.6-2.1,0L16,13.9l-8.9-9C6.5,4.3,5.6,4.3,5,4.9S4.4,6.4,5,7l8.9,8.9L5,24.8c-0.6,0.6-0.6,1.5,0,2.1c0.3,0.3,0.7,0.4,1.1,0.4s0.8-0.1,1.1-0.4l8.9-8.9l8.9,8.9c0.3,0.3,0.7,0.4,1.1,0.4s0.8-0.1,1.1-0.4c0.6-0.6,0.6-1.5,0-2.1L18.1,16z"></path>
            ) : (
              <path d="M2,14.5h18.4l-7.4-7.4c-0.6-0.6-0.6-1.5,0-2.1c0.6-0.6,1.5-0.6,2.1,0l10,10c0.6,0.6,0.6,1.5,0,2.1l-10,10c-0.3,0.3-0.7,0.4-1.1,0.4c-0.4,0-0.8-0.1-1.1-0.4c-0.6-0.6-0.6-1.5,0-2.1l7.4-7.4H2c-0.8,0-1.5-0.7-1.5-1.5C0.5,15.3,1.2,14.5,2,14.5z M28,3.5C28,2.7,28.7,2,29.5,2S31,2.7,31,3.5v25c0,0.8-0.7,1.5-1.5,1.5S28,29.3,28,28.5V3.5z"></path>
            )}
          </svg>
        </IconButton>
        <MenuListComponent
          anchorEl={anchorEl}
          isFullScreen={isFullScreen}
          onClose={handleMenuClose}
          onMenuTaskDelete={onMenuTaskDelete}
          openPdfDialog={openPdfDialog}
          openSubtaskOfAlert={openSubtaskOfAlert}
          openDuplicateTaskDialog={openDuplicateTaskDialog}
          openTaskToProjectDialog={openTaskToProjectDialog}
        />
      </div>
    </div>
  );
}

export default DetailsToolbar;
