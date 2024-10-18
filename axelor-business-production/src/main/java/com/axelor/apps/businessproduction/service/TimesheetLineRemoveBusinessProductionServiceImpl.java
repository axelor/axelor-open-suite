/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveServiceImpl;
import com.axelor.apps.production.db.OperationOrder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TimesheetLineRemoveBusinessProductionServiceImpl
    extends TimesheetLineRemoveServiceImpl {

  @Inject
  public TimesheetLineRemoveBusinessProductionServiceImpl(
      TimesheetLineRepository timeSheetLineRepository) {
    super(timeSheetLineRepository);
  }

  @Override
  @Transactional
  protected void removeTimesheetLine(TimesheetLine timesheetLine) {
    if (timesheetLine == null) {
      return;
    }

    if (timesheetLine.getOperationOrder() != null) {
      OperationOrder operationOrder = timesheetLine.getOperationOrder();
      timesheetLine.setTimesheet(null);
      operationOrder.removeTimesheetLineListItem(timesheetLine);
    }

    super.removeTimesheetLine(timesheetLine);
  }
}
