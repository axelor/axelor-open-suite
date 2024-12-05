package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class LeaveRequestCreateHelperDurationServiceImpl
    implements LeaveRequestCreateHelperDurationService {

  @Override
  public void checkDuration(BigDecimal duration, BigDecimal totalDuration) throws AxelorException {
    if (totalDuration.compareTo(duration) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get("You exceeded the available duration"));
    }
  }

  @Override
  public BigDecimal getTotalDuration(List<HashMap<String, Object>> leaveReasonList) {
    return leaveReasonList.stream()
        .filter(map -> map.get("duration") != null)
        .map(map -> new BigDecimal((String) map.get("duration")))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
