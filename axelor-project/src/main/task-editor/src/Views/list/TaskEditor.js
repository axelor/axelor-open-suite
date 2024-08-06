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
import React, { useState, useEffect, useReducer } from 'react';
import { makeStyles } from '@material-ui/core/styles';

import {
  fetchTasks,
  fetchSections,
  updateTask,
  deleteTask,
  deleteSection,
  updateSection,
  addTask,
  getInfo,
  saveFilter,
  deleteFilter,
  getFilters,
  fetchFields,
  getProject,
} from '../../Services/api';
import List from './List';
import { TaskEditorProvider } from './Context';
import { Snackbar } from '../../Components';
import { sortByKey, getColumns, reorder, getJsonField, transformCriteria, translate } from '../../utils';
import { getFilteredTask, getAvatarColor, getConnectedTaskIds } from './TaskEditor.utils';

import Details from './Details';

const useStyles = makeStyles(theme => ({
  root: {
    height: '100%',
    overflow: 'hidden',
    position: 'relative',
  },
  overlay: {
    position: 'absolute',
    height: '100%',
    width: '100%',
    background: 'rgba(0, 0, 0, 0.1)',
    zIndex: 1000,
  },
}));

const updateSequences = async (columns = []) => {
  return Promise.all(columns.map(c => updateTask(c, true)));
};

const updateSectionSequences = async (columns = []) => {
  return Promise.all(columns.map(c => updateSection(c, true)));
};

async function reorderCards({ columns, sourceColumn, destinationColumn, sourceIndex, destinationIndex }) {
  const getColumnIndex = columnId => columns.findIndex(c => c.id === columnId);
  const getRecords = columnId => [...(columns.find(c => c.id === columnId).records || [])];
  const getExistingSequence = (recordId, records) => records.find(r => r.id === recordId).sequence;
  const getExistingSection = (recordId, records) => {
    return (records.find(r => r.id === recordId).projectTaskSection || {}).id;
  };
  const updateSequence = records =>
    records.map((r, i) => ({
      ...r,
      sequence: i + 1,
    }));
  const getUpdateList = (records, previousRecords) => {
    const list = records.filter(
      r =>
        r.sequence !== getExistingSequence(r.id, previousRecords) ||
        (r.projectTaskSection || {}).id !== getExistingSection(r.id, previousRecords),
    );
    return list;
  };

  const current = getRecords(sourceColumn.id);
  const isSameList = sourceColumn.id === destinationColumn.id;

  // moving to same list
  if (isSameList) {
    const reorderList = updateSequence(reorder(current, sourceIndex, destinationIndex));
    const updateList = getUpdateList(reorderList, current); // Update only those records whose sequence has changed
    const updatedColumns = columns.slice();
    const columnIndex = getColumnIndex(sourceColumn.id);
    updatedColumns[columnIndex] = {
      ...updatedColumns[columnIndex],
      records: reorderList,
    };
    return [updatedColumns, updateSequences(updateList)];
  }

  const next = getRecords(destinationColumn.id);
  const target = current[sourceIndex];

  // moving to different list
  current.splice(sourceIndex, 1);
  next.splice(destinationIndex, 0, target);

  const reorderList = updateSequence(next).map((r, i) => ({
    ...r,
    projectTaskSection:
      destinationColumn && destinationColumn.id === -1
        ? null
        : { id: destinationColumn.id, name: destinationColumn.name, $version: destinationColumn.version },
  }));

  const updateList = getUpdateList(reorderList, next);

  // Update only those records whose sequence has changed
  const updatedColumns = columns.slice();

  const sourceColumnIndex = getColumnIndex(sourceColumn.id);
  updatedColumns[sourceColumnIndex] = {
    ...updatedColumns[sourceColumnIndex],
    records: current,
  };

  const destColumnIndex = getColumnIndex(destinationColumn.id);
  updatedColumns[destColumnIndex] = {
    ...updatedColumns[destColumnIndex],
    records: reorderList,
  };

  return [updatedColumns, updateSequences(updateList)];
}

function Container(props) {
  const { projectId } = props;
  const [refresh, forceRefresh] = useReducer(count => count + 1, 0);
  const [columns, setColumns] = useState([]);
  const [openTaskInFullScreen, setOpenTaskInFullScreen] = useState(false);
  const [openTaskInDrawer, setOpenTaskInDrawer] = useState(false);
  const [selectedTaskId, setSelectedTaskId] = useState(null);
  const [sections, setSections] = useState([]);
  const [info, setInfo] = useState(null);
  const [searchFilter, setSearchFilter] = useState();
  const [filters, setFilters] = useState([]);
  const [contentSearch, setContentSearch] = useState();
  const [snackbarProps, setSnackbarProps] = useState({
    message: '',
    open: false,
    severity: 'success',
  });
  const [fields, setFields] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [newTaskId, setNewTaskId] = useState(null);
  const [tasksToBeDeleted, setTasksToBeDeleted] = useState([]);
  const [project, setProject] = useState(null);
  const [menuFilter, setMenuFilter] = useState({
    code: 'incompleteTasks',
    value: 'Incomplete tasks',
  });
  const [filter, setFilter] = useState(null);
  const [sortColumn, setSortColumn] = useState(null);
  const [loading, setLoading] = useState(false);
  const classes = useStyles();

  const userId = info && info['user.id'];

  const handleSetSortColumnName = column => {
    if (sortColumn && sortColumn.code === column.code) {
      column = { code: 'sequence', name: translate('TaskEditor.none'), sortFunction: sortByKey };
    }
    setSortColumn(column);
  };

  const handleSetFilter = filter => {
    setFilter(filter);
  };

  const handleSetMenuFilter = filter => {
    setMenuFilter(filter);
  };

  const closeSnackbar = () => {
    setSnackbarProps({
      ...snackbarProps,
      open: false,
    });
  };

  const openSnackbar = props => {
    setSnackbarProps({
      open: true,
      ...props,
    });
  };
  const handleChangeFilter = React.useCallback(filter => setSearchFilter(filter), []);

  const handleSaveFilter = React.useCallback(async filter => {
    const savedFilter = await saveFilter(filter);
    setFilters(currentFilters => {
      const index = currentFilters.find(f => f.id === savedFilter.id);
      if (index === -1) currentFilters.push(savedFilter);
      return [...currentFilters];
    });
    return savedFilter;
  }, []);

  const handleDeleteFilter = React.useCallback(async filter => {
    await deleteFilter(filter);
    setFilters(currentFilters => {
      const index = currentFilters.find(f => f.id === filter.id);
      if (index !== -1) currentFilters.splice(index, 1);
      return [...currentFilters];
    });
  }, []);

  const handleContentSearch = React.useCallback(value => {
    setContentSearch(value);
  }, []);

  const getColumnIndex = React.useCallback(
    function getColumnIndex(id) {
      return columns.findIndex(c => c.id === id);
    },
    [columns],
  );

  const getRecordIndex = React.useCallback(
    function getRecordIndex(recordId, id) {
      let task = tasks.find(task => task.id === recordId) || {};
      if (!id && !task.id) return;
      let index = columns[getColumnIndex(id)] && columns[getColumnIndex(id)].records.findIndex(c => c.id === recordId);
      if (index < 0) {
        index = columns[getColumnIndex(id)].records.length;
      }
      return index;
    },
    [columns, getColumnIndex, tasks],
  );

  const onColumnAdd = React.useCallback(
    function onColumnAdd({ column }, isLastIndex) {
      if (getColumnIndex(column.id) !== -1) {
        alert('Column with same name already exists.');
        return;
      }
      setSections(sections => [...(sections || []), column]);
    },
    [getColumnIndex],
  );

  const onCardDelete = React.useCallback(
    async function onCardDelete(id, showSuccess = true, showFailure = true) {
      if (tasks.find(task => task.id === id)) {
        let res = await deleteTask(id);
        if (res.status !== -1) {
          //delete all subtask recursively when parent task is deleted
          const idsToDelete = getConnectedTaskIds(id, tasks);
          setTasks(tasks.filter(task => !idsToDelete.includes(task.id)));
          setTasksToBeDeleted(tasks => tasks.filter(task => !idsToDelete.includes(task.id)));
          showSuccess &&
            openSnackbar({
              severity: 'success',
              message: 'TaskEditor.taskDeleteSuccess',
            });
        } else {
          showFailure &&
            openSnackbar({
              severity: 'error',
              message: res && res.data && res.data.title,
            });
        }
      }
    },
    [tasks],
  );

  const onCardAdd = React.useCallback(function onCardAdd(record) {
    setTasks(tasks => [...tasks, { ...record }]);
  }, []);

  // if duaration is not provided, task will be permanently deleted, if not task will be hidden,after timeout ,it will get deleted unless cancelled
  const addTaskToBeDeleted = (givenId, duration = 0) => {
    if (givenId) {
      const deleteNow = duration <= 0;
      const idsToDelete = getConnectedTaskIds(givenId, tasks);
      let timer = null;
      if (!deleteNow) {
        timer = setTimeout(() => {
          addTaskToBeDeleted(givenId);
          //caveat: when callback executes,it will have reference to old tasks,since tasks never gets used,it will cause no issues.
        }, duration);
        openSnackbar({
          severity: 'success',
          message: 'TaskEditor.taskDeleteSuccess',
          isAction: true,
          onActionClick: () => {
            clearTimeout(timer);
            removeTaskToBeDeleted(givenId);
          },
          autoHideDuration: duration,
          clickAway: true,
        });
      }
      // comparator function for 'some'. Also clears previous timer
      //this is to ensure if multiple calls to delete the same id is made, previous timer will be cleared and new one will be added.
      //caveat:clears it's own timer when callback from timeout is executed. It will cause no issues.
      const testAndClearTimer = v => {
        const doesExist = v.id === givenId;
        doesExist && v.timer && clearTimeout(v.timer);
        return doesExist;
      };
      setTasksToBeDeleted(toBeDeleted => {
        //if given Id is already in the list , update it, other wise add it.
        if (toBeDeleted.some(testAndClearTimer)) {
          return toBeDeleted.map(v => (v.id === givenId ? { ...v, deleteNow, timer } : v));
        }
        return [
          ...toBeDeleted,
          ...idsToDelete.map(id => ({
            id,
            //since other connected ids get automatically deleted  by the server, deleteNow & timer set to false & null
            deleteNow: id === givenId ? deleteNow : false,
            timer: id === givenId ? timer : null,
          })),
        ];
      });
    }
  };
  // unhide the task if not permanently deleted and clear timeouts
  const removeTaskToBeDeleted = id => {
    const idsToRemove = getConnectedTaskIds(id, tasks);
    setTasksToBeDeleted(toBeDeleted =>
      toBeDeleted.filter(v => {
        v.id === id && v.timer && clearTimeout(v.timer);
        return !idsToRemove.includes(v.id);
      }),
    );
  };

  const onCardEdit = React.useCallback(
    function onCardEdit(record) {
      const cloneTasks = [...tasks];
      const index = cloneTasks && cloneTasks.findIndex(t => t.id === record.id);
      if (index > -1) {
        cloneTasks[index] = record;
        setTasks(cloneTasks);
      }
    },
    [tasks],
  );

  const onCardMove = React.useCallback(
    async function onCardMove({ column, index, source, record }) {
      const [updatedColumns, updatePromise] = await reorderCards({
        columns,
        destinationColumn: column,
        destinationIndex: index,
        sourceColumn: source,
        sourceIndex: getRecordIndex(record.id, source.id),
      });

      setLoading(true);
      updatePromise
        .then(result => {
          // Check if any update fail in sequence update
          const error = result.some(({ status }) => status === -1);
          if (error) {
            setTasks(tasks => [...tasks]);
            openSnackbar({
              severity: 'error',
              message: 'TaskEditor.concurrencyError',
            });
          } else {
            setTasks(tasks => {
              const updatedTasks = [...tasks];
              result.forEach(res => {
                const task = res && res.data && res.data[0];
                if (task) {
                  const ind = updatedTasks.findIndex(t => t.id === task.id);
                  if (ind > -1) {
                    updatedTasks[ind] = {
                      ...updatedTasks[ind],
                      ...task,
                    };
                  }
                }
              });
              return updatedTasks;
            });
          }
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
        });
      setColumns(updatedColumns);
    },
    [columns, getRecordIndex],
  );

  const onColumnMove = React.useCallback(
    function onColumnMove({ column, index }) {
      const getOldSequenceById = columnId => columns.find(c => c.id === columnId).sequence;

      const dragIndex = getColumnIndex(column.id);
      const hoverIndex = index;
      const reorderList = reorder(columns, dragIndex, hoverIndex).map((c, i) => ({
        ...c,
        sequence: i,
      }));

      // Update only those columns whose sequence has changed
      const updateList = reorderList
        .filter(c => c.sequence !== getOldSequenceById(c.id))
        .map(({ id, version, sequence }) => ({
          id,
          version,
          sequence,
        }));

      setLoading(true);
      updateSectionSequences(updateList)
        .then(result => {
          // Check if any update fail in sequence update
          const error = result.some(({ status }) => status === -1);
          if (error) {
            setSections(sections => [...sections]);
            openSnackbar({
              severity: 'error',
              message: 'TaskEditor.concurrencyError',
            });
          } else {
            setSections(sections => {
              const updatedSections = [...sections];
              result.forEach(res => {
                const section = res && res.data && res.data[0];
                if (section) {
                  const ind = updatedSections.findIndex(s => s.id === section.id);
                  if (ind > -1) {
                    updatedSections[ind] = {
                      ...updatedSections[ind],
                      ...section,
                    };
                  }
                }
              });
              return updatedSections;
            });
          }
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
        });

      setColumns(reorderList);
    },
    [columns, getColumnIndex],
  );

  const onColumnToggle = React.useCallback(
    function onColumnToggle({ column }) {
      const index = getColumnIndex(column.id);
      setColumns(_columns => {
        const columns = _columns.slice();
        columns[index] = {
          ...columns[index],
          collapsed: !columns[index].collapsed,
        };
        return columns;
      });
    },
    [getColumnIndex],
  );

  const onColumnUpdate = React.useCallback(function onColumnUpdate({ column: section }) {
    setSections(sections =>
      sections.map(s =>
        s.id === section.id
          ? {
              ...s,
              name: section.name,
              version: section.version,
            }
          : s,
      ),
    );
  }, []);

  const onColumnDelete = React.useCallback(async function onColumnDelete({ column: section }) {
    let res = await deleteSection(section.id);
    if (res.status !== -1) {
      setSections(sections => sections.filter(s => s.id !== section.id));
      openSnackbar({
        severity: 'success',
        message: 'TaskEditor.sectionDeleteSuccess',
      });
    } else {
      openSnackbar({
        severity: 'error',
        message: res && res.data && res.data.title,
      });
    }
  }, []);

  const addNewTask = async sectionIndex => {
    // ToDo replace with dynamic section ( based on selected or other criteria)
    let section = columns[sectionIndex];
    if (!section) section = columns[0];
    try {
      let newTask = await addTask({
        name: '',
        sequence: 1,
        project: projectId && { id: projectId },
        assignedTo: { id: userId },
        projectTaskSection:
          section.id === -1
            ? null
            : {
                id: section.id,
                $version: section.version,
                name: section.name,
              },
      });
      if (newTask) {
        setTasks(tasks => [newTask, ...tasks]);
        setNewTaskId(newTask.id);
        openSnackbar({
          severity: 'success',
          message: 'TaskEditor.taskAddSuccess',
        });
      }
    } catch (err) {
      openSnackbar({
        severity: 'error',
        message: 'TaskEditor.taskAddError',
      });
    }
  };

  function handleListKeyDown(event) {
    if (event.key === 'Tab') {
      event.preventDefault();
    }
  }

  const copyToClipboard = React.useCallback((recordName, message) => {
    navigator.clipboard.writeText(recordName || window.location.href);
    setSnackbarProps({
      open: true,
      message: message || translate('Copied to clipboard'),
    });
  }, []);

  const toggleTaskDrawer = (open, id) => {
    setOpenTaskInDrawer(open);
    if (open && id) setSelectedTaskId(id);
  };
  const toggleTaskFullScreen = (open, id) => {
    setOpenTaskInFullScreen(open);
    if (open && id) setSelectedTaskId(id);
  };

  // fetch project by projectId from url i.e. ?id=1
  useEffect(() => {
    (async () => {
      const project = await getProject(projectId, {
        related: {
          projectTaskPrioritySet: ['id', 'name', 'technicalTypeSelect', 'version'],
          projectTaskStatusSet: ['id', 'isCompleted', 'isDefaultCompleted', 'name', 'sequence', 'version'],
        },
      });
      setProject(project);
    })();
  }, [projectId, refresh]);

  // fetch user info, meta filter, meta fields, sections
  useEffect(() => {
    (async () => {
      const info = await getInfo();
      const filters = await getFilters();
      const sections = await fetchSections();
      const { jsonFields = {}, fields: modelFields = [] } =
        (await fetchFields('com.axelor.apps.project.db.ProjectTask')) || {};

      const mainFields = modelFields.filter(x => !jsonFields[x.name] && !['id', 'version'].includes(x.name));
      const extraFields = Object.keys(jsonFields).reduce((fields, jsonPath) => {
        const _fieldsObj = jsonFields[jsonPath];
        const _fieldInfo = modelFields.find(x => x.name === jsonPath);
        return [
          ...fields,
          ...Object.keys(_fieldsObj).map(k => getJsonField(_fieldInfo || { name: jsonPath }, _fieldsObj[k])),
        ];
      }, []);
      setFields([...mainFields, ...extraFields]);
      setInfo(info);
      setFilters(filters);
      setSections(sections);
      if (refresh) {
        openSnackbar({
          severity: 'success',
          message: 'Refreshed!!',
        });
      }
    })();
  }, [refresh]);

  const searchCriteria = searchFilter && searchFilter.query;

  // fetch tasks with criteria and compute with fields
  useEffect(() => {
    if (fields.length > 0) {
      const criteria = [].concat(searchCriteria ? [searchCriteria] : []);
      const addCriteria = (fieldName, operator, value) =>
        criteria.push({
          fieldName,
          operator,
          value,
        });

      if (projectId) {
        addCriteria('project.id', '=', projectId);
      }

      if (contentSearch) {
        addCriteria('name', 'like', contentSearch);
      }

      let data = {};
      let transformCriteriaValue = transformCriteria(criteria, fields);
      if (transformCriteriaValue.criteria.length > 0) {
        data = { ...transformCriteriaValue, operator: 'and' };
      }

      // fetch tasks
      fetchTasks({
        data,
      }).then(tasks => tasks && setTasks(tasks));
    }
  }, [fields, searchCriteria, contentSearch, projectId]);

  const menuFilterCode = menuFilter?.code;
  const filterCode = filter?.code;

  // compute columns from tasks and sections with sort
  useEffect(() => {
    const $tasks = getFilteredTask(tasks, menuFilterCode, filterCode, { userId, project });
    const columns = getColumns($tasks, sections) || [];
    const { code = 'sequence', sortFunction = sortByKey, fieldName } = sortColumn || {};
    setColumns(_columns =>
      columns.map(column => {
        const restoreColumn = (_columns || []).find(c => c.id === column.id);
        return {
          ...restoreColumn,
          ...column,
          records: sortFunction(column.records || [], code, fieldName),
        };
      }),
    );
  }, [tasks, sections, sortColumn, menuFilterCode, filterCode, userId, project]);

  // delete tasks that are to be deleted immediately
  useEffect(() => {
    const toDelete = tasksToBeDeleted.find(({ deleteNow }) => deleteNow);
    if (toDelete) {
      if (selectedTaskId !== toDelete.id || !(openTaskInDrawer || openTaskInFullScreen)) onCardDelete(toDelete.id);
    }
  }, [openTaskInDrawer, openTaskInFullScreen, tasksToBeDeleted, selectedTaskId, onCardDelete]);

  return (
    <TaskEditorProvider
      columns={columns}
      onCardAdd={onCardAdd}
      onCardEdit={onCardEdit}
      onCardMove={onCardMove}
      onCardDelete={onCardDelete}
      onColumnAdd={onColumnAdd}
      onColumnMove={onColumnMove}
      onColumnToggle={onColumnToggle}
      onColumnDelete={onColumnDelete}
      onColumnUpdate={onColumnUpdate}
      sections={sections}
      searchFilter={searchFilter}
      filters={filters}
      handleChangeFilter={handleChangeFilter}
      handleSaveFilter={handleSaveFilter}
      handleDeleteFilter={handleDeleteFilter}
      handleContentSearch={handleContentSearch}
      fields={fields}
      addNewTask={addNewTask}
      newTaskId={newTaskId}
      setNewTaskId={setNewTaskId}
      project={project}
      tasks={tasks}
      selectedTaskId={selectedTaskId}
      copyToClipboard={copyToClipboard}
      handleSetMenuFilter={handleSetMenuFilter}
      menuFilter={menuFilter}
      filter={filter}
      handleSetFilter={handleSetFilter}
      sortColumnName={sortColumn}
      handleSetSortColumnName={handleSetSortColumnName}
      selectedProject={project}
      userId={userId}
      language={info && info['user.lang']}
      projectId={projectId}
      userData={info}
      handleListKeyDown={handleListKeyDown}
      getAvatarColor={getAvatarColor}
      openTaskInDrawer={openTaskInDrawer}
      toggleTaskDrawer={toggleTaskDrawer}
      toggleTaskFullScreen={toggleTaskFullScreen}
      openSnackbar={openSnackbar}
      closeSnackbar={closeSnackbar}
      forceRefresh={forceRefresh}
      refresh={refresh}
      addTaskToBeDeleted={addTaskToBeDeleted}
      removeTaskToBeDeleted={removeTaskToBeDeleted}
      tasksToBeDeleted={tasksToBeDeleted}
      {...props}
    >
      <div className={classes.root}>
        {loading && <div className={classes.overlay} />}
        <List />
        <Details
          taskId={selectedTaskId}
          showFullScreen={openTaskInFullScreen}
          showDrawer={openTaskInDrawer}
          onFullScreenClose={() => toggleTaskFullScreen(false)}
          onDrawerClose={() => toggleTaskDrawer(false)}
          openSnackbar={openSnackbar}
          copyToClipboard={copyToClipboard}
        />
        <Snackbar {...snackbarProps} closeSnackbar={closeSnackbar} />
      </div>
    </TaskEditorProvider>
  );
}

export default function TaskEditorWrapper() {
  const projectId = React.useMemo(() => {
    const params = new URL(window.location).searchParams;
    return params.get('id');
  }, []);
  return <Container projectId={projectId} />;
}
