/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LeaveRequestEventServiceImpl implements LeaveRequestEventService {
  protected WeeklyPlanningService weeklyPlanningService;
  protected ICalendarService iCalendarService;
  protected ICalendarEventRepository iCalendarEventRepository;

  @Inject
  public LeaveRequestEventServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      ICalendarService iCalendarService,
      ICalendarEventRepository iCalendarEventRepository) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.iCalendarService = iCalendarService;
    this.iCalendarEventRepository = iCalendarEventRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LeaveRequest createEvents(LeaveRequest leave) throws AxelorException {
    User user = leave.getEmployee().getUser();
    if (user == null) {
      return null;
    }

    Employee employee = leave.getEmployee();
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();

    if (weeklyPlanning == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }

    LocalDateTime fromDateTime;
    LocalDateTime toDateTime;
    if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
      fromDateTime = getDefaultStart(weeklyPlanning, leave);
      toDateTime = getDefaultEnd(weeklyPlanning, leave);
    } else {
      fromDateTime = leave.getFromDateT();
      toDateTime = leave.getToDateT();
    }

    ICalendarEvent event =
        iCalendarService.createEvent(
            fromDateTime,
            toDateTime,
            user,
            leave.getComments(),
            4,
            leave.getLeaveReason().getName() + " " + leave.getEmployee().getName());
    iCalendarEventRepository.save(event);
    leave.setIcalendarEvent(event);

    return leave;
  }

  protected LocalDateTime getDefaultStart(WeeklyPlanning weeklyPlanning, LeaveRequest leave) {
    int startTimeHour = 0;
    int startTimeMin = 0;

    DayPlanning startDay =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, leave.getFromDateT().toLocalDate());

    if (leave.getStartOnSelect() == LeaveRequestRepository.SELECT_MORNING) {
      if (startDay != null && startDay.getMorningFrom() != null) {
        startTimeHour = startDay.getMorningFrom().getHour();
        startTimeMin = startDay.getMorningFrom().getMinute();
      } else {
        startTimeHour = 8;
        startTimeMin = 0;
      }
    } else {
      if (startDay != null && startDay.getAfternoonFrom() != null) {
        startTimeHour = startDay.getAfternoonFrom().getHour();
        startTimeMin = startDay.getAfternoonFrom().getMinute();
      } else {
        startTimeHour = 14;
        startTimeMin = 0;
      }
    }
    return LocalDateTime.of(
        leave.getFromDateT().toLocalDate(), LocalTime.of(startTimeHour, startTimeMin));
  }

  protected LocalDateTime getDefaultEnd(WeeklyPlanning weeklyPlanning, LeaveRequest leave) {
    int endTimeHour = 0;
    int endTimeMin = 0;

    DayPlanning endDay =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, leave.getToDateT().toLocalDate());

    if (leave.getEndOnSelect() == LeaveRequestRepository.SELECT_MORNING) {
      if (endDay != null && endDay.getMorningTo() != null) {
        endTimeHour = endDay.getMorningTo().getHour();
        endTimeMin = endDay.getMorningTo().getMinute();
      } else {
        endTimeHour = 12;
        endTimeMin = 0;
      }
    } else {
      if (endDay != null && endDay.getAfternoonTo() != null) {
        endTimeHour = endDay.getAfternoonTo().getHour();
        endTimeMin = endDay.getAfternoonTo().getMinute();
      } else {
        endTimeHour = 18;
        endTimeMin = 0;
      }
    }

    return LocalDateTime.of(
        leave.getToDateT().toLocalDate(), LocalTime.of(endTimeHour, endTimeMin));
  }
}
