package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;

public interface IncrementLeaveService {
  void updateEmployeeLeaveLines(LeaveReason leaveReason, Employee employee) throws AxelorException;
}
