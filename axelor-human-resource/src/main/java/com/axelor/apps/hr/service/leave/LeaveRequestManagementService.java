package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestManagementService {

  void manageSentLeaves(LeaveRequest leave) throws AxelorException;

  void manageValidateLeaves(LeaveRequest leave) throws AxelorException;

  void manageRefuseLeaves(LeaveRequest leave) throws AxelorException;

  void manageCancelLeaves(LeaveRequest leave) throws AxelorException;
}
