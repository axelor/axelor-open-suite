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
import React, { Component } from "react";
import moment from "moment";
import {
  ButtonGroup,
  ButtonToolbar,
  Glyphicon,
  FormControl,
  Label,
  Modal,
  FormGroup,
  ControlLabel,
} from "react-bootstrap";

import TimeSheet from "./timeSheet";
import "./main.css";
import Service from "./../services/service";
import { translate } from "./cellComponent";
import { connect } from "react-redux";
import { changeMode, changeKeyPress } from "./../store/redux";

export const validateDuration = (duration) => {
  const numbers = duration.split(":");
  if (numbers.length <= 2 && numbers.length >= 1) {
    const [hr, mins] = numbers;
    if (
      (!isNaN(hr) && mins === undefined && hr <= 60 && hr >= 0) ||
      (mins !== undefined && !isNaN(mins) && mins >= 0 && mins <= 60)
    )
      return true;
  }
  return false;
};

export const convertDuration = (duration) => {
  const numbers = duration.split(".");
  const [hr, mins] = numbers;
  let newDuration = null;
  let newHr = hr === undefined ? 0 : hr;
  let newMins = mins === undefined ? 0 : mins;
  if (mins > 59) {
    newMins = mins % 60;
    newHr += mins / 60;
  }
  newDuration = `${newHr}:${newMins}`;
  return newDuration;
};

export const convertTimeToNumber = (duration) => {
  const time = duration.split(":");
  const [hr, mins] = time;
  let newDuration = null;
  let newHr = hr === undefined ? 0 : Number(hr);
  let newMins = mins === undefined ? 0 : Number(mins);
  if (mins > 59) {
    newMins = mins % 60;
    newHr += mins / 60;
  }
  newDuration = newHr + newMins / 60;
  return `${newDuration}`;
};

const convertToTimeDigit = (number = null) => {
  let newNumber = "00";
  if (number.length >= 2) {
    newNumber = number;
  } else if (number.length === 1) {
    newNumber = `0${number}`;
  }
  return newNumber;
};

export const convertNumberToTime = (duration) => {
  let time = duration;
  if (duration.indexOf(":") < 0) {
    const numbers = duration.split(".");
    const [hr, mins] = numbers;
    time = convertToTimeDigit(hr);
    let minutes = convertToTimeDigit(
      `${Math.round(parseFloat(`0.${mins}`) * 60)}`
    );
    time = `${time}:${minutes}`;
  }
  return time;
};

const week = [
  "sunday",
  "monday",
  "tuesday",
  "wednesday",
  "thursday",
  "friday",
  "saturday",
];

class Container extends Component {
  constructor(props) {
    super(props);
    this.state = {
      show: false,
      html: 1,
      editor: {
        project: {},
        task: {},
        date: moment().format("YYYY-MM-DD"),
        duration: "0",
      },
      timesheet: {},
      user: {},
      selectedProjectTask: [],
      confirmPopUp: false,
      confirmMessage: "",
      currentDate: moment().format("DD-MM-YYYY"),
      fromDate: moment().format("YYYY-MM-DD"),
      toDate: "null",
      modeCount: 0,
      rowDates: [],
      dateWise: {},
      tasks: {},
      taskTotal: {},
      grandTotal: 0,
      mode: "week", // week/month
      projectTaskList: [],
      projectTasks: [],
      subTaskList: [],
      taskDateFormat: "YYYY-MM-DD",
      taskData: [],
      timeSheetUser: null,
      params: {},
      defaultActivity: null,
      sortingList: [
        {
          fieldName: "project.fullName",
          sort: "asc",
          sortBy: "project.fullName",
        },
        {
          fieldName: "product.fullName",
          sort: "asc",
          sortBy: "product.fullName",
        },
      ],
      collapseProject: [],
      HRConfig: {},
    };
  }

  getGanttData(tasks) {
    const GanttView = {};
    let counter = 1;
    Object.keys(tasks).forEach((t) => {
      const { user, projectId, project, taskId = 0, task } = tasks[t];
      const userIndex = `${counter++}u_${user}`;
      if (!GanttView[userIndex]) {
        GanttView[userIndex] = {
          title: user,
          level: 0,
        };
      }
      const projectIndex = `${counter++}p_${projectId}`;
      if (!GanttView[projectIndex]) {
        GanttView[projectIndex] = {
          title: project,
        };
      }
      const taskIndex = `${counter++}t_${taskId}`;
      if (!GanttView[taskIndex]) {
        GanttView[taskIndex] = {
          title: task,
          level: 2,
        };
      }
    });
  }

  groupTasks(data, rowDates) {
    let tasks = {};
    let dateWise = {};
    let taskTotal = {};
    const {
      HRConfig,
      params: { showActivity },
    } = this.state;
    const { uniqueTimesheetProduct } = HRConfig;
    const uniqueTaskId = uniqueTimesheetProduct && uniqueTimesheetProduct.id;
    const getDD = (dd) =>
      moment(dd, this.state.taskDateFormat).format(this.state.taskDateFormat);

    data.forEach((t) => {
      const { user, project, projectId, task, taskId, projectTask, id } = t;

      let projectTaskId = projectTask && projectTask.id;
      if (!showActivity && projectTaskId === null) {
        projectTaskId = undefined;
      }

      let projectKey = !showActivity
        ? `project_${projectId}_${projectTaskId}`
        : `project_${projectId}`;
      if (!tasks[projectKey]) {
        const isCollapse =
          this.state.collapseProject.filter((p) => p === projectId).length > 0;
        tasks[projectKey] = {
          user,
          project,
          projectId,
          title: project,
          isCollapse,
          projectTask: !showActivity ? projectTask : {},
          projectTaskId,
          id,
        };
      }
      /* check selected project line when  useUniqueProductForTimesheet is true*/
      if (!showActivity) {
        const selected =
          this.state.selectedProjectTask.filter(
            (s) =>
              s.projectId === projectId &&
              s.taskId === uniqueTaskId &&
              s.projectTaskId === projectTaskId
          ).length > 0;
        tasks[projectKey].selected = selected;
      }

      /* check collapse data */
      const isProjectCollapse =
        this.state.collapseProject.filter((p) => p === projectId).length > 0;
      if (isProjectCollapse || !showActivity) {
        return;
      }
      let projectTaskKey;
      if (projectTaskId === undefined) {
        projectTaskKey = `${projectId}_${taskId}_null`;
      } else {
        projectTaskKey = `${projectId}_${taskId}_${projectTaskId}`;
      }

      if (!tasks[projectTaskKey]) {
        const selected =
          this.state.selectedProjectTask.filter(
            (s) =>
              s.projectId === projectId &&
              s.taskId === taskId &&
              s.projectTaskId === projectTaskId &&
              s.id === id
          ).length > 0;
        tasks[projectTaskKey] = {
          user,
          project,
          projectId,
          task,
          taskId,
          selected,
          title: task,
          projectTask,
          projectTaskId,
          id,
        };
      }
    });
    Object.keys(tasks).forEach((t) => {
      if (t.startsWith("project")) {
        let { projectId, projectTaskId, id } = tasks[t];
        if (!showActivity && projectTaskId === undefined) {
          projectTaskId = null;
        }

        const isProjectExists = data.filter((e) => {
          return (
            e.projectId === projectId &&
            e.projectTaskId === projectTaskId &&
            e.id === id
          );
        });

        if (!isProjectExists) {
          delete tasks[t];
        } else {
          const curTask = data.filter((e) => {
            if (!showActivity)
              return (
                e.projectId === projectId &&
                e.projectTaskId === projectTaskId &&
                rowDates.indexOf(e.date) > -1
              );
            return e.projectId === projectId && rowDates.indexOf(e.date) > -1;
          });
          const projectTotalKey = `total_${t}_${projectId}`;
          if (curTask.length === 0) {
            taskTotal[projectTotalKey] = 0;
          } else {
            curTask.forEach((tsk) => {
              if (!taskTotal[projectTotalKey]) {
                taskTotal[projectTotalKey] = 0;
              }
              taskTotal[projectTotalKey] += tsk.duration;
            });
          }
        }
      } else {
        const { taskId, projectId, projectTaskId} = tasks[t];
        /* check collapse data */
        const isProjectCollapse =
          this.state.collapseProject.filter((p) => p === projectId).length > 0;
        if (isProjectCollapse || !showActivity) {
          return;
        }

        const isTaskExist = data.find((e) => {
          let value =
            e.taskId === taskId &&
            e.projectId === projectId &&
            (e.projectTaskId || (e.projectTask && e.projectTask.id)) ===
              projectTaskId;
          return value;
        });
        if (!isTaskExist) {
          delete tasks[t];
        } else {
          const curTask = data.filter(
            (e) =>
              e.taskId === taskId &&
              e.projectId === projectId &&
              e.projectTaskId === projectTaskId &&
              // e.id === id &&
              rowDates.indexOf(e.date) > -1
          );
          curTask.forEach((tsk) => {
            if (!taskTotal[t]) {
              taskTotal[t] = 0;
            }
            taskTotal[t] += tsk.duration;
          });
        }
        if (!taskTotal[t]) {
          taskTotal[t] = 0;
        }
      }
    });

    Object.keys(taskTotal).forEach((tsk) => {
      taskTotal[tsk] = taskTotal[tsk].toFixed(2);
    });

    let grandTotal = 0;
    rowDates.forEach((d) => {
      let dateRecord = dateWise[d] || { tasks: {}, total: 0 };
      let total = 0;
      Object.keys(tasks).forEach((t) => {
        if (t.startsWith("project")) {
          const { projectId, projectTask, id } = tasks[t];
          let projectTaskId = projectTask && projectTask.id;

          let dateTasks = !showActivity
            ? data.filter((e) => {
                return (
                  e.date === getDD(d) &&
                  e.projectId === projectId &&
                  e.projectTaskId === projectTaskId
                );
              })
            : data.filter(
                (e) => e.date === getDD(d) && e.projectId === projectId
              );
          let duration =
            dateTasks.length > 0
              ? dateTasks
                  .map((e) => e.duration)
                  .reduce((t1, t2) => Number(t1) + Number(t2))
              : 0;

          if (
            d >= this.state.fromDate &&
            (d <= this.state.toDate || this.state.toDate === null)
          ) {
            let isDisable = false;
            let hasNegative = false;
            const object = {
              isReadOnly: true,
              isDisable,
              hasNegative,
              projectId,
              duration: isDisable ? "" : duration.toFixed(2),
              date: d,
              projectTask,
              projectTaskId,
            };
            if (!showActivity) {
              const uniqueTaskId =
                HRConfig.uniqueTimesheetProduct &&
                HRConfig.uniqueTimesheetProduct.id;
              object["isReadOnly"] = false;
              object["taskId"] =
                HRConfig.uniqueTimesheetProduct &&
                HRConfig.uniqueTimesheetProduct.id;

              /* check selected project line when  useUniqueProductForTimesheet is true*/
              const selected =
                this.state.selectedProjectTask.filter((s) => {
                  return (
                    s.projectId === projectId &&
                    s.taskId === uniqueTaskId &&
                    s.id === id
                  );
                }).length > 0;
              object["selected"] = selected;
            }

            dateRecord.tasks[`project_${projectId}_${id}_${projectTaskId}`] =
              object;
            grandTotal += duration;
            total += duration;
          } else {
            const selected =
              this.state.selectedProjectTask.filter((s) => {
                if (!showActivity) {
                  return (
                    s.projectId === projectId &&
                    s.taskId === uniqueTaskId &&
                    s.id === id
                  );
                } else {
                  return (
                    s.projectId === projectId &&
                    s.taskId === uniqueTaskId &&
                    s.projectTaskId === projectTaskId &&
                    s.id === id
                  );
                }
              }).length > 0;

            dateRecord.tasks[
              `project_dummy${projectId}_${id}_${projectTaskId}`
            ] = {
              duration: "",
              selected,
            };
          }
        } else {
          const { taskId, projectId, task = "", projectTask, id } = tasks[t];
          const projectTaskId = projectTask && projectTask.id;

          if (!showActivity) {
            return;
          }
          /* check collapse data */
          const isProjectCollapse =
            this.state.collapseProject.filter((p) => p === projectId).length >
            0;
          if (isProjectCollapse) {
            return;
          }

          let dateTasks = data.filter(
            (e) =>
              e.date === getDD(d) &&
              e.taskId === taskId &&
              e.projectId === projectId &&
              e.projectTaskId === projectTaskId
            // &&
            // e.id === id
          );
          let duration =
            dateTasks.length > 0
              ? dateTasks
                  .map((e) => e.duration)
                  .reduce((t1, t2) => Number(t1) + Number(t2))
              : 0;
          if (
            d >= this.state.fromDate &&
            (d <= this.state.toDate || this.state.toDate === null)
          ) {
            const userData = this.state.user;
            const tdate = new Date(d);
            let isDisable = false;
            let isReadOnly = false;
            let hasNegative = false;
            if (
              userData.holiday.indexOf(d) > -1 ||
              userData.weekend.indexOf(tdate.getDay()) > -1
            ) {
              if (duration === 0) {
                isDisable = true;
              } else {
                isReadOnly = true;
              }
            }
            const isExcluded =
              this.state.projectTaskList.filter(
                (p) => p.id === projectId && p.excludeTimesheetEditor === true
              ).length > 0;
            if (isExcluded) {
              isReadOnly = true;
              isDisable = duration === 0 ? true : false;
            }
            if (
              dateTasks.length === 1 &&
              dateTasks.filter((tn) => tn.duration < 0).length > 0
            ) {
              hasNegative = true;
            }

            /* Check Timesheet is read only or not */
            isReadOnly = this.isReadOnlyTimesheet();

            let selected = false;

            selected =
              this.state.selectedProjectTask.filter(
                (s) =>
                  s.projectId === projectId &&
                  s.taskId === taskId &&
                  s.projectTaskId === projectTaskId &&
                  s.id === id
              ).length > 0;

            dateRecord.tasks[`${projectId}_${taskId}_${projectTaskId}_${id}`] =
              {
                isReadOnly,
                isDisable,
                hasNegative,
                selected,
                taskId,
                projectId,
                duration: isDisable ? "" : duration.toFixed(2),
                date: d,
                task,
                projectTask,
              };
          } else {
            const selected =
              this.state.selectedProjectTask.filter(
                (s) =>
                  s.projectId === projectId &&
                  s.taskId === taskId &&
                  s.projectTaskId === projectTaskId &&
                  s.id === id
              ).length > 0;
            dateRecord.tasks[
              `dummy${projectId}_${taskId}_${projectTaskId}_${id}`
            ] = {
              duration: "",
              selected,
            };
          }
        }
      });
      dateRecord.total = total.toFixed(2);
      dateWise[d] = dateRecord;
    });

    return { tasks, dateWise, taskTotal, grandTotal: grandTotal.toFixed(2) };
  }
  isReadOnlyTimesheet() {
    const { timesheet, user } = this.state;
    let isReadOnly = false;
    if (timesheet && timesheet.statusSelect >= 3 && user.hrManager === false) {
      isReadOnly = true;
    }
    return isReadOnly;
  }

  getRowDates(modeCount = 0, mode, fromDate, toDate = null) {
    let count = 7;
    let begin = moment().add(modeCount, "week").startOf("isoweek").weekday(1);
    if (fromDate) {
      begin = moment(fromDate, "YYYY-MM-DD")
        .add(modeCount, "week")
        .startOf("isoweek")
        .weekday(1);
    }
    const rowDates = [];
    if (mode === "month") {
      begin = moment(fromDate, "YYYY-MM-DD")
        .add(modeCount, "months")
        .startOf("months");
      count = begin.daysInMonth();
    }
    let counter = 0;
    for (let i = 0; i < count; i++) {
      rowDates.push(begin.format(this.state.taskDateFormat));
      if (begin.format(this.state.taskDateFormat) < fromDate) {
        counter++;
      } else if (
        toDate !== null &&
        begin.format(this.state.taskDateFormat) > toDate
      ) {
        counter++;
      }
      begin.add(1, "d");
    }
    return counter !== count ? rowDates : [];
  }

  toggleProjectList(project) {
    const {
      HRConfig,
      params: { showActivity },
    } = this.state;
    const selectedProjectTask = this.state.selectedProjectTask;

    let index = null;
    if (!showActivity) {
      project.taskId =
        HRConfig.uniqueTimesheetProduct && HRConfig.uniqueTimesheetProduct.id;
    }
    selectedProjectTask.forEach((sp, i) => {
      if (
        sp.projectId === project.projectId &&
        project.taskId === sp.taskId &&
        sp.projectTaskId === project.projectTaskId &&
        sp.id === project.id
      ) {
        index = i;
      }
    });
    if (index !== null) {
      selectedProjectTask.splice(index, 1);
    } else {
      selectedProjectTask.push(project);
    }
    this.setState({ selectedProjectTask });
    const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
      this.state.taskData,
      this.state.rowDates
    );

    const records = this.setDummyRecord(tasks, dateWise, taskTotal);
    this.setState({
      tasks: records.tasks,
      dateWise: records.dateWise,
      taskTotal: records.taskTotal,
      grandTotal,
    });
  }

  updateDuration(obj) {
    const { projectId, date, duration, taskId, projectTask, projectTaskId } =
      obj;
    const service = new Service();
    let record = {};
    const { taskData } = this.state;
    let flag = false;
    let isChanged = false;
    let counter = 0;
    if (duration !== "") {
      for (let i = 0; i < taskData.length; i++) {
        let task = taskData[i];
        if (
          date === task.date &&
          task.taskId === taskId &&
          projectId === task.projectId &&
          projectTaskId === (task && task.projectTask && task.projectTask.id)
        ) {
          if (task.enableEditor) {
            counter = 0;
            flag = true;
            const unableDuration = taskData.map((t) => {
              if (
                t.date === task.date &&
                t.taskId === task.taskId &&
                t.projectId === task.projectId &&
                !t.enableEditor
              ) {
                return t.duration;
              }
              return 0;
            });
            if (task.duration !== duration - unableDuration) {
              isChanged = true;
            }
            record = task;
          } else {
            counter += task.duration;
          }
        }
      }
      if (flag !== true && duration > 0) {
        let newTask = {
          user: "u1",
          date,
          projectId,
          duration,
        };
        if (taskId !== undefined) {
          newTask = {
            ...newTask,
            taskId,
            task: obj.task,
          };
        }
        taskData.push(newTask);
      }
      const totalDuration = taskData
        .map((task) => {
          if (
            date === task.date &&
            task.taskId === taskId &&
            projectId === task.projectId &&
            projectTaskId === task.projectTaskId
          ) {
            return task.duration;
          }
          return undefined;
        })
        .filter((t) => t)
        .reduce((t1, t2) => Number(t1) + Number(t2), 0);
      if (
        (isChanged && totalDuration !== duration) ||
        (!flag && duration !== 0)
      ) {
        isChanged = false;
        let object = {};
        if (Object.keys(record).length > 0) {
          const total = taskData
            .map((task) => {
              if (
                date === task.date &&
                task.taskId === taskId &&
                projectId === task.projectId &&
                task.enableEditor !== true &&
                projectTaskId === task.projectTaskId
              ) {
                return task.duration;
              }
              return null;
            })
            .filter((t) => t)
            .reduce((t1, t2) => Number(t1) + Number(t2), 0);
          object = {
            ...record,
            project: record.projectId ? { id: record.projectId } : null,
            product: { id: record.taskId },
            hoursDuration: duration - total,
            employee: (this.state.timesheet || {}).employee,
            projectTask,
          };
        } else {
          object = {
            timesheet: { id: this.state.timesheetId },
            project: obj.projectId ? { id: obj.projectId } : null,
            product: obj.taskId === null ? null : { id: obj.taskId },
            hoursDuration: obj.duration - counter,
            date: obj.date,
            enableEditor: true,
            employee: (this.state.timesheet || {}).employee,
            projectTask,
          };
        }
        const model = "com.axelor.apps.hr.db.TimesheetLine";
        const action = "action-timesheet-line-method-set-duration";
        const data = {
          context: {
            hoursDuration: object.hoursDuration,
            employee: (this.state.timesheet || {}).employee,
            ...object,
          },
        };
        service.getAction(model, action, data).then((res) => {
          if (res.values) {
            object.duration = res.values.duration;
          }
          this.saveData(object);
        });
      } else {
        this.forceUpdate();
      }
    }
  }

  gotoPrev() {
    const modeCount = this.state.modeCount - 1;
    const rowDates = this.getRowDates(
      modeCount,
      this.state.mode,
      this.state.fromDate,
      this.state.toDate
    );
    if (rowDates.length > 0) {
      const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
        this.state.taskData,
        rowDates
      );
      const records = this.setDummyRecord(tasks, dateWise, taskTotal);
      this.setState({
        modeCount,
        rowDates,
        tasks: records.tasks,
        taskTotal: records.taskTotal,
        grandTotal,
        dateWise: records.dateWise,
        editor: {
          ...this.state.editor,
          date: rowDates[0],
        },
      });
    }
  }

  gotoNext() {
    const modeCount = this.state.modeCount + 1;
    const rowDates = this.getRowDates(
      modeCount,
      this.state.mode,
      this.state.fromDate,
      this.state.toDate
    );
    if (rowDates.length > 0) {
      const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
        this.state.taskData,
        rowDates,
        this.state.fromDate
      );
      const records = this.setDummyRecord(tasks, dateWise, taskTotal);
      this.setState({
        modeCount,
        rowDates,
        tasks: records.tasks,
        taskTotal: records.taskTotal,
        grandTotal,
        dateWise: records.dateWise,
        editor: {
          ...this.state.editor,
          date: rowDates[0],
        },
      });
    }
  }

  changeMode(mode) {
    const modeCount = 0;
    const rowDates = this.getRowDates(
      modeCount,
      mode,
      this.state.fromDate,
      this.state.toDate
    );
    if (rowDates.length > 0) {
      const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
        this.state.taskData,
        rowDates
      );
      // this.getGanttData(tasks);
      const records = this.setDummyRecord(tasks, dateWise, taskTotal);
      this.props.changeMode(mode);
      this.setState({
        mode,
        rowDates,
        modeCount,
        dateWise: records.dateWise,
        tasks: records.tasks,
        taskTotal: records.taskTotal,
        grandTotal,
        editor: {
          ...this.state.editor,
          date: rowDates[0],
        },
      });
    }
  }

  getDummyRecordTotal() {
    let documentHeight = window.innerHeight;
    let netHeight = documentHeight - 120;
    let totalRows = Math.floor(netHeight / 25);
    return totalRows - 1;
  }

  setDummyRecord(tasks, dateWise, taskTotal) {
    let dummyLength = this.getDummyRecordTotal();
    if (Object.keys(tasks).length < dummyLength) {
      const start = Object.keys(tasks).length;
      for (let i = start; i < dummyLength; i++) {
        Object.keys(dateWise).forEach((attr) => {
          dateWise[attr].tasks[`dummy_${i}`] = { duration: "" };
        });
        tasks[`dummy_${i}`] = { project: "" };
        taskTotal[`dummy_${i}`] = "";
      }
    }
    return { tasks, taskTotal, dateWise };
  }

  async setEmployeeData(user) {
    const service = new Service();
    // const user = this.state.user;
    return new Promise((resolve, reject) => {
      const employeeList = [];
      if (user.employeeId !== null) {
        employeeList.push(user.employeeId);
      }
      if (employeeList.length === 0) {
        employeeList.push(-1);
      }
      let criteria = [{ fieldName: "id", operator: "in", value: employeeList }];
      service
        .search("com.axelor.apps.hr.db.Employee", criteria, 0, null, ["id"], [])
        .then((emp) => {
          if (emp.data && emp.data.length) {
            const empData = emp.data;
            empData.forEach((employee) => {
              if (user.employeeId === employee.id) {
                user.dailyWorkHours = employee.dailyWorkHours;
                user.planning = employee.weeklyPlanning;
                user.publicHolidayPlanning =
                  employee.publicHolidayEventsPlanning;
                user.hrManager = employee.hrManager;
              }
            });
            const holidayList = [];
            const holiday = user.publicHolidayPlanning;
            if (holiday && holiday !== null) {
              holidayList.push(holiday.id);
            }
            if (holidayList.length === 0) {
              holidayList.push(-1);
            }
            let holiCriteria = [
              { fieldName: "id", operator: "in", value: holidayList },
            ];
            service
              .search(
                "com.axelor.apps.base.db.EventsPlanning",
                holiCriteria,
                0,
                null,
                ["id"],
                []
              )
              .then((result) => {
                if (result.data && result.data.length) {
                  const holidayPlanningData = result.data;
                  holidayPlanningData.forEach((holidayPlanning) => {
                    const holidayList =
                      holidayPlanning.eventsPlanningLineList.map((h) => {
                        return h.id;
                      });
                    let holidayCriteria = [
                      { fieldName: "id", operator: "in", value: holidayList },
                    ];
                    service
                      .search(
                        "com.axelor.apps.base.db.EventsPlanningLine",
                        holidayCriteria,
                        0,
                        null,
                        ["id"],
                        []
                      )
                      .then((res) => {
                        if (res && res.data.length) {
                          const holiday = res.data.map((h) => {
                            return h.date;
                          });
                          if (
                            user.publicHolidayPlanning &&
                            user.publicHolidayPlanning.id === holidayPlanning.id
                          ) {
                            user.holiday = holiday;
                          }
                        }
                      });
                  });
                } else {
                  this.setState(() => {
                    return { user };
                  });
                  resolve(true);
                }
              });
            const weekendList = [];
            const planning = user.planning;
            if (planning && planning !== null) {
              weekendList.push(planning.id);
            }
            let weekendCriteria = [
              { fieldName: "id", operator: "in", value: weekendList },
            ];
            if (weekendList.length !== 0) {
              service
                .search(
                  "com.axelor.apps.base.db.WeeklyPlanning",
                  weekendCriteria,
                  0,
                  null,
                  ["id"],
                  []
                )
                .then((result) => {
                  if (result.data && result.data.length) {
                    const planning = result.data;
                    planning.forEach((weekPlan) => {
                      const weekDays = weekPlan.weekDays.map((e) => {
                        return e.id;
                      });
                      let criteria = [
                        { fieldName: "id", operator: "in", value: weekDays },
                      ];
                      service
                        .search(
                          "com.axelor.apps.base.db.DayPlanning",
                          criteria,
                          0,
                          null,
                          ["id"],
                          []
                        )
                        .then((res) => {
                          if (res.data.length) {
                            const weekDaysData = res.data;
                            const weekend = weekDaysData
                              .map((w) => {
                                if (
                                  w.morningFrom === null &&
                                  w.afternoonFrom === null
                                ) {
                                  return week.indexOf(w.name);
                                }
                                return null;
                              })
                              .filter((wId) => wId !== undefined);
                            if (
                              user.planning &&
                              user.planning.id === weekPlan.id
                            ) {
                              user.weekend = weekend;
                            }
                            this.setState(() => {
                              return { user };
                            });
                            resolve(true);
                          } else {
                            this.setState(() => {
                              return { user };
                            });
                            resolve(true);
                          }
                        });
                    });
                  }
                });
            }
          } else {
            resolve(true);
          }
        });
    });
  }

  setSortingField(fieldName) {
    const sortingList = this.state.sortingList;
    const targetIndex = sortingList.findIndex(
      (item) => item.fieldName === fieldName
    );
    if (targetIndex >= 0) {
      const item = Object.assign({}, sortingList[targetIndex]);
      if (item.sort === "asc") {
        item.sortBy = `-${fieldName}`;
        item.sort = "desc";
      } else {
        item.sort = "asc";
        item.sortBy = `${fieldName}`;
      }
      sortingList[targetIndex] = item;
    } else {
      sortingList.push({
        fieldName: fieldName,
        sort: "asc",
        sortBy: fieldName,
      });
    }
    this.setState({ sortingList }, () => {
      this.fetchTimesheet();
    });
  }

  fetchTimesheet(rowDates = null) {
    const service = new Service();
    const params = this.state.params;
    let taskData = [];
    const timesheetId = params.timesheetId !== "null" ? params.timesheetId : 0;
    const sortBy = this.state.sortingList.map((item) => item.sortBy);
    this.setState({ timesheetId: Number(timesheetId) });
    service
      .fetchTimesheet(timesheetId, "com.axelor.apps.hr.db.Timesheet", sortBy)
      .then(
        (res) => {
          const timesheet = res.timesheet;
          this.setState({ timesheet });
          this.setDefaultActivity(timesheet && timesheet.employee);
          if (res && res.timeline === null) {
            if (rowDates === null) {
              rowDates = this.getRowDates(
                this.state.modeCount,
                this.state.mode,
                this.state.fromDate
              );
              const { tasks, dateWise, taskTotal, grandTotal } =
                this.groupTasks(this.state.taskData, rowDates);
              const records = this.setDummyRecord(tasks, dateWise, taskTotal);
              this.setState({
                rowDates,
                tasks: records.tasks,
                dateWise: records.dateWise,
                taskTotal: records.taskTotal,
                grandTotal,
              });
            }
          } else {
            let newFromDate = this.state.fromDate;
            if (res.fromDate) {
              newFromDate = res.fromDate;
            }
            if (rowDates === null) {
              // let weekNumber = moment(res.fromDate, "YYYY-MM-DD").week();
              rowDates = this.getRowDates(
                this.state.modeCount,
                this.state.mode,
                res.fromDate,
                res.toDate
              );
              this.setState({ fromDate: newFromDate, toDate: res.toDate });
            }
            taskData = res.timeline;
            const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
              taskData,
              rowDates
            );
            const records = this.setDummyRecord(tasks, dateWise, taskTotal);
            this.setState({
              rowDates,
              tasks: records.tasks,
              dateWise: records.dateWise,
              taskTotal: records.taskTotal,
              grandTotal,
              taskData,
              timesheetVersion: res.timesheetVersion,
              timesheetId: res.timesheetId,
              timesheet,
              fromDate: newFromDate,
            });
          }
        },
        (reject) => {
          console.log(reject);
        }
      );
  }

  async fetchTasks(project) {
    const service = new Service();
    if (!project) return;
    const res = await service.search(
      "com.axelor.apps.project.db.ProjectTask",
      [],
      0,
      null,
      ["id"],
      ["fullName"],
      `self.project.id = ${project.id}`
    );
    this.setState({
      projectTasks: res.data,
    });
  }

  async refreshData(user, rowDates = null) {
    const service = new Service();
    let userCriteria = [];
    let domain = null;
    userCriteria.push({
      fieldName: "id",
      operator: "in",
      value: [user["user.id"]],
    });
    service
      .search(
        "com.axelor.auth.db.User",
        userCriteria,
        0,
        null,
        ["id"],
        [],
        domain
      )
      .then((res) => {
        if (res.data) {
          res.data.forEach((u) => {
            user = {
              ...user,
              id: u.id,
              name: u.name,
              fullName: u.fullName,
              weekend: [],
              holiday: [],
              employeeId: u.employee !== null ? u.employee.id : null,
            };
            this.setState({ user });
            this.setEmployeeData(user).then((res) => {
              this.setState(
                { timeSheetUser: { id: user.id, name: user["user.name"] } },
                () => {
                  this.fetchTimesheet(rowDates);
                }
              );
              service
                .getAction(
                  "com.axelor.apps.hr.db.TimesheetLine",
                  "action-timesheet-line-attrs-domain-project"
                )
                .then((res) => {
                  const _domain = res.attrs.project.domain;
                  service
                    .search(
                      "com.axelor.apps.project.db.Project",
                      [],
                      0,
                      null,
                      ["id"],
                      ["fullName", "excludeTimesheetEditor"],
                      _domain
                    )
                    .then((res) => {
                      this.setState({
                        projectTaskList: res.data || [],
                        editor: {
                          ...this.state.editor,
                          project: res.data ? res.data[0] : null,
                        },
                      });
                      this.fetchTasks(res && res.data && res.data[0]);
                    });
                });
              service
                .getAction(
                  "com.axelor.apps.hr.db.TimesheetLine",
                  "action-hr-timesheet-line-attrs-domain-product"
                )
                .then((res) => {
                  const domain = res.attrs.product.domain;
                  service
                    .search(
                      "com.axelor.apps.base.db.Product",
                      [],
                      0,
                      null,
                      ["id"],
                      ["fullName"],
                      domain
                    )
                    .then((res) => {
                      this.setState({
                        subTaskList: res.data,
                      });
                    });
                });
            });
          });
        }
      });
  }

  componentDidMount() {
    const service = new Service();
    const params = new URL(document.location).searchParams;
    const timesheetId = params.get("timesheetId");
    const showActivity = JSON.parse(params.get("showActivity"));

    const HREntity = "com.axelor.apps.hr.db.HRConfig";
    service.search(HREntity).then((res) => {
      const { data } = res;
      if (data && data.length > 0) {
        this.setState({ HRConfig: data[0] });
      }
    });
    service.info().then((res) => {
      this.setState(
        {
          params: {
            timesheetId,
            showActivity,
          },
          user: res,
          mode: this.props.mode,
        },
        () => {
          this.refreshData(res);
        }
      );
    });
  }

  setDefaultActivity(employee) {
    const service = new Service();
    const model = "com.axelor.apps.hr.db.TimesheetLine";
    const action = "action-timesheet-line-editor-record-get-default";
    const data = {
      context: {
        employee,
      },
    };
    service
      .getAction(model, action, data)
      .then((res) => {
        if (res.values) {
          this.setState({ defaultActivity: res.values.product });
        }
      })
      .catch((e) => {
        if (e === null) {
          this.setState({ defaultActivity: null });
        }
      });
  }

  getNewTaskId(projectId) {
    const { taskData } = this.state;
    const MaxId = Math.max(
      ...taskData
        .map((tsk) => {
          if (tsk.taskId !== undefined) return tsk.taskId;
          return null;
        })
        .filter((t) => t !== undefined)
    );
    return isFinite(MaxId) ? MaxId + 1 : 1;
  }

  addNewLine() {
    const { editor, taskData, rowDates } = this.state;
    const {
      params: { showActivity },
    } = this.state;
    const hasRecord = taskData.filter((task) => {
      if (showActivity && task.duration !== 0)
        return (
          task.date === editor.date &&
          task.projectId === (editor.project && editor.project.id) &&
          task.taskId === (editor.task && editor.task.id) &&
          (task.projectTask && task.projectTask.id) ===
            (editor.projectTask && editor.projectTask.id)
        );
      else if (!showActivity) {
        return (
          task.date === editor.date &&
          task.projectId === (editor.project && editor.project.id) &&
          task.taskId === (editor.task && editor.task.id) &&
          (task.projectTask && task.projectTask.id) ===
            (editor.projectTask && editor.projectTask.id)
        );
      }
      return null;
    });
    if (hasRecord.length > 0) {
      const obj = {
        projectId: editor.project && editor.project.id,
        taskId: editor.task && editor.task.id,
        projectTask: editor.projectTask,
        projectTaskId: editor.projectTask && editor.projectTask.id,
        task: editor.task && editor.task.fullName,
        date: editor.date,
        duration: editor.duration,
      };
      this.updateDuration(obj);
    } else {
      taskData.push({
        user: this.state.timeSheetUser,
        project: editor.project.fullName,
        projectId: editor.project.id,
        duration: parseFloat(editor.duration === "" ? 0 : editor.duration),
        date: editor.date,
        task: editor.task && editor.task.fullName,
        taskId: editor.task && editor.task.id,
        projectTask: editor.projectTask,
        projectTaskId: editor.projectTask && editor.projectTask.id,
        id: editor.task && editor.task.id,
      });
      taskData.sort((a, b) => {
        var x = a.projectId,
          y = b.projectId;
        return x < y ? -1 : x > y ? 1 : 0;
      });
      this.setState({ taskData });
      const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
        taskData,
        rowDates
      );
      const records = this.setDummyRecord(tasks, dateWise, taskTotal);
      this.setState({
        taskData,
        tasks: records.tasks,
        dateWise: records.dateWise,
        taskTotal: records.taskTotal,
        grandTotal,
      });
    }
    this.setState({ show: false });
  }

  saveData(task) {
    const sortBy = this.state.sortingList.map((item) => item.sortBy);
    const service = new Service();
    service
      .updateModel(task, "com.axelor.apps.hr.db.TimesheetLine")
      .then((res) => {
        let taskData = [];
        service
          .fetchTimesheet(
            this.state.timesheetId,
            "com.axelor.apps.hr.db.Timesheet",
            sortBy
          )
          .then((res) => {
            taskData = res.timeline;
            const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(
              taskData,
              this.state.rowDates
            );
            const records = this.setDummyRecord(tasks, dateWise, taskTotal);
            this.setState({
              tasks: records.tasks,
              dateWise: records.dateWise,
              taskTotal: records.taskTotal,
              grandTotal,
              taskData,
              timesheetVersion: res.timesheetVersion,
            });
          });
      });
  }

  cleanEditor() {
    const project = this.state.projectTaskList.filter(
      (pt) => pt.excludeTimesheetEditor === false
    )[0];
    this.setState(
      {
        show: true,
        editor: {
          project: project || { id: null },
          task: this.state.defaultActivity,
          date: moment().format("YYYY-MM-DD"),
          duration: "0",
        },
      },
      () => {
        this.fetchTasks(project);
      }
    );
  }

  removeTimesheetLine() {
    const service = new Service();
    const {
      params: { showActivity },
    } = this.state;
    this.state.selectedProjectTask.forEach((sp, i) => {
      if (!showActivity) {
        if (sp.projectTaskId === null) {
          sp.projectTaskId = undefined;
        } else if (sp.projectTaskId === undefined) {
          sp.projectTaskId = null;
        }
      }
      const taskIds = this.state.taskData
        .map((t) => {
          if (
            t.projectId === sp.projectId &&
            t.projectTaskId === sp.projectTaskId &&
            t.taskId === sp.taskId
          ) {
            return { id: t.id, version: t.version };
          }
          return null;
        })
        .filter((t) => t);
      service
        .removeAll(taskIds, "com.axelor.apps.hr.db.TimesheetLine")
        .then((result) => {
          service
            .fetchTimesheet(
              this.state.timesheetId,
              "com.axelor.apps.hr.db.Timesheet"
            )
            .then((res) => {
              const taskData = res.timeline;
              taskData.sort((a, b) => {
                var x = a.projectId,
                  y = b.projectId;
                return x < y ? -1 : x > y ? 1 : 0;
              });
              this.setState({
                selectedProjectTask: [],
              });
              const { tasks, dateWise, taskTotal, grandTotal } =
                this.groupTasks(taskData, this.state.rowDates);
              const records = this.setDummyRecord(tasks, dateWise, taskTotal);
              this.setState({
                tasks: records.tasks,
                dateWise: records.dateWise,
                taskTotal: records.taskTotal,
                grandTotal,
                taskData,
                timesheetVersion: res.timesheetVersion,
              });
            });
        });
    });
  }

  confirmRemoveTimesheetLine() {
    let message = "Do you really want to delete the selected record(s)?";
    this.setState({ confirmPopUp: true, confirmMessage: translate(message) });
  }

  showAddButton() {
    let flag =
      this.state.params &&
      this.state.params.timesheetId &&
      this.state.params.timesheetId !== "null";
    /* Check timesheet is read only or not */
    if (this.isReadOnlyTimesheet()) {
      flag = false;
    }
    return flag;
  }

  collapseProject(projectId) {
    const { collapseProject } = this.state;
    const target = collapseProject.findIndex((p) => p === projectId);
    if (target !== -1) {
      collapseProject.splice(target, 1);
    } else {
      collapseProject.push(projectId);
    }
    this.setState({ collapseProject });
  }

  render() {
    let close = () => this.setState({ show: false });
    let closeConfirm = () => this.setState({ confirmPopUp: false });
    return (
      <div style={{ display: "flex", flexDirection: "column" }}>
        <div>
          <div className="navbar" style={{ minHeight: 0 }}>
            <ButtonToolbar style={{ float: "left" }}>
              <ButtonGroup style={{ marginRight: 10 }}>
                <button
                  onClick={() => this.gotoPrev()}
                  className="navigation"
                  style={{ marginRight: 5 }}
                >
                  <Glyphicon glyph="chevron-left" style={{ color: "white" }} />
                </button>
                <button
                  onClick={() => this.gotoNext()}
                  className="navigation"
                  style={{ marginRight: 5 }}
                >
                  <Glyphicon glyph="chevron-right" style={{ color: "white" }} />
                </button>
                <button
                  onClick={() =>
                    this.refreshData(this.state.user, this.state.rowDates)
                  }
                  className="navigation"
                >
                  <Glyphicon glyph="refresh" style={{ color: "white" }} />
                </button>
                {/*<Button onClick={() => this.cleanEditor()}><Glyphicon glyph="plus" /></Button>*/}
              </ButtonGroup>
              <Label
                className={
                  this.state.mode === "week"
                    ? "activeLabel mode-label"
                    : "nonActive mode-label"
                }
                onClick={(e) => this.changeMode("week")}
              >
                {translate("Week")}
              </Label>
              <Label
                className={
                  this.state.mode === "month"
                    ? "activeLabel mode-label"
                    : "nonActive mode-label"
                }
                onClick={(e) => this.changeMode("month")}
              >
                {translate("Month")}
              </Label>
            </ButtonToolbar>
          </div>
        </div>
        <TimeSheet
          rowDates={this.state.rowDates}
          changeDuration={(obj) => {
            this.updateDuration(obj);
          }}
          dateWise={this.state.dateWise}
          tasks={this.state.tasks}
          taskTotal={this.state.taskTotal}
          grandTotal={this.state.grandTotal}
          isLarge={this.state.mode === "week" ? false : true}
          addLine={() => this.cleanEditor()}
          toggleProjectList={(project) => this.toggleProjectList(project)}
          moveNext={() => this.gotoNext()}
          movePrev={() => this.gotoPrev()}
          showActivity={this.state.params.showActivity}
          cleanEditor={() => this.cleanEditor()}
          removeTimesheetLine={() => this.confirmRemoveTimesheetLine()}
          showButton={this.showAddButton()}
          changeKeyPress={(navigationKey) =>
            this.props.changeKeyPress(navigationKey)
          }
          navigationKey={this.props.navigationKey}
          setSortingField={(fieldName) => this.setSortingField(fieldName)}
          sortingList={this.state.sortingList}
          collapseProject={(projectId) => this.collapseProject(projectId)}
        />
        <Modal
          show={this.state.show}
          onHide={close}
          container={this}
          aria-labelledby="modal-label"
          style={{ marginTop: -30 }}
          backdropStyle={{ backgroundColor: "transparent" }}
        >
          <Modal.Header
            closeButton
            style={{ padding: "8px 15px", overflow: "hidden" }}
          >
            <Modal.Title id="contained-modal-title">
              {translate("New project line")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body style={{ padding: "0px 15px", overflow: "hidden" }}>
            <FormGroup style={{ marginBottom: 5 }}>
              <ControlLabel style={{ float: "left" }}>
                {translate("Project")}
              </ControlLabel>
              <FormControl
                componentClass="select"
                value={
                  this.state.editor.project
                    ? this.state.editor.project.id
                    : null
                }
                onChange={(e) => {
                  const project = this.state.projectTaskList.filter(
                    (p) => `${p.id}` === `${e.target.value}`
                  )[0];
                  this.setState(
                    { editor: { ...this.state.editor, project } },
                    () => {
                      this.fetchTasks(project);
                    }
                  );
                }}
              >
                {this.state.projectTaskList
                  .filter((pl) => pl.excludeTimesheetEditor !== true)
                  .map((p, index) => (
                    <option key={index} value={p.id}>
                      {p.fullName}
                    </option>
                  ))}
              </FormControl>
            </FormGroup>
            {this.state.params.showActivity && (
              <FormGroup style={{ marginBottom: 5 }}>
                <ControlLabel style={{ float: "left" }}>
                  {translate("Activity")}
                </ControlLabel>
                <FormControl
                  componentClass="select"
                  value={
                    this.state.editor.task
                      ? this.state.editor.task.id
                      : undefined
                  }
                  onChange={(e) => {
                    const task = this.state.subTaskList.find(
                      (p) => `${p.id}` === `${e.target.value}`
                    );
                    this.setState({ editor: { ...this.state.editor, task } });
                  }}
                >
                  <option key="activity_default" value={null} />
                  {(this.state.subTaskList || []).length &&
                    this.state.subTaskList.map((p, index) => (
                      <option key={index} value={p.id}>
                        {p.fullName}
                      </option>
                    ))}
                </FormControl>
              </FormGroup>
            )}
            <FormGroup style={{ marginBottom: 5 }}>
              <ControlLabel style={{ float: "left" }}>
                {translate("Task")}
              </ControlLabel>
              <FormControl
                componentClass="select"
                value={
                  this.state.editor.projectTask
                    ? this.state.editor.projectTask.id
                    : undefined
                }
                onChange={(e) => {
                  const projectTask = this.state.projectTasks.find(
                    (p) => `${p.id}` === `${e.target.value}`
                  );
                  this.setState({
                    editor: { ...this.state.editor, projectTask },
                  });
                }}
              >
                <option key="task_default" value={null} />
                {(this.state.projectTasks || []).map((p, index) => (
                  <option key={index} value={p.id}>
                    {p.fullName}
                  </option>
                ))}
              </FormControl>
            </FormGroup>
          </Modal.Body>
          <Modal.Footer style={{ overflow: "hidden", padding: "8px 15px" }}>
            <button
              className="addLine"
              style={{ float: "right", color: "white" }}
              onClick={() => this.addNewLine()}
            >
              {translate("Add")}
            </button>
          </Modal.Footer>
        </Modal>
        <Modal
          show={this.state.confirmPopUp}
          onHide={closeConfirm}
          container={this}
          bsSize="small"
          aria-labelledby="contained-modal-title-sm"
        >
          <Modal.Header closeButton>
            <Modal.Title id="contained-modal-title">
              {translate("Confirm")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p>{this.state.confirmMessage}</p>
          </Modal.Body>
          <Modal.Footer>
            <button
              className="addLine"
              style={{ float: "right", color: "white" }}
              onClick={() => {
                this.setState({ confirmPopUp: false });
                this.removeTimesheetLine();
              }}
            >
              Ok
            </button>
            <button
              className="addLine"
              style={{ float: "right", color: "white" }}
              onClick={() => this.setState({ confirmPopUp: false })}
            >
              {translate("Cancel")}
            </button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    mode: state.mode,
    navigationKey: state.navigationKey,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    changeMode: (mode) => {
      dispatch(changeMode(mode));
    },
    changeKeyPress: (navigationKey) => {
      dispatch(changeKeyPress(navigationKey));
    },
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Container);
