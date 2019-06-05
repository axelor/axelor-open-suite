/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.production.db.OperationOrder;
import java.util.Objects;

public class OperationOrderValidateBusinessServiceImpl
    implements OperationOrderValidateBusinessService {

  @Override
  public long checkTimesheet(OperationOrder operationOrder) {
    return operationOrder
        .getTimesheetLineList()
        .stream()
        .map(TimesheetLine::getTimesheet)
        .filter(Objects::nonNull)
        .filter(timesheet -> timesheet.getStatusSelect() == TimesheetRepository.STATUS_CONFIRMED)
        .count();
  }
}
