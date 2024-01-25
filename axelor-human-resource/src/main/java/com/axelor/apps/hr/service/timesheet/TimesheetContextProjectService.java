package com.axelor.apps.hr.service.timesheet;

import com.axelor.meta.CallMethod;
import java.util.Set;

public interface TimesheetContextProjectService {
  @CallMethod
  Set<Long> getContextProjectIds();
}
