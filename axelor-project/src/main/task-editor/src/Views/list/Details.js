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
import React, { useState, useCallback, useMemo, useEffect } from 'react';
import DetailsToolbar from './DetailsToolbar';
import TaskForm from '../form/TaskForm';
import { FullScreenDialog, ConfirmationDialog } from '../../Components';
import { Drawer } from '@material-ui/core';
import {
  addTask,
  updateTask,
  uploadFile,
  addDMSFile,
  removeDMSFile,
  downloadDMSFile,
  getDMSFiles,
  removeMessage,
  addMessage,
  getMessages,
} from '../../Services/api';
import { useTaskEditor } from './Context';
import useOpenClose from './useOpenClose';
import { getCompletedStatus, filesToItems, getAttachmentBlob, getHeaders, getStatus, translate } from '../../utils';
import { makeStyles } from '@material-ui/core/styles';
import { mobileDrawerWidth, drawerWidth, TOOLBAR_HEIGHT } from '../../constants';
import ConvertToTaskProject from './menu/ConvertToTaskProject';
import DuplicateTaskDialog from './menu/DuplicateTaskDialog';
import PdfDialog from './menu/PdfDialog';

const useStyles = makeStyles(theme => ({
  drawer: {
    flexShrink: 0,
    height: '100%',
    overflow: 'hidden',
  },
  drawerPaper: {
    width: drawerWidth,
    [theme.breakpoints.only('xs')]: {
      width: mobileDrawerWidth,
    },
  },
  formView: {
    height: `calc(100% - ${TOOLBAR_HEIGHT}px - 66px)`,
    marginTop: TOOLBAR_HEIGHT,
    overflowY: 'auto',
  },
  fullScreenForm: {
    marginTop: 50,
    height: '100%',
    boxShadow: '0px 2px 4px -1px rgba(0,0,0,0.2), 0px 4px 5px 0px rgba(0,0,0,0.14), 0px 1px 10px 0px rgba(0,0,0,0.12)',
  },
}));

function Details({
  taskId,
  showFullScreen,
  showDrawer,
  onFullScreenClose,
  onDrawerClose,
  openSnackbar,
  copyToClipboard,
}) {
  const { tasks, onCardAdd, onCardEdit, project, userId, maxFiles, refresh, addTaskToBeDeleted } = useTaskEditor();
  const [isDeleteConfirmationDialogOpen, openDeleteConfirmationDialog, closeDeleteConfirmationDialog] = useOpenClose();
  const [isTaskToProjectDialogOpen, openTaskToProjectDialog, closeTaskToProjectDialog] = useOpenClose();
  const [isPdfDialogOpen, openPdfDialog, closePdfDialog] = useOpenClose();
  const [isDuplicateTaskDialogOpen, openDuplicateTaskDialog, closeDuplicateTaskDialog] = useOpenClose();
  const [isSubTaskOfAlertOpen, openSubtaskOfAlert, closeSubtaskOfAlert] = useOpenClose();
  const [attachmentList, setAttachmentList] = useState([]);
  const [newSubtaskId, setNewSubtaskId] = useState(null);
  const [comments, setComments] = useState([]);
  const [commentParameters, setCommentParameters] = useState({
    limit: 4,
    total: 0,
    offset: 0,
  });

  const classes = useStyles();

  const taskInView = useMemo(() => tasks.find(task => task.id === taskId), [tasks, taskId]);

  const subTasks = useMemo(() => tasks.filter(task => taskId && task.parentTask && task.parentTask.id === taskId), [
    tasks,
    taskId,
  ]);

  const isCompleted = useMemo(() => getStatus(taskInView, project), [taskInView, project]);

  const projectId = project && project.id;

  const addNewSubtask = useCallback(async () => {
    if (!taskInView) return;
    let res = await addTask({
      name: '',
      sequence: 1,
      project: { id: projectId },
      assignedTo: { id: userId },
      parentTask: { id: taskInView.id },
      projectTaskSection:
        !taskInView.projectTaskSection || !taskInView.projectTaskSection.id
          ? null
          : {
              id: taskInView.projectTaskSection.id,
              $version: taskInView.projectTaskSection.version,
            },
    });
    if (res) {
      setNewSubtaskId(res.id);
      onCardAdd(res);
    }
  }, [taskInView, onCardAdd, projectId, userId]);

  const onMenuTaskDelete = useCallback(() => {
    addTaskToBeDeleted(taskId, 10000);
  }, [taskId, addTaskToBeDeleted]);

  const toggleStatus = useCallback(
    async e => {
      e && e.stopPropagation();
      let status;
      if (isCompleted) {
        status = taskInView.statusBeforeComplete;
      } else {
        status = getCompletedStatus(project);
      }
      let res = await updateTask({
        ...taskInView,
        status,
        statusBeforeComplete: taskInView.status,
      });
      if (res) {
        onCardEdit({ ...res, 'priority.technicalTypeSelect': taskInView['priority.technicalTypeSelect'] });
      }
    },
    [onCardEdit, isCompleted, taskInView, project],
  );
  const uploadAttachment = attachmentList => {
    for (let i = 0; i < attachmentList.length; i++) {
      if (!attachmentList[i].id) {
        uploadChunk(attachmentList[i]);
      }
    }
  };
  const uploadChunk = async (file, offset = 0) => {
    let attachment = getAttachmentBlob(file);
    const chunkSize = 100000;
    const end = offset + chunkSize < attachment && attachment.size ? offset + chunkSize : attachment && attachment.size;
    const blob = attachment && attachment.slice(offset, end);
    const headers = getHeaders(file, offset);
    let result = await uploadFile(blob, headers);
    if (result && result.id) {
      addToDMS(result);
    } else {
      if (offset < attachment && attachment.size) {
        if (result.fileId) {
          file.id = result.fileId;
        }
        uploadChunk(file, chunkSize + offset);
      }
    }
  };

  const addToDMS = async result => {
    const payload = {
      fileName: result.fileName,
      metaFile: { id: result.id },
      relatedId: taskInView.id,
      relatedModel: 'com.axelor.apps.project.db.ProjectTask',
    };
    let res = await addDMSFile(payload);
    if (res && res.length) {
      setAttachmentList(attachmentList => [...(attachmentList || []), res[0]]);
      openSnackbar({
        severity: 'success',
        message: 'TaskEditor.uploadSuccess',
      });
    } else {
      openSnackbar({
        severity: 'error',
        message: 'TaskEditor.uploadError',
      });
    }
  };

  //maxFiles will be undefined,
  const selectFile = e => {
    let list = attachmentList
      ? attachmentList.concat([...filesToItems(e.target.files, maxFiles)])
      : filesToItems(e.target.files);
    uploadAttachment(list);
  };

  const removeAttachment = async file => {
    let res = await removeDMSFile([
      {
        id: file.id,
        version: file.version,
      },
    ]);
    if (res) {
      let targetIndex = attachmentList.findIndex(item => JSON.stringify(item) === JSON.stringify(file));
      attachmentList.splice(targetIndex, 1);
      setAttachmentList(attachmentList);
      openSnackbar({
        severity: 'success',
        message: 'TaskEditor.deleteSuccess',
      });
    } else {
      openSnackbar({
        severity: 'error',
        message: 'TaskEditor.error',
      });
    }
  };

  const downloadAttachment = useCallback(file => {
    downloadDMSFile(file);
  }, []);

  const loadMoreComments = async () => {
    const { limit, offset } = commentParameters;
    let res = await getMessages({
      offset: offset + 4,
      limit,
      id: taskId,
      relatedModel: 'com.axelor.apps.project.db.ProjectTask',
    });
    if (res) {
      let newComments = [...comments, ...res.comments];
      setComments(newComments);
      setCommentParameters({
        ...commentParameters,
        offset: res.offset,
        total: res.total,
      });
      return res;
    }
  };

  const removeComment = async item => {
    let { total } = commentParameters;
    let res = await removeMessage(item);
    if (res && res.id) {
      const targetIndex = comments.findIndex(i => i.id === res.id);
      comments.splice(targetIndex, 1);
      setComments(comments);
      setCommentParameters({ ...commentParameters, total: total - 1 });
      openSnackbar({
        message: 'TaskEditor.removeSuccess',
      });
    } else {
      openSnackbar({
        message: 'TaskEditor.error',
        severity: 'error',
      });
    }
  };

  const addComment = useCallback(
    async commentMessage => {
      if (!commentMessage) return;
      const res = await addMessage({
        id: taskId,
        entityModel: 'com.axelor.apps.project.db.ProjectTask',
        body: commentMessage,
      });
      if (res) {
        setComments(comments => [res, ...comments]);
        return res;
      }
    },
    [taskId],
  );
  const onDeleteConfirm = () => {
    onDrawerClose();
    onFullScreenClose();
    closeDeleteConfirmationDialog();
    addTaskToBeDeleted(taskId);
  };

  useEffect(() => {
    let isSameTaskId = true;
    // if taskId changes before request gets completed, then state won't update
    taskId && getDMSFiles(taskId).then(attachments => attachments && isSameTaskId && setAttachmentList(attachments));

    return () => {
      isSameTaskId = false;
      setAttachmentList([]);
      //reset comments only when taskId changes, It will be fetched  in other useEffect
      setComments([]);
      //close subtaskof snackbar when id changes
      closeSubtaskOfAlert();
    };
  }, [taskId, refresh, closeSubtaskOfAlert]);
  const taskVersion = taskInView && taskInView.version;
  useEffect(() => {
    let isSameTaskIdAndVersion = true;

    if (taskId && (showFullScreen || showDrawer)) {
      (async () => {
        let res = await getMessages({
          id: taskId,
          relatedModel: 'com.axelor.apps.project.db.ProjectTask',
        });
        // if taskId or version changes before request gets completed, then state won't update
        if (res && isSameTaskIdAndVersion) {
          setComments(res.comments);
          setCommentParameters({
            limit: 4,
            offset: res.offset,
            total: res.total,
          });
        }
      })();
    }
    return () => {
      isSameTaskIdAndVersion = false;
    };
  }, [taskId, taskVersion, showFullScreen, showDrawer, refresh]);

  useEffect(() => {
    // this will ensure modal/drawer will close if task gets deleted after refresh
    //this useEffect will run everytime
    if (!taskInView && (showDrawer || showFullScreen)) {
      onFullScreenClose();
      onDrawerClose();
    }
  });
  return (
    <React.Fragment>
      {showFullScreen && (
        <FullScreenDialog
          open={showFullScreen}
          handleFullScreenClose={onFullScreenClose}
          toolbar={
            <DetailsToolbar
              isFullScreen={true}
              toggleStatus={toggleStatus}
              isCompleted={isCompleted}
              attachmentList={attachmentList}
              selectFile={selectFile}
              addNewSubtask={addNewSubtask}
              subTaskCount={subTasks.length}
              copyToClipboard={copyToClipboard}
              handleClose={onFullScreenClose}
              onMenuTaskDelete={onMenuTaskDelete}
              openPdfDialog={openPdfDialog}
              openSubtaskOfAlert={openSubtaskOfAlert}
              openDuplicateTaskDialog={openDuplicateTaskDialog}
              openTaskToProjectDialog={openTaskToProjectDialog}
            />
          }
        >
          <div className={classes.fullScreenForm}>
            <TaskForm
              isFullScreen={true}
              task={taskInView}
              addNewSubtask={addNewSubtask}
              newSubtaskId={newSubtaskId}
              setNewSubtaskId={setNewSubtaskId}
              permanentlyDeleteTask={openDeleteConfirmationDialog}
              closeSubtaskOfClicked={closeSubtaskOfAlert}
              isSubtaskOfClicked={isSubTaskOfAlertOpen}
              removeAttachment={removeAttachment}
              downloadAttachment={downloadAttachment}
              attachmentList={attachmentList}
              removeComment={removeComment}
              addComment={addComment}
              loadMoreComments={loadMoreComments}
              comments={comments}
              commentParameters={commentParameters}
            />
          </div>
        </FullScreenDialog>
      )}
      <Drawer
        className={classes.drawer}
        variant="persistent"
        anchor="right"
        open={showDrawer}
        classes={{
          paper: classes.drawerPaper,
        }}
      >
        {taskInView && (
          <React.Fragment>
            <DetailsToolbar
              isFullScreen={false}
              toggleStatus={toggleStatus}
              isCompleted={isCompleted}
              attachmentList={attachmentList}
              selectFile={selectFile}
              addNewSubtask={addNewSubtask}
              subTaskCount={subTasks.length}
              copyToClipboard={copyToClipboard}
              handleClose={onDrawerClose}
              onMenuTaskDelete={onMenuTaskDelete}
              openPdfDialog={openPdfDialog}
              openSubtaskOfAlert={openSubtaskOfAlert}
              openDuplicateTaskDialog={openDuplicateTaskDialog}
              openTaskToProjectDialog={openTaskToProjectDialog}
            />
            <div className={classes.formView}>
              <TaskForm
                isFullScreen={false}
                isFullScreenOpen={showFullScreen}
                task={taskInView}
                addNewSubtask={addNewSubtask}
                newSubtaskId={newSubtaskId}
                setNewSubtaskId={setNewSubtaskId}
                permanentlyDeleteTask={openDeleteConfirmationDialog}
                closeSubtaskOfClicked={closeSubtaskOfAlert}
                isSubtaskOfClicked={isSubTaskOfAlertOpen}
                removeAttachment={removeAttachment}
                downloadAttachment={downloadAttachment}
                attachmentList={attachmentList}
                removeComment={removeComment}
                addComment={addComment}
                loadMoreComments={loadMoreComments}
                comments={comments}
                commentParameters={commentParameters}
              />
            </div>
          </React.Fragment>
        )}
      </Drawer>
      {taskInView && (
        <React.Fragment>
          {isDeleteConfirmationDialogOpen && (
            <ConfirmationDialog
              open={isDeleteConfirmationDialogOpen}
              onClose={closeDeleteConfirmationDialog}
              onConfirm={onDeleteConfirm}
              title={translate('Delete Task')}
            />
          )}

          <ConvertToTaskProject
            open={isTaskToProjectDialogOpen}
            handleClose={closeTaskToProjectDialog}
            task={taskInView}
            openSnackbar={openSnackbar}
          />
          {isPdfDialogOpen && (
            <PdfDialog open={isPdfDialogOpen} handleClose={closePdfDialog} task={taskInView} subTasks={subTasks} />
          )}
          <DuplicateTaskDialog
            open={isDuplicateTaskDialogOpen}
            handleClose={closeDuplicateTaskDialog}
            task={taskInView}
            attachmentList={attachmentList}
            onCardAdd={onCardAdd}
            openSnackbar={openSnackbar}
          />
        </React.Fragment>
      )}
    </React.Fragment>
  );
}

export default Details;
