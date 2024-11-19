package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface LeaveRequestComputeLeaveDaysService {

  BigDecimal computeLeaveDaysByLeaveRequest(
      LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee)
      throws AxelorException;
}
