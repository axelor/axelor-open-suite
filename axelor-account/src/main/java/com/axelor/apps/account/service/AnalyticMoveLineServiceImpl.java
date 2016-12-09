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
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class AnalyticMoveLineServiceImpl implements AnalyticMoveLineService{
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected AccountManagementService accountManagementService;
	
	protected AnalyticMoveLineRepository analyticMoveLineRepository;
	
	@Inject
	public AnalyticMoveLineServiceImpl(GeneralService generalService, AnalyticMoveLineRepository analyticMoveLineRepository){
		
		this.generalService = generalService;
		this.analyticMoveLineRepository = analyticMoveLineRepository;
	}
	
	
	@Override
	public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine){
		BigDecimal amount = BigDecimal.ZERO;
		if(analyticMoveLine.getInvoiceLine() != null){
			amount = analyticMoveLine.getPercentage().multiply(analyticMoveLine.getInvoiceLine().getExTaxTotal()
					.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		if(analyticMoveLine.getMoveLine() != null){
			if(analyticMoveLine.getMoveLine().getCredit().compareTo(BigDecimal.ZERO) != 0){
				amount = analyticMoveLine.getPercentage().multiply(analyticMoveLine.getMoveLine().getCredit()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
			}
			else{
				amount = analyticMoveLine.getPercentage().multiply(analyticMoveLine.getMoveLine().getDebit()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
			}
		}
		return amount;
	}
	
	@Override
	public BigDecimal chooseComputeWay(Context context, AnalyticMoveLine analyticMoveLine){
		if(analyticMoveLine.getInvoiceLine() == null && analyticMoveLine.getMoveLine() == null){
			if(context.getParentContext().getContextClass() == InvoiceLine.class){
				analyticMoveLine.setInvoiceLine(context.getParentContext().asType(InvoiceLine.class));
			}
			else{
				analyticMoveLine.setMoveLine(context.getParentContext().asType(MoveLine.class));
			}
		}
		return this.computeAmount(analyticMoveLine);
	}
	
	@Override
	public List<AnalyticMoveLine> generateLines(Partner partner, Product product, Company company,BigDecimal total) throws AxelorException{
		List<AnalyticMoveLine> analyticDistributionLineList = new ArrayList<AnalyticMoveLine>();
		if(generalService.getGeneral().getAnalyticDistributionTypeSelect() == GeneralRepository.DISTRIBUTION_TYPE_PARTNER){
			analyticDistributionLineList = this.generateLinesFromPartner(partner, total);
		}
		else if(generalService.getGeneral().getAnalyticDistributionTypeSelect() == GeneralRepository.DISTRIBUTION_TYPE_PRODUCT){
			analyticDistributionLineList = this.generateLinesFromProduct(product, company, total);
		}
		return analyticDistributionLineList;
	}
	
	@Override
	public List<AnalyticMoveLine> generateLinesFromPartner(Partner partner, BigDecimal total){
		List<AnalyticMoveLine> analyticDistributionLineList = new ArrayList<AnalyticMoveLine>();
		AnalyticDistributionTemplate analyticDistributionTemplate= partner.getAnalyticDistributionTemplate();
		if(analyticDistributionTemplate != null){
			for (AnalyticMoveLine analyticDistributionLineIt : analyticDistributionTemplate.getAnalyticMoveLineList()) {
				AnalyticMoveLine analyticDistributionLine = new AnalyticMoveLine();
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
	public List<AnalyticMoveLine> generateLinesFromProduct(Product product, Company company, BigDecimal total) throws AxelorException{
		List<AnalyticMoveLine> analyticDistributionLineList = new ArrayList<AnalyticMoveLine>();
		AnalyticDistributionTemplate analyticDistributionTemplate = null;
		AccountManagement accountManagement= accountManagementService.getAccountManagement(product, company);
		if(accountManagement != null){
			analyticDistributionTemplate = accountManagement.getAnalyticDistributionTemplate();
		}
		if(analyticDistributionTemplate != null){
			for (AnalyticMoveLine analyticDistributionLineIt : analyticDistributionTemplate.getAnalyticMoveLineList()) {
				AnalyticMoveLine analyticDistributionLine = new AnalyticMoveLine();
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
	public List<AnalyticMoveLine> generateLinesWithTemplate(AnalyticDistributionTemplate template, BigDecimal total){
		List<AnalyticMoveLine> analyticDistributionLineList = new ArrayList<AnalyticMoveLine>();
		if(template != null){
			for (AnalyticMoveLine analyticDistributionLineIt : template.getAnalyticMoveLineList()) {
				AnalyticMoveLine analyticDistributionLine = new AnalyticMoveLine();
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
	public boolean validateLines(List<AnalyticMoveLine> analyticDistributionLineList){
		if(analyticDistributionLineList != null){
			Map<AnalyticAxis, BigDecimal> map = new HashMap<AnalyticAxis, BigDecimal>();
			for (AnalyticMoveLine analyticDistributionLine : analyticDistributionLineList) {
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
	
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<AnalyticMoveLine> analyticMoveLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(AnalyticMoveLine analyticMoveLine : analyticMoveLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, analyticMoveLine));

		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, AnalyticMoveLine analyticMoveLine) throws AxelorException  {

		Product product = analyticMoveLine.getProduct();
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), product.getSalePrice().multiply(new BigDecimal(-1)),
					product.getSalePrice().multiply(new BigDecimal(-1)),null,analyticMoveLine.getQte(),product.getUnit(), null,10,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					null, null,false)  {

			@Override
			public List<InvoiceLine> creates() throws AxelorException {

				InvoiceLine invoiceLine = this.createInvoiceLine();

				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);

				return invoiceLines;
			}
		};

		return invoiceLineGenerator.creates();
	}

}
