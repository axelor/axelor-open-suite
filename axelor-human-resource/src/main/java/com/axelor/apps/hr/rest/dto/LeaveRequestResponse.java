package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.utils.api.ResponseStructure;

public class LeaveRequestResponse extends ResponseStructure {

  private final Long leaveRequestId;

  public LeaveRequestResponse(LeaveRequest leaveRequest) {
    super(leaveRequest.getVersion());
    this.leaveRequestId = leaveRequest.getId();
  }

  public Long getLeaveRequestId() {
    return leaveRequestId;
  }
}
