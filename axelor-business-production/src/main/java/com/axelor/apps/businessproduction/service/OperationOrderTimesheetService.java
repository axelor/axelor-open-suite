/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.production.db.OperationOrder;
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
