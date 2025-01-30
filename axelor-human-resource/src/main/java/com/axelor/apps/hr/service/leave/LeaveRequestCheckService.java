package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestCheckService {
  void checkCompany(LeaveRequest leaveRequest) throws AxelorException;

  void checkDates(LeaveRequest leaveRequest) throws AxelorException;

  boolean isDatesInvalid(LeaveRequest leaveRequest);

  boolean isDurationInvalid(LeaveRequest leaveRequest);
}
