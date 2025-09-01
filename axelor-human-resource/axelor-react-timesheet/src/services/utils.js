import { DATE_FORMATS, TIME_LOGGING_PREFERENCES } from "../constant";
import dayjs from "../lib/dayjs";
import {
  fetchPublicHolidays,
  fetchTimesheets,
  fetchValidatedLeaves,
} from "./api";

export const isEmpty = (obj) => Object.keys(obj).length === 0;

export const extractValue = (dateString, timeUnit) => {
  const date = dayjs(dateString, DATE_FORMATS.YYYY_MM_DD);
  if (!date.isValid()) {
    console.error("Invalid date string", dateString);
    return;
  }
  switch (timeUnit) {
    case "YEAR":
      return date.year();
    case "MONTH":
      return date.month() + 1;
    case "DAY":
      return date.date();
    default:
      return "unknown";
  }
};

export const getSelectionLabel = (fromDate, toDate) => {
  const fromDay = dayjs(fromDate, DATE_FORMATS.YYYY_MM_DD);
  const toDay = dayjs(toDate, DATE_FORMATS.YYYY_MM_DD);

  if (!fromDay.isSame(toDay, "year")) {
    return `${fromDay.format("D")} ${fromDay.format("MMM")} ${fromDay.format(
      "YYYY"
    )} to
        ${toDay.format("D")} ${toDay.format("MMM")} ${toDay.format("YYYY")}`;
  } else if (!fromDay.isSame(toDay, "month")) {
    return `from ${fromDay.format("MMM")} ${fromDay.format(
      "D"
    )} to ${toDay.format("MMM")} ${toDay.format("D")}`;
  } else {
    return `from ${fromDay.format("D")} to ${toDay.format(
      "D"
    )} ${fromDay.format("MMM")}`;
  }
};

export function getDateRange(fromDate, toDate) {
  const result = [];
  let current = dayjs(fromDate, DATE_FORMATS.YYYY_MM_DD);
  const end = dayjs(toDate, DATE_FORMATS.YYYY_MM_DD);

  if (!current.isValid() || !end.isValid()) {
    console.error(
      "Invalid date string provided to getDateRange:",
      fromDate,
      toDate
    );
    return [];
  }
  while (current.isSameOrBefore(end, "day")) {
    result.push(current.format(DATE_FORMATS.YYYY_MM_DD));
    current = current.add(1, "day");
  }
  return result;
}

export function datetoString(date) {
  const d = dayjs.isDayjs(date) ? date : dayjs(date);
  if (!d.isValid()) {
    console.error("not a valid date", d);
  }
  return d.format(DATE_FORMATS.YYYY_MM_DD);
}

export function groupDatesByWeek(dates) {
  const weeks = [];
  let week = [];
  if (!dates?.length) return week;
  const paddedDates = [...(dates || [])];

  const start = new Date(paddedDates[0]);
  const startDay = start.getDay();
  const diffToMonday = startDay === 0 ? 6 : startDay - 1;
  [...Array(diffToMonday)].forEach(() => {
    start.setDate(start.getDate() - 1);
    paddedDates.unshift(datetoString(start));
  });

  const end = new Date(paddedDates[paddedDates.length - 1]);
  const endDay = end.getDay();
  const diffToSunday = endDay === 0 ? 0 : 7 - endDay;

  [...Array(diffToSunday)].forEach(() => {
    end.setDate(end.getDate() + 1);
    paddedDates.push(datetoString(end));
  });

  paddedDates.forEach((date) => {
    week.push(date);
    if (week.length === 7) {
      weeks.push(week);
      week = [];
    }
  });
  return weeks;
}

export function formatHours(h, m) {
  return `${h} h${m > 0 ? ` ${m.toString().padStart(2, "0")}` : " 00"}`;
}

export function formatDays(d) {
  return `${d} d`;
}

export function formatMinutes(d) {
  return `${d} m`;
}

export function getDayNameAndNumber(dateStr, options = {}) {
  const d = dayjs(dateStr, DATE_FORMATS.YYYY_MM_DD);
  if (options.compactView) {
    return [d.format("ddd"), d.date()];
  }
  return [d.format("dddd"), d.date()];
}

export function isWeekend(dateStr) {
  const d = dayjs(dateStr, DATE_FORMATS.YYYY_MM_DD);
  const day = d.day();
  return day === 0 || day === 6;
}

export const formatWeekLabel = (week) => {
  if (!week || !week.length) return "";

  const start = dayjs(week[0], DATE_FORMATS.YYYY_MM_DD);
  const end = dayjs(week[week.length - 1], DATE_FORMATS.YYYY_MM_DD);

  if (!start.isValid() || !end.isValid()) {
    console.error(
      "Invalid date found in week array:",
      week[0],
      week[week.length - 1]
    );
    return "";
  }
  if (!start.isSame(end, "year")) {
    return `${start.format("D MMM YYYY")} - ${end.format("D MMM YYYY")}`;
  } else if (!start.isSame(end, "month")) {
    return `${start.format("D MMMM")} - ${end.format("D MMMM")}`;
  } else {
    return `${start.format("D")} - ${end.format("D MMMM")}`;
  }
};

export const groupByYear = (data) => {
  return data.reduce((acc, curr) => {
    const year = extractValue(curr.toDate, "YEAR");

    const isExist = acc.findIndex((item) => item.label === year);
    const a = {
      ...curr,
      value: curr.id,
      label: getSelectionLabel(curr.fromDate, curr.toDate),
    };
    let updatedacc = [];
    if (isExist !== -1) {
      updatedacc = acc.map((item) =>
        item.label === year ? { ...item, options: [...item.options, a] } : item
      );
    } else {
      updatedacc = [...acc, { label: year, options: [a] }];
    }
    return updatedacc;
  }, []);
};

export const getDayClassName = (date) => {
  const d = dayjs.isDayjs(date) ? date : dayjs(date);
  const day = d.day();
  if (day === 0 || day === 6) {
    return "react-datepicker__day--weekend";
  }
  return undefined;
};

export const getIncludedIntervals = async (date, employee) => {
  const startofMonth = dayjs(date)
    .startOf("month")
    .format(DATE_FORMATS.YYYY_MM_DD);

  const endOfMonth = dayjs(date)
    .add(1, "month")
    .startOf("month")
    .format(DATE_FORMATS.YYYY_MM_DD);

  const res = await fetchTimesheets({
    data: {
      _domain: `self.employee.id = ${employee.id} `,
      criteria: [
        {
          fieldName: "toDate",
          operator: ">=",
          value: startofMonth,
        },
        {
          fieldName: "fromDate",
          operator: "<",
          value: endOfMonth,
        },
      ],
      operator: "and",
    },
    fields: ["fromDate", "toDate"],
  });

  const interval = (res.data || []).map((item) => {
    const startDate = dayjs(item.fromDate, DATE_FORMATS.YYYY_MM_DD);
    const endDate = dayjs(item.toDate, DATE_FORMATS.YYYY_MM_DD);
    return {
      start: startDate.format(DATE_FORMATS.iso_like_local_timestamp),
      end: endDate.format(DATE_FORMATS.iso_like_local_timestamp),
    };
  });
  return interval;
};

export function splitIntHourMin(duration) {
  if (!duration) {
    return { hour: 0, min: 0 };
  }
  return {
    hour: Number(duration.split(".")[0] || "0"),
    min: Number(duration.split(".")[1] || "0"),
  };
}

export const getPublicHolidaysDate = async (employee, fromDate, toDate) => {
  const data = await fetchPublicHolidays(employee, fromDate, toDate);
  return (data || []).map((item) => new Date(item.date));
};

export function isValidResponse(res) {
  if (res?.status === 0) {
    return res;
  }
  throw new Error(res?.data?.message || "Something Went wrong");
}

export function convertIntoTSline(row, lines) {
  if (lines && lines.length > 0) {
    return lines;
  }
  if (!row.isProject) {
    return [{ projectTask: row, project: row.project }];
  }
  return [{ project: row }];
}

export const getLeaveListInterval = async (employee, fromDate, toDate) => {
  const data = await fetchValidatedLeaves(employee, fromDate, toDate);

  const excludedInterval = (data || []).map((item) => {
    let startDate =
      item.startOnSelect === 2
        ? dayjs(item.fromDateT)
        : dayjs(item.fromDateT).subtract(1, "day");

    let endDate =
      item.endOnSelect === 1
        ? dayjs(item.toDateT).subtract(1, "day")
        : dayjs(item.toDateT);

    startDate = dayjs(startDate, DATE_FORMATS.YYYY_MM_DD);
    endDate = dayjs(endDate, DATE_FORMATS.YYYY_MM_DD);

    return {
      start: startDate.format(DATE_FORMATS.iso_like_local_timestamp),
      end: endDate.format(DATE_FORMATS.iso_like_local_timestamp),
    };
  });
  return excludedInterval;
};

export const isOnLeave = (date, leaveList) => {
  const onLeave = leaveList?.find((leave) => {
    const leaveStart = dayjs(leave.fromDateT).format("YYYY-MM-DD");
    const leaveEnd = dayjs(leave.toDateT).format("YYYY-MM-DD");
    return dayjs(date).isBetween(leaveStart, leaveEnd, "day", "[]");
  });
  return onLeave;
};

export const getDateStatus = (date, timesheet, holiday, leave) => {
  if (
    new Date(date) < new Date(timesheet.fromDate) ||
    new Date(date) > new Date(timesheet.toDate)
  ) {
    return {
      disable: true,
      message: "This day is not part of the this timesheet",
      outsidePeriod: true,
    };
  }

  if (holiday) {
    return {
      disable: true,
      message: holiday.description
        ? `Public Holiday- ${holiday.description}`
        : "Holiday",
      outsidePeriod: false,
      holiday,
    };
  }
  if (leave) {
    return {
      disable: true,
      leave,
      message: leave.leaveReason?.name,
      outsidePeriod: false,
    };
  }

  if (isWeekend(date)) {
    return {
      disable: false,
      message: "Weekend",
      outsidePeriod: false,
      isWeekend: true,
    };
  }

  return { disable: false, message: null, outsidePeriod: false };
};

export function parseHours(str) {
  if (!str) return { h: 0, m: 0 };
  const [h, m] = str.toString().split(".");
  return { h: Number(h), m: Math.round(Number("0." + (m || "0")) * 60) };
}

export function parseDays(str) {
  const value = parseFloat(str);
  if (isNaN(value)) return { d: 0.0 };
  return { d: Math.round(value * 100) / 100 };
}

export function parseMinutes(str) {
  if (!str) return { m: 0 };

  const minutes = parseFloat(str);
  if (isNaN(minutes)) return { m: 0.0 };
  return { m: minutes.toFixed(2) };
}

export function getTSSelectionOptions(
  lines,
  loggingPreference = TIME_LOGGING_PREFERENCES.HOURS
) {
  if (!lines || lines.length < 1) return [];

  return lines.map((entry) => {
    let durationLabel;

    switch (loggingPreference) {
      case TIME_LOGGING_PREFERENCES.DAYS:
        durationLabel = `${entry.duration || 0} days`;
        break;

      case TIME_LOGGING_PREFERENCES.MINUTES:
        durationLabel = `${entry.duration || 0} minutes`;
        break;

      case TIME_LOGGING_PREFERENCES.HOURS:
      default:
        durationLabel = `${entry.hoursDuration || 0} hours`;
        break;
    }

    return {
      ...entry,
      value: entry.id,
      label: `${entry.project?.fullName || "Unknown Project"}${
        entry.projectTask ? " - " + entry.projectTask.fullName : ""
      } - (${durationLabel})`,
    };
  });
}

export function getAxelorScope() {
  return window.top.parent?.axelor;
}

export function openTabView(view) {
  const scope = getAxelorScope();
  scope && scope.openView && scope.openView(view);
}

export function toHour({ hour = 0, min = 0 }) {
  const decimal = (min || 0) / 60;
  return Number((hour + decimal).toFixed(2));
}

export function sortBy(input) {
  function naturalCompare(a, b) {
    return a.localeCompare(b, undefined, {
      numeric: true,
      sensitivity: "base",
    });
  }
  const sorted = input.sort((a, b) => {
    const [aMain, aSub = ""] = a.label.split("|").map((s) => s.trim());
    const [bMain, bSub = ""] = b.label.split("|").map((s) => s.trim());

    const mainCompare = naturalCompare(aMain, bMain);
    if (mainCompare !== 0) return mainCompare;

    return naturalCompare(aSub, bSub);
  });

  return sorted;
}

export const getLatestLineDate = (timesheetLineList) => {
  if (!timesheetLineList?.length) {
    return null;
  }

  const latestDate = timesheetLineList.reduce((maxDate, line) => {
    const match = line.fullName?.match(/\d{4}-\d{2}-\d{2}/);
    if (match) {
      const parsedDate = dayjs(match[0]);

      if (parsedDate.isValid()) {
        return maxDate
          ? parsedDate.isAfter(maxDate)
            ? parsedDate
            : maxDate
          : parsedDate;
      }
    }
    return maxDate;
  }, null);

  return latestDate;
};

export const getTsLines = (lines, row, date) => {
  if (!lines || !lines.length) return [];
  const tslines = lines.filter((line) => {
    return (
      hasSameProject(line, row) &&
      hasSameProjectTask(line, row) &&
      line.date === date
    );
  });
  return tslines;
};

export function formatSigned(value, formatted) {
  if (value > 0) return `+ ${formatted}`;
  if (value < 0) return `- ${formatted.replace(/^-/, "")}`;
  return formatted;
}

export function isDailyLimitExceed(
  info = {},
  duration,
  durationKey,
  planningDurationKey,
  isMinutesPreference
) {
  const current = Number(info[durationKey] || 0);
  const leave = Number(info.leaveDuration || 0) || 1;

  const newTotal = current + duration;

  const limit =
    Number(info[planningDurationKey] || 0) * (isMinutesPreference ? 60 : 1);

  const multiplier = leave > 0 ? leave : 1;
  const expectedTotal = limit * multiplier;

  return newTotal > expectedTotal;
}

export const norm = (v) => String(v ?? "null");

export function hasSameProject(line, row) {
  return (
    norm(line.project?.id) === norm(row?.isProject ? row.id : row.project?.id)
  );
}

export function hasSameProjectTask(line, row) {
  return norm(line.projectTask?.id) === norm(!row.isProject ? row.id : null);
}

export const createTimesheetRow = (projects, projectTasks, tslines) => {
  const { tsProjects, tsProjectTasks, extra } = (tslines || []).reduce(
    (acc, t) => {
      if (t.project) {
        if (t.project && !t.projectTask) {
          acc.tsProjects.push(t.project);
        }
        if (t.projectTask) {
          acc.tsProjectTasks.push({
            ...t.projectTask,
            project: t.project,
            invoicingType: t?.invoicingType || t?.["projectTask.invoicingType"],
          });
        }
      } else {
        acc.extra.push(t);
      }
      return acc;
    },
    { tsProjects: [], tsProjectTasks: [], extra: [] }
  );

  const sections = [
    ...Array.from(
      new Map([...projects, ...tsProjects].map((p) => [p?.id, p])).values()
    ).map((p) => ({
      ...p,
      isProject: true,
      label: p.fullName,
    })),
    ...Array.from(
      new Map(
        [...projectTasks, ...tsProjectTasks].map((t) => [t?.id, t])
      ).values()
    ).map((t) => ({
      ...t,
      isProject: false,
      label: `${t?.project?.fullName || ""}${t?.project ? " | " : ""}${
        t?.fullName || ""
      }`,
      invoicingType: t?.invoicingType || t?.["projectTask.invoicingType"],
    })),
    ...(extra.length
      ? [
          {
            isProject: false,
            label: "Miscellaneous",
          },
        ]
      : []),
  ];
  return sortBy(sections);
};

export function getLoggingPreference({
  isDayPreference = false,
  isMinutesPreference = false,
}) {
  if (isDayPreference) {
    return TIME_LOGGING_PREFERENCES.DAYS;
  }
  if (isMinutesPreference) {
    return TIME_LOGGING_PREFERENCES.MINUTES;
  }
  return TIME_LOGGING_PREFERENCES.HOURS;
}
