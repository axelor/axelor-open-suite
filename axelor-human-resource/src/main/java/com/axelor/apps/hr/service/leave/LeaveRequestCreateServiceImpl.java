package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDurationService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class LeaveRequestCreateServiceImpl implements LeaveRequestCreateService {

  protected final LeaveRequestInitValueService leaveRequestInitValueService;
  protected final LeaveRequestRepository leaveRequestRepository;
  protected final LeaveRequestComputeDurationService leaveRequestComputeDurationService;

  @Inject
  public LeaveRequestCreateServiceImpl(
      LeaveRequestInitValueService leaveRequestInitValueService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveRequestComputeDurationService leaveRequestComputeDurationService) {
    this.leaveRequestInitValueService = leaveRequestInitValueService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public LeaveRequest createLeaveRequest(
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      int startOnSelect,
      int endOnSelect,
      String comment,
      LeaveReason leaveReason)
      throws AxelorException {
    LeaveRequest leaveRequest = new LeaveRequest();
    leaveRequestInitValueService.initLeaveRequest(leaveRequest);

    leaveRequest.setFromDateT(fromDateTime);
    leaveRequest.setToDateT(toDateTime);
    leaveRequest.setStartOnSelect(startOnSelect);
    leaveRequest.setEndOnSelect(endOnSelect);
    leaveRequest.setLeaveReason(leaveReason);
    leaveRequest.setComments(comment);

    leaveRequest.setDuration(leaveRequestComputeDurationService.computeDuration(leaveRequest));
    return leaveRequestRepository.save(leaveRequest);
  }
}
