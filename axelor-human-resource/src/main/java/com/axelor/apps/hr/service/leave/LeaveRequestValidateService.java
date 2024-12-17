package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestValidateService {
  void validate(LeaveRequest leaveRequest) throws AxelorException;
}
