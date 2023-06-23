package com.axelor.apps.hr.service.leave;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveLineService {
  LeaveLine getLeaveLine(LeaveRequest leaveRequest);

  LeaveLine addLeaveReasonOrCreateIt(Employee employee, LeaveReason leaveReason);

  void updateDaysToValidate(LeaveLine leaveLine);
}
