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
package com.axelor.apps.hr.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.PayrollLeave;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PayrollPreparationService {
	
	@Inject
	protected LeaveService leaveService;
	
	@Inject
	protected LeaveRequestRepository leaveRequestRepo;
	
	@Inject
	protected WeeklyPlanningService weeklyPlanningService;

	public PayrollPreparation generateFromEmploymentContract(PayrollPreparation payrollPreparation, EmploymentContract employmentContract){
		if(payrollPreparation.getEmployee() == null){
			payrollPreparation.setEmployee(employmentContract.getEmployee());
		}
		if(payrollPreparation.getCompany() == null){
			payrollPreparation.setCompany(employmentContract.getPayCompany());
		}
		if(payrollPreparation.getEmploymentContract() == null){
			payrollPreparation.setEmploymentContract(employmentContract);
		}

		payrollPreparation.setOtherCostsEmployeeSet(employmentContract.getOtherCostsEmployeeSet());
		payrollPreparation.setAnnualGrossSalary(employmentContract.getAnnualGrossSalary());
		return payrollPreparation;
	}
	
	public List<PayrollLeave> fillInPayrollPreparation(PayrollPreparation payrollPreparation) throws AxelorException{
		List<PayrollLeave> payrollLeaveList = this.fillInLeaves(payrollPreparation);
		this.fillInExtraHours(payrollPreparation);
		payrollPreparation.setDuration(this.computeWorkingDaysNumber(payrollPreparation,payrollLeaveList));
		payrollPreparation.setExpenseAmount(this.computeExpenseAmount(payrollPreparation));
		return payrollLeaveList;
	}
	
	public List<PayrollLeave> fillInLeaves(PayrollPreparation payrollPreparation) throws AxelorException{
		List<PayrollLeave> payrollLeaveList = new ArrayList<PayrollLeave>();
		LocalDate fromDate = new LocalDate(payrollPreparation.getYearPeriod(), payrollPreparation.getMonthSelect(), 1);
		LocalDate toDate = new LocalDate(fromDate);
		toDate = toDate.dayOfMonth().withMaximumValue();
		Employee employee = payrollPreparation.getEmployee();
		if(employee.getPublicHolidayPlanning() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PUBLIC_HOLIDAY),employee.getName()), IException.CONFIGURATION_ERROR);
		}
		if(employee.getPlanning()== null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PLANNING),employee.getName()), IException.CONFIGURATION_ERROR);
		}
		
		List<LeaveRequest> leaveRequestList = leaveRequestRepo.all().filter("self.statusSelect = 3 AND self.user.employee = ?3 AND self.dateFrom <= ?1 AND self.dateTo >= ?2",toDate, fromDate,employee).fetch();
		for (LeaveRequest leaveRequest : leaveRequestList) {
			PayrollLeave payrollLeave = new PayrollLeave();
			if(leaveRequest.getDateFrom().isBefore(fromDate)){
				payrollLeave.setFromDate(fromDate);
			}
			else{
				payrollLeave.setFromDate(leaveRequest.getDateFrom());
			}
			if(leaveRequest.getDateTo().isAfter(toDate)){
				payrollLeave.setToDate(toDate);
			}
			else{
				payrollLeave.setToDate(leaveRequest.getDateTo());
			}
			
			payrollLeave.setDuration(leaveService.computeLeaveDaysByLeaveRequest(fromDate, toDate, leaveRequest, employee));
			payrollLeave.setReason(leaveRequest.getReason());
			payrollLeave.setLeaveRequest(leaveRequest);
			payrollLeaveList.add(payrollLeave);
		}
		return payrollLeaveList;
	}
	
	public BigDecimal computeWorkingDaysNumber(PayrollPreparation payrollPreparation, List<PayrollLeave> payrollLeaveList){
		LocalDate fromDate = new LocalDate(payrollPreparation.getYearPeriod(), payrollPreparation.getMonthSelect(), 1);
		LocalDate toDate = new LocalDate(fromDate);
		toDate = toDate.dayOfMonth().withMaximumValue();
		LocalDate itDate = new LocalDate(fromDate);
		BigDecimal workingDays = BigDecimal.ZERO;
		while(!itDate.isAfter(toDate)){
			workingDays = workingDays.add(new BigDecimal(weeklyPlanningService.workingDayValue(payrollPreparation.getEmployee().getPlanning(), itDate)));
			itDate = itDate.plusDays(1);
		}
		if(payrollLeaveList != null){
			for (PayrollLeave payrollLeave : payrollLeaveList) {
				workingDays = workingDays.subtract(payrollLeave.getDuration());
			}
		}
		
		return workingDays;
	}
	
	public void fillInExtraHours(PayrollPreparation payrollPreparation){
		LocalDate fromDate = new LocalDate(payrollPreparation.getYearPeriod(), payrollPreparation.getMonthSelect(), 1);
		LocalDate toDate = new LocalDate(fromDate);
		toDate = toDate.dayOfMonth().withMaximumValue();
		for(ExtraHoursLine extraHoursLine : Beans.get(ExtraHoursLineRepository.class).all().filter("self.user.employee = ?1 AND self.extraHours.statusSelect = 3 AND self.date BETWEEN ?2 AND ?3 AND self.payrollPreparation = null", payrollPreparation.getEmployee(), fromDate, toDate).fetch()){
			payrollPreparation.addExtraHoursLineListItem(extraHoursLine);
		}
	}
	
	public BigDecimal computeExpenseAmount(PayrollPreparation payrollPreparation){
		BigDecimal expenseAmount = BigDecimal.ZERO;
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user.employee = ?1 AND self.statusSelect = 3 AND self.payrollPreparation = null AND self.expenseCompanyCb = false", payrollPreparation.getEmployee()).fetch();
		for (Expense expense : expenseList) {
			expenseAmount = expenseAmount.add(expense.getInTaxTotal());
			payrollPreparation.addExpenseListItem(expense);
		}
		return expenseAmount;
	}
}
