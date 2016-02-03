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
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class AnalyticDistributionLineServiceImpl implements AnalyticDistributionLineService{
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected AccountManagementService accountManagementService;
	
	@Override
	public BigDecimal computeAmount(AnalyticDistributionLine analyticDistributionLine){
		BigDecimal amount = BigDecimal.ZERO;
		if(analyticDistributionLine.getInvoiceLine() != null){
			amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getInvoiceLine().getExTaxTotal()
					.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		if(analyticDistributionLine.getMoveLine() != null){
			if(analyticDistributionLine.getMoveLine().getCredit().compareTo(BigDecimal.ZERO) != 0){
				amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getMoveLine().getCredit()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
			}
			else{
				amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getMoveLine().getDebit()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
			}
		}
		return amount;
	}
	
	@Override
	public BigDecimal chooseComputeWay(Context context, AnalyticDistributionLine analyticDistributionLine){
		if(analyticDistributionLine.getInvoiceLine() == null && analyticDistributionLine.getMoveLine() == null){
			if(context.getParentContext().getContextClass() == InvoiceLine.class){
				analyticDistributionLine.setInvoiceLine(context.getParentContext().asType(InvoiceLine.class));
			}
			else{
				analyticDistributionLine.setMoveLine(context.getParentContext().asType(MoveLine.class));
			}
		}
		return this.computeAmount(analyticDistributionLine);
	}
	
	@Override
	public List<AnalyticDistributionLine> generateLines(Partner partner, Product product, Company company,BigDecimal total) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = new ArrayList<AnalyticDistributionLine>();
		if(generalService.getGeneral().getAnalyticDistributionTypeSelect() == GeneralRepository.DISTRIBUTION_TYPE_PARTNER){
			analyticDistributionLineList = this.generateLinesFromPartner(partner, total);
		}
		else if(generalService.getGeneral().getAnalyticDistributionTypeSelect() == GeneralRepository.DISTRIBUTION_TYPE_PRODUCT){
			analyticDistributionLineList = this.generateLinesFromProduct(product, company, total);
		}
		return analyticDistributionLineList;
	}
	
	@Override
	public List<AnalyticDistributionLine> generateLinesFromPartner(Partner partner, BigDecimal total){
		List<AnalyticDistributionLine> analyticDistributionLineList = new ArrayList<AnalyticDistributionLine>();
		AnalyticDistributionTemplate analyticDistributionTemplate= partner.getAnalyticDistributionTemplate();
		if(analyticDistributionTemplate != null){
			for (AnalyticDistributionLine analyticDistributionLineIt : analyticDistributionTemplate.getAnalyticDistributionLineList()) {
				AnalyticDistributionLine analyticDistributionLine = new AnalyticDistributionLine();
				analyticDistributionLine.setAmount(analyticDistributionLineIt.getPercentage().multiply(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
				analyticDistributionLine.setAnalyticAccount(analyticDistributionLineIt.getAnalyticAccount());
				analyticDistributionLine.setAnalyticAxis(analyticDistributionLineIt.getAnalyticAxis());
				analyticDistributionLine.setAnalyticJournal(analyticDistributionLineIt.getAnalyticJournal());
				analyticDistributionLine.setDate(generalService.getTodayDate());
				analyticDistributionLine.setPercentage(analyticDistributionLineIt.getPercentage());
				analyticDistributionLineList.add(analyticDistributionLine);
			}
		}
		return analyticDistributionLineList;
	}
	
	@Override
	public List<AnalyticDistributionLine> generateLinesFromProduct(Product product, Company company, BigDecimal total) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = new ArrayList<AnalyticDistributionLine>();
		AnalyticDistributionTemplate analyticDistributionTemplate = null;
		AccountManagement accountManagement= accountManagementService.getAccountManagement(product, company);
		if(accountManagement != null){
			analyticDistributionTemplate = accountManagement.getAnalyticDistributionTemplate();
		}
		if(analyticDistributionTemplate != null){
			for (AnalyticDistributionLine analyticDistributionLineIt : analyticDistributionTemplate.getAnalyticDistributionLineList()) {
				AnalyticDistributionLine analyticDistributionLine = new AnalyticDistributionLine();
				analyticDistributionLine.setAmount(analyticDistributionLineIt.getPercentage().multiply(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
				analyticDistributionLine.setAnalyticAccount(analyticDistributionLineIt.getAnalyticAccount());
				analyticDistributionLine.setAnalyticAxis(analyticDistributionLineIt.getAnalyticAxis());
				analyticDistributionLine.setAnalyticJournal(analyticDistributionLineIt.getAnalyticJournal());
				analyticDistributionLine.setDate(generalService.getTodayDate());
				analyticDistributionLine.setPercentage(analyticDistributionLineIt.getPercentage());
				analyticDistributionLineList.add(analyticDistributionLine);
			}
		}
		return analyticDistributionLineList;
	}
	
	@Override
	public List<AnalyticDistributionLine> generateLinesWithTemplate(AnalyticDistributionTemplate template, BigDecimal total){
		List<AnalyticDistributionLine> analyticDistributionLineList = new ArrayList<AnalyticDistributionLine>();
		if(template != null){
			for (AnalyticDistributionLine analyticDistributionLineIt : template.getAnalyticDistributionLineList()) {
				AnalyticDistributionLine analyticDistributionLine = new AnalyticDistributionLine();
				analyticDistributionLine.setAmount(analyticDistributionLineIt.getPercentage().multiply(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
				analyticDistributionLine.setAnalyticAccount(analyticDistributionLineIt.getAnalyticAccount());
				analyticDistributionLine.setAnalyticAxis(analyticDistributionLineIt.getAnalyticAxis());
				analyticDistributionLine.setAnalyticJournal(analyticDistributionLineIt.getAnalyticJournal());
				analyticDistributionLine.setDate(generalService.getTodayDate());
				analyticDistributionLine.setPercentage(analyticDistributionLineIt.getPercentage());
				analyticDistributionLineList.add(analyticDistributionLine);
			}
		}
		return analyticDistributionLineList;
	}
	
	@Override
	public boolean validateLines(List<AnalyticDistributionLine> analyticDistributionLineList){
		if(analyticDistributionLineList != null){
			Map<AnalyticAxis, BigDecimal> map = new HashMap<AnalyticAxis, BigDecimal>();
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				if(map.containsKey(analyticDistributionLine.getAnalyticAxis())){
					map.put(analyticDistributionLine.getAnalyticAxis(), map.get(analyticDistributionLine.getAnalyticAxis()).add(analyticDistributionLine.getPercentage()));
				}
				else{
					map.put(analyticDistributionLine.getAnalyticAxis(), analyticDistributionLine.getPercentage());
				}
			}
			for (AnalyticAxis analyticAxis : map.keySet()) {
				if(map.get(analyticAxis).compareTo(new BigDecimal(100)) > 0){
					return false;
				}
			}
		}
		return true;
	}

}
