package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.hr.db.Timesheet;

public interface TimesheetCheckResponseService {

  CheckResponse createResponse(Timesheet timesheet) throws AxelorException;
}
