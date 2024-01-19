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
import moment from 'moment';
import { Grid, TextField, FormControl, Typography, Button, IconButton } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import AddIcon from '@material-ui/icons/Add';
import DeleteOutlineIcon from '@material-ui/icons/DeleteOutline';
import CloseIcon from '@material-ui/icons/Close';
import useMediaQuery from '@material-ui/core/useMediaQuery';

import { Editor, CommentList, AttachmentList, StaticSelect, DatePicker, ColumnSelect } from '../../Components';
import CardItem from './FormCardItem';
import AssigneeSelection from './AssigneeSelection';
import { TaskPrioritySelect, TaskProgressSelect } from './TaskComponents';
import { updateTask } from '../../Services/api';
import { useTaskEditor } from '../list/Context';
import { translate } from '../../utils';

import 'react-draft-wysiwyg/dist/react-draft-wysiwyg.css';

const drawerWidth = '50%';
const mobileDrawerWidth = '100%';

const useStyles = makeStyles(theme => ({
  typography: {
    textAlign: 'left',
    textTransform: 'none',
  },
  common: {
    width: '100%',
    marginBottom: 15,
  },
  drawerFooter: {
    display: 'flex',
    alignItems: 'center',
    background: '#F6F8F9',
    padding: 13,
    justifyContent: 'space-between',
    width: drawerWidth,
    position: 'fixed',
    border: '1px solid lightgray',
    bottom: 0,
    zIndex: 100,
    [theme.breakpoints.only('xs')]: {
      width: mobileDrawerWidth,
    },
  },
  commentBtnActive: {
    backgroundColor: '#14aaf5',
    color: 'white',
    fontSize: '9pt',
    fontWeight: 600,
    borderRadius: 50,
    border: 'none',
    marginLeft: 10,
    textTransform: 'none',
    '&:hover': {
      backgroundColor: '#098ccd !important',
    },
  },
  formControl: {
    width: '100%',
    marginBottom: 15,
  },
  commentBtnDisable: {
    backgroundColor: '#14aaf5',
    '&:hover': {
      backgroundColor: '#098ccd !important',
    },
  },
  select: {
    padding: '18.5px 14px',
  },
  picker: {
    width: '100%',
    marginBottom: 15,
    marginTop: 0,
    padding: 0,
  },
  inputClassName: {
    padding: '4.5px 10.5px 3.5px 10.5px',
  },
  addSubtaskButton: {
    width: 'fit-content',
    textTransform: 'none',
    color: '#b4b6b8',
    '&:hover': {
      color: '#3F51B5 !important',
    },
  },
  undeleteIcon: {
    '&:hover': {
      color: 'black',
      border: '1px solid black',
    },
    textTransform: 'none',
    padding: '2px 5px',
    color: '#9CA2A9',
    border: '1px solid #9CA2A9',
    marginRight: 5,
  },
  permanentlyDeleteIcon: {
    '&:hover': {
      background: '#E8384F',
      border: '1px solid #E8384F',
    },
    textTransform: 'none',
    padding: '2px 5px',
    background: '#FF5263',
    border: '1px solid #FF5263',
    color: 'white',
  },
  deleteContainer: {
    background: '#FFEDEF',
    justifyContent: 'space-between',
    display: 'flex',
    alignItems: 'center',
    padding: 12,
  },
  selectContainer: {
    background: '#F6F8F9',
    justifyContent: 'space-between',
    display: 'flex',
    alignItems: 'center',
    flexDirection: 'column',
    padding: 12,
  },
  subtaskContainer: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
  },
  subtaskHint: {
    color: '#6F7782',
    fontSize: 14,
  },
  commentInput: {
    width: '100%',
    '& div': {
      borderRadius: 16,
    },
    '& input': {
      fontSize: 14,
    },
  },
}));

export default function TaskForm({
  task: propTask,
  addNewSubtask,
  permanentlyDeleteTask,
  closeSubtaskOfClicked,
  isSubtaskOfClicked,
  removeAttachment,
  downloadAttachment,
  attachmentList: attachments,
  addComment,
  removeComment,
  loadMoreComments,
  comments,
  commentParameters,
  newSubtaskId,
  setNewSubtaskId,
  isFullScreen,
  isFullScreenOpen,
}) {
  const {
    onCardEdit,
    onCardDelete,
    userId,
    maxFiles,
    selectedProject = {},
    projectId,
    getAvatarColor,
    tasks,
    tasksToBeDeleted,
    removeTaskToBeDeleted,
    closeSnackbar,
  } = useTaskEditor();
  const { isShowPriority = false, isShowStatus = false, isShowProgress = false, isShowTaskCategory = false } =
    selectedProject || {};
  const [task, setTask] = useState({
    name: '',
    project: {},
    parentTask: {},
    assignedTo: {},
    status: '',
    priority: '',
    projectTaskCategory: '',
    taskDate: null,
    progressSelect: 0,
    taskEndDate: null,
    description: '',
  });
  const {
    name,
    project,
    parentTask,
    assignedTo,
    status,
    priority,
    taskDate,
    progressSelect,
    taskEndDate,
    description,
    projectTaskCategory,
  } = task || {};
  const classes = useStyles();
  const [, setContent] = useState(null);

  const [subTasks, setSubTasks] = useState([]);
  const [anotherParentTask, setAnotherParentTask] = useState(null);
  const isMobile = useMediaQuery('(max-width:600px)');

  const onUpdateTask = async task => {
    let res = await updateTask(task);
    if (res) {
      onCardEdit({ ...res, 'priority.technicalTypeSelect': task['priority.technicalTypeSelect'] });
      setTask({ ...res });
    }
  };
  const handleChange = e => {
    setTask({
      ...task,
      [e.target.name]: e.target.value,
    });
    if (e.target.name !== 'name') {
      handleBlur(e);
    }
  };

  const onChange = (val, name) => {
    let value = {
      ...task,
      [name]: (name === 'taskDate' || name === 'taskEndDate') && val ? moment(val).format('YYYY-MM-DD') : val,
    };
    if (name === 'status') {
      value.statusBeforeComplete = task.status;
    }
    if (name === 'priority') {
      value['priority.technicalTypeSelect'] = val['technicalTypeSelect'];
    }
    setTask(value);
    onUpdateTask(value);
  };

  const handleBlur = e => {
    onUpdateTask({
      ...task,
      [e.target.name]: e.target.value,
    });
  };

  const setDescription = description => {
    setContent(description);
  };

  const onDescriptionChange = e => {
    onUpdateTask({ ...task, description: e });
  };

  const reorder = (list, startIndex, endIndex) => {
    const result = Array.from(list);
    const [removed] = result.splice(startIndex, 1);
    result.splice(endIndex, 0, removed);
    return result;
  };

  const onDragEnd = result => {
    if (!result.destination) {
      return;
    }
    const items = reorder(subTasks, result.source.index, result.destination.index);
    setSubTasks(items);
  };

  const onDelete = subtask => {
    if (!subtask) return;
    const subtasks = [...subTasks];
    const subTaskIndex = subTasks.findIndex(task => task.id === subtask.id);
    subtasks.splice(subTaskIndex, 1);
    setSubTasks([...subtasks]);
    onCardDelete(subtask.id);
  };
  const unDelete = () => {
    removeTaskToBeDeleted(propTask.id);
    closeSnackbar();
  };

  const onEdit = subtask => {
    const subtasks = [...subTasks];
    const subTaskIndex = subTasks.findIndex(task => task.id === subtask.id);
    subtasks[subTaskIndex] = {
      ...subtask,
    };
    setSubTasks([...subtasks]);
    onCardEdit({ ...subtask });
  };

  const handleAddComment = React.useCallback(
    async commentMessage => {
      let res = await addComment(commentMessage);
      return res;
    },
    [addComment],
  );

  useEffect(() => {
    if (isMobile) {
      const iframe = window.top && window.top.document && window.top.document.getElementsByTagName('iframe');
      let parentElement = iframe && iframe[0] && iframe[0].parentElement;
      if (parentElement && parentElement.parentElement) {
        let element = parentElement.parentElement;
        if (element.className === 'html-view ng-scope' && element.style) {
          element.style.height = '100%';
        }
      }
    }
  }, [isMobile]);

  useEffect(() => {
    setTask({ ...propTask });
  }, [propTask]);

  useEffect(() => {
    const subTasks = tasks && tasks.filter(task => task.parentTask && task.parentTask.id === propTask.id);
    setSubTasks([...(subTasks || [])]);
  }, [tasks, propTask]);

  return (
    <Grid container>
      {tasksToBeDeleted.some(v => v.id === propTask.id) && (
        <Grid item xs={12} className={classes.deleteContainer}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <DeleteOutlineIcon style={{ color: '#E8384F' }} />
            <Typography style={{ color: '#E8384F', fontSize: 14 }}>{translate('This task is deleted')}</Typography>
          </div>
          <div>
            <Button variant="outlined" className={classes.undeleteIcon} onClick={unDelete}>
              {translate('Undelete')}
            </Button>
            <Button variant="outlined" className={classes.permanentlyDeleteIcon} onClick={permanentlyDeleteTask}>
              {translate('Delete Permanently')}
            </Button>
          </div>
        </Grid>
      )}
      {isSubtaskOfClicked && (
        <Grid item xs={12} className={classes.selectContainer}>
          <div className={classes.subtaskContainer}>
            <Typography className={classes.subtaskHint}>
              {translate('Make this task a subtask of another project task.')}
            </Typography>
            <IconButton onClick={closeSubtaskOfClicked}>
              <CloseIcon />
            </IconButton>
          </div>
          <div style={{ width: '100%' }}>
            <ColumnSelect
              model="com.axelor.apps.project.db.ProjectTask"
              title="Parent Task"
              optionLabel="fullName"
              name="parentTask"
              value={anotherParentTask}
              onChange={value => {
                setAnotherParentTask(value);
                onChange(value, 'parentTask');
                closeSubtaskOfClicked();
              }}
              data={parentTask}
              label={translate('Find a task')}
              criteria={[
                {
                  fieldName: 'id',
                  operator: '!=',
                  value: propTask.id,
                },
                {
                  fieldName: 'project.id',
                  operator: '!=',
                  value: projectId,
                },
              ]}
              operator="and"
            />
          </div>
        </Grid>
      )}
      <Grid item container style={{ padding: 15 }}>
        <Grid item xs={12}>
          <TextField
            style={{ width: '100%', marginBottom: 15 }}
            name="name"
            placeholder={translate('Subject')}
            variant="outlined"
            value={name}
            onChange={handleChange}
            onBlur={handleBlur}
          />
        </Grid>
        <Grid item xs={4}>
          <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
            {translate('Project')}
          </Typography>
        </Grid>
        <Grid item xs={8} className={classes.formControl}>
          <ColumnSelect
            model="com.axelor.apps.project.db.Project"
            title={translate('Project')}
            name="project"
            optionLabel="fullName"
            value={project}
            onChange={value => onChange(value, 'project')}
          />
        </Grid>
        <Grid item xs={4}>
          <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
            {translate('Parent Task')}
          </Typography>
        </Grid>
        <Grid item xs={8} className={classes.formControl}>
          <ColumnSelect
            model="com.axelor.apps.project.db.ProjectTask"
            title={translate('Parent Task')}
            optionLabel="fullName"
            name="parentTask"
            value={parentTask}
            onChange={value => onChange(value, 'parentTask')}
            disableClearable={false}
            data={parentTask}
            criteria={[
              {
                fieldName: 'id',
                operator: '!=',
                value: propTask.id,
              },
              {
                fieldName: 'project.id',
                operator: '=',
                value: projectId,
              },
            ]}
            operator="and"
          />
        </Grid>
        <Grid item xs={4}>
          <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
            {translate('Assigned To')}
          </Typography>
        </Grid>
        <Grid item xs={8} className={classes.formControl}>
          <AssigneeSelection
            model="com.axelor.auth.db.User"
            title={translate('Assigned To')}
            optionLabel="fullName"
            name="assignedTo"
            value={assignedTo}
            update={value => {
              onChange(value, 'assignedTo');
            }}
            options={selectedProject.membersUserSet}
            getAvatarColor={getAvatarColor}
          />
        </Grid>
        {isShowStatus && (
          <React.Fragment>
            <Grid item xs={4}>
              <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
                {translate('Status')}
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <StaticSelect
                className={classes.formControl}
                value={status}
                name="status"
                options={selectedProject.projectTaskStatusSet}
                onChange={(value, e) => onChange(value, e.target.name)}
                getAvatarColor={getAvatarColor}
              />
            </Grid>
          </React.Fragment>
        )}
        {isShowPriority && (
          <React.Fragment>
            <Grid item xs={4}>
              <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
                {translate('Priority')}
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TaskPrioritySelect
                className={classes.formControl}
                value={priority}
                options={selectedProject.projectTaskPrioritySet}
                onChange={(value, e) => onChange(value, e.target.name)}
              />
            </Grid>
          </React.Fragment>
        )}
        {isShowTaskCategory && (
          <React.Fragment>
            <Grid item xs={4}>
              <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
                {translate('Category')}
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <StaticSelect
                hasNoneOption
                className={classes.formControl}
                value={projectTaskCategory}
                name="projectTaskCategory"
                options={selectedProject.projectTaskCategorySet}
                onChange={(value, e) => onChange(value, e.target.name)}
                getAvatarColor={getAvatarColor}
              />
            </Grid>
          </React.Fragment>
        )}
        {isShowProgress && (
          <React.Fragment>
            <Grid item xs={4}>
              <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
                {translate('Progress')}
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <FormControl variant="outlined" style={{ width: '100%', marginBottom: 15 }}>
                <TaskProgressSelect t={translate} value={progressSelect} onChange={handleChange} />
              </FormControl>
            </Grid>
          </React.Fragment>
        )}
        <Grid item xs={4}>
          <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
            {translate('TaskEditor.taskDate')}
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <DatePicker
            className={classes.picker}
            inputClassName={classes.inputClassName}
            value={taskDate}
            name="taskDate"
            label={translate('TaskEditor.taskDate')}
            onChange={(value, e) => onChange(value, 'taskDate')}
          />
        </Grid>
        <Grid item xs={4}>
          <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
            {translate('Task end')}
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <DatePicker
            className={classes.picker}
            inputClassName={classes.inputClassName}
            value={taskEndDate}
            name="taskEndDate"
            label="Task end"
            onChange={(value, e) => onChange(value, 'taskEndDate')}
          />
        </Grid>
        <Grid item xs={4}>
          <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
            {translate('Description')}
          </Typography>
        </Grid>
        <Grid
          item
          xs={8}
          style={{
            width: '100%',
            marginBottom: 15,
            border: '1px solid #C4C4C4',
            borderRadius: 4,
          }}
        >
          <Editor
            placeholder="editor"
            content={description || ''}
            onContentChange={e => setDescription(e)}
            onDescriptionChange={e => onDescriptionChange(e)}
          />
        </Grid>
        {attachments && attachments.length > 0 && (
          <React.Fragment>
            <Grid item xs={12}>
              <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
                {translate('Attachments')}
              </Typography>
            </Grid>
            <Grid item xs={12}>
              <AttachmentList
                files={attachments}
                maxFiles={maxFiles}
                removeAttachment={removeAttachment}
                downloadAttachment={downloadAttachment}
              />
            </Grid>
          </React.Fragment>
        )}
        {subTasks && subTasks.length > 0 && (
          <Grid item xs={12}>
            <Typography variant="overline" className={classes.typography} display="block" gutterBottom>
              {translate('Sub Tasks')}
            </Typography>
          </Grid>
        )}
        {subTasks && subTasks.length > 0 && (
          <Grid item xs={12}>
            <DragDropContext onDragEnd={onDragEnd}>
              <Droppable droppableId="droppable">
                {provided => (
                  <div {...provided.droppableProps} ref={provided.innerRef}>
                    {subTasks.map(
                      (subtask, index) =>
                        subtask.parentTask &&
                        subtask.parentTask.id === task.id && (
                          <Draggable key={subtask.id} draggableId={`${subtask.id}`} index={index}>
                            {provided => (
                              <div ref={provided.innerRef} {...provided.draggableProps} {...provided.dragHandleProps}>
                                <CardItem
                                  record={subtask}
                                  onDelete={res => onDelete(res)}
                                  onEdit={res => onEdit(res)}
                                  newSubtaskId={newSubtaskId}
                                  setNewSubtaskId={setNewSubtaskId}
                                  isFullScreen={isFullScreen}
                                  isFullScreenOpen={isFullScreenOpen}
                                />
                              </div>
                            )}
                          </Draggable>
                        ),
                    )}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>
            </DragDropContext>
          </Grid>
        )}
        <Grid item xs={12} style={{ textAlign: 'left' }}>
          <Button startIcon={<AddIcon />} className={classes.addSubtaskButton} onClick={() => addNewSubtask()}>
            {translate('Add Subtask')}
          </Button>
        </Grid>
      </Grid>
      <Grid item xs={12}>
        <CommentList
          limit={commentParameters.limit}
          offset={commentParameters.offset}
          total={commentParameters.total}
          comments={comments}
          loadComment={loadMoreComments}
          removeComment={removeComment}
          userId={userId}
        />
      </Grid>
      <CommentInput handleAddComment={handleAddComment} taskId={propTask.id} />
    </Grid>
  );
}

function CommentInput({ handleAddComment, taskId }) {
  const classes = useStyles();
  const [commentMessage, setCommentMessage] = useState('');
  const handleAdd = async () => {
    const res = await handleAddComment(commentMessage);
    if (res) {
      setCommentMessage('');
    }
  };
  const handleKeyDown = e => {
    if (e.key === 'Enter') {
      handleAdd();
    }
  };

  useEffect(() => {
    setCommentMessage('');
  }, [taskId]);

  return (
    <Grid className={classes.drawerFooter}>
      <Grid style={{ flex: 1 }}>
        <TextField
          className={classes.commentInput}
          size="small"
          name="name"
          placeholder={translate('TaskEditor.comment')}
          variant="outlined"
          value={commentMessage}
          onChange={e => setCommentMessage(e.target.value)}
          onKeyDown={handleKeyDown}
        />
      </Grid>
      <Grid>
        <Button
          variant="contained"
          color="primary"
          className={classnames(classes.commentBtnActive, {
            [classes.commentBtnDisable]: !commentMessage,
          })}
          onClick={handleAdd}
        >
          {translate('Add')}
        </Button>
      </Grid>
    </Grid>
  );
}
