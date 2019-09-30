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

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface OperationOrderTimesheetService {

  /**
   * Given an unsaved timesheet line, update the related orders by taking the unsaved timesheet line
   * and the timesheet line in db.
   *
   * @param operationOrder
   * @param oldTimesheetLineList
   * @param newTimesheetLineList
   */
  void updateOperationOrder(
      OperationOrder operationOrder,
      List<TimesheetLine> oldTimesheetLineList,
      List<TimesheetLine> newTimesheetLineList);

  /**
   * Given an unsaved timesheet, update the related orders by taking all timesheet lines and
   * timesheet lines in db.
   *
   * @param timesheet an unsaved timesheet.
   */
  void updateOperationOrders(Timesheet timesheet) throws AxelorException;

  /**
   * Compute real duration of operation orders related to a timesheet line list.
   *
   * @param timesheetLineList a list of timesheet lines linked to operation orders.
   */
  void updateAllRealDuration(List<TimesheetLine> timesheetLineList);
}
