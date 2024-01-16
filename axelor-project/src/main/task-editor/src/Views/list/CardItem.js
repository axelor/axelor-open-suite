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
import React, { useState, useEffect, useMemo } from 'react';
import moment from 'moment';
import classnames from 'classnames';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { Typography, Paper, IconButton, Divider, Avatar, FormControl } from '@material-ui/core';
import { ContextMenu, MenuItem as ContextMenuItem, ContextMenuTrigger } from 'react-contextmenu';
import { makeStyles } from '@material-ui/core/styles';

import DragIcon from '@material-ui/icons/DragIndicator';
import CheckCircleOutlineIcon from '@material-ui/icons/CheckCircleOutline';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import VisibilityIcon from '@material-ui/icons/Visibility';
import DeleteOutlineIcon from '@material-ui/icons/DeleteOutline';
import AssigneeSelection from '../form/AssigneeSelection';

import Card from './Card';
import { updateTask, getDMSFiles } from '../../Services/api';
import { ConfirmationDialog, DatePicker } from '../../Components';
import { TaskProgressSelect } from '../form/TaskComponents';
import { getStatus, getCompletedStatus, translate } from '../../utils';
import { useTaskEditor } from './Context';
import 'moment/min/locales';
import './css/react-context-menu.css';
import DuplicateTaskDialog from './menu/DuplicateTaskDialog';

const SPACING = 8;

const useStyles = makeStyles(theme => ({
  container: {
    border: ({ selected }) => (selected ? '1px solid #058EE3' : 'inherit'),
  },
  title: {
    display: 'flex',
    alignItems: 'center',
    flex: 1,
  },
  details: {
    visibility: 'hidden',
    display: 'flex',
    alignItems: 'center',
    '$cardItem:hover &': {
      visibility: 'visible',
    },
    [theme.breakpoints.down('xs')]: {
      visibility: 'visible',
    },
  },
  dragHandle: {
    color: '#b4b6b8',
    fontSize: '1.2rem',
    cursor: 'grab',
    visibility: 'hidden',
    '$cardItem:hover &': {
      visibility: 'visible',
    },
    [theme.breakpoints.down('xs')]: {
      visibility: 'visible',
    },
  },
  card: {
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: '#f5f8fa',
    },
  },
  cardItem: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottom: '1px solid #E8ECEE',
    borderRadius: 0,
    boxShadow: 'none',
    position: 'relative',
    backgroundColor: ({ selected }) => (selected ? '#EDF8FF' : 'white'),
    '&:hover': {
      backgroundColor: '#f5f8fa',
    },
    [theme.breakpoints.down('xs')]: {
      backgroundColor: '#f5f8fa !important',
    },
  },
  spacer: {
    paddingLeft: SPACING,
    paddingRight: SPACING,
  },
  stickyElement: {
    backgroundColor: ({ selected }) => (selected ? '#EDF8FF' : 'white'),
    '&:hover': {
      backgroundColor: '#f5f8fa',
    },
    [theme.breakpoints.down('xs')]: {
      backgroundColor: '#f5f8fa !important',
    },
    minWidth: 430,
    width: '100%',
    display: 'flex',
    alignItems: 'center',
    position: 'sticky',
    zIndex: 300,
    left: 0,
    padding: '0px 8px',
    background: 'white',
    justifyContent: 'space-between',
    [theme.breakpoints.only('xs')]: {
      minWidth: '100%',
    },
  },
  contextMenuItem: {
    fontSize: 14,
  },
  contextIcon: {
    color: '#a7a9ac',
    marginRight: 10,
  },
  contextSVGIcon: {
    fill: '#6f7782',
    height: 16,
    width: 16,
    marginRight: 10,
  },
  selection: {
    minWidth: 200,
  },
  select: {
    marginBottom: 0,
    minWidth: 200,
    textAlign: 'left',
    [theme.breakpoints.only('xs')]: {
      minWidth: 150,
    },
  },
  formControl: {
    minWidth: 200,
    [theme.breakpoints.only('xs')]: {
      minWidth: 150,
    },
  },
  chip: {
    height: 20,
    color: 'white',
    maxWidth: 55,
    marginRight: 3,
  },
  picker: {
    width: '100%',
    marginTop: 0,
    padding: 0,
  },
  inputClassName: {
    padding: '2px 10.5px 2px 10.5px',
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

const CardItem = React.forwardRef(function CardItem(
  { onDelete, record, style, canDelete, canEdit, onEdit, disabled, dragHandleProps = {}, ...rest },
  ref,
) {
  const [openConfirmation, setOpenConfirmation] = useState(false);
  const [isCompleted, setCompleted] = useState(false);
  const [openDuplicateTaskDialog, setOpenDuplicateTaskDialog] = useState(false);
  const [attachmentList, setAttachmentList] = useState([]);
  const {
    getAvatarColor,
    selectedProject,
    tasks,
    openTaskInDrawer = false,
    selectedTaskId,
    copyToClipboard,
    language,
    onCardAdd,
    openSnackbar,
    toggleTaskDrawer,
    toggleTaskFullScreen,
  } = useTaskEditor();

  const task = useMemo(() => tasks.find(task => task.id === selectedTaskId), [tasks, selectedTaskId]);

  const { isShowProgress = false } = selectedProject || {};

  const subTasksCount = tasks && tasks.filter(task => task.parentTask && task.parentTask.id === record.id).length;
  const classes = useStyles({ selected: (record && record.id) === (task && task.id) });
  const isMobile = useMediaQuery('(max-width:600px)');

  const contextMenuList = [
    {
      name: 'markComplete',
      label: isCompleted ? 'Mark Incomplete' : 'Mark Complete',
      icon: CheckCircleOutlineIcon,
    },
    {
      name: 'viewDetails',
      label: 'View details',
      icon: VisibilityIcon,
    },
    {
      name: 'fullScreen',
      label: 'Full screen',
      iconType: 'svg',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M13.7,19.7L5.4,28H13c0.6,0,1,0.4,1,1s-0.4,1-1,1H3c-0.6,0-1-0.4-1-1V19c0-0.6,0.4-1,1-1s1,0.4,1,1v7.6l8.3-8.3c0.4-0.4,1-0.4,1.4,0S14.1,19.3,13.7,19.7z M29,2H19c-0.6,0-1,0.4-1,1s0.4,1,1,1h7.6l-8.3,8.3c-0.4,0.4-0.4,1,0,1.4c0.2,0.2,0.5,0.3,0.7,0.3s0.5-0.1,0.7-0.3L28,5.4V13c0,0.6,0.4,1,1,1s1-0.4,1-1V3C30,2.4,29.6,2,29,2z"></path>
        </svg>
      ),
    },
    {
      name: 'copyTaskLink',
      label: 'Copy task link',
      iconType: 'svg',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M9,32c-2.3,0-4.6-0.9-6.4-2.6c-3.5-3.5-3.5-9.2,0-12.7l4-4c0.4-0.4,1-0.4,1.4,0c0.4,0.4,0.4,1,0,1.4l-4,4c-2.7,2.7-2.7,7.2,0,9.9s7.2,2.7,9.9,0l4-4c2.7-2.7,2.7-7.2,0-9.9c-0.8-0.8-1.8-1.4-2.9-1.7c-0.5-0.2-0.8-0.7-0.7-1.3c0.2-0.5,0.7-0.8,1.3-0.7c1.4,0.4,2.7,1.2,3.7,2.2c3.5,3.5,3.5,9.2,0,12.7l-4,4C13.6,31.1,11.3,32,9,32z M16.6,21.6c-0.1,0-0.2,0-0.3,0c-1.4-0.4-2.7-1.2-3.7-2.2c-1.7-1.7-2.6-4-2.6-6.4s0.9-4.7,2.6-6.4l4-4c3.5-3.5,9.2-3.5,12.7,0s3.5,9.2,0,12.7l-4,4c-0.4,0.4-1,0.4-1.4,0s-0.4-1,0-1.4l4-4c2.7-2.7,2.7-7.2,0-9.9S20.7,1.3,18,4l-4,4c-1.3,1.4-2,3.1-2,5s0.7,3.6,2.1,5c0.8,0.8,1.8,1.4,2.9,1.7c0.5,0.2,0.8,0.7,0.7,1.3C17.5,21.4,17.1,21.6,16.6,21.6z"></path>
        </svg>
      ),
    },
    {
      name: 'duplicateTask',
      label: 'Duplicate task',
      iconType: 'svg',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M27,32H13c-2.8,0-5-2.2-5-5V13c0-2.8,2.2-5,5-5h14c2.8,0,5,2.2,5,5v14C32,29.8,29.8,32,27,32z M13,10c-1.7,0-3,1.3-3,3v14  c0,1.7,1.3,3,3,3h14c1.7,0,3-1.3,3-3V13c0-1.7-1.3-3-3-3H13z M6,23c0-0.6-0.4-1-1-1c-1.7,0-3-1.3-3-3V5c0-1.7,1.3-3,3-3h14  c1.7,0,3,1.3,3,3c0,0.6,0.4,1,1,1s1-0.4,1-1c0-2.8-2.2-5-5-5H5C2.2,0,0,2.2,0,5v14c0,2.8,2.2,5,5,5C5.6,24,6,23.6,6,23z"></path>
        </svg>
      ),
    },
    {
      name: 'copyTaskName',
      label: 'Copy task name',
      iconType: 'svg',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M9,32c-2.3,0-4.6-0.9-6.4-2.6c-3.5-3.5-3.5-9.2,0-12.7l4-4c0.4-0.4,1-0.4,1.4,0c0.4,0.4,0.4,1,0,1.4l-4,4c-2.7,2.7-2.7,7.2,0,9.9s7.2,2.7,9.9,0l4-4c2.7-2.7,2.7-7.2,0-9.9c-0.8-0.8-1.8-1.4-2.9-1.7c-0.5-0.2-0.8-0.7-0.7-1.3c0.2-0.5,0.7-0.8,1.3-0.7c1.4,0.4,2.7,1.2,3.7,2.2c3.5,3.5,3.5,9.2,0,12.7l-4,4C13.6,31.1,11.3,32,9,32z M16.6,21.6c-0.1,0-0.2,0-0.3,0c-1.4-0.4-2.7-1.2-3.7-2.2c-1.7-1.7-2.6-4-2.6-6.4s0.9-4.7,2.6-6.4l4-4c3.5-3.5,9.2-3.5,12.7,0s3.5,9.2,0,12.7l-4,4c-0.4,0.4-1,0.4-1.4,0s-0.4-1,0-1.4l4-4c2.7-2.7,2.7-7.2,0-9.9S20.7,1.3,18,4l-4,4c-1.3,1.4-2,3.1-2,5s0.7,3.6,2.1,5c0.8,0.8,1.8,1.4,2.9,1.7c0.5,0.2,0.8,0.7,0.7,1.3C17.5,21.4,17.1,21.6,16.6,21.6z"></path>
        </svg>
      ),
    },
    {
      name: 'deleteTask',
      label: 'Delete Task',
      icon: DeleteOutlineIcon,
    },
  ];

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
      onDelete();
    },
    [onDelete, handleCloseConfirmation],
  );

  const onColumnWidgetChange = React.useCallback(
    async (name, value, e) => {
      let data = { ...record, [name]: value };
      if (name === 'status') {
        data.statusBeforeComplete = record.status;
      }
      if (name === 'priority') {
        data['priority.technicalTypeSelect'] = value.technicalTypeSelect;
      }
      // to update record state changes
      onEdit(data);

      // to update updated record changes
      const res = await updateTask(data);
      onEdit({ ...res, 'priority.technicalTypeSelect': data['priority.technicalTypeSelect'] });
    },
    [onEdit, record],
  );

  let today = moment().format('YYYY-MM-DD');
  let startDate = task && task.taskDate && moment(task.taskDate).format('YYYY-MM-DD');
  let dueDate = task && task.taskEndDate && moment(task.taskEndDate).format('YYYY-MM-DD');
  let dateColor =
    startDate === today
      ? 'green'
      : dueDate === today
      ? 'green'
      : startDate > today
      ? '#707883'
      : dueDate > today
      ? '#707883'
      : 'red';

  const toggleStatus = React.useCallback(
    async e => {
      e && e.stopPropagation();
      let status;
      if (isCompleted) {
        status = record.statusBeforeComplete;
      } else {
        status = getCompletedStatus(selectedProject);
      }
      let res = await updateTask({
        ...record,
        status,
        statusBeforeComplete: record.status,
      });
      if (res) {
        onEdit({ ...res, 'priority.technicalTypeSelect': record['priority.technicalTypeSelect'] });
      }
    },
    [onEdit, selectedProject, record, isCompleted],
  );

  function handleContextMenuClick(e, record, action) {
    e.preventDefault();
    e.stopPropagation();
    switch (action) {
      case 'markComplete':
        toggleStatus();
        return;
      case 'viewDetails':
        toggleTaskDrawer(true, record.id);
        return;
      case 'fullScreen':
        toggleTaskFullScreen(true, record.id);
        return;
      case 'copyTaskLink':
        copyToClipboard();
        return;
      case 'duplicateTask':
        toggleDuplicateTaskDialog(true);
        return;
      case 'copyTaskName':
        if (!record) return;
        copyToClipboard(record.name, 'Task name is copied to clipboard');
        return;
      case 'deleteTask':
        handleOpenConfirmation();
        return;
      default:
        return;
    }
  }

  const getDate = value => {
    let localLocale = moment(value);
    if (!language) return localLocale;
    localLocale.locale(language);
    return localLocale;
  };
  const toggleDuplicateTaskDialog = open => {
    setOpenDuplicateTaskDialog(open);
  };

  useEffect(() => {
    const isCompleted = getStatus(record, selectedProject);
    setCompleted(isCompleted);
  }, [record, selectedProject]);

  useEffect(() => {
    openDuplicateTaskDialog && getDMSFiles(record.id).then(attachments => setAttachmentList(attachments || []));
  }, [openDuplicateTaskDialog, record.id]);

  return (
    <div className={classes.container}>
      {openDuplicateTaskDialog && (
        <DuplicateTaskDialog
          open={openDuplicateTaskDialog}
          handleClose={() => toggleDuplicateTaskDialog(false)}
          task={record}
          attachmentList={attachmentList}
          onCardAdd={onCardAdd}
          openSnackbar={openSnackbar}
        />
      )}
      <ContextMenuTrigger id={`${record.id}`} name={record.name} holdToDisplay={1000}>
        <Paper ref={ref} {...rest} className={classnames(classes.cardItem, classes.card)} style={style}>
          <div
            className={classes.stickyElement}
            style={{ width: openTaskInDrawer ? '50%' : '100%' }}
            onClick={() => toggleTaskDrawer(true, record.id)}
          >
            <div className={classes.title}>
              <div {...dragHandleProps}>
                <DragIcon className={classes.dragHandle} />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', flexGrow: isMobile ? 1 : 0 }}>
                <Card record={record} onEdit={onEdit} />
                {subTasksCount > 0 && (
                  <IconButton style={{ padding: 10, marginLeft: isMobile ? 'auto' : 0 }}>
                    <span style={{ fontSize: 10.5, color: '#707883' }}>{subTasksCount}</span>
                    {getSubTaskIcon()}
                  </IconButton>
                )}
              </div>
            </div>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
              }}
            >
              <div className={classes.details}>
                {!isMobile && (
                  <Typography variant="caption" style={{ color: '#707883' }}>
                    {translate('Details')}
                  </Typography>
                )}
                <ChevronRightIcon className={classes.dragHandle} />
              </div>
              {openTaskInDrawer && task && record && record.id === task.id && (
                <div style={{ display: 'flex' }}>
                  <Typography
                    variant="caption"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      padding: '0px 5px',
                      whiteSpace: 'nowrap',
                      color: dateColor,
                      textTransform: 'capitalize',
                    }}
                  >
                    {task.taskDate && getDate(task.taskDate).format('DD MMM')}
                    {task.taskEndDate && ` - ${getDate(task.taskEndDate).format('DD MMM')}`}
                  </Typography>
                  {task.assignedTo && (
                    <Typography variant="caption">
                      <Avatar
                        aria-label="recipe"
                        style={{
                          backgroundColor: getAvatarColor(record.assignedTo && record.assignedTo.id),
                          fontSize: '0.75rem',
                          height: 25,
                          width: 25,
                        }}
                      >
                        {task.assignedTo &&
                          task.assignedTo.fullName &&
                          (task.assignedTo.fullName.substring(0, 2) || '').toUpperCase()}
                      </Avatar>
                    </Typography>
                  )}
                </div>
              )}
            </div>
          </div>
          <div
            style={{
              display: 'flex',
              width: 'fit-content',
              alignItems: 'center',
            }}
          >
            {!openTaskInDrawer && !isMobile && (
              <React.Fragment>
                <div className={classes.spacer}>
                  <AssigneeSelection
                    model="com.axelor.auth.db.User"
                    title={translate('Assigned To')}
                    optionLabel="fullName"
                    name="assignedTo"
                    value={record.assignedTo}
                    update={(value, e) => onColumnWidgetChange('assignedTo', value, e)}
                    options={selectedProject.membersUserSet}
                    getAvatarColor={getAvatarColor}
                  />
                </div>
                <div className={classes.spacer}>
                  <DatePicker
                    className={classes.picker}
                    inputClassName={classes.inputClassName}
                    value={record.taskDate}
                    name="taskDate"
                    label={translate('TaskEditor.taskDate')}
                    onChange={(value, e) => onColumnWidgetChange('taskDate', value, e)}
                  />
                </div>
                <div className={classes.spacer}>
                  <DatePicker
                    className={classes.picker}
                    inputClassName={classes.inputClassName}
                    value={record.taskEndDate}
                    name="taskEndDate"
                    label="Task end"
                    onChange={(value, e) => onColumnWidgetChange('taskEndDate', value, e)}
                  />
                </div>
                {isShowProgress && (
                  <div className={classes.spacer}>
                    <FormControl variant="outlined" className={classes.formControl}>
                      <TaskProgressSelect
                        t={translate}
                        className={classes.select}
                        value={record.progressSelect}
                        onChange={e => onColumnWidgetChange('progressSelect', e.target.value, e)}
                        size="small"
                      />
                    </FormControl>
                  </div>
                )}
              </React.Fragment>
            )}
          </div>
          <ConfirmationDialog
            open={openConfirmation}
            onClose={handleCloseConfirmation}
            onConfirm={handleDelete}
            title={translate('Delete Task')}
          />
        </Paper>
      </ContextMenuTrigger>
      {!isMobile && (
        <ContextMenu id={`${record.id}`}>
          {contextMenuList.map((menu, index) => (
            <React.Fragment key={menu.name}>
              <ContextMenuItem onClick={e => handleContextMenuClick(e, record, menu.name)}>
                {menu.iconType === 'svg' ? (
                  menu.icon
                ) : (
                  <menu.icon
                    fontSize="small"
                    className={classes.contextIcon}
                    style={{
                      color: menu.name === 'deleteTask' ? 'red' : '#a7a9ac',
                    }}
                  />
                )}
                <Typography
                  className={classes.contextMenuItem}
                  style={{
                    color: menu.name === 'deleteTask' ? 'red' : 'inherit',
                  }}
                >
                  {translate(menu.label)}
                </Typography>
              </ContextMenuItem>
              {index > 3 && <Divider />}
            </React.Fragment>
          ))}
        </ContextMenu>
      )}
    </div>
  );
});

CardItem.defaultProps = {
  onDelete: () => {},
  record: {},
  style: {},
  canDelete: true,
  canEdit: true,
  onEdit: () => {},
  disabled: false,
};

export default CardItem;
