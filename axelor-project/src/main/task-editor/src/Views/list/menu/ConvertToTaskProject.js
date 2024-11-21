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

import { addProject } from '../../../Services/api';
import { translate } from '../../../utils';

const useStyles = makeStyles(() => ({
  paper: {
    minWidth: '50%',
    padding: 15,
  },
}));

export default function ConvertToTaskProject({ open, handleClose, task, openSnackbar }) {
  const [project, setProject] = useState({
    name: '',
    code: '',
    projectTypeSelect: 1,
    isProject: true,
    statusSelect: 1,
  });
  const [state, setState] = useState({
    description: false,
    assignedTo: false,
  });
  const classes = useStyles();

  const handleChange = event => {
    setState({ ...state, [event.target.name]: event.target.checked });
    if (event.target.checked) {
      setProject({
        ...project,
        [event.target.name]: task[event.target.name],
      });
    }
  };

  const handleNameChange = event => {
    setProject({
      ...project,
      [event.target.name]: event.target.value,
      code: event.target.value,
    });
  };

  const addNewProject = async () => {
    let data = {
      ...project,
    };
    if (state.assignedTo) {
      data.membersUserSet = [{ ...project.assignedTo }];
    }
    if (!state.description) {
      data.description = null;
    }
    let res = await addProject({
      ...data,
    });
    if (res) {
      openSnackbar({
        message: `${project.name} ${translate('TaskEditor.projectCreateSuccess')}`,
        severity: 'success',
        autoHideDuration: 3000,
      });
    }
    handleClose();
  };

  useEffect(() => {
    if (!task) {
      return;
    }
    setProject({
      name: task.name,
      code: task.name,
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
      <DialogTitle id="form-dialog-title">{translate('Convert Task to Project')}</DialogTitle>
      <DialogContent>
        <Grid>
          <TextField
            style={{ width: '100%', marginBottom: 15 }}
            placeholder={translate('Project name')}
            variant="outlined"
            value={project.name}
            name="name"
            onChange={handleNameChange}
          />
        </Grid>
        <Grid>
          <Typography variant="caption">{translate('Include')}</Typography>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox color="primary" checked={state.description} onChange={handleChange} name="description" />
              }
              label={translate('Task Description as Project Description')}
            />
            <FormControlLabel
              control={
                <Checkbox color="primary" checked={state.assignedTo} onChange={handleChange} name="assignedTo" />
              }
              label={translate('Task User as Project Members')}
            />
          </FormGroup>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={addNewProject}
          variant="outlined"
          color="primary"
          style={{
            textTransform: 'none',
          }}
        >
          {translate('Convert')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
