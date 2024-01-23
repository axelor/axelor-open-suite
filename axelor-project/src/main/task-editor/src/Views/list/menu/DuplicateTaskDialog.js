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
import React, { useState, useEffect } from 'react';
import moment from 'moment';
import {
  FormGroup,
  Checkbox,
  FormControlLabel,
  Typography,
  Grid,
  Button,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import { addTask, addDMSFile } from '../../../Services/api';
import { DUPLICATE_TASK_OPTIONS } from '../../../constants';
import { translate } from '../../../utils';

const useStyles = makeStyles(() => ({
  paper: {
    minWidth: '50%',
    padding: 15,
  },
}));

export default function DuplicateTaskDialog({ open, handleClose, task, attachmentList, onCardAdd, openSnackbar }) {
  const [newTask, setNewTask] = useState({
    name: '',
  });
  const [state, setState] = React.useState({
    description: false,
    parentTask: false,
    attachments: false,
    projectTaskSection: true,
    taskEndDate: false,
    priority: false,
    status: false,
    taskDate: true,
    progressSelect: false,
    projectTaskCategory: false,
  });
  const classes = useStyles();

  const handleChange = event => {
    setState({ ...state, [event.target.name]: event.target.checked });
    if (event.target.checked) {
      setNewTask({
        ...newTask,
        [event.target.name]: task[event.target.name],
      });
    }
  };

  const handleNameChange = event => {
    setNewTask({
      ...newTask,
      [event.target.name]: event.target.value,
    });
  };

  const addToDMS = async (result, taskId) => {
    const payload = {
      fileName: result.fileName,
      metaFile: result.metaFile,
      relatedId: taskId,
      relatedModel: 'com.axelor.apps.project.db.ProjectTask',
    };
    await addDMSFile(payload);
  };

  const addNewTask = async () => {
    if (!state.taskDate) {
      setNewTask({
        ...newTask,
        taskDate: moment(),
      });
    }
    if (!state.projectTaskSection) {
      setNewTask({
        ...newTask,
        projectTaskSection: null,
      });
    }
    let res = await addTask({
      ...newTask,
    });
    if (res) {
      if (state.attachments) {
        attachmentList &&
          attachmentList.forEach(element => {
            addToDMS(element, res.id);
          });
      }
      openSnackbar({
        message: `${newTask.name} ${translate('TaskEditor.projectCreateSuccess')}`,
        severity: 'success',
        autoHideDuration: 3000,
      });
      onCardAdd(res);
    }
    handleClose();
  };

  useEffect(() => {
    if (!task) {
      return;
    }
    setNewTask({
      name: `(COPY) ${task.name}`,
      assignedTo: task.assignedTo,
      project: task.project,
      projectTaskSection: task.projectTaskSection,
      taskDate: task.taskDate,
    });
  }, [task]);

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      aria-labelledby="form-dialog-title"
      classes={{
        paper: classes.paper,
      }}
    >
      <DialogTitle id="form-dialog-title">{translate('Duplicate Task')}</DialogTitle>
      <DialogContent>
        <Grid>
          <TextField
            style={{ width: '100%', marginBottom: 15 }}
            placeholder="Task name"
            variant="outlined"
            value={newTask.name}
            name="name"
            onChange={handleNameChange}
          />
        </Grid>
        <Grid>
          <Typography variant="caption">{translate('Include')}</Typography>
          <FormGroup>
            {DUPLICATE_TASK_OPTIONS.map((option, index) => (
              <FormControlLabel
                key={index}
                control={
                  <Checkbox color="primary" checked={state[option.name]} onChange={handleChange} name={option.name} />
                }
                label={option.label}
              />
            ))}
          </FormGroup>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={addNewTask}
          variant="outlined"
          color="primary"
          style={{
            textTransform: 'none',
          }}
        >
          {translate('Create New Task')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
