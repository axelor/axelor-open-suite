package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeComputeDaysLeaveBonusService extends EmployeeComputeDaysLeaveService {
  protected LeaveService leaveService;

  @Inject
  public EmployeeComputeDaysLeaveBonusService(
      EmployeeService employeeService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveService leaveService) {
    super(employeeService, leaveRequestRepository);
    this.leaveService = leaveService;
  }

  @Override
  protected BigDecimal computeDuration(
      LeaveRequest leaveRequest, LocalDate fromDate, LocalDate toDate) throws AxelorException {
    return leaveService.computeDuration(leaveRequest, fromDate, toDate);
  }
}
