/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.AppTimesheetRepository;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.TimesheetReminder;
import com.axelor.apps.hr.db.TimesheetReminderLine;
import com.axelor.apps.hr.db.TimesheetReport;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.ExtraHoursRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetReminderRepository;
import com.axelor.apps.hr.db.repo.TimesheetReportRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.tool.QueryBuilder;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;

public class TimesheetReportServiceImpl implements TimesheetReportService {

  @Inject private TimesheetReminderRepository timesheetReminderRepo;

  @Inject private MessageService messageService;
  @Inject private TemplateMessageService templateMessageService;

  private static final DateTimeFormatter dtFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

  public List<User> getUserToBeReminded(TimesheetReport timesheetReport) {
    List<User> userList = new ArrayList<>();
    BigDecimal worksHour = BigDecimal.ZERO, workedHour = BigDecimal.ZERO;

    List<User> users = getUsers(timesheetReport);

    for (User user : users) {
      Employee employee = user.getEmployee();
      LocalDate fromDate = timesheetReport.getFromDate();
      LocalDate toDate = timesheetReport.getToDate();
      try {
        worksHour = workedHour = BigDecimal.ZERO;
        BigDecimal publicHolidays =
            Beans.get(PublicHolidayService.class)
                .computePublicHolidayDays(
                    timesheetReport.getFromDate(),
                    timesheetReport.getToDate(),
                    employee.getWeeklyPlanning(),
                    employee.getPublicHolidayEventsPlanning());
        worksHour = getTotalWeekWorksHours(user, fromDate, toDate, publicHolidays);
        workedHour = getTotalWeekWorkedHours(user, fromDate, toDate, publicHolidays);
        if (worksHour.compareTo(workedHour) != 0) {
          if (!userList.contains(user)) {
            userList.add(user);
          }
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
    return userList;
  }

  @Transactional
  public List<Message> sendReminders(TimesheetReport timesheetReport) throws AxelorException {
    Template reminderTemplate =
        Beans.get(AppTimesheetRepository.class).all().fetchOne().getTimesheetReminderTemplate();
    if (reminderTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.EMPLOYEE_TIMESHEET_REMINDER_TEMPLATE));
    }
    List<TimesheetReminder> timesheetReminders = getTimesheetReminderList(timesheetReport);
    return sendEmailMessage(timesheetReminders, reminderTemplate);
  }

  private List<TimesheetReminder> getTimesheetReminderList(TimesheetReport timesheetReport) {
    List<TimesheetReminder> timesheetReminders = new ArrayList<>();

    List<User> users = getUsers(timesheetReport);
    try {
      addTimesheetReminder(timesheetReport, users, timesheetReminders);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return timesheetReminders;
  }

  private void addTimesheetReminder(
      TimesheetReport timesheetReport, List<User> users, List<TimesheetReminder> timesheetReminders)
      throws AxelorException {
    BigDecimal worksHour = BigDecimal.ZERO,
        workedHour = BigDecimal.ZERO,
        missingHour = BigDecimal.ZERO,
        extraHour = BigDecimal.ZERO;
    LocalDate fromDate = timesheetReport.getFromDate();
    LocalDate toDate = null;
    do {
      toDate = fromDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      if (toDate.until(timesheetReport.getToDate()).getDays() < 0) {
        toDate = timesheetReport.getToDate();
      }

      for (User user : users) {
        Employee employee = user.getEmployee();
        missingHour = BigDecimal.ZERO;
        extraHour = BigDecimal.ZERO;

        BigDecimal publicHolidays =
            Beans.get(PublicHolidayService.class)
                .computePublicHolidayDays(
                    fromDate,
                    toDate,
                    employee.getWeeklyPlanning(),
                    employee.getPublicHolidayEventsPlanning());

        worksHour = getTotalWeekWorksHours(user, fromDate, toDate, publicHolidays);

        workedHour = getTotalWeekWorkedHours(user, fromDate, toDate, publicHolidays);
        if (worksHour.compareTo(workedHour) == 1) {
          missingHour = worksHour.subtract(workedHour);
        } else if (worksHour.compareTo(workedHour) == -1) {
          extraHour = workedHour.subtract(worksHour);
        }

        if (missingHour.compareTo(BigDecimal.ZERO) == 0
            && extraHour.compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        Optional<TimesheetReminder> optReminder =
            timesheetReminders
                .stream()
                .filter(reminder -> reminder.getEmployee().getId().compareTo(employee.getId()) == 0)
                .findFirst();

        TimesheetReminder timesheetReminder = null;
        if (optReminder.isPresent()) {
          timesheetReminder = optReminder.get();
          timesheetReminder.addTimesheetReminderLineListItem(
              createTimesheetReminderLine(fromDate, toDate, worksHour, missingHour, extraHour));
        } else {
          List<TimesheetReminderLine> timesheetReminderLines = new ArrayList<>();
          timesheetReminder = new TimesheetReminder();
          timesheetReminder.setEmployee(employee);
          timesheetReminder.setTimesheetReminderLineList(timesheetReminderLines);
          timesheetReminder.addTimesheetReminderLineListItem(
              createTimesheetReminderLine(fromDate, toDate, worksHour, missingHour, extraHour));
          timesheetReminders.add(timesheetReminder);
        }
        timesheetReminderRepo.save(timesheetReminder);
      }
      fromDate = fromDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    } while (toDate.until(timesheetReport.getToDate()).getDays() > 0);
  }

  private List<Message> sendEmailMessage(
      List<TimesheetReminder> timesheetReminders, Template reminderTemplate) {

    List<Message> messages = new ArrayList<>();
    for (TimesheetReminder timesheetReminder : timesheetReminders) {
      try {
        Message message =
            templateMessageService.generateMessage(timesheetReminder, reminderTemplate);
        message = messageService.sendMessage(message);
        timesheetReminder.setEmailSentDateT(LocalDateTime.now());
        messages.add(message);
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }

    return messages;
  }

  private TimesheetReminderLine createTimesheetReminderLine(
      LocalDate fromDate,
      LocalDate toDate,
      BigDecimal worksHour,
      BigDecimal missingHour,
      BigDecimal extraHour) {
    TimesheetReminderLine line = new TimesheetReminderLine();
    line.setFromDate(fromDate);
    line.setToDate(toDate);
    line.setRequiredHours(worksHour);
    line.setExtraHours(extraHour);
    line.setMissingHours(missingHour);
    line.setWorkHour(worksHour.subtract(missingHour).add(extraHour));
    return line;
  }

  public List<Map<String, Object>> getTimesheetReportList(String TimesheetReportId) {

    List<Map<String, Object>> list = new ArrayList<>();
    WeekFields weekFields = WeekFields.of(Locale.getDefault());

    TimesheetReport timesheetReport =
        Beans.get(TimesheetReportRepository.class)
            .find(Long.parseLong(TimesheetReportId.toString()));
    int numOfDays = timesheetReport.getFromDate().until(timesheetReport.getToDate()).getDays();
    List<LocalDate> daysRange =
        Stream.iterate(timesheetReport.getFromDate(), date -> date.plusDays(1))
            .limit(numOfDays + 1)
            .collect(Collectors.toList());

    List<User> users = getUsers(timesheetReport);

    for (User user : users) {
      Employee employee = user.getEmployee();
      BigDecimal dailyWorkingHours = employee.getDailyWorkHours();
      WeeklyPlanning planning = employee.getWeeklyPlanning();
      if (planning == null) {
        continue;
      }
      Integer weekNumber = 1;
      int lastDayNumber = -1;
      try {
        for (LocalDate date : daysRange) {
          DayPlanning dayPlanning =
              Beans.get(WeeklyPlanningService.class).findDayPlanning(planning, date);
          if (dayPlanning == null) {
            continue;
          }
          int dayIndex = planning.getWeekDays().indexOf(dayPlanning);
          if (lastDayNumber < dayIndex) {
            lastDayNumber = dayIndex;
          } else {
            lastDayNumber = -1;
            weekNumber++;
          }
          BigDecimal weeklyWorkHours = employee.getWeeklyWorkHours().multiply(BigDecimal.valueOf((dayIndex)/6.0)).setScale(2,RoundingMode.HALF_EVEN);
          Map<String, Object> map = getTimesheetMap(user, date, dailyWorkingHours, weekFields);
          map.put("weeklyWorkHours", weeklyWorkHours);
          map.put("weekNumber", weekNumber.toString());
          list.add(map);
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    }

    return list;
  }

  private Map<String, Object> getTimesheetMap(
      User user, LocalDate date, BigDecimal dailyWorkingHours, WeekFields weekFields)
      throws AxelorException {
    Employee employee = user.getEmployee();
    BigDecimal worksHour = BigDecimal.ZERO, workedHour = BigDecimal.ZERO;

    boolean isPublicHoliday =
        Beans.get(PublicHolidayService.class)
            .checkPublicHolidayDay(date, employee.getPublicHolidayEventsPlanning());
    worksHour = getTotalWorksHours(user, date, isPublicHoliday, dailyWorkingHours);

    try {
      workedHour = getTotalWorkedHours(user, date, isPublicHoliday, dailyWorkingHours);
    } catch (Exception e) {
      System.out.println(e);
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("userName", user.getFullName());
    map.put("date", date.format(dtFormat));
    map.put("workedHour", workedHour);
    map.put("workingHour", worksHour);
    return map;
  }

  private BigDecimal getTotalWorksHours(
      User user, LocalDate date, boolean isPublicHoliday, BigDecimal dailyWorkingHours)
      throws AxelorException {
    Employee employee = user.getEmployee();
    BigDecimal worksHour =
        Beans.get(EmployeeService.class)
            .getDaysWorksInPeriod(employee, date, date)
            .multiply(employee.getDailyWorkHours());
    if (isPublicHoliday) {
      worksHour = worksHour.add(dailyWorkingHours);
    }
    double extraHours =
        Beans.get(ExtraHoursLineRepository.class)
            .all()
            .filter(
                "self.user = ? AND self.date = ? AND (self.extraHours.statusSelect = ? OR self.extraHours.statusSelect = ?)",
                user,
                date,
                ExtraHoursRepository.STATUS_VALIDATED,
                ExtraHoursRepository.STATUS_CONFIRMED)
            .fetchStream()
            .mapToDouble(ehl -> Double.parseDouble(ehl.getQty().toString()))
            .sum();
    worksHour = worksHour.add(new BigDecimal(extraHours));
    return worksHour;
  }

  private BigDecimal getTotalWeekWorksHours(
      User user, LocalDate fromDate, LocalDate toDate, BigDecimal publicHolidays)
      throws AxelorException {
    Employee employee = user.getEmployee();
    BigDecimal worksHour =
        Beans.get(EmployeeService.class)
            .getDaysWorksInPeriod(employee, fromDate, toDate)
            .multiply(employee.getDailyWorkHours());
    worksHour = worksHour.add(publicHolidays.multiply(employee.getDailyWorkHours()));
    double extraHours =
        Beans.get(ExtraHoursLineRepository.class)
            .all()
            .filter(
                "self.user = ? AND (self.date BETWEEN ? AND ?) AND (self.extraHours.statusSelect = ? OR self.extraHours.statusSelect = ?)",
                user,
                fromDate,
                toDate,
                ExtraHoursRepository.STATUS_VALIDATED,
                ExtraHoursRepository.STATUS_CONFIRMED)
            .fetchStream()
            .mapToDouble(ehl -> Double.parseDouble(ehl.getQty().toString()))
            .sum();
    worksHour = worksHour.add(new BigDecimal(extraHours));
    return worksHour;
  }

  private BigDecimal getTotalWorkedHours(
      User user, LocalDate date, boolean isPublicHoliday, BigDecimal dailyWorkingHours)
      throws AxelorException {
    BigDecimal totalHours = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLineList =
        Beans.get(TimesheetLineRepository.class)
            .all()
            .filter(
                "self.user = ? AND self.date = ? AND (self.timesheet.statusSelect = ? OR self.timesheet.statusSelect = ?)",
                user,
                date,
                TimesheetRepository.STATUS_CONFIRMED,
                TimesheetRepository.STATUS_VALIDATED)
            .fetch();

    Duration totalDuration =
        Beans.get(TimesheetLineService.class).computeTotalDuration(timesheetLineList);
    totalHours = new BigDecimal(totalDuration.getSeconds()).divide(BigDecimal.valueOf(3600));

    if (isPublicHoliday) {
      totalHours = totalHours.add(dailyWorkingHours);
    } else {
      BigDecimal leaveHours = getLeaveHours(user, date, dailyWorkingHours);
      if (leaveHours.compareTo(BigDecimal.ZERO) != 0) {
        totalHours = leaveHours;
      }
    }
    return totalHours;
  }

  private BigDecimal getTotalWeekWorkedHours(
      User user, LocalDate fromDate, LocalDate toDate, BigDecimal publicHolidays)
      throws AxelorException {
    BigDecimal totalHours = BigDecimal.ZERO;
    Employee employee = user.getEmployee();

    List<TimesheetLine> timesheetLineList =
        Beans.get(TimesheetLineRepository.class)
            .all()
            .filter(
                "self.user = ? AND (self.date BETWEEN ? AND ?) AND (self.timesheet.statusSelect = ? OR self.timesheet.statusSelect = ?)",
                user,
                fromDate,
                toDate,
                TimesheetRepository.STATUS_VALIDATED,
                TimesheetRepository.STATUS_CONFIRMED)
            .fetch();

    Duration totalDuration =
        Beans.get(TimesheetLineService.class).computeTotalDuration(timesheetLineList);
    totalHours = new BigDecimal(totalDuration.toHours());
    totalHours = totalHours.add(publicHolidays.multiply(employee.getDailyWorkHours()));
    totalHours =
        totalHours.add(getWeekLeaveHours(user, fromDate, toDate, employee.getDailyWorkHours()));

    return totalHours;
  }

  private BigDecimal getLeaveHours(User user, LocalDate date, BigDecimal dailyWorkingHours)
      throws AxelorException {
    LeaveRequest leave = Beans.get(LeaveService.class).getLeave(user, date);
    if (leave != null) {
      return Beans.get(LeaveService.class)
          .computeDuration(leave, date, date)
          .multiply(dailyWorkingHours);
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal getWeekLeaveHours(
      User user, LocalDate fromDate, LocalDate toDate, BigDecimal dailyWorkingHours)
      throws AxelorException {
    BigDecimal leaveHours = BigDecimal.ZERO;
    do {
      LeaveRequest leave = Beans.get(LeaveService.class).getLeave(user, fromDate);
      if (leave != null) {

        boolean isPublicHoliday =
            Beans.get(PublicHolidayService.class)
                .checkPublicHolidayDay(
                    fromDate, user.getEmployee().getPublicHolidayEventsPlanning());
        if (!isPublicHoliday) {
          leaveHours =
              leaveHours.add(
                  Beans.get(LeaveService.class)
                      .computeDuration(leave, fromDate, fromDate)
                      .multiply(dailyWorkingHours));
        }
      }
      fromDate = fromDate.plusDays(1);
    } while (fromDate.until(toDate).getDays() > -1);
    return leaveHours;
  }

  protected List<User> getUsers(TimesheetReport timesheetReport) {
    QueryBuilder<User> userQuery = QueryBuilder.of(User.class);
    userQuery.add(
        "self.employee IS NOT NULL AND self.employee.weeklyPlanning IS NOT NULL AND self.employee.publicHolidayEventsPlanning IS NOT NULL AND self.employee.mainEmploymentContract IS NOT NULL");
    if (!CollectionUtils.isEmpty(timesheetReport.getUserSet())) {
      userQuery.add("self IN :users");
      userQuery.bind("users", timesheetReport.getUserSet());
    }
    return userQuery.build().fetch();
  }
}
