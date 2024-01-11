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
import React, { useState, useEffect, useRef } from 'react';
import classnames from 'classnames';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { Paper, IconButton, Menu, MenuItem, TextField } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import CaretIcon from '@material-ui/icons/ArrowDropDown';
import DragIcon from '@material-ui/icons/DragIndicator';
import CheckCircleOutlineIcon from '@material-ui/icons/CheckCircleOutline';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';

import { getStatus, getCompletedStatus, translate } from '../../utils';
import { updateTask } from '../../Services/api';
import { useTaskEditor } from '../list/Context';
import ConfirmationDialog from '../../Components/ConfirmationDialog';

const useStyles = makeStyles(theme => ({
  title: { display: 'flex', alignItems: 'center' },
  input: {
    padding: 4,
    '&:hover': {
      border: '1px solid #b4b6b8',
    },
    border: '1px solid transparent',
    width: '100%',
  },
  checkedIcon: {
    color: '#b4b6b8',
    '&:hover': {
      color: '#25e8c8 !important',
    },
  },
  dragHandle: {
    color: '#b4b6b8',
    fontSize: '1.2rem',
    cursor: 'grab',
  },
  card: {
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: '#f5f8fa',
    },
  },
  cardTask: {
    marginLeft: theme.spacing(5),
    display: 'flex',
    width: '100%',
    alignItems: 'center',
    [theme.breakpoints.only('xs')]: {
      marginLeft: theme.spacing(1),
    },
  },
  checkIconButton: {
    padding: theme.spacing(1),
  },
  cardItem: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottom: '1px solid #E8ECEE',
    borderRadius: 0,
    boxShadow: 'none',
    position: 'relative',
  },
  stickyElement: {
    '&:hover': {
      backgroundColor: '#f5f8fa',
    },
    width: '100%',
    display: 'flex',
    alignItems: 'center',
  },
  menuPaper: {
    border: '1px solid #d3d4d5',
  },
}));

export function Card({ record, onEdit, newSubtaskId, setNewSubtaskId, isFullScreen, isFullScreenOpen }) {
  const classes = useStyles();
  const [state, setState] = useState({
    name: record.name,
    status: record.status,
  });
  const inputRef = useRef(null);
  const [isCompleted, setCompleted] = useState(false);
  const { selectedProject } = useTaskEditor();

  const onChange = React.useCallback(e => {
    const { name, value } = e.target;
    e.stopPropagation();
    setState(state => ({ ...state, [name]: value }));
  }, []);

  const toggleStatus = React.useCallback(
    async e => {
      e && e.stopPropagation();
      let status;
      if (isCompleted) {
        status = record.statusBeforeComplete;
      } else {
        status = getCompletedStatus(selectedProject);
      }
      setState(state => ({
        ...state,
        status,
      }));
      let res = await updateTask({
        ...record,
        status,
        statusBeforeComplete: record.status,
      });
      if (res) {
        onEdit({ ...res, 'priority.technicalTypeSelect': record['priority.technicalTypeSelect'] });
      }
    },
    [onEdit, record, isCompleted, selectedProject],
  );

  const onBlur = React.useCallback(async () => {
    if (record.name !== state.name) {
      let res = await updateTask({
        ...record,
        name: state.name,
      });
      onEdit({ ...res, 'priority.technicalTypeSelect': state['priority.technicalTypeSelect'] });
    }
  }, [state, onEdit, record]);

  useEffect(() => {
    setState(record);
    const isCompleted = getStatus(record, selectedProject);
    setCompleted(isCompleted);
  }, [record, selectedProject]);

  useEffect(() => {
    if (record.id === newSubtaskId && record.name === '' && (isFullScreen ? true : !isFullScreenOpen)) {
      inputRef.current.focus();
      isFullScreen && inputRef.current.scrollIntoView();
      setNewSubtaskId(null);
    }
  }, [newSubtaskId, setNewSubtaskId, record, isFullScreen, isFullScreenOpen]);

  return (
    <div className={classes.cardTask}>
      <IconButton size="small" onClick={toggleStatus} className={classes.checkIconButton}>
        {isCompleted ? (
          <CheckCircleIcon
            fontSize="small"
            style={{
              color: '#25e8c8',
            }}
            className={classes.checkedIcon}
          />
        ) : (
          <CheckCircleOutlineIcon
            fontSize="small"
            style={{
              color: '#b4b6b8',
            }}
            className={classes.checkedIcon}
          />
        )}
      </IconButton>
      <TextField
        onClick={e => e.stopPropagation()}
        onChange={onChange}
        onBlur={onBlur}
        name="name"
        value={state.name}
        InputProps={{ disableUnderline: true }}
        className={classes.input}
        style={{ marginRight: 2 }}
        inputRef={inputRef}
      >
        {record.name}
      </TextField>
    </div>
  );
}

export default function CardItem({
  onDelete,
  record,
  style,
  Card,
  canDelete,
  canEdit,
  onEdit,
  dragHandleProps = {},
  newSubtaskId,
  setNewSubtaskId,
  isFullScreen,
  isFullScreenOpen,
  ...rest
}) {
  const [anchorEl, setAnchorEl] = useState(null);
  const [isHovered, setHovered] = useState(false);
  const [openConfirmation, setOpenConfirmation] = React.useState(false);
  const isMobile = useMediaQuery('(max-width:600px)');

  const classes = useStyles();
  const handleClick = e => {
    e.stopPropagation();
    setAnchorEl(e.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const onMouseEnter = () => {
    setHovered(true);
  };
  const onMouseLeave = () => {
    setHovered(false);
  };

  const handleOpenConfirmation = React.useCallback(e => {
    setOpenConfirmation(true);
  }, []);

  const handleCloseConfirmation = React.useCallback(e => {
    e && e.stopPropagation();
    setOpenConfirmation(false);
  }, []);

  const handleDelete = React.useCallback(
    e => {
      e.stopPropagation();
      handleCloseConfirmation();
      onDelete(record);
    },
    [onDelete, record, handleCloseConfirmation],
  );

  return (
    <Paper
      {...rest}
      className={classnames(classes.cardItem, classes.card)}
      style={{
        ...style,
      }}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <div className={classes.stickyElement}>
        <div className={classes.title}>
          <div {...dragHandleProps}>
            <DragIcon
              className={classes.dragHandle}
              style={{ visibility: isHovered || isMobile ? 'visible' : 'hidden' }}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Card
              record={record}
              onEdit={onEdit}
              newSubtaskId={newSubtaskId}
              setNewSubtaskId={setNewSubtaskId}
              isFullScreen={isFullScreen}
              isFullScreenOpen={isFullScreenOpen}
            />
            <div>
              <IconButton onClick={handleClick} size="small">
                <CaretIcon style={{ fontSize: 15 }} />
              </IconButton>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
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
                {canDelete && (
                  <MenuItem
                    onClick={e => {
                      e.stopPropagation();
                      handleClose();
                      handleOpenConfirmation();
                    }}
                  >
                    {translate('Delete')}
                  </MenuItem>
                )}
              </Menu>
            </div>
          </div>
        </div>
      </div>
      <ConfirmationDialog
        open={openConfirmation}
        onClose={handleCloseConfirmation}
        onConfirm={handleDelete}
        title={translate('Delete Task')}
      />
    </Paper>
  );
}

CardItem.defaultProps = {
  onDelete: () => {},
  record: {},
  style: {},
  Card,
  canDelete: true,
  canEdit: true,
  onEdit: () => {},
};
