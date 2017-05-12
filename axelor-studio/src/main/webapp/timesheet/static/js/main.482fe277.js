webpackJsonp([1],{

/***/ 0:
/***/ function(module, exports, __webpack_require__) {

	__webpack_require__(322);
	module.exports = __webpack_require__(326);


/***/ },

/***/ 79:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	Object.defineProperty(exports, "__esModule", {
	  value: true
	});
	exports.convertNumberToTime = exports.convertTimeToNumber = exports.convertDuration = exports.validateDuration = undefined;
	
	var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();
	
	var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();
	
	var _react = __webpack_require__(2);
	
	var _react2 = _interopRequireDefault(_react);
	
	var _moment = __webpack_require__(1);
	
	var _moment2 = _interopRequireDefault(_moment);
	
	var _reactBootstrap = __webpack_require__(106);
	
	var _reactBootstrapDatePicker = __webpack_require__(415);
	
	var _reactBootstrapDatePicker2 = _interopRequireDefault(_reactBootstrapDatePicker);
	
	var _cellComponent = __webpack_require__(128);
	
	var _cellComponent2 = _interopRequireDefault(_cellComponent);
	
	var _timeSheet = __webpack_require__(325);
	
	var _timeSheet2 = _interopRequireDefault(_timeSheet);
	
	__webpack_require__(150);
	
	var _service = __webpack_require__(129);
	
	var _service2 = _interopRequireDefault(_service);
	
	function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
	
	function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }
	
	function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
	
	function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
	
	function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }
	
	function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }
	
	var validateDuration = exports.validateDuration = function validateDuration(duration) {
	  var numbers = duration.split(":");
	  if (numbers.length <= 2 && numbers.length >= 1) {
	    var _numbers = _slicedToArray(numbers, 2),
	        hr = _numbers[0],
	        mins = _numbers[1];
	
	    if (!isNaN(hr) && mins === undefined && hr <= 60 && hr >= 0 || mins !== undefined && !isNaN(mins) && mins >= 0 && mins <= 60) return true;
	  }
	  return false;
	};
	
	var convertDuration = exports.convertDuration = function convertDuration(duration) {
	  var numbers = duration.split(".");
	
	  var _numbers2 = _slicedToArray(numbers, 2),
	      hr = _numbers2[0],
	      mins = _numbers2[1];
	
	  var newDuration = null;
	  var newHr = hr === undefined ? 0 : hr;
	  var newMins = mins === undefined ? 0 : mins;
	  if (mins > 59) {
	    newMins = mins % 60;
	    newHr += mins / 60;
	  }
	  newDuration = newHr + ':' + newMins;
	  return newDuration;
	};
	
	var convertTimeToNumber = exports.convertTimeToNumber = function convertTimeToNumber(duration) {
	  var time = duration.split(":");
	
	  var _time = _slicedToArray(time, 2),
	      hr = _time[0],
	      mins = _time[1];
	
	  var newDuration = null;
	  var newHr = hr === undefined ? 0 : Number(hr);
	  var newMins = mins === undefined ? 0 : Number(mins);
	  if (mins > 59) {
	    newMins = mins % 60;
	    newHr += mins / 60;
	  }
	  newDuration = newHr + newMins / 60;
	  return '' + newDuration;
	};
	
	var convertToTimeDigit = function convertToTimeDigit() {
	  var number = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
	
	  var newNumber = '00';
	  if (number.length >= 2) {
	    newNumber = number;
	  } else if (number.length === 1) {
	    newNumber = '0' + number;
	  }
	  return newNumber;
	};
	
	var convertNumberToTime = exports.convertNumberToTime = function convertNumberToTime(duration) {
	  var time = duration;
	  if (duration.indexOf(":") < 0) {
	    var numbers = duration.split(".");
	
	    var _numbers3 = _slicedToArray(numbers, 2),
	        hr = _numbers3[0],
	        mins = _numbers3[1];
	
	    time = convertToTimeDigit(hr);
	    var minutes = convertToTimeDigit('' + Math.round(parseFloat('0.' + mins) * 60));
	    time = time + ':' + minutes;
	  }
	  return time;
	};
	
	var Container = function (_Component) {
	  _inherits(Container, _Component);
	
	  function Container(props) {
	    _classCallCheck(this, Container);
	
	    var _this = _possibleConstructorReturn(this, (Container.__proto__ || Object.getPrototypeOf(Container)).call(this, props));
	
	    _this.state = {
	      show: false,
	      html: 1,
	      editor: {
	        project: {},
	        task: {},
	        date: (0, _moment2.default)().format('YYYY-MM-DD'),
	        duration: '0'
	      },
	      currentDate: (0, _moment2.default)().format('DD-MM-YYYY'),
	      fromDate: (0, _moment2.default)().format('YYYY-MM-DD'),
	      toDate: 'null',
	      modeCount: 0,
	      rowDates: [],
	      dateWise: {},
	      tasks: {},
	      taskTotal: {},
	      grandTotal: 0,
	      mode: 'week', // week/month
	      projectTaskList: [],
	      subTaskList: [],
	      taskDateFormat: 'YYYY-MM-DD',
	      taskData: []
	    };
	
	    return _this;
	  }
	
	  _createClass(Container, [{
	    key: 'getGanttData',
	    value: function getGanttData(tasks) {
	      var GanttView = {};
	      var counter = 1;
	      Object.keys(tasks).forEach(function (t) {
	        var _tasks$t = tasks[t],
	            user = _tasks$t.user,
	            projectId = _tasks$t.projectId,
	            project = _tasks$t.project,
	            _tasks$t$taskId = _tasks$t.taskId,
	            taskId = _tasks$t$taskId === undefined ? 0 : _tasks$t$taskId,
	            task = _tasks$t.task;
	
	        var userIndex = counter++ + 'u_' + user;
	        if (!GanttView[userIndex]) {
	          GanttView[userIndex] = {
	            title: user,
	            level: 0
	          };
	        }
	        var projectIndex = counter++ + 'p_' + projectId;
	        if (!GanttView[projectIndex]) {
	          GanttView[projectIndex] = _defineProperty({
	            title: project
	          }, 'title', 1);
	        }
	        var taskIndex = counter++ + 't_' + taskId;
	        if (!GanttView[taskIndex]) {
	          GanttView[taskIndex] = {
	            title: task,
	            level: 2
	          };
	        }
	      });
	      console.log('gantt', GanttView);
	    }
	  }, {
	    key: 'groupTasks',
	    value: function groupTasks(data, rowDates) {
	      var _this2 = this;
	
	      var tasks = {};
	      var dateWise = {};
	      var taskTotal = {};
	      var getDD = function getDD(dd) {
	        return (0, _moment2.default)(dd, _this2.state.taskDateFormat).format(_this2.state.taskDateFormat);
	      };
	
	      data.forEach(function (t) {
	        var user = t.user,
	            project = t.project,
	            projectId = t.projectId,
	            task = t.task,
	            taskId = t.taskId,
	            duration = t.duration;
	
	        var projectTaskKey = projectId + '_' + taskId;
	        if (!tasks[projectTaskKey]) {
	          tasks[projectTaskKey] = { user: user, project: project, projectId: projectId, task: task, taskId: taskId };
	        } else {}
	      });
	
	      console.log(JSON.parse(JSON.stringify(tasks)), JSON.parse(JSON.stringify(taskTotal)), data, rowDates);
	      Object.keys(tasks).forEach(function (t) {
	        var _tasks$t2 = tasks[t],
	            taskId = _tasks$t2.taskId,
	            projectId = _tasks$t2.projectId;
	
	        var isTaskExist = data.find(function (e) {
	          return e.taskId === taskId && e.projectId === projectId && rowDates.indexOf(getDD(e.date)) > -1;
	        });
	        data.find(function (e) {
	          return e.taskId === taskId && e.projectId === projectId && rowDates.indexOf(getDD(e.date)) > -1;
	        });
	        // console.log('is exist', tasks[t], isTaskExist , data);
	        if (!isTaskExist) {
	          delete tasks[t];
	        } else {
	          var curTask = data.filter(function (e) {
	            return e.taskId === taskId && e.projectId === projectId && rowDates.indexOf(e.date) > -1;
	          });
	          curTask.forEach(function (tsk) {
	            if (!taskTotal[t]) {
	              taskTotal[t] = 0;
	            }
	            taskTotal[t] += tsk.duration;
	          });
	        }
	      });
	      console.log('tasks', tasks);
	      Object.keys(taskTotal).forEach(function (tsk) {
	        taskTotal[tsk] = taskTotal[tsk].toFixed(2);
	      });
	      rowDates.forEach(function (d) {
	        var dateRecord = dateWise[d] || { tasks: {}, total: 0 };
	        var total = 0;
	        Object.keys(tasks).forEach(function (t) {
	          var _tasks$t3 = tasks[t],
	              taskId = _tasks$t3.taskId,
	              projectId = _tasks$t3.projectId,
	              _tasks$t3$task = _tasks$t3.task,
	              task = _tasks$t3$task === undefined ? "" : _tasks$t3$task;
	
	          var dateTasks = data.filter(function (e) {
	            return e.date === getDD(d) && e.taskId === taskId && e.projectId === projectId;
	          });
	          var duration = dateTasks.length > 0 ? dateTasks.map(function (e) {
	            return e.duration;
	          }).reduce(function (t1, t2) {
	            return Number(t1) + Number(t2);
	          }) : 0;
	          console.log(d >= _this2.state.fromDate, d, _this2.state.fromDate, _this2.state.toDate);
	          if (d >= _this2.state.fromDate && (d <= _this2.state.toDate || _this2.state.toDate === null)) {
	            dateRecord.tasks[projectId + '_' + taskId] = { taskId: taskId, projectId: projectId, duration: duration.toFixed(2), date: d, task: task };
	          } else {
	            console.log(tasks[t]);
	            dateRecord.tasks['dummy' + projectId + '_' + taskId] = { duration: '' };
	          }
	          total += duration;
	        });
	        dateRecord.total = total.toFixed(2);
	        dateWise[d] = dateRecord;
	      });
	      var grandTotal = parseFloat(Object.keys(taskTotal).length > 0 ? Object.keys(taskTotal).map(function (t) {
	        return taskTotal[t];
	      }).reduce(function (t1, t2) {
	        return Number(t1) + Number(t2);
	      }) : 0).toFixed(2);
	
	      console.log(tasks, dateWise, taskTotal, parseFloat(grandTotal).toFixed(2));
	      return { tasks: tasks, dateWise: dateWise, taskTotal: taskTotal, grandTotal: grandTotal };
	    }
	  }, {
	    key: 'getRowDates',
	    value: function getRowDates() {
	      var modeCount = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;
	      var mode = arguments[1];
	      var fromDate = arguments[2];
	      var toDate = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : null;
	
	      var count = 7;
	      var begin = (0, _moment2.default)().add(modeCount, 'week').startOf('week').weekday(1);
	      var rowDates = [];
	      if (mode === 'month') {
	        begin = (0, _moment2.default)().add(modeCount, 'months').startOf('months');
	        count = begin.daysInMonth();
	      }
	      var counter = 0;
	      for (var i = 0; i < count; i++) {
	        console.log('date', begin.format(this.state.taskDateFormat), fromDate, begin.format(this.state.taskDateFormat) >= fromDate);
	        rowDates.push(begin.format(this.state.taskDateFormat));
	        if (begin.format(this.state.taskDateFormat) < fromDate || begin.format(this.state.taskDateFormat) > toDate || toDate === null) {
	          counter++;
	        }
	        begin.add('d', 1);
	      }
	      console.log(counter, count, rowDates, fromDate);
	      return counter !== count ? rowDates : [];
	    }
	  }, {
	    key: 'updateDuration',
	    value: function updateDuration(obj) {
	      var projectId = obj.projectId,
	          date = obj.date,
	          duration = obj.duration,
	          taskId = obj.taskId,
	          task = obj.task;
	
	      var record = {};
	      var _state = this.state,
	          taskData = _state.taskData,
	          rowDates = _state.rowDates;
	
	      var flag = false;
	      var isChanged = false;
	      if (duration !== '') {
	        for (var i = 0; i < taskData.length; i++) {
	          var _task = taskData[i];
	          if (date === _task.date && _task.taskId === taskId && projectId === _task.projectId) {
	            console.log('changed', _task.duration, duration);
	            if (_task.duration !== duration) {
	              isChanged = true;
	            }
	            _task.duration = duration;
	            taskData[i] = _task;
	            flag = true;
	            record = _task;
	          }
	        }
	        if (!flag && duration > 0) {
	          var newTask = {
	            user: 'u1',
	            date: date,
	            projectId: projectId,
	            duration: duration
	          };
	          if (taskId !== undefined) {
	            newTask = Object.assign({}, newTask, {
	              taskId: taskId,
	              task: task
	            });
	          }
	          taskData.push(newTask);
	        }
	        console.log(taskData);
	        if (isChanged || !flag && duration !== 0) {
	          isChanged = false;
	          console.log(record);
	          var object = {};
	          if (Object.keys(record).length > 0) {
	            object = Object.assign({}, record, {
	              project: { id: record.projectId },
	              product: { id: record.taskId },
	              visibleDuration: record.duration,
	              durationStored: record.duration
	            });
	          } else {
	            object = {
	              timesheet: { id: this.state.timesheetId },
	              project: { id: obj.projectId },
	              product: { id: obj.taskId },
	              visibleDuration: obj.duration,
	              durationStored: obj.duration,
	              date: obj.date
	            };
	          }
	          this.saveData(object);
	        }
	        // const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(taskData, rowDates);
	        // this.setState({ taskData, tasks, dateWise, taskTotal, grandTotal });
	      }
	    }
	  }, {
	    key: 'gotoPrev',
	    value: function gotoPrev() {
	      console.log('prev');
	      var modeCount = this.state.modeCount - 1;
	      var rowDates = this.getRowDates(modeCount, this.state.mode, this.state.fromDate, this.state.toDate);
	      if (rowDates.length > 0) {
	        var _groupTasks = this.groupTasks(this.state.taskData, rowDates),
	            tasks = _groupTasks.tasks,
	            dateWise = _groupTasks.dateWise,
	            taskTotal = _groupTasks.taskTotal,
	            grandTotal = _groupTasks.grandTotal;
	
	        var records = this.setDummyRecord(tasks, dateWise, taskTotal);
	        console.log(taskTotal);
	        this.setState({
	          modeCount: modeCount,
	          rowDates: rowDates,
	          tasks: records.tasks,
	          taskTotal: records.taskTotal,
	          grandTotal: grandTotal,
	          dateWise: records.dateWise,
	          editor: Object.assign({}, this.state.editor, {
	            date: rowDates[0]
	          })
	        });
	      }
	    }
	  }, {
	    key: 'gotoNext',
	    value: function gotoNext() {
	      console.log('next');
	      var modeCount = this.state.modeCount + 1;
	      var rowDates = this.getRowDates(modeCount, this.state.mode, this.state.fromDate, this.state.toDate);
	      if (rowDates.length > 0) {
	        var _groupTasks2 = this.groupTasks(this.state.taskData, rowDates, this.state.fromDate),
	            tasks = _groupTasks2.tasks,
	            dateWise = _groupTasks2.dateWise,
	            taskTotal = _groupTasks2.taskTotal,
	            grandTotal = _groupTasks2.grandTotal;
	
	        var records = this.setDummyRecord(tasks, dateWise, taskTotal);
	        this.setState({
	          modeCount: modeCount,
	          rowDates: rowDates,
	          tasks: records.tasks,
	          taskTotal: records.taskTotal,
	          grandTotal: grandTotal,
	          dateWise: records.dateWise,
	          editor: Object.assign({}, this.state.editor, {
	            date: rowDates[0]
	          })
	        });
	      }
	    }
	  }, {
	    key: 'changeMode',
	    value: function changeMode(mode) {
	      var modeCount = 0;
	      var rowDates = this.getRowDates(modeCount, mode, this.state.fromDate, this.state.toDate);
	      console.log(rowDates);
	      if (rowDates.length > 0) {
	        var _groupTasks3 = this.groupTasks(this.state.taskData, rowDates),
	            tasks = _groupTasks3.tasks,
	            dateWise = _groupTasks3.dateWise,
	            taskTotal = _groupTasks3.taskTotal,
	            grandTotal = _groupTasks3.grandTotal;
	        // this.getGanttData(tasks);
	
	
	        var records = this.setDummyRecord(tasks, dateWise, taskTotal);
	        this.setState({
	          mode: mode,
	          rowDates: rowDates,
	          modeCount: modeCount,
	          dateWise: records.dateWise,
	          tasks: records.tasks,
	          taskTotal: records.taskTotal,
	          grandTotal: grandTotal,
	          editor: Object.assign({}, this.state.editor, {
	            date: rowDates[0]
	          })
	        });
	      }
	    }
	  }, {
	    key: 'setDummyRecord',
	    value: function setDummyRecord(tasks, dateWise, taskTotal) {
	      if (Object.keys(tasks).length < 5) {
	        var start = Object.keys(tasks).length;
	
	        var _loop = function _loop(i) {
	          Object.keys(dateWise).forEach(function (attr) {
	            dateWise[attr].tasks['dummy_' + i] = { duration: '' };
	          });
	          tasks['dummy_' + i] = { project: '' };
	          taskTotal['dummy_' + i] = '';
	        };
	
	        for (var i = start; i < 5; i++) {
	          _loop(i);
	        }
	      }
	      console.log(dateWise);
	      return { tasks: tasks, taskTotal: taskTotal, dateWise: dateWise };
	    }
	  }, {
	    key: 'refreshData',
	    value: function refreshData() {
	      var _this3 = this;
	
	      var rowDates = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
	
	      var service = new _service2.default();
	      var taskData = [];
	      console.log(window.location.href);
	      var regex = /[?&]([^=#]+)=([^&#]*)/g;
	      var url = window.location.href;
	      var params = {};
	      var match = void 0;
	      while (match = regex.exec(url)) {
	        params[match[1]] = match[2];
	      }
	      // let timeSheetId = 
	      service.fetchTimesheet(params.timesheetId, 'com.axelor.apps.hr.db.Timesheet').then(function (res) {
	        taskData = res.timeline;
	        if (rowDates === null) {
	          rowDates = _this3.getRowDates(_this3.state.modeCount, 'week', res.fromDate, res.toDate);
	          _this3.setState({ fromDate: res.fromDate, toDate: res.toDate });
	        }
	
	        var _groupTasks4 = _this3.groupTasks(taskData, rowDates),
	            tasks = _groupTasks4.tasks,
	            dateWise = _groupTasks4.dateWise,
	            taskTotal = _groupTasks4.taskTotal,
	            grandTotal = _groupTasks4.grandTotal;
	
	        console.log(dateWise, tasks, Object.keys(tasks).length);
	        var records = _this3.setDummyRecord(tasks, dateWise, taskTotal);
	        _this3.setState({ rowDates: rowDates, tasks: records.tasks, dateWise: records.dateWise, taskTotal: records.taskTotal, grandTotal: grandTotal, taskData: taskData, timesheetVersion: res.timesheetVersion, timesheetId: res.timesheetId, fromDate: res.fromDate });
	      });
	
	      service.getAction('com.axelor.apps.hr.db.TimesheetLine', 'action-timesheet-line-attrs-domain-project').then(function (res) {
	        var _domain = res.attrs.project.domain;
	        console.log(_domain);
	        service.search('com.axelor.apps.project.db.Project', [], 0, null, ["id"], ["fullName"], _domain).then(function (res) {
	          console.log(res.data[0]);
	          _this3.setState({
	            projectTaskList: res.data,
	            editor: Object.assign({}, _this3.state.editor, {
	              project: res.data[0]
	            })
	          });
	        });
	      });
	      service.getAction('com.axelor.apps.hr.db.TimesheetLine', 'action-hr-timesheet-line-attrs-domain-product').then(function (res) {
	        var domain = res.attrs.product.domain;
	        console.log(domain);
	        service.search('com.axelor.apps.base.db.Product', [], 0, null, ["id"], ["fullName"], domain).then(function (res) {
	          console.log(res);
	          _this3.setState({
	            subTaskList: res.data,
	            editor: Object.assign({}, _this3.state.editor, {
	              task: res.data[0]
	            })
	          });
	        });
	      });
	    }
	  }, {
	    key: 'componentDidMount',
	    value: function componentDidMount() {
	      this.refreshData();
	    }
	  }, {
	    key: 'getNewTaskId',
	    value: function getNewTaskId(projectId) {
	      var taskData = this.state.taskData;
	
	      var MaxId = Math.max.apply(Math, _toConsumableArray(taskData.map(function (tsk) {
	        if (tsk.taskId !== undefined) return tsk.taskId;
	      }).filter(function (t) {
	        return t !== undefined;
	      })));
	      console.log(MaxId, isFinite(MaxId));
	      return isFinite(MaxId) ? MaxId + 1 : 1;
	    }
	  }, {
	    key: 'addNewLine',
	    value: function addNewLine() {
	      var _state2 = this.state,
	          editor = _state2.editor,
	          taskData = _state2.taskData,
	          rowDates = _state2.rowDates;
	
	      var hasRecord = taskData.filter(function (task) {
	        return task.date === editor.date;
	      });
	      console.log(hasRecord);
	      if (hasRecord.length > 0) {
	        console.log('ss');
	        var obj = {
	          projectId: editor.project.id,
	          taskId: editor.task.id,
	          task: editor.task.fullName,
	          date: editor.date,
	          duration: editor.duration
	        };
	        this.updateDuration(obj);
	      } else {
	        taskData.push({
	          user: 'u1',
	          project: editor.project.fullName,
	          projectId: editor.project.id,
	          duration: parseFloat(editor.duration === '' ? 0 : editor.duration),
	          date: editor.date,
	          task: editor.task.fullName,
	          taskId: editor.task.id
	        });
	        this.setState({ taskData: taskData });
	
	        var _groupTasks5 = this.groupTasks(taskData, rowDates),
	            tasks = _groupTasks5.tasks,
	            dateWise = _groupTasks5.dateWise,
	            taskTotal = _groupTasks5.taskTotal,
	            grandTotal = _groupTasks5.grandTotal;
	
	        var records = this.setDummyRecord(tasks, dateWise, taskTotal);
	        this.setState({
	          taskData: taskData,
	          tasks: records.tasks,
	          dateWise: records.dateWise,
	          taskTotal: records.taskTotal,
	          grandTotal: grandTotal
	        });
	      }
	      this.setState({ show: false });
	      console.log(taskData);
	    }
	  }, {
	    key: 'saveData',
	    value: function saveData(task) {
	      var _this4 = this;
	
	      var service = new _service2.default();
	      service.updateModel(task, 'com.axelor.apps.hr.db.TimesheetLine').then(function (res) {
	        console.log(res);
	        var taskData = [];
	        service.fetchTimesheet(_this4.state.timesheetId, 'com.axelor.apps.hr.db.Timesheet').then(function (res) {
	          taskData = res.timeline;
	
	          var _groupTasks6 = _this4.groupTasks(taskData, _this4.state.rowDates),
	              tasks = _groupTasks6.tasks,
	              dateWise = _groupTasks6.dateWise,
	              taskTotal = _groupTasks6.taskTotal,
	              grandTotal = _groupTasks6.grandTotal;
	
	          var records = _this4.setDummyRecord(tasks, dateWise, taskTotal);
	          console.log(dateWise);
	          _this4.setState({ tasks: records.tasks, dateWise: records.dateWise, taskTotal: records.taskTotal, grandTotal: grandTotal, taskData: taskData, timesheetVersion: res.timesheetVersion });
	        });
	      });
	      // ).then(res => {
	      //   console.log(res);
	      //   service.fetchTimesheet(this.state.timesheetId, 'com.axelor.apps.hr.db.Timesheet').then(res => {
	      //       taskData = res.timeline;
	      //       const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(taskData, this.state.rowDates);
	      //       console.log(dateWise);
	      //       this.setState({ tasks, dateWise, taskTotal, grandTotal, taskData, timesheetVersion: res.timesheetVersion });
	      //     });
	      // })
	      // service.updateModel()
	      // service.updateTimeSheet(taskData, this.state.timesheetVersion, this.state.timesheetId).then(res => {
	      //   if (res.statusCode === 200) {
	      //     let taskData = [];
	      //     service.fetchTimesheet(this.state.timesheetId, 'com.axelor.apps.hr.db.Timesheet').then(res => {
	      //       taskData = res.timeline;
	      //       const { tasks, dateWise, taskTotal, grandTotal } = this.groupTasks(taskData, this.state.rowDates);
	      //       console.log(dateWise);
	      //       this.setState({ tasks, dateWise, taskTotal, grandTotal, taskData, timesheetVersion: res.timesheetVersion });
	      //     });
	      //     console.log('success');
	
	      //   } else {
	      //     console.log('Failed');
	      //   }
	      // });
	    }
	  }, {
	    key: 'cleanEditor',
	    value: function cleanEditor() {
	      this.setState({
	        show: true,
	        editor: {
	          project: this.state.editor.project,
	          task: this.state.editor.task,
	          date: (0, _moment2.default)().format('YYYY-MM-DD'),
	          duration: '0'
	        }
	      });
	    }
	  }, {
	    key: 'render',
	    value: function render() {
	      var _this5 = this;
	
	      // console.log(global._t('Monday'))
	      var service = new _service2.default();
	      var close = function close() {
	        return _this5.setState({ show: false });
	      };
	      return _react2.default.createElement(
	        'div',
	        { style: { display: 'flex', flexDirection: 'column' } },
	        _react2.default.createElement(
	          'div',
	          { style: { padding: '0% 3%' } },
	          _react2.default.createElement(
	            'div',
	            { className: 'navbar', style: { minHeight: 0 } },
	            _react2.default.createElement(
	              _reactBootstrap.ButtonToolbar,
	              null,
	              _react2.default.createElement(
	                _reactBootstrap.ButtonGroup,
	                { style: { marginRight: 10 } },
	                _react2.default.createElement(
	                  'button',
	                  { onClick: function onClick() {
	                      return _this5.gotoPrev();
	                    }, className: 'navigation', style: { marginRight: 5 } },
	                  _react2.default.createElement(_reactBootstrap.Glyphicon, { glyph: 'chevron-left', style: { color: 'white' } })
	                ),
	                _react2.default.createElement(
	                  'button',
	                  { onClick: function onClick() {
	                      return _this5.gotoNext();
	                    }, className: 'navigation' },
	                  _react2.default.createElement(_reactBootstrap.Glyphicon, { glyph: 'chevron-right', style: { color: 'white' } })
	                )
	              ),
	              _react2.default.createElement(
	                _reactBootstrap.Label,
	                { className: this.state.mode === 'week' ? 'activeLabel mode-label' : 'nonActive mode-label', onClick: function onClick(e) {
	                    return _this5.changeMode('week');
	                  } },
	                (0, _cellComponent.translate)('Week')
	              ),
	              _react2.default.createElement(
	                _reactBootstrap.Label,
	                { className: this.state.mode === 'month' ? 'activeLabel mode-label' : 'nonActive mode-label', onClick: function onClick(e) {
	                    return _this5.changeMode('month');
	                  } },
	                (0, _cellComponent.translate)('Month')
	              )
	            )
	          )
	        ),
	        _react2.default.createElement(_timeSheet2.default, {
	          rowDates: this.state.rowDates,
	          changeDuration: function changeDuration(obj) {
	            console.log(obj);
	            _this5.updateDuration(obj);
	          },
	          dateWise: this.state.dateWise,
	          tasks: this.state.tasks,
	          taskTotal: this.state.taskTotal,
	          grandTotal: this.state.grandTotal,
	          isLarge: this.state.mode === 'week' ? false : true,
	          addLine: function addLine() {
	            return _this5.cleanEditor();
	          }
	        }),
	        _react2.default.createElement(
	          _reactBootstrap.Modal,
	          {
	            show: this.state.show,
	            onHide: close,
	            container: this,
	            'aria-labelledby': 'contained-modal-title'
	          },
	          _react2.default.createElement(
	            _reactBootstrap.Modal.Header,
	            { closeButton: true },
	            _react2.default.createElement(
	              _reactBootstrap.Modal.Title,
	              { id: 'contained-modal-title' },
	              (0, _cellComponent.translate)('TimeSheet Line')
	            )
	          ),
	          _react2.default.createElement(
	            _reactBootstrap.Modal.Body,
	            null,
	            _react2.default.createElement(
	              _reactBootstrap.FormGroup,
	              null,
	              _react2.default.createElement(
	                _reactBootstrap.ControlLabel,
	                { style: { float: 'left' } },
	                (0, _cellComponent.translate)('Date')
	              ),
	              ' ',
	              _react2.default.createElement('br', null),
	              _react2.default.createElement(
	                'div',
	                { style: { float: 'left' } },
	                _react2.default.createElement(_reactBootstrapDatePicker2.default, {
	                  id: 'example-datepicker',
	                  value: (0, _moment2.default)(this.state.editor.date, this.state.taskDateFormat).format(),
	                  dateFormat: 'DD-MM-YYYY',
	                  minDate: (0, _moment2.default)(this.state.fromDate, 'YYYY-MM-DD').toISOString(),
	                  onChange: function onChange(value, formattedValue) {
	                    _this5.setState({ editor: Object.assign({}, _this5.state.editor, { date: (0, _moment2.default)(value).format(_this5.state.taskDateFormat) }) });
	                  }
	                })
	              )
	            ),
	            _react2.default.createElement(
	              _reactBootstrap.FormGroup,
	              null,
	              _react2.default.createElement(
	                _reactBootstrap.ControlLabel,
	                { style: { float: 'left' } },
	                (0, _cellComponent.translate)('Project')
	              ),
	              _react2.default.createElement(
	                _reactBootstrap.FormControl,
	                {
	                  componentClass: 'select',
	                  value: this.state.editor.project.id,
	                  onChange: function onChange(e) {
	                    var project = _this5.state.projectTaskList.filter(function (p) {
	                      return p.id == e.target.value;
	                    })[0];
	                    console.log(project);
	                    _this5.setState({ editor: Object.assign({}, _this5.state.editor, { project: project }) });
	                  }
	                },
	                this.state.projectTaskList.map(function (p, index) {
	                  return _react2.default.createElement(
	                    'option',
	                    { key: index, value: p.id },
	                    p.fullName
	                  );
	                })
	              )
	            ),
	            _react2.default.createElement(
	              _reactBootstrap.FormGroup,
	              null,
	              _react2.default.createElement(
	                _reactBootstrap.ControlLabel,
	                { style: { float: 'left' } },
	                (0, _cellComponent.translate)('Activity')
	              ),
	              _react2.default.createElement(
	                _reactBootstrap.FormControl,
	                {
	                  componentClass: 'select',
	                  value: this.state.editor.task.id,
	                  onChange: function onChange(e) {
	                    var task = _this5.state.subTaskList.filter(function (p) {
	                      return p.id == e.target.value;
	                    })[0];
	                    _this5.setState({ editor: Object.assign({}, _this5.state.editor, { task: task }) });
	                  }
	                },
	                this.state.subTaskList.map(function (p, index) {
	                  return _react2.default.createElement(
	                    'option',
	                    { key: index, value: p.id },
	                    p.fullName
	                  );
	                })
	              )
	            ),
	            _react2.default.createElement(
	              _reactBootstrap.FormGroup,
	              null,
	              _react2.default.createElement(
	                _reactBootstrap.ControlLabel,
	                { style: { float: 'left' } },
	                (0, _cellComponent.translate)('Duration')
	              ),
	              _react2.default.createElement(_reactBootstrap.FormControl, {
	                type: 'text',
	                placeholder: this.state.editor.duration,
	                value: this.state.editor.duration,
	                onChange: function onChange(e) {
	                  var valid = validateDuration(e.target.value);
	                  console.log(valid);
	                  if (valid) {
	                    _this5.setState({ editor: Object.assign({}, _this5.state.editor, { duration: e.target.value }) });
	                  }
	                }
	              })
	            )
	          ),
	          _react2.default.createElement(
	            _reactBootstrap.Modal.Footer,
	            null,
	            _react2.default.createElement(
	              'button',
	              { className: 'addLine', style: { float: 'right', color: 'white' }, onClick: function onClick() {
	                  return _this5.addNewLine();
	                } },
	              'ADD A LINE'
	            )
	          )
	        )
	      );
	    }
	  }]);
	
	  return Container;
	}(_react.Component);
	
	exports.default = Container;

/***/ },

/***/ 128:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	Object.defineProperty(exports, "__esModule", {
	  value: true
	});
	
	var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();
	
	exports.translate = translate;
	
	var _react = __webpack_require__(2);
	
	var _react2 = _interopRequireDefault(_react);
	
	var _moment = __webpack_require__(1);
	
	var _moment2 = _interopRequireDefault(_moment);
	
	var _reactBootstrap = __webpack_require__(106);
	
	var _container = __webpack_require__(79);
	
	function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
	
	function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
	
	function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }
	
	function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }
	
	function translate(str) {
	  if (window._t && typeof str === 'string') {
	    return window._t(str);
	  }
	  return str;
	}
	
	var CellComponent = function (_Component) {
	  _inherits(CellComponent, _Component);
	
	  function CellComponent(props) {
	    _classCallCheck(this, CellComponent);
	
	    var _this = _possibleConstructorReturn(this, (CellComponent.__proto__ || Object.getPrototypeOf(CellComponent)).call(this, props));
	
	    _this.state = {
	      data: {
	        tasks: {}
	      },
	      isField: true
	    };
	    return _this;
	  }
	
	  _createClass(CellComponent, [{
	    key: 'getData',
	    value: function getData(taskIndex, duration) {
	      var task = this.props.data.tasks[taskIndex];
	      return { date: task.date, projectId: task.projectId, taskId: task.taskId, task: task.task, duration: parseFloat(duration === '' ? 0 : duration) };
	    }
	  }, {
	    key: 'componentDidMount',
	    value: function componentDidMount() {
	      var _this2 = this;
	
	      if (this.props.isField) {
	        var data = {};
	        Object.keys(this.props.data.tasks).forEach(function (d) {
	          var obj = _this2.props.data.tasks[d];
	          obj.duration = (0, _container.convertNumberToTime)(obj.duration);
	          data[d] = obj;
	        });
	        console.log(data);
	        this.setState({ data: Object.assign({}, this.props.data, { tasks: data }), isField: this.props.isField });
	      } else {
	        this.setState({ data: this.props.data, isField: this.props.isField });
	      }
	    }
	  }, {
	    key: 'componentWillReceiveProps',
	    value: function componentWillReceiveProps(nextProps) {
	      if (nextProps.isField) {
	        var data = {};
	        Object.keys(nextProps.data.tasks).forEach(function (d) {
	          var obj = nextProps.data.tasks[d];
	          obj.duration = (0, _container.convertNumberToTime)(obj.duration);
	          data[d] = obj;
	        });
	        console.log(data);
	        this.setState({ data: Object.assign({}, nextProps.data, { tasks: data }), isField: nextProps.isField });
	      } else {
	        this.setState({ data: nextProps.data, isField: nextProps.isField });
	      }
	    }
	  }, {
	    key: 'render',
	    value: function render() {
	      var _this3 = this;
	
	      var _props = this.props,
	          isField = _props.isField,
	          isToday = _props.isToday,
	          cellBackGroundColor = _props.cellBackGroundColor;
	
	      var backgroundColor = this.props.cellBackgroundColor || '#FFFFFF';
	      backgroundColor = this.props.isToday ? 'aliceblue' : backgroundColor;
	      // console.log(this.state.isField, this.state.data);
	      var styles = {
	        backgroundColor: cellBackGroundColor !== undefined && '' + cellBackGroundColor,
	        color: cellBackGroundColor && 'white',
	        borderTop: cellBackGroundColor && '0px !important'
	      };
	      return _react2.default.createElement(
	        'div',
	        { style: { position: 'relative', minWidth: this.state.isField ? 120 : 'auto', height: '100%', borderTop: '1px solid #DDDDDD' } },
	        _react2.default.createElement(
	          'div',
	          { style: { backgroundColor: backgroundColor } },
	          isField ? _react2.default.createElement(
	            'div',
	            { className: 'cell-height cell-header', style: { height: 50 } },
	            _react2.default.createElement(
	              'div',
	              { style: { height: 'inherit', padding: 10, paddingBottom: 20, backgroundColor: isToday && 'rgba(2,117,216, 0.298039)' } },
	              _react2.default.createElement(
	                'span',
	                { style: { display: 'block', fontWeight: 'bold' } },
	                translate((0, _moment2.default)(this.props.header, 'YYYY-MM-DD').format('dddd'))
	              ),
	              _react2.default.createElement(
	                'span',
	                { style: { display: 'block', fontWeight: 'bold' } },
	                translate((0, _moment2.default)(this.props.header, 'YYYY-MM-DD').format('MMM')),
	                ' ',
	                translate((0, _moment2.default)(this.props.header, 'YYYY-MM-DD').format('DD'))
	              )
	            )
	          ) : _react2.default.createElement(
	            'div',
	            {
	              className: 'cell-height cell-header',
	              style: Object.assign({}, styles, { padding: 10, paddingBottom: 20, height: 50, fontWeight: 'bold' }) },
	            _react2.default.createElement(
	              'span',
	              null,
	              translate(this.props.header)
	            )
	          ),
	          _react2.default.createElement(
	            'div',
	            { className: this.props.bodyStyleClass },
	            this.props.isField ? Object.keys(this.state.data.tasks).map(function (task, index) {
	              return _react2.default.createElement(
	                'div',
	                { className: 'cell-height', key: index, style: { borderTop: '1px solid #DDDDDD', backgroundColor: index % 2 !== 0 ? '#FFFFFF' : '#F2F2F2' } },
	                _react2.default.createElement(
	                  'div',
	                  { style: { height: 'inherit', backgroundColor: isToday && 'rgba(2,117,216, 0.3)' } },
	                  task.startsWith('dummy') ? _react2.default.createElement('span', null) : _react2.default.createElement('input', {
	                    className: 'duration-input',
	                    type: 'text',
	                    tabIndex: index * _this3.props.noOfDays + _this3.props.index,
	                    style: { backgroundColor: 'transparent' } //index % 2 !== 0 ? '#FFFFFF' : '#F2F2F2'
	                    , value: _this3.state.data.tasks[task].duration,
	                    onChange: function onChange(e) {
	                      if ((0, _container.validateDuration)(e.target.value)) {
	                        var data = _this3.state.data;
	                        data.tasks[task].duration = e.target.value;
	                        _this3.setState({
	                          data: data
	                        });
	                      }
	                    },
	                    onBlur: function onBlur(e) {
	                      if ((0, _container.validateDuration)(e.target.value)) {
	
	                        console.log(e.target.value, (0, _container.convertTimeToNumber)(e.target.value));
	                        _this3.props.changeDuration(_this3.getData(task, (0, _container.convertTimeToNumber)(e.target.value)));
	                      }
	                    }
	                  })
	                )
	              );
	            }) : Object.keys(this.props.data).map(function (task, index) {
	              return _react2.default.createElement(
	                'div',
	                {
	                  className: 'cell-height',
	                  key: index,
	                  style: Object.assign({}, styles, {
	                    //minWidth: 75,
	                    borderTop: !cellBackGroundColor && '1px solid #DDDDDD',
	                    overflowY: 'auto',
	                    backgroundColor: cellBackGroundColor ? styles.backgroundColor : index % 2 !== 0 ? '#FFFFFF' : '#F2F2F2'
	                  })
	                },
	                _react2.default.createElement(
	                  'div',
	                  { style: { padding: '2px 5px' } },
	                  isNaN(_this3.props.data[task]) || _this3.props.data[task] === '' ? _this3.props.data[task] : (0, _container.convertNumberToTime)(_this3.props.data[task])
	                )
	              );
	            })
	          ),
	          _react2.default.createElement(
	            'div',
	            { className: 'task-footer', style: { color: 'white', borderTop: !cellBackGroundColor && '1px solid #DDDDDD', backgroundColor: '#334250', textAlign: 'center' } },
	            isToday ? _react2.default.createElement(
	              'div',
	              { style: { height: 'inherit', backgroundColor: 'rgba(2,117,216, 0.3)' } },
	              _react2.default.createElement(
	                'span',
	                { style: { display: 'block', fontWeight: 'bold', paddingTop: 10 } },
	                translate(this.props.footer) || (0, _container.convertNumberToTime)(this.props.data.total)
	              )
	            ) : _react2.default.createElement(
	              'span',
	              { style: { display: 'block', fontWeight: 'bold', paddingTop: 10 } },
	              translate(this.props.footer) || (0, _container.convertNumberToTime)(this.props.data.total)
	            )
	          )
	        )
	      );
	    }
	  }]);
	
	  return CellComponent;
	}(_react.Component);
	
	exports.default = CellComponent;

/***/ },

/***/ 129:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	Object.defineProperty(exports, "__esModule", {
	  value: true
	});
	
	var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();
	
	var _react = __webpack_require__(2);
	
	var _react2 = _interopRequireDefault(_react);
	
	var _superagent = __webpack_require__(559);
	
	var _superagent2 = _interopRequireDefault(_superagent);
	
	function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
	
	function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
	
	var headers = {
	  'Accept': 'application/json',
	  'Content-Type': 'application/json',
	  'X-Requested-With': 'XMLHttpRequest'
	};
	
	var Services = function () {
	  function Services(props) {
	    _classCallCheck(this, Services);
	
	    this.state = {
	      baseURL: '/axelor-erp/',
	      restURL: '../../ws/rest/',
	      actionURL: '../../ws/action/',
	      // actionURL: '/axelor-erp/ws/action/',
	      // restURL: '/axelor-erp/ws/rest/',
	      actionName: 'com.axelor.apps.hr.service.timesheet.TimesheetService:getCurrentOrCreateTimesheet()'
	    };
	  }
	
	  _createClass(Services, [{
	    key: 'doLogin',
	    value: function doLogin(userName, password) {
	      var context = this;
	      _superagent2.default
	      //.post(`${this.state.actionURL}${this.state.actionName}`)
	      .post(this.state.baseURL + 'login.jsp').withCredentials().send({ username: userName, password: password }).set('Accept', 'application/json').set('Content-Type', 'application/json').set('X-Requested-With', 'XMLHttpRequest').end(function (err, res) {
	        console.log(err, res);
	        context.fetchTimesheet();
	      });
	    }
	  }, {
	    key: 'info',
	    value: function info() {
	      fetch(this.state.baseURL + 'ws/app/info', {
	        method: 'GET',
	        credentials: 'include',
	        headers: headers
	      }).then(function (res) {
	        return console.log(res);
	      });
	    }
	  }, {
	    key: 'updateModel',
	    value: function updateModel(model, entity) {
	      return fetch('' + this.state.restURL + entity, {
	        method: 'POST',
	        credentials: 'include',
	        headers: headers,
	        body: JSON.stringify({
	          "data": model
	        })
	      }).then(function (response) {
	        return response.json();
	      }).then(function (body) {
	        return body;
	      });
	    }
	  }, {
	    key: 'updateTimeSheet',
	    value: function updateTimeSheet(data, version, id) {
	      var _this = this;
	
	      var timeSheetLine = [];
	      data.forEach(function (line) {
	        var record = {};
	        record['id'] = line.id || null;
	        record.version = line.version;
	        record.project = { id: line.projectId };
	        record.product = { id: line.taskId };
	        record.visibleDuration = line.duration;
	        record.durationStored = line.duration;
	        record.date = line.date;
	        timeSheetLine.push(record);
	      });
	      return new Promise(function (resolve, reject) {
	        _superagent2.default.post(_this.state.restURL + 'com.axelor.apps.hr.db.Timesheet').withCredentials().send({
	          data: {
	            id: id,
	            version: version,
	            timesheetLineList: timeSheetLine
	          }
	        }).set('Accept', 'application/json').set('Content-Type', 'application/json').set('X-Requested-With', 'XMLHttpRequest').end(function (err, res) {
	          if (res) {
	            resolve(res);
	          } else {
	            reject(err);
	          }
	        });
	      });
	    }
	  }, {
	    key: 'fetchTimesheet',
	    value: function fetchTimesheet() {
	      var _this2 = this;
	
	      var timeSheetId = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;
	      var entity = arguments[1];
	
	      var context = this;
	      var id = void 0;
	      return new Promise(function (resolve, reject) {
	        _superagent2.default.post('' + _this2.state.restURL + entity + '/' + timeSheetId + '/fetch').withCredentials().send({
	          "data": {}
	        }).set('Accept', 'application/json').set('Content-Type', 'application/json').set('X-Requested-With', 'XMLHttpRequest').end(function (err, res) {
	          var body = res.body;
	
	          var record = body.data[0];
	          id = record.id;
	          var criteria = [];
	          var value = [];
	          if (record.timesheetLineList.length > 0) {
	            record.timesheetLineList.forEach(function (_ref) {
	              var id = _ref.id;
	              return value.push(id);
	            });
	          }
	          var sortBy = ["id"];
	          criteria.push({ fieldName: 'id', operator: 'in', value: value });
	          context.search('com.axelor.apps.hr.db.TimesheetLine', criteria, 0, -1, sortBy, []).then(function (body) {
	            var timeline = [];
	            body.data.length && body.data.forEach(function (task) {
	              timeline.push({
	                id: task.id,
	                version: task.version,
	                projectId: task.project.id,
	                project: task.project.fullName,
	                date: task.date,
	                duration: Number(task.visibleDuration),
	                taskId: task.product.id,
	                task: task.product.fullName
	              });
	            });
	            resolve({ timeline: timeline, timesheetVersion: record.version, timesheetId: id, fromDate: record.fromDate, toDate: record.toDate });
	          }, reject);
	        });
	      });
	    }
	  }, {
	    key: 'getAction',
	    value: function getAction(model, action) {
	      var _this3 = this;
	
	      var body = JSON.stringify({
	        "action": '' + action,
	        "model": '' + model,
	        "data": {}
	      });
	      return new Promise(function (resolve, reject) {
	        _superagent2.default.post('' + _this3.state.actionURL).withCredentials().send(body).set('Accept', 'application/json').set('Content-Type', 'application/json').set('X-Requested-With', 'XMLHttpRequest').end(function (err, res) {
	          console.log(err, res);
	          if (res) {
	            resolve(res.body.data[0]);
	          } else {
	            reject(err);
	          }
	        });
	      });
	    }
	  }, {
	    key: 'search',
	    value: function search(entity) {
	      var criteria = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
	      var offset = arguments[2];
	      var limit = arguments[3];
	      var sortBy = arguments[4];
	      var fields = arguments.length > 5 && arguments[5] !== undefined ? arguments[5] : [];
	      var domain = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : null;
	
	      var body = JSON.stringify({
	        "data": {
	          _domain: domain,
	          criteria: criteria,
	          operator: 'and'
	        },
	        "offset": offset,
	        "limit": limit,
	        "sortBy": sortBy
	      });
	      if (fields.length > 0) {
	        var temp = JSON.parse(body);
	        temp.fields = fields;
	        body = JSON.stringify(temp);
	      }
	
	      return fetch('' + this.state.restURL + entity + '/search', {
	        method: 'POST',
	        credentials: 'include',
	        headers: headers,
	        body: body
	      }).then(function (response) {
	        return response.json();
	      }).then(function (body) {
	        return body;
	      });
	    }
	  }]);
	
	  return Services;
	}();
	
	exports.default = Services;

/***/ },

/***/ 150:
/***/ function(module, exports) {

	// removed by extract-text-webpack-plugin

/***/ },

/***/ 322:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	if (typeof Promise === 'undefined') {
	  // Rejection tracking prevents a common issue where React gets into an
	  // inconsistent state due to an error, but it gets swallowed by a Promise,
	  // and the user has no idea what causes React's erratic future behavior.
	  __webpack_require__(410).enable();
	  window.Promise = __webpack_require__(409);
	}
	
	// fetch() polyfill for making API calls.
	__webpack_require__(567);
	
	// Object.assign() is commonly used with React.
	// It will use the native implementation if it's present and isn't buggy.
	Object.assign = __webpack_require__(15);


/***/ },

/***/ 324:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	Object.defineProperty(exports, "__esModule", {
	  value: true
	});
	
	var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();
	
	var _react = __webpack_require__(2);
	
	var _react2 = _interopRequireDefault(_react);
	
	var _logo = __webpack_require__(406);
	
	var _logo2 = _interopRequireDefault(_logo);
	
	__webpack_require__(392);
	
	var _service = __webpack_require__(129);
	
	var _service2 = _interopRequireDefault(_service);
	
	var _container = __webpack_require__(79);
	
	var _container2 = _interopRequireDefault(_container);
	
	function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
	
	function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
	
	function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }
	
	function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }
	
	var App = function (_Component) {
	  _inherits(App, _Component);
	
	  function App() {
	    _classCallCheck(this, App);
	
	    return _possibleConstructorReturn(this, (App.__proto__ || Object.getPrototypeOf(App)).apply(this, arguments));
	  }
	
	  _createClass(App, [{
	    key: 'render',
	    value: function render() {
	      return _react2.default.createElement(
	        'div',
	        { className: 'App' },
	        _react2.default.createElement(_container2.default, null)
	      );
	    }
	  }]);
	
	  return App;
	}(_react.Component);
	
	exports.default = App;

/***/ },

/***/ 325:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	Object.defineProperty(exports, "__esModule", {
	  value: true
	});
	
	var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();
	
	var _react = __webpack_require__(2);
	
	var _react2 = _interopRequireDefault(_react);
	
	var _moment = __webpack_require__(1);
	
	var _moment2 = _interopRequireDefault(_moment);
	
	var _reactBootstrap = __webpack_require__(106);
	
	var _container = __webpack_require__(79);
	
	__webpack_require__(150);
	
	var _cellComponent = __webpack_require__(128);
	
	var _cellComponent2 = _interopRequireDefault(_cellComponent);
	
	function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
	
	function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
	
	function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }
	
	function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }
	
	var TimeSheet = function (_Component) {
	  _inherits(TimeSheet, _Component);
	
	  function TimeSheet() {
	    _classCallCheck(this, TimeSheet);
	
	    return _possibleConstructorReturn(this, (TimeSheet.__proto__ || Object.getPrototypeOf(TimeSheet)).apply(this, arguments));
	  }
	
	  _createClass(TimeSheet, [{
	    key: 'isToday',
	    value: function isToday(date) {
	      console.log((0, _moment2.default)(date, 'YYYY-MM-DD').isSame((0, _moment2.default)().format('YYYY-MM-DD')));
	      return (0, _moment2.default)(date, 'YYYY-MM-DD').isSame((0, _moment2.default)().format('YYYY-MM-DD'));
	    }
	  }, {
	    key: 'makeProjectTitle',
	    value: function makeProjectTitle(tasks) {
	      var task = {};
	      Object.keys(tasks).map(function (tsk) {
	        var temp = tasks[tsk];
	        var title = '' + temp.project;
	        if (temp.taskId) {
	          title = title + ' / ' + temp.task;
	        }
	        task[tsk] = title;
	      });
	      return task;
	    }
	  }, {
	    key: 'render',
	    value: function render() {
	      var _this2 = this;
	
	      var backgroundColor = '#DDDDDD';
	      var isLarge = this.props.isLarge;
	
	      var divStyle = { overflowX: 'auto', overflowY: 'hidden', display: 'inline-block', backgroundColor: '#FFFFFF', borderBottom: '1px solid #DDDDDD' };
	      if (isLarge) {
	        divStyle = Object.assign({}, divStyle);
	      }
	      return _react2.default.createElement(
	        'div',
	        { className: 'timesheet-content' },
	        _react2.default.createElement(
	          'div',
	          { className: 'task-content' },
	          _react2.default.createElement(_cellComponent2.default, {
	            bodyStyleClass: 'text-content',
	            footer: _react2.default.createElement(
	              'button',
	              {
	                className: 'addLine',
	                onClick: function onClick() {
	                  return _this2.props.addLine();
	                }
	              },
	              'ADD A LINE'
	            ),
	            data: this.makeProjectTitle(this.props.tasks),
	            isField: false
	          })
	        ),
	        _react2.default.createElement(
	          'div',
	          { style: Object.assign({}, divStyle, { maxWidth: '65%' }) },
	          _react2.default.createElement(
	            'div',
	            { className: 'duration-content', style: { width: Object.keys(this.props.dateWise).length * 120 } },
	            this.props.dateWise && Object.keys(this.props.dateWise).map(function (r, i) {
	              return _react2.default.createElement(
	                'div',
	                { key: i, style: { width: 120, display: 'inline-block', height: '100%' } },
	                _react2.default.createElement(_cellComponent2.default, {
	                  header: r,
	                  isToday: _this2.isToday(r),
	                  isField: true,
	                  index: i + 1,
	                  noOfDays: _this2.props.rowDates.length,
	                  data: _this2.props.dateWise[r], changeDuration: function changeDuration(d) {
	                    return _this2.props.changeDuration(d);
	                  }
	                })
	              );
	            })
	          )
	        ),
	        _react2.default.createElement(
	          'div',
	          { className: 'footer-content', style: { maxWidth: '10%', width: '5%', flex: 1 } },
	          _react2.default.createElement(_cellComponent2.default, {
	            bodyStyleClass: 'footer-context',
	            cellBackGroundColor: '#334250',
	            header: (0, _cellComponent.translate)('Total'),
	            footer: (0, _container.convertNumberToTime)('' + this.props.grandTotal),
	            data: this.props.taskTotal,
	            isField: false
	          })
	        )
	      );
	    }
	  }]);
	
	  return TimeSheet;
	}(_react.Component);
	
	exports.default = TimeSheet;

/***/ },

/***/ 326:
/***/ function(module, exports, __webpack_require__) {

	'use strict';
	
	var _react = __webpack_require__(2);
	
	var _react2 = _interopRequireDefault(_react);
	
	var _reactDom = __webpack_require__(20);
	
	var _reactDom2 = _interopRequireDefault(_reactDom);
	
	var _App = __webpack_require__(324);
	
	var _App2 = _interopRequireDefault(_App);
	
	__webpack_require__(393);
	
	var _lodash = __webpack_require__(407);
	
	var _lodash2 = _interopRequireDefault(_lodash);
	
	function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
	
	_reactDom2.default.render(_react2.default.createElement(_App2.default, null), document.getElementById('root'));

/***/ },

/***/ 392:
150,

/***/ 393:
150,

/***/ 406:
/***/ function(module, exports, __webpack_require__) {

	module.exports = __webpack_require__.p + "static/media/logo.5d5d9eef.svg";

/***/ }

});
//# sourceMappingURL=main.482fe277.js.map