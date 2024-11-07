package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface LeaveRequestComputeDayDurationService {

  BigDecimal computeDurationInDays(
      LeaveRequest leave,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate,
      int startOn,
      int endOn)
      throws AxelorException;
}
