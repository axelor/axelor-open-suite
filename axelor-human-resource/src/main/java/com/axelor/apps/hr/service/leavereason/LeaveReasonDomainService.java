package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;

public interface LeaveReasonDomainService {
  String getLeaveReasonDomain(LeaveReason leaveReason, Employee employee);
}
