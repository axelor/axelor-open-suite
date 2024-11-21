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
import classnames from 'classnames';
import { Typography, Paper, IconButton, TextField } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import DeleteIcon from '@material-ui/icons/Delete';
import AddIcon from '@material-ui/icons/Add';
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown';
import DragIcon from '@material-ui/icons/DragIndicator';
import ArrowRightIcon from '@material-ui/icons/ArrowRight';

import { updateSection } from '../../Services/api';
import { ConfirmationDialog } from '../../Components';
import { useTaskEditor } from './Context';
import { translate } from '../../utils';

const useStyles = makeStyles(theme => ({
  paper: ({ collapsed }) => ({
    padding: theme.spacing(1),
    transition: 'transform 0.3s, margin 0.2s',
    boxShadow: 'none',
    width: '100%',
    borderTop: !collapsed ? '1px solid #E8ECEE' : 'none',
    borderBottom: !collapsed ? '1px solid #E8ECEE' : 'none',
    borderRadius: '0',
    position: 'relative',
  }),
  section: {
    display: 'flex',
    alignItems: 'center',
    position: 'sticky',
    left: 0,
    width: 'fit-content',
    background: 'white',
    zIndex: 200,
    [theme.breakpoints.down('xs')]: {
      width: '100%',
    },
  },
  input: {
    padding: theme.spacing(0.5),
    border: '1px solid transparent',
    width: '100%',
    '&:hover': {
      borderColor: '#b4b6b8',
    },
    [theme.breakpoints.only('xs')]: {
      maxWidth: 'max-content',
    },
  },
  arrowBtn: {
    color: 'black',
    padding: theme.spacing(0, 1.5),
  },
  dragHandle: {
    color: '#b4b6b8',
    fontSize: '1.2rem',
    cursor: 'grab',
    visibility: 'hidden',
    [`$paper:hover &`]: {
      visibility: 'visible',
    },
    [theme.breakpoints.down('xs')]: {
      visibility: 'visible',
    },
  },
  actions: {
    display: 'flex',
    visibility: 'hidden',
    '$paper:hover &': {
      visibility: 'visible',
    },
    [theme.breakpoints.down('xs')]: {
      visibility: 'visible',
      marginLeft: 'auto',
    },
  },
  action: {
    padding: 2.5,
    color: '#b4b6b8',
  },
}));

export default function Header({
  column,
  disabled,
  disableColumnCollapse,
  disableColumnDelete,
  index,
  isCustom,
  ColumnHeader,
  ...rest
}) {
  const {
    onColumnDelete,
    onColumnUpdate,
    onColumnToggle,
    selectedProject,
    openTaskInDrawer,
    addNewTask,
  } = useTaskEditor();
  const { collapsed } = column;
  const classes = useStyles({ collapsed });
  const [name, setName] = useState(column.name);
  const [openConfirmation, setOpenConfirmation] = React.useState(false);

  const { isShowPriority = false, isShowStatus = false, isShowProgress = false, isShowTaskCategory = false } =
    selectedProject || {};
  const containTasks = column.records && column.records.length;
  const length = openTaskInDrawer
    ? 1
    : Boolean(isShowPriority) + Boolean(isShowStatus) + Boolean(isShowProgress) + Boolean(isShowTaskCategory);

  const handleOpenConfirmation = React.useCallback(e => {
    setOpenConfirmation(true);
  }, []);

  const handleCloseConfirmation = React.useCallback(e => {
    e && e.stopPropagation();
    setOpenConfirmation(false);
  }, []);

  const handleDelete = React.useCallback(() => {
    handleCloseConfirmation();
    onColumnDelete({ column });
  }, [onColumnDelete, column, handleCloseConfirmation]);

  const handleInputChange = React.useCallback(e => {
    setName(e.target.value);
  }, []);

  const handleInputBlur = React.useCallback(async () => {
    let $name = name;
    if (column.name !== name || name === '') {
      if (name === '') {
        $name = translate('Untitled Section');
        setName($name);
      }
      const res = await updateSection({
        ...column,
        name: $name,
      });
      onColumnUpdate({ column: { ...column, ...res } });
    }
  }, [onColumnUpdate, column, name]);

  const handleClick = e => {
    // blur active input if any
    if (document.activeElement.tagName === 'INPUT' && e.target.tagName !== 'INPUT') {
      document.activeElement.blur();
    }
  };

  useEffect(() => {
    setName(column.name || '');
  }, [column.name]);

  if (isCustom) {
    return <ColumnHeader {...rest} style={{ width: '100%' }} />;
  }

  const Icon = collapsed ? ArrowRightIcon : ArrowDropDownIcon;

  return (
    <Paper {...rest} className={classnames(classes.paper, rest.className)} onClick={handleClick}>
      <div className={classes.section}>
        <DragIcon className={classes.dragHandle} />
        {!disableColumnCollapse && (
          <IconButton className={classes.arrowBtn} onClick={() => onColumnToggle({ column })}>
            <Icon />
          </IconButton>
        )}
        <TextField
          name="name"
          value={name}
          className={classes.input}
          placeholder={translate('Add Section')}
          InputProps={{ disableUnderline: true }}
          onChange={handleInputChange}
          onBlur={handleInputBlur}
        />
        <div className={classes.actions}>
          <IconButton className={classes.action} onClick={e => addNewTask(index)}>
            <AddIcon />
          </IconButton>
          {!disabled && !disableColumnDelete && (
            <IconButton className={classes.action} onClick={handleOpenConfirmation}>
              <DeleteIcon />
            </IconButton>
          )}
        </div>
      </div>
      <div style={{ minWidth: length * 216 }}></div>
      <ConfirmationDialog
        open={openConfirmation}
        title={translate('Delete Section')}
        content={
          containTasks ? translate('Cannot delete section with tasks') : translate('TaskEditor.deleteRecordConfirm')
        }
        disabled={Boolean(containTasks)}
        onClose={handleCloseConfirmation}
        onConfirm={handleDelete}
      />
    </Paper>
  );
}

Header.defaultProps = {
  column: {},
  ColumnHeader: ({ title }) => (
    <Typography variant="subtitle1" style={{ fontSize: '1.08rem' }}>
      <b>{title}</b>
    </Typography>
  ),
};
