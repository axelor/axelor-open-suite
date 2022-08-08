package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeComputeDaysLeaveLunchVoucherService extends EmployeeComputeDaysLeaveService {
  protected LeaveService leaveService;

  @Inject
  public EmployeeComputeDaysLeaveLunchVoucherService(
      EmployeeService employeeService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveService leaveService) {
    super(employeeService, leaveRequestRepository);
    this.leaveService = leaveService;
  }

  /**
   * To compute duration for lunch vouchers, we need to count half-day leave as full days. Taking a
   * leave for a half-day will result in the employee not having the right to a lunch voucher for
   * this day.
   */
  @Override
  protected BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    return leaveService.computeDuration(
        leave,
        leave.getFromDateT(),
        leave.getToDateT(),
        LeaveRequestRepository.SELECT_MORNING,
        LeaveRequestRepository.SELECT_AFTERNOON);
  }
}
