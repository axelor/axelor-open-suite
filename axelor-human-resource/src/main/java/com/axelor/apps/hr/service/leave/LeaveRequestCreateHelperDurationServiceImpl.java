package com.axelor.apps.hr.service.leave;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class LeaveRequestCreateHelperDurationServiceImpl
    implements LeaveRequestCreateHelperDurationService {

  @Override
  public boolean durationIsExceeded(BigDecimal duration, BigDecimal totalDuration) {
    return totalDuration.compareTo(duration) > 0;
  }

  @Override
  public BigDecimal getTotalDuration(List<HashMap<String, Object>> leaveReasonList) {
    return leaveReasonList.stream()
        .filter(map -> map.get("duration") != null)
        .map(map -> new BigDecimal((String) map.get("duration")))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
