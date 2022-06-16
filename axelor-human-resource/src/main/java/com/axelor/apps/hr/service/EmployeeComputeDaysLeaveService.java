package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public abstract class EmployeeComputeDaysLeaveService {

  protected EmployeeService employeeService;

  public EmployeeComputeDaysLeaveService(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  public BigDecimal getDaysWorkedInPeriod(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    return employeeService
        .getDaysWorksInPeriod(employee, fromDate, toDate)
        .subtract(computeDaysLeave(employee, fromDate, toDate));
  }

  protected BigDecimal computeDaysLeave(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    List<LeaveRequest> leaveRequestList = getEmployeeDaysLeave(employee, fromDate, toDate);

    BigDecimal daysLeave = BigDecimal.ZERO;
    for (LeaveRequest leaveRequest : leaveRequestList) {
      daysLeave = daysLeave.add(this.computeDuration(leaveRequest, fromDate, toDate));
    }
    return daysLeave;
  }

  protected List<LeaveRequest> getEmployeeDaysLeave(
      Employee employee, LocalDate fromDate, LocalDate toDate) {
    return Beans.get(LeaveRequestRepository.class)
        .all()
        .filter(
            "self.user = ?1 AND self.duration > 0"
                + " AND self.statusSelect = ?2"
                + " AND (self.fromDateT BETWEEN ?3 AND ?4 OR self.toDateT BETWEEN ?3 AND ?4 OR ?3 BETWEEN self.fromDateT"
                + " AND self.toDateT OR ?4 BETWEEN self.fromDateT AND self.toDateT)",
            employee.getUser(),
            LeaveRequestRepository.STATUS_VALIDATED,
            fromDate,
            toDate)
        .fetch();
  }

  protected abstract BigDecimal computeDuration(
      LeaveRequest leaveRequest, LocalDate fromDate, LocalDate toDate) throws AxelorException;
}
