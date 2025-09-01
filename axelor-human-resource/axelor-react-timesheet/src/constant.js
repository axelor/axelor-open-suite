export const MONTH = [
  "Jan",
  "Feb",
  "Mar",
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec",
];

export const FULLMONTH = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

export const DAYS = [
  "Sunday",
  "Monday",
  "Tuesday",
  "Wednesday",
  "Thursday",
  "Friday",
  "Saturday",
];

/**
 * DATE FORMATS
 */
export const DATE_FORMATS = {
  timestamp_with_seconds: "YYYY-MM-DD HH:mm:ss",
  iso_like_local_timestamp: "YYYY-MM-DDTHH:mm:ss",
  timestamp_with_microseconds: "YYYY-MM-DD HH:mm:ss.SSSSSS",
  iso_8601_utc_timestamp: "YYYY-MM-DDTHH:mm[Z]",
  us_date: "MM/DD/YYYY",
  full_month_day_year_12_hour: "MMMM D YYYY - hA",
  custom: "MMMM D YYYY - h:mm A",
  full_date: "MMMM Do YYYY",
  DD_MM_YYYY: "DD-MM-YYYY",
  YYYY_MM_DD: "YYYY-MM-DD",
  MM_YYYY: "MM-YYYY",
  YYYY: "YYYY",
  hours_12_hour: "hA",
};
/**
 * MODEL FIELDS
 */

export const LEAVE_MODEL_FIELDS = [
  "leaveReason",
  "fromDateT",
  "startOnSelect",
  "toDateT",
  "endOnSelect",
];
export const TIMESHEET_LINE_FIELDS = [
  "comments",
  "date",
  "duration",
  "employee",
  "fullName",
  "hoursDuration",
  "project",
  "projectTask",
  "product",
  "projectTask.invoicingType",
  "timesheet",
  "toInvoice",
];

export const EMPLOYEE_FIELDS = [
  "name",
  "user",
  "timesheetProjectSet",
  "timesheetProjectTaskSet",
  "publicHolidayEventsPlanning",
  "managerUser",
];

export const MOBILE_VIEW = ["xs", "sm"];
export const TABLET_VIEW = ["md", "lg"];
export const DESKTOP_VIEW = ["xl", "xxl"];

export const TIMESHEET_FIELDS = [
  "company",
  "fromDate",
  "toDate",
  "sentDateTime",
  "employee",
  "employee.user",
  "isCompleted",
  "timesheetLineList",
  "timeLoggingPreferenceSelect",
];

export const DATE_PARTS = {
  DAY: "day",
  MONTH: "month",
  YEAR: "year",
};

export const TIME_LOGGING_PREFERENCES = {
  DAYS: "days",
  HOURS: "hours",
  MINUTES: "minutes",
};
