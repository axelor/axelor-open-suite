package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.base.rest.dto.CheckResponseLine;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class TimesheetCheckResponseServiceImpl implements TimesheetCheckResponseService {
  protected TimesheetLineService timesheetLineService;
  protected DateService dateService;

  @Inject
  public TimesheetCheckResponseServiceImpl(
      TimesheetLineService timesheetLineService, DateService dateService) {
    this.timesheetLineService = timesheetLineService;
    this.dateService = dateService;
  }

  @Override
  public CheckResponse createResponse(Timesheet timesheet) throws AxelorException {
    List<CheckResponseLine> checkResponseLineList = new ArrayList<>();
    checkResponseLineList.add(checkFromDate(timesheet));
    checkResponseLineList.add(checkToDate(timesheet));
    checkDailyLimit(timesheet, checkResponseLineList);
    List<CheckResponseLine> filteredList =
        checkResponseLineList.stream().filter(Objects::nonNull).collect(Collectors.toList());

    return new CheckResponse(timesheet, filteredList);
  }

  protected void checkDailyLimit(Timesheet timesheet, List<CheckResponseLine> checkResponseLineList)
      throws AxelorException {
    Integer dailyLimit = timesheetLineService.getDailyLimitFromApp();
    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();

    if (dailyLimit == 0 || CollectionUtils.isEmpty(timesheetLineList)) {
      return;
    }

    List<LocalDate> localDateList =
        timesheet.getTimesheetLineList().stream()
            .map(TimesheetLine::getDate)
            .distinct()
            .collect(Collectors.toList());

    for (LocalDate date : localDateList) {
      checkDailyLimit(timesheet, checkResponseLineList, date, dailyLimit);
    }
  }

  protected void checkDailyLimit(
      Timesheet timesheet,
      List<CheckResponseLine> checkResponseLineList,
      LocalDate date,
      Integer dailyLimit)
      throws AxelorException {
    BigDecimal totalDuration = getTotalDuration(timesheet, date);
    if (timesheetLineService.isExceedingDailyLimit(totalDuration, BigDecimal.ZERO, dailyLimit)) {
      checkResponseLineList.add(handleExceedingDailyLimit(dailyLimit, date, timesheet));
    }
  }

  protected BigDecimal getTotalDuration(Timesheet timesheet, LocalDate date) {
    return timesheet.getTimesheetLineList().stream()
        .filter(line -> line.getDate().isEqual(date))
        .map(TimesheetLine::getHoursDuration)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  protected CheckResponseLine handleExceedingDailyLimit(
      Integer dailyLimit, LocalDate date, Timesheet timesheet) throws AxelorException {
    return new CheckResponseLine(
        timesheet,
        String.format(
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINES_EXCEED_DAILY_LIMIT),
            dailyLimit,
            date.format(dateService.getDateFormat())),
        CheckResponseLine.CHECK_TYPE_ERROR);
  }

  protected CheckResponseLine checkFromDate(Timesheet timesheet) {
    if (timesheet.getFromDate() == null) {
      return new CheckResponseLine(
          timesheet,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_NULL_FROM_DATE),
          CheckResponseLine.CHECK_TYPE_ALERT);
    }
    return null;
  }

  protected CheckResponseLine checkToDate(Timesheet timesheet) {
    if (timesheet.getToDate() == null) {
      return new CheckResponseLine(
          timesheet,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_NULL_TO_DATE),
          CheckResponseLine.CHECK_TYPE_ALERT);
    }
    return null;
  }
}
