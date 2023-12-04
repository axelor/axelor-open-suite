package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;

public class LeaveReasonServiceImpl implements LeaveReasonService {

  @Override
  public boolean isExceptionalDaysReason(LeaveReason leaveReason) {
    return leaveReason.getLeaveReasonTypeSelect()
        == LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS;
  }
}
