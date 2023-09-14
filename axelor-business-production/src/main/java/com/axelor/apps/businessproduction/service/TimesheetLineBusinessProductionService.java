package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.production.db.OperationOrderDuration;
import java.math.BigDecimal;
import java.util.Optional;

public interface TimesheetLineBusinessProductionService {

  Optional<TimesheetLine> createTimesheetLine(OperationOrderDuration operationOrderDuration)
      throws AxelorException;

  BigDecimal computeDuration(Timesheet timesheet, long durationInSeconds) throws AxelorException;
}
