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
