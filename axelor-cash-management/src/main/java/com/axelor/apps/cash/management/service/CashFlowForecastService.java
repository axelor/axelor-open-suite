package com.axelor.apps.cash.management.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.cash.management.db.CashFlowForecast;
import com.axelor.apps.cash.management.db.CashFlowForecastGenerator;
import com.axelor.apps.cash.management.db.CashFlowReason;
import com.axelor.apps.cash.management.db.repo.CashFlowForecastRepository;
import com.google.inject.Inject;

public class CashFlowForecastService extends CashFlowForecastRepository{
	
	@Inject
	protected GeneralService generalService;
	
	public void generate(CashFlowForecastGenerator cashFlowForecastGenerator){
		LocalDate fromDate = cashFlowForecastGenerator.getFromDate();
		LocalDate toDate = cashFlowForecastGenerator.getToDate();
		LocalDate itDate = new LocalDate(fromDate);
		LocalDate todayDate = generalService.getTodayDate();
		
		if(cashFlowForecastGenerator.getCashFlowForecastList() != null && !cashFlowForecastGenerator.getCashFlowForecastList().isEmpty()){
			List<CashFlowForecast> cashFlowForecastList = cashFlowForecastGenerator.getCashFlowForecastList();
			for (CashFlowForecast cashFlowForecast : cashFlowForecastList) {
				if(cashFlowForecast.getRealizedSelect() == REALISED_SELECT_NO){
					cashFlowForecastList.remove(cashFlowForecast);
				}
				else if(cashFlowForecast.getRealizedSelect() == REALISED_SELECT_AUTO && cashFlowForecast.getEstimatedDate().isAfter(todayDate)){
					cashFlowForecastList.remove(cashFlowForecast);
				}
			}
		}
		
		while(!itDate.isAfter(toDate)){
			CashFlowForecast cashFlowForecast = this.createCashFlowForecast(cashFlowForecastGenerator.getCompany(), cashFlowForecastGenerator.getBankDetails(),
														cashFlowForecastGenerator.getTypeSelect(), cashFlowForecastGenerator.getAmount(), itDate, cashFlowForecastGenerator.getCashFlowReason(),
														cashFlowForecastGenerator.getComments(),cashFlowForecastGenerator.getRealizedSelect());
			cashFlowForecastGenerator.addCashFlowForecastListItem(cashFlowForecast);
			itDate.plusMonths(cashFlowForecastGenerator.getPeriodicitySelect());
		}
	}
	
	public CashFlowForecast createCashFlowForecast(Company company, BankDetails bankDetails, int typeSelect, BigDecimal amount,
													LocalDate estimatedDate, CashFlowReason reason, String comments, int realizedSelect){
		
		CashFlowForecast cashFlowForecast = new CashFlowForecast();
		cashFlowForecast.setCompany(company);
		cashFlowForecast.setBankDetails(bankDetails);
		cashFlowForecast.setTypeSelect(typeSelect);
		cashFlowForecast.setAmount(amount);
		cashFlowForecast.setEstimatedDate(estimatedDate);
		cashFlowForecast.setCashFlowReason(reason);
		cashFlowForecast.setComments(comments);
		cashFlowForecast.setRealizedSelect(realizedSelect);
		
		return cashFlowForecast;
	}
	
}
