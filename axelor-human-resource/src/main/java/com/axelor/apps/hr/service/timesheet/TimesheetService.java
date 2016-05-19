/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public interface TimesheetService {
	public void getTimeFromTask(Timesheet timesheet);
	public void cancelTimesheet(Timesheet timesheet);
	public Timesheet generateLines(Timesheet timesheet, LocalDate fromGenerationDate, LocalDate toGenerationDate, BigDecimal logTime, ProjectTask projectTask, Product product) throws AxelorException;
	public LocalDate getFromPeriodDate();
	public Timesheet getCurrentTimesheet();
	public Timesheet getCurrentOrCreateTimesheet();
	public Timesheet createTimesheet(User user, LocalDate fromDate, LocalDate toDate);
	public TimesheetLine createTimesheetLine(ProjectTask project, Product product, User user, LocalDate date, Timesheet timesheet, BigDecimal hours, String comments);
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException;
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Product product, User user, String date, BigDecimal visibleDuration, int priority) throws AxelorException;
	@Transactional
	public void computeTimeSpent(Timesheet timesheet);
	public BigDecimal computeSubTimeSpent(ProjectTask projectTask);
	public void computeParentTimeSpent(ProjectTask projectTask);
	public BigDecimal computeTimeSpent(ProjectTask projectTask);
	public void getActivities(ActionRequest request, ActionResponse response);
	@Transactional
	public void insertTSLine(ActionRequest request, ActionResponse response);
	public String computeFullName(Timesheet timeSheet);
	public List<TimesheetLine> computeVisibleDuration(Timesheet timesheet);
}
