package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestComputeLeaveHoursService {

  BigDecimal computeTotalLeaveHours(
      LocalDate date, BigDecimal dayValueInHours, List<LeaveRequest> leaveList)
      throws AxelorException;
}
