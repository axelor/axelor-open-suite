package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestSendService {

  String send(LeaveRequest leaveRequest) throws AxelorException;
}
