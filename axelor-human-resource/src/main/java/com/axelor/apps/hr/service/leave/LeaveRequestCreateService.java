package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import java.time.LocalDateTime;

public interface LeaveRequestCreateService {
  LeaveRequest createLeaveRequest(
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      int startOnSelect,
      int endOnSelect,
      String comment,
      LeaveReason leaveReason)
      throws AxelorException;
}
