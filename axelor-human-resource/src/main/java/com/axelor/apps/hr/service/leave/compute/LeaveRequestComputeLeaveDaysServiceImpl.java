package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaveRequestComputeLeaveDaysServiceImpl
    implements LeaveRequestComputeLeaveDaysService {

  protected final WeeklyPlanningService weeklyPlanningService;
  protected final PublicHolidayHrService publicHolidayHrService;
  protected final LeaveRequestComputeHalfDayService leaveRequestComputeHalfDayService;

  @Inject
  public LeaveRequestComputeLeaveDaysServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService publicHolidayHrService,
      LeaveRequestComputeHalfDayService leaveRequestComputeHalfDayService) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
    this.leaveRequestComputeHalfDayService = leaveRequestComputeHalfDayService;
  }

  @Override
  public BigDecimal computeLeaveDaysByLeaveRequest(
      LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee) {
    BigDecimal leaveDays = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
    LocalDate leaveFrom = leaveRequest.getFromDateT().toLocalDate();
    LocalDate leaveTo = leaveRequest.getToDateT().toLocalDate();

    LocalDate itDate = fromDate;
    if (fromDate.isBefore(leaveFrom) || fromDate.equals(leaveFrom)) {
      itDate = leaveFrom;
    }

    boolean morningHalf = false;
    boolean eveningHalf = false;
    BigDecimal daysToAdd = BigDecimal.ZERO;
    if (leaveTo.equals(leaveFrom)
        && leaveRequest.getStartOnSelect() == leaveRequest.getEndOnSelect()) {
      eveningHalf = leaveRequest.getStartOnSelect() == LeaveRequestRepository.SELECT_AFTERNOON;
      morningHalf = leaveRequest.getStartOnSelect() == LeaveRequestRepository.SELECT_MORNING;
    }

    while (!itDate.isEqual(leaveTo.plusDays(1)) && !itDate.isEqual(toDate.plusDays(1))) {

      if (itDate.equals(leaveFrom) && !morningHalf) {
        daysToAdd =
            BigDecimal.valueOf(
                leaveRequestComputeHalfDayService.computeStartDateWithSelect(
                    itDate, leaveRequest.getStartOnSelect(), weeklyPlanning));
      } else if (itDate.equals(leaveTo) && !eveningHalf) {
        daysToAdd =
            BigDecimal.valueOf(
                leaveRequestComputeHalfDayService.computeEndDateWithSelect(
                    itDate, leaveRequest.getEndOnSelect(), weeklyPlanning));
      } else {
        daysToAdd =
            BigDecimal.valueOf(
                weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, itDate));
      }

      if (!publicHolidayHrService.checkPublicHolidayDay(itDate, employee)) {
        leaveDays = leaveDays.add(daysToAdd);
      }
      itDate = itDate.plusDays(1);
    }

    return leaveDays;
  }
}
