package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestWorkflowService {
  void confirm(LeaveRequest leaveRequest) throws AxelorException;

  void validate(LeaveRequest leaveRequest) throws AxelorException;

  void refuse(LeaveRequest leaveRequest) throws AxelorException;

  void cancel(LeaveRequest leaveRequest) throws AxelorException;
}
