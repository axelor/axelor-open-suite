package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LeaveRequestComputeLeaveHoursServiceImpl
    implements LeaveRequestComputeLeaveHoursService {

  protected final LeaveRequestComputeDurationService leaveRequestComputeDurationService;

  @Inject
  public LeaveRequestComputeLeaveHoursServiceImpl(
      LeaveRequestComputeDurationService leaveRequestComputeDurationService) {
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
  }

  @Override
  public BigDecimal computeTotalLeaveHours(
      LocalDate date, BigDecimal dayValueInHours, List<LeaveRequest> leaveList)
      throws AxelorException {
    BigDecimal totalLeaveHours = BigDecimal.ZERO;
    for (LeaveRequest leave : leaveList) {
      BigDecimal leaveHours = leaveRequestComputeDurationService.computeDuration(leave, date, date);
      if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
        leaveHours = leaveHours.multiply(dayValueInHours);
      }
      totalLeaveHours = totalLeaveHours.add(leaveHours);
    }
    return totalLeaveHours;
  }
}
