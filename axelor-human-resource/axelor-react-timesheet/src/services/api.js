import {
  DATE_FORMATS,
  DATE_PARTS,
  LEAVE_MODEL_FIELDS,
  TIMESHEET_FIELDS,
} from "../constant";
import Service from "./index";
import { isValidResponse, openTabView, getLatestLineDate } from "./utils";
import dayjs from "../lib/dayjs";

export const TIMESHEET_MODEL = "com.axelor.apps.hr.db.Timesheet";
const USER_MODEL = "com.axelor.auth.db.User";
const EMPLOYEE_MODEL = "com.axelor.apps.hr.db.Employee";
const TIMESHEET_LINE_MODEL = "com.axelor.apps.hr.db.TimesheetLine";
const EVENTS_PLANNING_LINE = "com.axelor.apps.base.db.EventsPlanningLine";
const PROJECT_DB_MODEL = "com.axelor.apps.project.db.Project";
const PROJECT_TASK_MODEL = "com.axelor.apps.project.db.ProjectTask";
const Project_ACTIVITY_MODEL = "com.axelor.apps.base.db.Product";
const LEAVE_REQUEST_MODEL = "com.axelor.apps.hr.db.LeaveRequest";

export const fetchInfo = async () => {
  const res = await Service.get("ws/public/app/info");
  return res || {};
};

export const fetchTimesheets = async (options) => {
  const res = await Service.search(TIMESHEET_MODEL, options);
  const data = res?.data || [];

  const updatedData = data.map((item) => {
    if (item.toDate || !item.fromDate) {
      return item;
    }

    const fromDate = dayjs(item.fromDate, DATE_FORMATS.YYYY_MM_DD);
    const latestLineDate = getLatestLineDate(item.timesheetLineList);

    let computedToDate;

    const isLatestLineInFromMonth =
      latestLineDate && latestLineDate.month() === fromDate.month();

    if (!latestLineDate || isLatestLineInFromMonth) {
      computedToDate = fromDate
        .endOf(DATE_PARTS.MONTH)
        .format(DATE_FORMATS.YYYY_MM_DD);
    } else {
      computedToDate = latestLineDate.format(DATE_FORMATS.YYYY_MM_DD);
    }

    return {
      ...item,
      toDate: computedToDate,
    };
  });

  return { ...res, data: updatedData };
};

export const fetchUsers = async (options) => {
  const res = await Service.search(USER_MODEL, options);
  return res || [];
};

export const fetchEmployees = async (options) => {
  const res = await Service.search(EMPLOYEE_MODEL, options);
  return res || [];
};

export const getCounts = async (timesheetId) => {
  if (!timesheetId) return;
  const res = await Service.get(`/ws/aos/timesheet-line/count/${timesheetId}`);
  return res;
};

export const getDefaultTimesheet = async (employee) => {
  if (!employee?.id) return;
  const today = new Date();

  const getTimesheet = async (criteria, sortBy = [], limit = 1) =>
    (
      await fetchTimesheets({
        data: { criteria, operator: "AND" },
        fields: TIMESHEET_FIELDS,
        sortBy,
        limit,
      })
    )?.data?.[0];

  // 1. Covers today (open-ended allowed)
  const current = await getTimesheet([
    { fieldName: "employee.id", operator: "=", value: employee.id },
    { fieldName: "fromDate", operator: "<=", value: today, type: "date" },
    {
      criteria: [
        { fieldName: "toDate", operator: ">=", value: today, type: "date" },
        { fieldName: "toDate", operator: "isNull" },
      ],
      operator: "OR",
    },
  ]);
  if (current) return current;

  // 2. Nearest future
  const future = await getTimesheet(
    [
      { fieldName: "employee.id", operator: "=", value: employee.id },
      { fieldName: "fromDate", operator: ">", value: today, type: "date" },
    ],
    ["fromDate"]
  );
  if (future) return future;

  // 3. Latest past
  return await getTimesheet(
    [
      { fieldName: "employee.id", operator: "=", value: employee.id },
      { fieldName: "toDate", operator: "<", value: today, type: "date" },
    ],
    ["-toDate"]
  );
};

export const fetchTimesheetOptions = async (employee, options = {}) => {
  if (!employee || !employee.id) {
    return [];
  }
  const res =
    (await fetchTimesheets({
      data: {
        criteria: [
          {
            fieldName: "employee.id",
            operator: "=",
            value: employee.id,
          },
        ],
      },
      fields: TIMESHEET_FIELDS,
      limit: 10,
      sortBy: ["-fromDate"],
      ...options,
    })) || [];
  return res;
};

export const fetchTimesheetLines = async (options = {}) => {
  const res = await Service.search(TIMESHEET_LINE_MODEL, options);
  return res || [];
};

export const fetchPublicHolidays = async (employee, fromDate, toDate) => {
  if (!employee || !employee.id) {
    return [];
  }
  const { publicHolidayEventsPlanning } = employee;

  const criteria = [
    {
      fieldName: "eventsPlanning.id",
      operator: "=",
      value: publicHolidayEventsPlanning.id,
    },
  ];
  if (fromDate) {
    criteria.push({
      fieldName: "date",
      operator: ">=",
      value: new Date(fromDate),
      type: "date",
    });
  }
  if (toDate) {
    criteria.push({
      fieldName: "date",
      operator: "<=",
      value: new Date(toDate),
      type: "date",
    });
  }

  const options = {
    offset: 0,
    fields: ["year", "date", "description"],
    data: {
      criteria,
      operator: "AND",
    },
  };

  const res = await Service.search(EVENTS_PLANNING_LINE, options);
  isValidResponse(res);
  return res.data || [];
};

export const fetchProject = async (employee, fields) => {
  try {
    const options = {
      translate: true,
      fields: ["id", "fullName", "code", ...(fields || [])],
      limit: 10,
      data: {
        _domain: `self.manageTimeSpent = true AND (${employee?.user?.id} MEMBER OF self.membersUserSet OR self.assignedTo.id = ${employee?.user?.id})`,
      },
    };
    const res = await Service.search(PROJECT_DB_MODEL, options);
    isValidResponse(res);
    return res.data || [];
  } catch (error) {
    return error;
  }
};

export const fetchProjectTask = async (projectId, fields = []) => {
  try {
    const options = {
      fields: ["id", "fullName", "code", "invoicingType", ...(fields || [])],
      data: {
        _domain:
          " self.project.id = :id  AND self.project.manageTimeSpent = true ",
        _domainContext: {
          id: projectId,
        },
      },
    };
    const res = await Service.search(PROJECT_TASK_MODEL, options);
    isValidResponse(res);
    return res.data;
  } catch (error) {
    return error;
  }
};

export const fetchActivity = async () => {
  try {
    const options = {
      translate: true,
      fields: ["id", "fullName", "code"],
      data: {
        _domain: "self.isActivity = true AND self.dtype = 'Product'",
      },
    };
    const res = await Service.search(Project_ACTIVITY_MODEL, options);
    isValidResponse(res);
    return res.data;
  } catch (error) {
    return error;
  }
};

export const fetchValidatedLeaves = async (employee, fromDate, toDate) => {
  if (!employee) return [];
  const criteria = [
    {
      fieldName: "statusSelect",
      operator: "=",
      value: "3",
    },
  ];
  if (fromDate) {
    criteria.push({
      fieldName: "fromDateT",
      operator: ">=",
      value: new Date(fromDate),
      type: "date",
    });
  }

  if (toDate) {
    criteria.push({
      fieldName: "toDateT",
      operator: "<=",
      value: new Date(toDate),
      type: "date",
    });
  }
  const options = {
    fields: LEAVE_MODEL_FIELDS,
    data: {
      _domain:
        "self.employee.user.id = :user_id AND self.toJustifyLeaveReason = :to_justify_leave_reason",
      _domainContext: {
        user_id: employee.user.id,
        to_justify_leave_reason: false,
      },
      criteria,
      operator: "and",
    },
  };
  const res = await Service.search(LEAVE_REQUEST_MODEL, options);
  return res.data || [];
};

export const getProjectTasks = async (tasks) => {
  if (!tasks?.length) return [];
  const res = await Service.search(PROJECT_TASK_MODEL, {
    data: {
      criteria: [
        {
          fieldName: "id",
          operator: "IN",
          value: tasks.map((task) => task.id),
        },
      ],
      operator: "AND",
    },
    fields: ["id", "fullName", "name", "project", "invoicingType"],
  });
  return res?.data || [];
};

export const getDefaults = async (employee) => {
  const records = await fetchTimesheetOptions(employee);
  const timesheet =
    records?.total === 1
      ? records?.data?.[0]
      : await getDefaultTimesheet(employee);
  const projectTasks = await getProjectTasks(
    employee?.timesheetProjectTaskSet || []
  );
  // const counts = await getCounts(timesheet);
  return { records, timesheet, projectTasks };
};

export const updateTimesheetLine = async (id, data) => {
  if (!id)
    return {
      status: -1,
      data: { message: "Id should not be null" },
    };
  const url = `ws/rest/${TIMESHEET_LINE_MODEL}`;
  const res = await Service.post(url, { data: { id, ...data } });
  return res || {};
};

export const updateTSDuration = async (options) => {
  const res = await Service.post(`ws/aos/timesheet-line/update`, options);
  return res;
};

export const handleAddTSline = async (record) => {
  const res = await Service.post(`ws/aos/timesheet-line`, record);
  return res.object;
};

export const fetchTimesheetLineById = async (TSlineId) => {
  if (!TSlineId) return null;
  const res = await Service.fetchId(TIMESHEET_LINE_MODEL, TSlineId);
  return res.data?.[0] || {};
};

export const removeDurations = async (options) => {
  const res = await Service.delete(`/ws/aos/timesheet-line`, options);
  return res;
};

export const editTimesheetLine = async (options) => {
  const { timesheetId, date, projectTaskId, projectId } = options;
  openTabView({
    domain: `self.timesheet.id = '${timesheetId}' AND self.date = '${date}' AND self.projectTask.id = '${projectTaskId}' AND self.project.id = '${projectId}'`,
    viewType: "grid",
    model: "com.axelor.apps.hr.db.TimesheetLine",
    title: "Timesheet lines",
    name: "timesheet-line-timesheet-grid",
    views: [
      {
        name: "timesheet-line-timesheet-grid",
        type: "grid",
      },
      {
        name: "timesheet-line-timesheet-form",
        type: "form",
      },
    ],
    params: {
      popup: "reload",
    },
  });

  const res = await Service.action({
    action: "action-timesheet-line-view-timesheet-editor-timesheet-line",
    model: TIMESHEET_LINE_MODEL,
    data: {
      context: {
        ...options,
      },
    },
  });
  return res;
};

export const updateToInvoice = async (data) => {
  const res = await Service.post(
    `ws/aos/timesheet-line/update-to-invoice`,
    data
  );
  return (
    res || { status: -1, data: { message: "Failed to update to invoice" } }
  );
};
