package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.rest.dto.LeaveRequestCreateResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestReasonRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestResponse;
import com.axelor.apps.hr.service.leave.LeaveRequestCreateHelperService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeaveRequestCreateRestServiceImpl implements LeaveRequestCreateRestService {

  protected final LeaveRequestCreateHelperService leaveRequestCreateHelperService;
  protected final LeaveRequestRepository leaveRequestRepository;

  @Inject
  public LeaveRequestCreateRestServiceImpl(
      LeaveRequestCreateHelperService leaveRequestCreateHelperService,
      LeaveRequestRepository leaveRequestRepository) {
    this.leaveRequestCreateHelperService = leaveRequestCreateHelperService;
    this.leaveRequestRepository = leaveRequestRepository;
  }

  @Override
  public List<Long> createLeaveRequests(
      LocalDate fromDate, int startOnSelect, List<LeaveRequestReasonRequest> leaveRequestReasonList)
      throws AxelorException {
    List<HashMap<String, Object>> leaveReasonRequestMap = new ArrayList<>();
    for (LeaveRequestReasonRequest leaveReason : leaveRequestReasonList) {
      HashMap<String, Object> leaveReasonMap = new HashMap<>();
      leaveReasonMap.put("duration", leaveReason.getDuration());
      leaveReasonMap.put("comment", leaveReason.getComment());

      HashMap<String, Object> leaveReasonIdMap = new HashMap<>();
      leaveReasonIdMap.put("id", leaveReason.fetchLeaveReason().getId());
      leaveReasonMap.put("leaveReason", leaveReasonIdMap);

      leaveReasonRequestMap.add(leaveReasonMap);
    }

    return leaveRequestCreateHelperService.createLeaveRequests(
        fromDate, startOnSelect, leaveReasonRequestMap);
  }

  @Override
  public LeaveRequestCreateResponse createLeaveRequestResponse(List<Long> leaveRequestIdList) {
    List<LeaveRequestResponse> leaveRequestResponseList = new ArrayList<>();
    for (Long leaveRequestId : leaveRequestIdList) {
      leaveRequestResponseList.add(
          new LeaveRequestResponse(leaveRequestRepository.find(leaveRequestId)));
    }
    return new LeaveRequestCreateResponse(leaveRequestResponseList);
  }
}
