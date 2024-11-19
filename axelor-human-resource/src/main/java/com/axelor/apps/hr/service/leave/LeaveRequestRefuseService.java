package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestRefuseService {

  void refuse(LeaveRequest leaveRequest) throws AxelorException;
}
