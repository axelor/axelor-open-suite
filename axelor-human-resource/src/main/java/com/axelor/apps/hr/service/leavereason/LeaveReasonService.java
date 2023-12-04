package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.LeaveReason;

public interface LeaveReasonService {
  boolean isExceptionalDaysReason(LeaveReason leaveReason);
}
