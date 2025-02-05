package com.axelor.apps.hr.rest.dto;

import java.util.List;

public class LeaveRequestCreateResponse {
  private List<LeaveRequestResponse> leaveRequestResponseList;

  public LeaveRequestCreateResponse(List<LeaveRequestResponse> leaveRequestResponseList) {
    this.leaveRequestResponseList = leaveRequestResponseList;
  }

  public List<LeaveRequestResponse> getLeaveRequestResponseList() {
    return leaveRequestResponseList;
  }
}
