package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.LeaveReason;
import java.util.List;

public interface LeaveReasonService {
  boolean isExceptionalDaysReason(LeaveReason leaveReason);

  List<Integer> getIncrementLeaveReasonTypeSelects();
}
