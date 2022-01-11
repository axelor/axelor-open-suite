/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.tool.QueryBuilder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface TimesheetLineBusinessService {

  TimesheetLine getDefaultToInvoice(TimesheetLine timesheetLine);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public TimesheetLine updateTimesheetLines(TimesheetLine timesheetLine);

  public TimesheetLine setTimesheet(TimesheetLine timesheetLine);

  public QueryBuilder<TimesheetLine> getTimesheetLineInvoicingFilter();

  public void timsheetLineInvoicing(Project project);
}
