package com.axelor.apps.cash.management.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.cash.management.db.CashFlowForecast;
import com.axelor.apps.cash.management.db.CashFlowReason;
import com.axelor.apps.cash.management.db.CashManagementRecap;
import com.axelor.apps.cash.management.db.CashManagementRecapLine;
import com.axelor.apps.cash.management.db.repo.CashFlowForecastRepository;
import com.axelor.apps.cash.management.db.repo.CashManagementRecapLineRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CashManagementRecapService {
	
	@Inject
	protected TimetableService timetableService;
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected CashFlowForecastRepository cashFlowForecastRepo;
	
	@Inject
	protected CashManagementRecapLineRepository cashManagementRecapLineRepo;
	
	@Inject
	protected CurrencyService currencyService;
	
	public void populate(CashManagementRecap cashManagementRecap) throws AxelorException{
		List<CashManagementRecapLine> cashManagementRecapLineList = cashManagementRecap.getCashManagementRecapLineList();
		if(cashManagementRecapLineList != null && !cashManagementRecapLineList.isEmpty()){
			for (CashManagementRecapLine cashManagementRecapLine : cashManagementRecapLineList) {
				if(cashManagementRecapLine.getId() != null && cashManagementRecapLine.getId() > 0){
					cashManagementRecapLineRepo.remove(cashManagementRecapLine);
				}
			}
			cashManagementRecapLineList.clear();
		}
		cashManagementRecap.setCurrentBalance(cashManagementRecap.getStartingBalance());
		this.populateWithInvoices(cashManagementRecap);
		this.populateWithSalaries(cashManagementRecap);
		this.populateWithTimetables(cashManagementRecap);
		this.populateWithForecasts(cashManagementRecap);
		this.populateWithExpenses(cashManagementRecap);
		cashManagementRecap.setEndingBalance(cashManagementRecap.getCurrentBalance());
	}
	
	public void populateWithInvoices(CashManagementRecap cashManagementRecap){
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		if(cashManagementRecap.getBankDetails() != null){
			invoiceList = Beans.get(InvoiceRepository.class).all().filter("self.company = ?1 AND self.bankDetails = ?2 AND self.statusSelect = 3 AND self.invoiceDate BETWEEN ?3 AND ?4 AND self.companyInTaxTotalRemaining != 0",
								cashManagementRecap.getCompany(),cashManagementRecap.getBankDetails(), cashManagementRecap.getFromDate(), cashManagementRecap.getToDate()).fetch();
		}
		else{
			invoiceList = Beans.get(InvoiceRepository.class).all().filter("self.company = ?1 AND self.statusSelect = 3 AND self.invoiceDate BETWEEN ?2 AND ?3 AND self.companyInTaxTotalRemaining != 0",
								cashManagementRecap.getCompany(), cashManagementRecap.getFromDate(), cashManagementRecap.getToDate()).fetch();
		}
		for (Invoice invoice : invoiceList) {
			if(invoice.getOperationTypeSelect() == 2 || invoice.getOperationTypeSelect() == 3){
				cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().add(invoice.getCompanyInTaxTotalRemaining()));
				cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(invoice.getInvoiceDate(), 1, null,
						invoice.getCompanyInTaxTotalRemaining(), cashManagementRecap.getCurrentBalance()));
			}
			if(invoice.getOperationTypeSelect() == 1 || invoice.getOperationTypeSelect() == 4){
				cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().subtract(invoice.getCompanyInTaxTotalRemaining()));
				cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(invoice.getInvoiceDate(), 2, null,
						invoice.getCompanyInTaxTotalRemaining(), cashManagementRecap.getCurrentBalance()));
			}
		}
	}
	
	public void populateWithSalaries(CashManagementRecap cashManagementRecap){
		List<Employee> employeeList = new ArrayList<Employee>();
		if(cashManagementRecap.getBankDetails() != null){
			employeeList = Beans.get(EmployeeRepository.class).all().filter("self.user.activeCompany = ?1 AND self.bankDetails = ?2",cashManagementRecap.getCompany(),cashManagementRecap.getBankDetails()).fetch();
		}
		else{
			employeeList = Beans.get(EmployeeRepository.class).all().filter("self.user.activeCompany = ?1",cashManagementRecap.getCompany()).fetch();
		}
		LocalDate itDate = new LocalDate(cashManagementRecap.getFromDate());
		while(!itDate.isAfter(cashManagementRecap.getToDate())){
			if(itDate.isEqual(new LocalDate(itDate.getYear(), itDate.getMonthOfYear(), itDate.dayOfMonth().getMaximumValue()))){
				for (Employee employee : employeeList) {
					cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().subtract(employee.getHourlyRate()));
					cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(itDate, 2, null,
							employee.getHourlyRate(), cashManagementRecap.getCurrentBalance()));
				}
				itDate = itDate.plusMonths(1);
			}
			else{
				itDate = new LocalDate(itDate.getYear(), itDate.getMonthOfYear(), itDate.dayOfMonth().getMaximumValue());
			}
		}
	}
	
	public void populateWithTimetables(CashManagementRecap cashManagementRecap) throws AxelorException{
		List<Timetable> timetableSaleOrderList = new ArrayList<Timetable>();
		List<Timetable> timetablePurchaseOrderList = new ArrayList<Timetable>();
		if(cashManagementRecap.getBankDetails() != null){
			timetableSaleOrderList = Beans.get(TimetableRepository.class).all().filter("self.estimatedDate BETWEEN ?1 AND ?2 AND self.saleOrder.company = ?3 AND"
					+ " self.saleOrder.bankDetails = ?4 AND self.saleOrder.statusSelect = 3 AND self.amountToInvoice != 0", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany(),cashManagementRecap.getBankDetails()).fetch();
			timetablePurchaseOrderList = Beans.get(TimetableRepository.class).all().filter("self.estimatedDate BETWEEN ?1 AND ?2 AND self.purchaseOrder.company = ?3 AND"
					+ " self.purchaseOrder.bankDetails = ?4 AND self.purchaseOrder.statusSelect = 3 AND self.amountToInvoice != 0", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany(),cashManagementRecap.getBankDetails()).fetch();
		}
		else{
			timetableSaleOrderList = Beans.get(TimetableRepository.class).all().filter("self.estimatedDate BETWEEN ?1 AND ?2 AND self.saleOrder.company = ?3 AND"
					+ " self.saleOrder.statusSelect = 3 AND self.amountToInvoice != 0", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany()).fetch();
			timetablePurchaseOrderList = Beans.get(TimetableRepository.class).all().filter("self.estimatedDate BETWEEN ?1 AND ?2 AND self.purchaseOrder.company = ?3 AND"
					+ " self.purchaseOrder.statusSelect = 3 AND self.amountToInvoice != 0", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany()).fetch();
		}
		for (Timetable timetable : timetableSaleOrderList) {
			timetableService.updateTimetable(timetable.getSaleOrder());
			BigDecimal amountCompanyCurr = currencyService.getAmountCurrencyConverted(
					timetable.getSaleOrder().getCurrency(), timetable.getSaleOrder().getCompany().getCurrency(), timetable.getAmountToInvoice(), generalService.getTodayDate())
					.setScale(2, RoundingMode.HALF_UP);
			cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().add(amountCompanyCurr));
			cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(timetable.getEstimatedDate(), 1, null, amountCompanyCurr, cashManagementRecap.getCurrentBalance()));
		}
		for (Timetable timetable : timetablePurchaseOrderList) {
			timetableService.updateTimetable(timetable.getPurchaseOrder());
			BigDecimal amountCompanyCurr = currencyService.getAmountCurrencyConverted(
					timetable.getPurchaseOrder().getCurrency(), timetable.getPurchaseOrder().getCompany().getCurrency(), timetable.getAmountToInvoice(), generalService.getTodayDate())
					.setScale(2, RoundingMode.HALF_UP);
			cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().subtract(amountCompanyCurr));
			cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(timetable.getEstimatedDate(), 2, null, amountCompanyCurr, cashManagementRecap.getCurrentBalance()));
		}
	}
	
	@Transactional
	public void populateWithForecasts(CashManagementRecap cashManagementRecap){
		List<CashFlowForecast> cashFlowForecastList = new ArrayList<CashFlowForecast>();
		if(cashManagementRecap.getBankDetails() != null){
			cashFlowForecastList = Beans.get(CashFlowForecastRepository.class).all().filter("self.estimatedDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
					+ " self.bankDetails = ?4 AND (self.realizedSelect = 2 OR (self.realizedSelect = 3 AND self.estimatedDate <= ?5))", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany(),cashManagementRecap.getBankDetails(), generalService.getTodayDate()).fetch();
		}
		else{
			cashFlowForecastList = Beans.get(CashFlowForecastRepository.class).all().filter("self.estimatedDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
					+ " (self.realizedSelect = 2 OR (self.realizedSelect = 3 AND self.estimatedDate <= ?4))", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany(), generalService.getTodayDate()).fetch();
		}
		for (CashFlowForecast cashFlowForecast : cashFlowForecastList) {
			if(cashFlowForecast.getTypeSelect() == 1){
				cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().add(cashFlowForecast.getAmount()));
			}
			else{
				cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().subtract(cashFlowForecast.getAmount()));
			}
			cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(cashFlowForecast.getEstimatedDate(), cashFlowForecast.getTypeSelect(), cashFlowForecast.getCashFlowReason(), cashFlowForecast.getAmount(), cashManagementRecap.getCurrentBalance()));
			cashFlowForecast.setRealizedSelect(CashFlowForecastRepository.REALISED_SELECT_YES);
			cashFlowForecastRepo.save(cashFlowForecast);
		}
	}
	
	public void populateWithExpenses(CashManagementRecap cashManagementRecap){
		List<Expense> expenseList = new ArrayList<Expense>();
		if(cashManagementRecap.getBankDetails() != null){
			expenseList = Beans.get(ExpenseRepository.class).all().filter("self.validationDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
					+ " self.bankDetails = ?4 AND self.statusSelect = 3", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany(),cashManagementRecap.getBankDetails()).fetch();
		}
		else{
			expenseList = Beans.get(ExpenseRepository.class).all().filter("self.validationDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
					+ " self.statusSelect = 3", cashManagementRecap.getFromDate(), cashManagementRecap.getToDate(), cashManagementRecap.getCompany()).fetch();
		}
		for (Expense expense : expenseList) {
			cashManagementRecap.setCurrentBalance(cashManagementRecap.getCurrentBalance().subtract(expense.getInTaxTotal()));
			cashManagementRecap.addCashManagementRecapLineListItem(this.createCashManagementRecapLine(expense.getValidationDate(), 2, null, expense.getInTaxTotal(), cashManagementRecap.getCurrentBalance()));
		}
	}
	
	public CashManagementRecapLine createCashManagementRecapLine(LocalDate date, int type, CashFlowReason reason, BigDecimal amount, BigDecimal balance){
		CashManagementRecapLine cashManagementRecapLine = new CashManagementRecapLine();
		cashManagementRecapLine.setEstimatedDate(date);
		cashManagementRecapLine.setTypeSelect(type);
		cashManagementRecapLine.setCashFlowReason(reason);
		cashManagementRecapLine.setAmount(amount);
		cashManagementRecapLine.setBalance(balance);
		return cashManagementRecapLine;
	}
}
