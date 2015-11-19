/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import org.joda.time.LocalDate;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public interface TimesheetService {
	public void getTimeFromTask(Timesheet timesheet);
	public void cancelTimesheet(Timesheet timesheet);
	public Timesheet generateLines(Timesheet timesheet) throws AxelorException;
	public LocalDate getFromPeriodDate();
	@Transactional
	public void computeTimeSpent(Timesheet timesheet);
	public void getActivities(ActionRequest request, ActionResponse response);
	@Transactional
	public void insertTSLine(ActionRequest request, ActionResponse response);
}
