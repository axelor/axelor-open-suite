package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.auth.db.User;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestService {
  void confirm(LeaveRequest leaveRequest) throws AxelorException;

  void validate(LeaveRequest leaveRequest) throws AxelorException;

  void refuse(LeaveRequest leaveRequest) throws AxelorException;

  void cancel(LeaveRequest leaveRequest) throws AxelorException;

  List<LeaveRequest> getLeaves(Employee employee, LocalDate date);

  boolean willHaveEnoughDays(LeaveRequest leaveRequest);

  String getLeaveCalendarDomain(User user);

  boolean isLeaveDay(Employee employee, LocalDate date);
}
