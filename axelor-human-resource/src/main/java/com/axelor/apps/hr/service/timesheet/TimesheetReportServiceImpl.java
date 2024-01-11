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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
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
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetReminderRepository;
import com.axelor.apps.hr.db.repo.TimesheetReportRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.utils.QueryBuilder;
import com.axelor.utils.date.DateTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;

public class TimesheetReportServiceImpl implements TimesheetReportService {

  protected TimesheetReminderRepository timesheetReminderRepo;
  protected TimesheetReportRepository timesheetReportRepository;
  protected ExtraHoursLineRepository extraHoursLineRepository;
  protected TimesheetLineRepository timesheetLineRepository;
  protected LeaveRequestRepository leaveRequestRepository;

  protected MessageService messageService;
  protected TemplateMessageService templateMessageService;
  protected PublicHolidayService publicHolidayService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected EmployeeService employeeService;
  protected TimesheetLineService timesheetLineService;
  protected LeaveService leaveService;

  @Inject
  public TimesheetReportServiceImpl(
      TimesheetReminderRepository timesheetReminderRepo,
      TimesheetReportRepository timesheetReportRepository,
      ExtraHoursLineRepository extraHoursLineRepository,
      TimesheetLineRepository timesheetLineRepository,
      LeaveRequestRepository leaveRequestRepository,
      MessageService messageService,
      TemplateMessageService templateMessageService,
      PublicHolidayService publicHolidayService,
      WeeklyPlanningService weeklyPlanningService,
      EmployeeService employeeService,
      TimesheetLineService timesheetLineService,
      LeaveService leaveService) {
    this.timesheetReminderRepo = timesheetReminderRepo;
    this.timesheetReportRepository = timesheetReportRepository;
    this.extraHoursLineRepository = extraHoursLineRepository;
    this.timesheetLineRepository = timesheetLineRepository;
    this.leaveRequestRepository = leaveRequestRepository;

    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
    this.publicHolidayService = publicHolidayService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.employeeService = employeeService;
    this.timesheetLineService = timesheetLineService;
    this.leaveService = leaveService;
  }

  @Override
  public Set<Employee> getEmployeeToBeReminded(TimesheetReport timesheetReport) {
    Set<Employee> employeeSet = new HashSet<>();
    BigDecimal worksHour = BigDecimal.ZERO, workedHour = BigDecimal.ZERO;

    List<Employee> employees = getEmployees(timesheetReport);
    LocalDate fromDate = timesheetReport.getFromDate();
    LocalDate toDate = timesheetReport.getToDate();

    for (Employee employee : employees) {
      try {
        worksHour = workedHour = BigDecimal.ZERO;
        BigDecimal publicHolidays =
            publicHolidayService.computePublicHolidayDays(
                fromDate,
                toDate,
                employee.getWeeklyPlanning(),
                employee.getPublicHolidayEventsPlanning());
        worksHour = getTotalWeekWorksHours(employee, fromDate, toDate, publicHolidays);
        workedHour = getTotalWeekWorkedHours(employee, fromDate, toDate, publicHolidays);
        if (worksHour.compareTo(workedHour) != 0) {
          employeeSet.add(employee);
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
    return employeeSet;
  }

  @Transactional(rollbackOn = {Exception.class})
  public List<Message> sendReminders(TimesheetReport timesheetReport) throws AxelorException {

    Template reminderTemplate =
        Beans.get(AppHumanResourceService.class).getAppTimesheet().getTimesheetReminderTemplate();
    if (reminderTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_TIMESHEET_REMINDER_TEMPLATE));
    }
    List<TimesheetReminder> timesheetReminders = getTimesheetReminderList(timesheetReport);
    return sendEmailMessage(timesheetReminders, reminderTemplate);
  }

  private List<TimesheetReminder> getTimesheetReminderList(TimesheetReport timesheetReport) {
    List<TimesheetReminder> timesheetReminders = new ArrayList<>();

    List<Employee> employees = new ArrayList<>(timesheetReport.getReminderEmployeeSet());
    try {
      addTimesheetReminder(timesheetReport, employees, timesheetReminders);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return timesheetReminders;
  }

  protected void addTimesheetReminder(
      TimesheetReport timesheetReport,
      List<Employee> employees,
      List<TimesheetReminder> timesheetReminders)
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

      for (Employee employee : employees) {
        missingHour = BigDecimal.ZERO;
        extraHour = BigDecimal.ZERO;

        BigDecimal publicHolidays =
            publicHolidayService.computePublicHolidayDays(
                fromDate,
                toDate,
                employee.getWeeklyPlanning(),
                employee.getPublicHolidayEventsPlanning());

        worksHour = getTotalWeekWorksHours(employee, fromDate, toDate, publicHolidays);

        workedHour = getTotalWeekWorkedHours(employee, fromDate, toDate, publicHolidays);
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
            timesheetReminders.stream()
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

  protected TimesheetReminderLine createTimesheetReminderLine(
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

  public List<Map<String, Object>> getTimesheetReportList(String timesheetReportId) {

    List<Map<String, Object>> list = new ArrayList<>();
    WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 5);

    TimesheetReport timesheetReport =
        timesheetReportRepository.find(Long.parseLong(timesheetReportId.toString()));
    int numOfDays = timesheetReport.getFromDate().until(timesheetReport.getToDate()).getDays();
    List<LocalDate> daysRange =
        Stream.iterate(timesheetReport.getFromDate(), date -> date.plusDays(1))
            .limit(numOfDays + 1)
            .collect(Collectors.toList());

    List<Employee> employees = getEmployees(timesheetReport);

    for (Employee employee : employees) {
      BigDecimal dailyWorkingHours = employee.getDailyWorkHours();
      WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();

      Integer weekNumber = 1;
      int lastDayIndex = -1;
      int daysInWeek = 0;
      try {
        for (LocalDate date : daysRange) {
          DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning, date);
          if (dayPlanning == null) {
            continue;
          }
          int dayIndex = date.get(weekFields.dayOfWeek()) - 1;
          if (lastDayIndex < dayIndex) {
            lastDayIndex = dayIndex;
            if (weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date) != 0) {
              daysInWeek++;
            }
          } else {
            lastDayIndex = -1;
            daysInWeek = 1;
            weekNumber++;
          }
          BigDecimal weeklyWorkHours =
              daysInWeek <= 5
                  ? employee
                      .getWeeklyWorkHours()
                      .multiply(BigDecimal.valueOf(daysInWeek / 5.00))
                      .setScale(2, RoundingMode.HALF_UP)
                  : employee.getWeeklyWorkHours();
          Map<String, Object> map = getTimesheetMap(employee, date, dailyWorkingHours);
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
      Employee employee, LocalDate date, BigDecimal dailyWorkingHours) throws AxelorException {
    BigDecimal worksHour = BigDecimal.ZERO, workedHour = BigDecimal.ZERO;

    boolean isPublicHoliday =
        publicHolidayService.checkPublicHolidayDay(date, employee.getPublicHolidayEventsPlanning());
    worksHour = getTotalWorksHours(employee, date, isPublicHoliday, dailyWorkingHours);

    try {
      workedHour = getTotalWorkedHours(employee, date, isPublicHoliday, dailyWorkingHours);
    } catch (Exception e) {
      System.out.println(e);
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("userName", employee.getUser().getFullName());
    map.put("date", DateTool.toDate(date));
    map.put("workedHour", workedHour);
    map.put("workingHour", worksHour);
    return map;
  }

  protected BigDecimal getTotalWorksHours(
      Employee employee, LocalDate date, boolean isPublicHoliday, BigDecimal dailyWorkingHours)
      throws AxelorException {
    BigDecimal worksHour =
        employeeService
            .getDaysWorksInPeriod(employee, date, date)
            .multiply(employee.getDailyWorkHours())
            .setScale(2, RoundingMode.HALF_UP);
    if (isPublicHoliday) {
      worksHour = worksHour.add(dailyWorkingHours);
    }
    double extraHours =
        extraHoursLineRepository
            .all()
            .filter(
                "self.employee = ? AND self.date = ? AND (self.extraHours.statusSelect = ? OR self.extraHours.statusSelect = ?)",
                employee,
                date,
                ExtraHoursRepository.STATUS_VALIDATED,
                ExtraHoursRepository.STATUS_CONFIRMED)
            .fetchStream()
            .mapToDouble(ehl -> Double.parseDouble(ehl.getQty().toString()))
            .sum();
    worksHour = worksHour.add(new BigDecimal(extraHours));
    return worksHour.setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getTotalWeekWorksHours(
      Employee employee, LocalDate fromDate, LocalDate toDate, BigDecimal publicHolidays)
      throws AxelorException {
    BigDecimal worksHour =
        employeeService
            .getDaysWorksInPeriod(employee, fromDate, toDate)
            .multiply(employee.getDailyWorkHours())
            .setScale(2, RoundingMode.HALF_UP);
    worksHour = worksHour.add(publicHolidays.multiply(employee.getDailyWorkHours()));
    double extraHours =
        extraHoursLineRepository
            .all()
            .filter(
                "self.employee = ? AND (self.date BETWEEN ? AND ?) AND (self.extraHours.statusSelect = ? OR self.extraHours.statusSelect = ?)",
                employee,
                fromDate,
                toDate,
                ExtraHoursRepository.STATUS_VALIDATED,
                ExtraHoursRepository.STATUS_CONFIRMED)
            .fetchStream()
            .mapToDouble(ehl -> Double.parseDouble(ehl.getQty().toString()))
            .sum();
    worksHour = worksHour.add(new BigDecimal(extraHours));
    return worksHour.setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getTotalWorkedHours(
      Employee employee, LocalDate date, boolean isPublicHoliday, BigDecimal dailyWorkingHours)
      throws AxelorException {
    BigDecimal totalHours = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLineList =
        timesheetLineRepository
            .all()
            .filter(
                "self.employee = ? AND self.date = ? AND (self.timesheet.statusSelect = ? OR self.timesheet.statusSelect = ?)",
                employee,
                date,
                TimesheetRepository.STATUS_CONFIRMED,
                TimesheetRepository.STATUS_VALIDATED)
            .fetch();

    Duration totalDuration = timesheetLineService.computeTotalDuration(timesheetLineList);
    totalHours =
        new BigDecimal(totalDuration.getSeconds())
            .divide(BigDecimal.valueOf(3600))
            .setScale(2, RoundingMode.HALF_UP);

    if (isPublicHoliday) {
      totalHours = totalHours.add(dailyWorkingHours);
    } else {
      totalHours = totalHours.add(getLeaveHours(employee, date, dailyWorkingHours));
    }

    return totalHours.setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getTotalWeekWorkedHours(
      Employee employee, LocalDate fromDate, LocalDate toDate, BigDecimal publicHolidays)
      throws AxelorException {
    BigDecimal totalHours = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLineList =
        timesheetLineRepository
            .all()
            .filter(
                "self.employee = ? AND (self.date BETWEEN ? AND ?) AND (self.timesheet.statusSelect = ? OR self.timesheet.statusSelect = ?)",
                employee,
                fromDate,
                toDate,
                TimesheetRepository.STATUS_VALIDATED,
                TimesheetRepository.STATUS_CONFIRMED)
            .fetch();

    Duration totalDuration = timesheetLineService.computeTotalDuration(timesheetLineList);
    totalHours =
        new BigDecimal(totalDuration.getSeconds())
            .divide(BigDecimal.valueOf(3600))
            .setScale(2, RoundingMode.HALF_UP);
    totalHours = totalHours.add(publicHolidays.multiply(employee.getDailyWorkHours()));
    totalHours =
        totalHours.add(getWeekLeaveHours(employee, fromDate, toDate, employee.getDailyWorkHours()));

    return totalHours.setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getLeaveHours(
      Employee employee, LocalDate date, BigDecimal dailyWorkingHours) throws AxelorException {
    List<LeaveRequest> leavesList = leaveService.getLeaves(employee, date);
    BigDecimal totalLeaveHours = BigDecimal.ZERO;
    for (LeaveRequest leave : leavesList) {
      BigDecimal leaveHours = leaveService.computeDuration(leave, date, date);
      if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
        leaveHours = leaveHours.multiply(dailyWorkingHours);
      }
      totalLeaveHours = totalLeaveHours.add(leaveHours);
    }
    return totalLeaveHours;
  }

  protected BigDecimal getWeekLeaveHours(
      Employee employee, LocalDate fromDate, LocalDate toDate, BigDecimal dailyWorkingHours)
      throws AxelorException {
    BigDecimal leaveHours = BigDecimal.ZERO;
    do {

      boolean isPublicHoliday =
          publicHolidayService.checkPublicHolidayDay(
              fromDate, employee.getPublicHolidayEventsPlanning());
      if (!isPublicHoliday) {
        leaveHours = leaveHours.add(getLeaveHours(employee, fromDate, dailyWorkingHours));
      }
      fromDate = fromDate.plusDays(1);
    } while (fromDate.until(toDate).getDays() > -1);
    return leaveHours;
  }

  protected List<Employee> getEmployees(TimesheetReport timesheetReport) {
    QueryBuilder<Employee> userQuery = QueryBuilder.of(Employee.class);
    userQuery.add(
        "self.user IS NOT NULL AND self.weeklyPlanning IS NOT NULL AND self.publicHolidayEventsPlanning IS NOT NULL AND self.mainEmploymentContract IS NOT NULL");
    if (!CollectionUtils.isEmpty(timesheetReport.getEmployeeSet())) {
      userQuery.add("self IN :employees");
      userQuery.bind("employees", timesheetReport.getEmployeeSet());
    }
    return userQuery.build().fetch();
  }
}
