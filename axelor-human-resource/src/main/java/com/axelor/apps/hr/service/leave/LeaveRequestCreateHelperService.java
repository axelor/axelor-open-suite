package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public interface LeaveRequestCreateHelperService {

  List<Long> createLeaveRequests(
      LocalDate fromDate, int startOnSelect, List<HashMap<String, Object>> leaveReasonList)
      throws AxelorException;
}
