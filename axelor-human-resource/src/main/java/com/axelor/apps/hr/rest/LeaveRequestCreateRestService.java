package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.rest.dto.LeaveRequestCreateResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestReasonRequest;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestCreateRestService {

  List<Long> createLeaveRequests(
      LocalDate fromDate, int startOnSelect, List<LeaveRequestReasonRequest> leaveRequestReasonList)
      throws AxelorException;

  LeaveRequestCreateResponse createLeaveRequestResponse(List<Long> leaveRequestIdList);
}
