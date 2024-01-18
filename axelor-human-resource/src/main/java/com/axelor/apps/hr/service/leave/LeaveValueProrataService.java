package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface LeaveValueProrataService {
  BigDecimal getProratedValue(
      BigDecimal value,
      LeaveReason leaveReason,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate)
      throws AxelorException;
}
