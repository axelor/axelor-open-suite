package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestCheckResponseService {
  CheckResponse createResponse(LeaveRequest leaveRequest);
}
