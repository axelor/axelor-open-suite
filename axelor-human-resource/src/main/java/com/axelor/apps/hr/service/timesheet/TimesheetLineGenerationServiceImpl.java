package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimesheetLineGenerationServiceImpl implements TimesheetLineGenerationService {

  protected LeaveRequestService leaveRequestService;
  protected PublicHolidayHrService publicHolidayHrService;
  protected TimesheetLineService timesheetLineService;

  @Inject
  public TimesheetLineGenerationServiceImpl(
      LeaveRequestService leaveRequestService,
      PublicHolidayHrService publicHolidayHrService,
      TimesheetLineService timesheetLineService) {
    this.leaveRequestService = leaveRequestService;
    this.publicHolidayHrService = publicHolidayHrService;
    this.timesheetLineService = timesheetLineService;
  }

  @Override
  public Timesheet generateLines(
      Timesheet timesheet,
      LocalDate fromGenerationDate,
      LocalDate toGenerationDate,
      BigDecimal logTime,
      Project project,
      Product product)
      throws AxelorException {

    Employee employee = timesheet.getEmployee();

    if (fromGenerationDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_FROM_DATE));
    }
    if (toGenerationDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TO_DATE));
    }
    if (product == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_PRODUCT));
    }
    if (employee.getUser() == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.NO_USER_FOR_EMPLOYEE),
          employee.getName());
    }

    WeeklyPlanning planning = employee.getWeeklyPlanning();
    if (planning == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING),
          employee.getUser().getName());
    }
    List<DayPlanning> dayPlanningList = planning.getWeekDays();
    Map<Integer, String> correspMap = getCorresMap();

    LocalDate fromDate = fromGenerationDate;
    LocalDate toDate = toGenerationDate;

    if (employee.getPublicHolidayEventsPlanning() == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_PUBLIC_HOLIDAY_EVENTS_PLANNING),
          employee.getUser().getName());
    }

    while (!fromDate.isAfter(toDate)) {
      if (isWorkedDay(fromDate, correspMap, dayPlanningList)
          && !leaveRequestService.isLeaveDay(employee, fromDate)
          && !publicHolidayHrService.checkPublicHolidayDay(fromDate, employee)) {

        TimesheetLine timesheetLine =
            timesheetLineService.createTimesheetLine(
                project,
                product,
                employee,
                fromDate,
                timesheet,
                timesheetLineService.computeHoursDuration(timesheet, logTime, true),
                "");
        timesheetLine.setDuration(logTime);
      }

      fromDate = fromDate.plusDays(1);
    }
    return timesheet;
  }

  public void checkEmptyPeriod(Timesheet timesheet) throws AxelorException {

    Employee employee = timesheet.getEmployee();
    if (employee == null) {
      return;
    }
    if (employee.getPublicHolidayEventsPlanning() == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_PUBLIC_HOLIDAY_EVENTS_PLANNING),
          employee.getName());
    }
    WeeklyPlanning planning = employee.getWeeklyPlanning();
    if (planning == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING),
          employee.getName());
    }
    List<DayPlanning> dayPlanningList = planning.getWeekDays();
    Map<Integer, String> correspMap = getCorresMap();

    List<TimesheetLine> timesheetLines = timesheet.getTimesheetLineList();
    timesheetLines.sort(Comparator.comparing(TimesheetLine::getDate));
    for (int i = 0; i < timesheetLines.size(); i++) {

      if (i + 1 < timesheetLines.size()) {
        LocalDate date1 = timesheetLines.get(i).getDate();
        LocalDate date2 = timesheetLines.get(i + 1).getDate();
        LocalDate missingDay = date1.plusDays(1);

        while (ChronoUnit.DAYS.between(date1, date2) > 1) {

          if (isWorkedDay(missingDay, correspMap, dayPlanningList)
              && !leaveRequestService.isLeaveDay(employee, missingDay)
              && !publicHolidayHrService.checkPublicHolidayDay(missingDay, employee)) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD, "Line for %s is missing.", missingDay);
          }

          date1 = missingDay;
          missingDay = missingDay.plusDays(1);
        }
      }
    }
  }

  protected Map<Integer, String> getCorresMap() {
    Map<Integer, String> correspMap = new HashMap<>();
    correspMap.put(1, "monday");
    correspMap.put(2, "tuesday");
    correspMap.put(3, "wednesday");
    correspMap.put(4, "thursday");
    correspMap.put(5, "friday");
    correspMap.put(6, "saturday");
    correspMap.put(7, "sunday");
    return correspMap;
  }

  protected boolean isWorkedDay(
      LocalDate date, Map<Integer, String> correspMap, List<DayPlanning> dayPlanningList) {
    DayPlanning dayPlanningCurr = new DayPlanning();
    for (DayPlanning dayPlanning : dayPlanningList) {
      if (dayPlanning.getNameSelect().equals(correspMap.get(date.getDayOfWeek().getValue()))) {
        dayPlanningCurr = dayPlanning;
        break;
      }
    }

    return dayPlanningCurr.getMorningFrom() != null
        || dayPlanningCurr.getMorningTo() != null
        || dayPlanningCurr.getAfternoonFrom() != null
        || dayPlanningCurr.getAfternoonTo() != null;
  }
}
