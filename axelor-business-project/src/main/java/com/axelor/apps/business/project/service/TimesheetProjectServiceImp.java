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
package com.axelor.apps.business.project.service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class TimesheetProjectServiceImp extends TimesheetServiceImp{
	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		int count = 0;
		DateFormat ddmmFormat = new SimpleDateFormat("dd/MM");
		HashMap<String, Object[]> timeSheetInformationsMap = new HashMap<String, Object[]>();
		//Check if a consolidation by product and user must be done
		boolean consolidate = generalService.getGeneral().getConsolidateTSLine();

		for (TimesheetLine timesheetLine : timesheetLineList) {
			Object[] tabInformations = new Object[6];
			tabInformations[0] = timesheetLine.getProduct();
			tabInformations[1] = timesheetLine.getUser();
			//Start date
			tabInformations[2] = timesheetLine.getDate();
			//End date, useful only for consolidation
			tabInformations[3] = timesheetLine.getDate();
			tabInformations[4] = timesheetLine.getVisibleDuration();
			tabInformations[5] = timesheetLine.getProjectTask();

			String key = null;
			if(consolidate){
				key = timesheetLine.getProduct().getId() + "|" + timesheetLine.getUser().getId() + "|" + timesheetLine.getProjectTask().getId();
				if (timeSheetInformationsMap.containsKey(key)){
					tabInformations = timeSheetInformationsMap.get(key);
					//Update date
					if (timesheetLine.getDate().compareTo((LocalDate)tabInformations[2]) < 0){
						//If date is lower than start date then replace start date by this one
						tabInformations[2] = timesheetLine.getDate();
					}else if (timesheetLine.getDate().compareTo((LocalDate)tabInformations[3]) > 0){
						//If date is upper than end date then replace end date by this one
						tabInformations[3] = timesheetLine.getDate();
					}
					tabInformations[4] = ((BigDecimal)tabInformations[4]).add(timesheetLine.getVisibleDuration());
				}else{
					timeSheetInformationsMap.put(key, tabInformations);
				}
			}else{
				key = String.valueOf(timesheetLine.getId());
				timeSheetInformationsMap.put(key, tabInformations);
			}

			timesheetLine.setInvoiced(true);

		}

		for(Object[] timesheetInformations : timeSheetInformationsMap.values())  {

			String strDate = null;
			Product product = (Product)timesheetInformations[0];
			User user = (User)timesheetInformations[1];
			LocalDate startDate = (LocalDate)timesheetInformations[2];
			LocalDate endDate = (LocalDate)timesheetInformations[3];
			BigDecimal visibleDuration = (BigDecimal) timesheetInformations[4];
			ProjectTask projectTask = (ProjectTask) timesheetInformations[5];

			if (consolidate){
				strDate = ddmmFormat.format(startDate.toDate()) + " - " + ddmmFormat.format(endDate.toDate());
			}else{
				strDate = ddmmFormat.format(startDate.toDate());
			}

			invoiceLineList.addAll(this.createInvoiceLine(invoice, product, user, strDate, visibleDuration, priority*100+count));
			invoiceLineList.get(0).setProject(projectTask);
			count++;
		}

		return invoiceLineList;

	}
	
	@Override
	public Timesheet generateLines(Timesheet timesheet) throws AxelorException{

		if(timesheet.getFromGenerationDate() == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_FROM_DATE)), IException.CONFIGURATION_ERROR);
		}
		if(timesheet.getToGenerationDate() == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_TO_DATE)), IException.CONFIGURATION_ERROR);
		}
		if(timesheet.getProduct() == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_PRODUCT)), IException.CONFIGURATION_ERROR);
		}
		if(timesheet.getUser().getEmployee() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),timesheet.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		WeeklyPlanning planning = timesheet.getUser().getEmployee().getPlanning();
		if(planning == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING),timesheet.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		List<DayPlanning> dayPlanningList = planning.getWeekDays();

		LocalDate fromDate = timesheet.getFromGenerationDate();
		LocalDate toDate = timesheet.getToGenerationDate();
		Map<Integer,String> correspMap = new HashMap<Integer,String>();
		correspMap.put(1, "monday");
		correspMap.put(2, "tuesday");
		correspMap.put(3, "wednesday");
		correspMap.put(4, "thursday");
		correspMap.put(5, "friday");
		correspMap.put(6, "saturday");
		correspMap.put(7, "sunday");
		List<LeaveRequest> leaveList = LeaveRequestRepository.of(LeaveRequest.class).all().filter("self.user = ?1 AND (self.statusSelect = 2 OR self.statusSelect = 3)", timesheet.getUser()).fetch();
		while(!fromDate.isAfter(toDate)){
			DayPlanning dayPlanningCurr = new DayPlanning();
			for (DayPlanning dayPlanning : dayPlanningList) {
				if(dayPlanning.getName().equals(correspMap.get(fromDate.getDayOfWeek()))){
					dayPlanningCurr = dayPlanning;
					break;
				}
			}
			if(dayPlanningCurr.getMorningFrom() != null || dayPlanningCurr.getMorningTo() != null || dayPlanningCurr.getAfternoonFrom() != null || dayPlanningCurr.getAfternoonTo() != null)
			{
				boolean noLeave = true;
				if(leaveList != null){
					for (LeaveRequest leave : leaveList) {
						if((leave.getDateFrom().isBefore(fromDate) && leave.getDateTo().isAfter(fromDate))
							|| leave.getDateFrom().isEqual(fromDate) || leave.getDateTo().isEqual(fromDate))
						{
							noLeave = false;
							break;
						}
					}
				}
				if(noLeave){
					TimesheetLine timesheetLine = new TimesheetLine();
					timesheetLine.setDate(fromDate);
					timesheetLine.setUser(timesheet.getUser());
					timesheetLine.setProjectTask(timesheet.getProjectTask());
					timesheetLine.setVisibleDuration(timesheet.getLogTime());
					timesheetLine.setDurationStored(employeeService.getDurationHours(timesheet.getLogTime()));
					timesheetLine.setProduct(timesheet.getProduct());
					if(timesheet.getProjectTask().getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_TIME_BASED
							|| (timesheet.getProjectTask().getProject() != null && timesheet.getProjectTask().getProject().getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_TIME_BASED)){
						timesheetLine.setToInvoice(true);
					}
					timesheet.addTimesheetLineListItem(timesheetLine);
				}
			}
			fromDate=fromDate.plusDays(1);
		}
		return timesheet;
	}
}
