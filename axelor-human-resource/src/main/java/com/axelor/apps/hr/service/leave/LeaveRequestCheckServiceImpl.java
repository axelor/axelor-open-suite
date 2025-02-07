package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeaveRequestCheckServiceImpl implements LeaveRequestCheckService {

  @Override
  public void checkCompany(LeaveRequest leaveRequest) throws AxelorException {

    if (ObjectUtils.isEmpty(leaveRequest.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NO_COMPANY));
    }
  }

  @Override
  public void checkDates(LeaveRequest leaveRequest) throws AxelorException {
    if (isDatesInvalid(leaveRequest)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.INVALID_DATES));
    }

    if (isDurationInvalid(leaveRequest)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_WRONG_DURATION));
    }
  }

  @Override
  public boolean isDatesInvalid(LeaveRequest leaveRequest) {
    LocalDateTime fromDateTime = leaveRequest.getFromDateT();
    LocalDateTime toDateTime = leaveRequest.getToDateT();
    int startOnSelect = leaveRequest.getStartOnSelect();
    int endOnSelect = leaveRequest.getEndOnSelect();
    return toDateTime.isBefore(fromDateTime)
        || (toDateTime.isEqual(fromDateTime) && startOnSelect > endOnSelect);
  }

  @Override
  public boolean isDurationInvalid(LeaveRequest leaveRequest) {
    BigDecimal duration = leaveRequest.getDuration();
    return duration.signum() == 0;
  }
}
