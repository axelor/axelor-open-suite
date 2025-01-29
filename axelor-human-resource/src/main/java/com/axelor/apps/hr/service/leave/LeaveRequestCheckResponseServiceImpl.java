package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.base.rest.dto.CheckResponseLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveRequestCheckResponseServiceImpl implements LeaveRequestCheckResponseService {

  protected final LeaveRequestCheckService leaveRequestCheckService;
  protected final LeaveRequestService leaveRequestService;

  @Inject
  public LeaveRequestCheckResponseServiceImpl(
      LeaveRequestCheckService leaveRequestCheckService, LeaveRequestService leaveRequestService) {
    this.leaveRequestCheckService = leaveRequestCheckService;
    this.leaveRequestService = leaveRequestService;
  }

  @Override
  public CheckResponse createResponse(LeaveRequest leaveRequest) {
    List<CheckResponseLine> checkResponseLineList = new ArrayList<>();
    checkDate(leaveRequest).ifPresent(checkResponseLineList::add);
    checkDuration(leaveRequest).ifPresent(checkResponseLineList::add);
    checkAvailableDays(leaveRequest).ifPresent(checkResponseLineList::add);

    return new CheckResponse(leaveRequest, checkResponseLineList);
  }

  protected Optional<CheckResponseLine> checkDate(LeaveRequest leaveRequest) {
    if (leaveRequestCheckService.isDatesInvalid(leaveRequest)) {
      return Optional.of(
          new CheckResponseLine(
              leaveRequest,
              I18n.get(HumanResourceExceptionMessage.INVALID_DATES),
              CheckResponseLine.CHECK_TYPE_ERROR));
    }
    return Optional.empty();
  }

  protected Optional<CheckResponseLine> checkDuration(LeaveRequest leaveRequest) {
    if (leaveRequestCheckService.isDurationInvalid(leaveRequest)) {
      return Optional.of(
          new CheckResponseLine(
              leaveRequest,
              I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_WRONG_DURATION),
              CheckResponseLine.CHECK_TYPE_ERROR));
    }
    return Optional.empty();
  }

  protected Optional<CheckResponseLine> checkAvailableDays(LeaveRequest leaveRequest) {
    LeaveReason leaveReason = leaveRequest.getLeaveReason();
    if (!leaveRequestService.willHaveEnoughDays(leaveRequest) && leaveReason != null) {

      if (leaveReason.getAllowNegativeValue()) {
        return Optional.of(
            new CheckResponseLine(
                leaveRequest,
                I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_ALERT_2),
                CheckResponseLine.CHECK_TYPE_ALERT));
      } else {
        return Optional.of(
            new CheckResponseLine(
                leaveRequest,
                I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NOT_ENOUGH_DAYS),
                CheckResponseLine.CHECK_TYPE_ALERT));
      }
    }
    return Optional.empty();
  }
}
