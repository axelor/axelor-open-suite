package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leave.LeaveRequestPlanningService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaveRequestComputeDayDurationServiceImpl
    implements LeaveRequestComputeDayDurationService {

  protected final LeaveRequestPlanningService leaveRequestPlanningService;
  protected final LeaveRequestComputeHalfDayService leaveRequestComputeHalfDayService;
  protected final WeeklyPlanningService weeklyPlanningService;
  protected final PublicHolidayHrService publicHolidayHrService;

  @Inject
  public LeaveRequestComputeDayDurationServiceImpl(
      LeaveRequestPlanningService leaveRequestPlanningService,
      LeaveRequestComputeHalfDayService leaveRequestComputeHalfDayService,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService publicHolidayHrService) {
    this.leaveRequestPlanningService = leaveRequestPlanningService;
    this.leaveRequestComputeHalfDayService = leaveRequestComputeHalfDayService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
  }

  /**
   * Computes the duration in days of a leave, according to the input planning.
   *
   * @param leave
   * @param employee
   * @param fromDate
   * @param toDate
   * @param startOn
   * @param endOn
   * @return
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDurationInDays(
      LeaveRequest leave,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate,
      int startOn,
      int endOn)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = leaveRequestPlanningService.getWeeklyPlanning(leave, employee);
    EventsPlanning holidayPlanning =
        leaveRequestPlanningService.getPublicHolidayEventsPlanning(leave, employee);

    // If the leave request is only for 1 day
    if (fromDate.isEqual(toDate)) {
      if (startOn == endOn) {
        if (startOn == LeaveRequestRepository.SELECT_MORNING) {
          duration =
              duration.add(
                  BigDecimal.valueOf(
                      weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                          weeklyPlanning, fromDate, true, false)));
        } else {
          duration =
              duration.add(
                  BigDecimal.valueOf(
                      weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                          weeklyPlanning, fromDate, false, true)));
        }
      } else {
        duration =
            duration.add(
                BigDecimal.valueOf(
                    weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                        weeklyPlanning, fromDate, true, true)));
      }

      // Else if it's on several days
    } else {
      duration =
          duration.add(
              BigDecimal.valueOf(
                  leaveRequestComputeHalfDayService.computeStartDateWithSelect(
                      fromDate, startOn, weeklyPlanning)));

      LocalDate itDate = fromDate.plusDays(1);
      while (!itDate.isEqual(toDate) && !itDate.isAfter(toDate)) {
        duration =
            duration.add(
                BigDecimal.valueOf(
                    weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, itDate)));
        itDate = itDate.plusDays(1);
      }

      duration =
          duration.add(
              BigDecimal.valueOf(
                  leaveRequestComputeHalfDayService.computeEndDateWithSelect(
                      toDate, endOn, weeklyPlanning)));
    }

    if (holidayPlanning != null) {
      duration =
          duration.subtract(
              publicHolidayHrService.computePublicHolidayDays(
                  fromDate, toDate, weeklyPlanning, holidayPlanning));
    }

    return duration;
  }
}
