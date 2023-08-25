package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class LeaveRequestWorkflowServiceImpl implements LeaveRequestWorkflowService {

  protected LeaveRequestManagementService leaveRequestManagementService;
  protected LeaveLineService leaveLineService;
  protected LeaveRequestEventService leaveRequestEventService;
  protected ICalendarEventRepository iCalendarEventRepository;

  @Inject
  public LeaveRequestWorkflowServiceImpl(
      LeaveRequestManagementService leaveRequestManagementService,
      LeaveLineService leaveLineService,
      LeaveRequestEventService leaveRequestEventService,
      ICalendarEventRepository iCalendarEventRepository,
      LeaveRequestRepository leaveRequestRepository,
      AppBaseService appBaseService) {
    this.leaveRequestManagementService = leaveRequestManagementService;
    this.leaveLineService = leaveLineService;
    this.leaveRequestEventService = leaveRequestEventService;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.leaveRequestRepository = leaveRequestRepository;
    this.appBaseService = appBaseService;
  }

  protected LeaveRequestRepository leaveRequestRepository;
  protected AppBaseService appBaseService;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageSentLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
    leaveRequest.setRequestDate(appBaseService.getTodayDate(leaveRequest.getCompany()));

    leaveRequestRepository.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void refuse(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageRefuseLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_REFUSED);
    leaveRequest.setRefusedBy(AuthUtils.getUser());
    leaveRequest.setRefusalDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    leaveRequestRepository.save(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void validate(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
      isOverlapped(leaveRequest);
    }
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageValidateLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_VALIDATED);
    leaveRequest.setValidatedBy(AuthUtils.getUser());
    leaveRequest.setValidationDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leaveRequest);
    if (leaveLine != null) {
      leaveRequest.setQuantityBeforeValidation(leaveLine.getQuantity());
    }
    leaveRequestRepository.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLine);
    }
    leaveRequestEventService.createEvents(leaveRequest);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageCancelLeaves(leaveRequest);
    }

    if (leaveRequest.getIcalendarEvent() != null) {
      ICalendarEvent event = leaveRequest.getIcalendarEvent();
      leaveRequest.setIcalendarEvent(null);
      iCalendarEventRepository.remove(iCalendarEventRepository.find(event.getId()));
    }
    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_CANCELED);
    leaveRequestRepository.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }

  protected void checkCompany(LeaveRequest leaveRequest) throws AxelorException {

    if (ObjectUtils.isEmpty(leaveRequest.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NO_COMPANY));
    }
  }

  protected void isOverlapped(LeaveRequest leaveRequest) throws AxelorException {
    List<LeaveRequest> leaveRequestList =
        leaveRequestRepository
            .all()
            .filter(
                "self.employee = ?1 AND self.statusSelect = ?2",
                leaveRequest.getEmployee(),
                LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();
    for (LeaveRequest leaveRequest2 : leaveRequestList) {
      if (isOverlapped(leaveRequest, leaveRequest2)) {
        throw new AxelorException(
            leaveRequest,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_DATES_OVERLAPPED));
      }
    }
  }

  protected boolean isOverlapped(LeaveRequest request1, LeaveRequest request2) {

    if (isDatesNonOverlapped(request1, request2)
        || isSelectsNonOverlapped(request1, request2)
        || isSelectsNonOverlapped(request2, request1)) {
      return false;
    }

    return true;
  }

  protected boolean isDatesNonOverlapped(LeaveRequest request1, LeaveRequest request2) {
    return request2.getToDateT().isBefore(request1.getFromDateT())
        || request1.getToDateT().isBefore(request2.getFromDateT())
        || request1.getToDateT().isBefore(request1.getFromDateT())
        || request2.getToDateT().isBefore(request2.getFromDateT());
  }

  protected boolean isSelectsNonOverlapped(LeaveRequest request1, LeaveRequest request2) {
    return request1.getEndOnSelect() == LeaveRequestRepository.SELECT_MORNING
        && request2.getStartOnSelect() == LeaveRequestRepository.SELECT_AFTERNOON
        && request1.getToDateT().isEqual(request2.getFromDateT());
  }
}
