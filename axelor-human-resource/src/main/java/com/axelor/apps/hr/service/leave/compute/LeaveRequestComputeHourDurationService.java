package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LeaveRequestComputeHourDurationService {

  BigDecimal computeDurationInHours(
      LeaveRequest leave, Employee employee, LocalDateTime fromDateT, LocalDateTime toDateT)
      throws AxelorException;
}
