package com.axelor.apps.hr.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeAdvance;
import com.axelor.apps.hr.db.EmployeeAdvanceUsage;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.EmployeeAdvanceRepository;
import com.axelor.apps.hr.db.repo.EmployeeAdvanceUsageRepository;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class EmployeeAdvanceService {
	
	@Inject
	private EmployeeAdvanceRepository employeeAdvanceRepository;
	
	@Inject
	private EmployeeAdvanceUsageRepository employeeAdvanceUsageRepository;
	
	
	@Transactional
	public void fillExpenseWithAdvances(Expense expense ){
		
		Employee employee = Beans.get(EmployeeRepository.class).find( expense.getUser().getEmployee().getId() );
		
		List<EmployeeAdvance> advanceList = Lists.newArrayList();
		
		advanceList = employeeAdvanceRepository.all().filter("self.employee.id = ?1 AND self.remainingAmount > 0 AND self.date < ?2 AND self.statusSelect = ?3 ", employee.getId(), expense.getPeriod().getToDate(), EmployeeAdvanceRepository.STATUS_VALIDATED ).fetch() ;
		
		if ( advanceList != null && !advanceList.isEmpty() ){
			
			BigDecimal currentAmountToRefund = expense.getInTaxTotal().subtract(expense.getPersonalExpenseAmount()).subtract(expense.getWithdrawnCash());
			
			for (EmployeeAdvance advance : advanceList) {
				
				if(currentAmountToRefund.compareTo(BigDecimal.ZERO) == 0) { break; }
				
				
				currentAmountToRefund = withdrawFromAdvance(advance, expense, currentAmountToRefund);
				employeeAdvanceRepository.save(advance);
				
			}
			expense.setAdvanceAmount( expense.getInTaxTotal().subtract(currentAmountToRefund) );
		}
		
	}
	
	
	public BigDecimal withdrawFromAdvance(EmployeeAdvance employeeAdvance, Expense expense, BigDecimal maxAmount){
		
		if (maxAmount.compareTo(employeeAdvance.getRemainingAmount()) == 1){
			maxAmount = maxAmount.subtract(employeeAdvance.getRemainingAmount());
			createEmployeeAdvanceUsage(employeeAdvance, expense, employeeAdvance.getRemainingAmount());
			employeeAdvance.setRemainingAmount(BigDecimal.ZERO);
			
		}else{
			createEmployeeAdvanceUsage(employeeAdvance, expense, maxAmount);
			employeeAdvance.setRemainingAmount( employeeAdvance.getRemainingAmount().subtract(maxAmount) );
			maxAmount = BigDecimal.ZERO;
		}
		
		return maxAmount;
	}
	
	@Transactional
	public void createEmployeeAdvanceUsage(EmployeeAdvance employeeAdvance, Expense expense, BigDecimal amount){
		
		EmployeeAdvanceUsage usage = new EmployeeAdvanceUsage();
		
		usage.setEmployeeAdvance(employeeAdvance);
		usage.setExpense(expense);
		usage.setUsedAmount(amount);
		employeeAdvanceUsageRepository.save(usage);
	}
	

}
