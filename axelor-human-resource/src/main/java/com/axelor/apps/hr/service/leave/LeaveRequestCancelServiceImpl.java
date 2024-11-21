package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leavereason.LeaveReasonService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveRequestCancelServiceImpl implements LeaveRequestCancelService {

  protected LeaveRequestManagementService leaveRequestManagementService;
  protected LeaveLineService leaveLineService;
  protected ICalendarEventRepository iCalendarEventRepository;
  protected LeaveReasonService leaveReasonService;
  protected LeaveRequestRepository leaveRequestRepository;
  protected LeaveRequestCheckService leaveRequestCheckService;

  @Inject
  public LeaveRequestCancelServiceImpl(
      LeaveRequestManagementService leaveRequestManagementService,
      LeaveLineService leaveLineService,
      ICalendarEventRepository iCalendarEventRepository,
      LeaveReasonService leaveReasonService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveRequestCheckService leaveRequestCheckService) {
    this.leaveRequestManagementService = leaveRequestManagementService;
    this.leaveLineService = leaveLineService;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.leaveReasonService = leaveReasonService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveRequestCheckService = leaveRequestCheckService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(LeaveRequest leaveRequest) throws AxelorException {
    LeaveReason leaveReason = leaveRequest.getLeaveReason();

    leaveRequestCheckService.checkCompany(leaveRequest);
    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveRequestManagementService.manageCancelLeaves(leaveRequest);
    }

    if (leaveRequest.getIcalendarEvent() != null) {
      ICalendarEvent event = leaveRequest.getIcalendarEvent();
      leaveRequest.setIcalendarEvent(null);
      iCalendarEventRepository.remove(iCalendarEventRepository.find(event.getId()));
    }
    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_CANCELED);
    leaveRequestRepository.save(leaveRequest);

    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }
}
