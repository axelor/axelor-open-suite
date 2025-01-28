package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;

public class LeaveDaysToDatePostRequest extends RequestPostStructure {

  private LocalDate toDate;

  private Long leaveReasonId;

  public LocalDate getToDate() {
    return toDate;
  }

  public void setToDate(LocalDate toDate) {
    this.toDate = toDate;
  }

  public Long getLeaveReasonId() {
    return leaveReasonId;
  }

  public void setLeaveReasonId(Long leaveReasonId) {
    this.leaveReasonId = leaveReasonId;
  }

  public LeaveReason fetchLeaveReason() {
    if (leaveReasonId == null || leaveReasonId == 0L) {
      return null;
    }
    return ObjectFinder.find(LeaveReason.class, leaveReasonId, ObjectFinder.NO_VERSION);
  }
}
