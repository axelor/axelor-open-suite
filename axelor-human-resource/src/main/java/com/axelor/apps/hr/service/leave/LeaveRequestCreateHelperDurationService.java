package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface LeaveRequestCreateHelperDurationService {

  boolean durationIsExceeded(BigDecimal duration, BigDecimal totalDuration) throws AxelorException;

  BigDecimal getTotalDuration(List<HashMap<String, Object>> leaveReasonList);
}
