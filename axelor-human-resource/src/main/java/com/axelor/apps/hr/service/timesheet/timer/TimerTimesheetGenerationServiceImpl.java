package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.timesheet.TimesheetCreateService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class TimerTimesheetGenerationServiceImpl implements TimerTimesheetGenerationService {
  protected TimesheetCreateService timesheetCreateService;
  protected TimesheetTimerService timesheetTimerService;
  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimerTimesheetGenerationServiceImpl(
      TimesheetCreateService timesheetCreateService,
      TimesheetTimerService timesheetTimerService,
      TimesheetRepository timesheetRepository) {
    this.timesheetCreateService = timesheetCreateService;
    this.timesheetTimerService = timesheetTimerService;
    this.timesheetRepository = timesheetRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public Timesheet addTimersToTimesheet(List<TSTimer> timerList, Timesheet timesheet)
      throws AxelorException {
    if (timesheet == null || CollectionUtils.isEmpty(timerList)) {
      return timesheet;
    }
    int statusSelect = timesheet.getStatusSelect();
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(TimesheetRepository.STATUS_DRAFT);
    authorizedStatus.add(TimesheetRepository.STATUS_CONFIRMED);
    if (!authorizedStatus.contains(statusSelect)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_ADD_TIMER_WRONG_STATUS));
    }

    timerList =
        timerList.stream()
            .filter(timer -> timer.getTimesheetLine() == null)
            .collect(Collectors.toList());
    for (TSTimer timer : timerList) {
      timesheetTimerService.generateTimesheetLine(timer, timesheet);
    }
    return timesheet;
  }
}
