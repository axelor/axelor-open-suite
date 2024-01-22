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
import { IconButton, TextField } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import CheckCircleOutlineIcon from '@material-ui/icons/CheckCircleOutline';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';

import { updateTask } from '../../Services/api';
import { getStatus, getCompletedStatus } from '../../utils';
import { useTaskEditor } from '../list/Context';

const useStyles = makeStyles(theme => ({
  input: {
    padding: 4,
    '&:hover': {
      border: '1px solid #b4b6b8',
    },
    border: '1px solid transparent',
    width: '100%',
    minWidth: 215,
    marginRight: 2,
    [theme.breakpoints.only('xs')]: {
      minWidth: 'calc(100% - 36px)',
    },
  },
  cardTask: {
    marginLeft: theme.spacing(5),
    display: 'flex',
    width: '100%',
    alignItems: 'center',
    [theme.breakpoints.only('xs')]: {
      marginLeft: theme.spacing(1),
      width: 'fit-content',
    },
  },
  checkedIcon: {
    color: '#b4b6b8',
    '&:hover': {
      color: '#25e8c8 !important',
    },
  },
  checkIconButton: {
    padding: theme.spacing(1),
  },
}));

export default function Card({ record, onEdit }) {
  const classes = useStyles();
  const [state, setState] = useState({
    name: record.name,
    status: record.status,
  });
  const inputRef = useRef(null);
  const [isCompleted, setCompleted] = useState(false);
  const { selectedProject, newTaskId, setNewTaskId } = useTaskEditor();

  const onChange = React.useCallback(e => {
    e.stopPropagation();
    const { name, value } = e.target;
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

  const onBlur = React.useCallback(
    async e => {
      e.stopPropagation();
      if (record.name !== state.name) {
        let res = await updateTask({
          ...record,
          name: state.name,
        });
        onEdit({ ...res, 'priority.technicalTypeSelect': state['priority.technicalTypeSelect'] });
      }
    },
    [state, onEdit, record],
  );

  useEffect(() => {
    setState(record);
    const isCompleted = getStatus(record, selectedProject);
    setCompleted(isCompleted);
  }, [record, selectedProject]);

  useEffect(() => {
    if (newTaskId === record.id && record.name === '') {
      inputRef.current.focus();
      setNewTaskId(null);
    }
  }, [newTaskId, record, setNewTaskId]);

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
        inputRef={inputRef}
      >
        {record.name}
      </TextField>
    </div>
  );
}
