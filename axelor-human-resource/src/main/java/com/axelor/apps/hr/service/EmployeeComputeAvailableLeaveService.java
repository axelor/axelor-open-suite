package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;
import java.math.BigDecimal;

public interface EmployeeComputeAvailableLeaveService {
  BigDecimal computeAvailableLeaveQuantityForActiveUser(Employee employee, LeaveReason leaveReason);
}
