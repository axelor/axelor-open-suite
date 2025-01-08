package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.utils.api.ObjectFinder;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class LeaveRequestReasonRequest {

  @NotNull private Long leaveReasonId;

  @NotNull private BigDecimal duration;

  private String comment;

  public Long getLeaveReasonId() {
    return leaveReasonId;
  }

  public void setLeaveReasonId(Long leaveReasonId) {
    this.leaveReasonId = leaveReasonId;
  }

  public BigDecimal getDuration() {
    return duration;
  }

  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public LeaveReason fetchLeaveReason() {
    if (leaveReasonId == null || leaveReasonId == 0L) {
      return null;
    }
    return ObjectFinder.find(LeaveReason.class, leaveReasonId, ObjectFinder.NO_VERSION);
  }
}
