package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LeaveRequestComputeDurationService {
  BigDecimal computeDuration(LeaveRequest leave) throws AxelorException;

  BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  BigDecimal computeDuration(
      LeaveRequest leave, LocalDateTime from, LocalDateTime to, int startOn, int endOn)
      throws AxelorException;

  double computeStartDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning);

  double computeEndDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning);

  BigDecimal computeLeaveDaysByLeaveRequest(
      LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee)
      throws AxelorException;
}
