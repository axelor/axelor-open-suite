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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PurchaseOrderLineServiceSupplychainImpl extends PurchaseOrderLineServiceImpl  {
	
	@Inject
	protected AnalyticDistributionLineService analyticDistributionLineService;
	
	@Inject
	protected UnitConversionService unitConversionService;
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineServiceSupplychainImpl.class); 
	
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		LOG.debug("Création d'une ligne de commande fournisseur pour le produit : {}",
				new Object[] { saleOrderLine.getProductName() });
		
		Unit unit = null;
		BigDecimal qty = BigDecimal.ZERO;
		
		if(!saleOrderLine.getIsTitleLine())  {
			unit = saleOrderLine.getProduct().getPurchasesUnit();
			qty = saleOrderLine.getQty();
			if(unit == null){
				unit = saleOrderLine.getUnit();
			}
			else{
				qty = unitConversionService.convertWithProduct(saleOrderLine.getUnit(), unit, qty, saleOrderLine.getProduct());
			}
		}
		
		PurchaseOrderLine purchaseOrderLine = super.createPurchaseOrderLine(
														purchaseOrder, 
														saleOrderLine.getProduct(), 
														saleOrderLine.getProductName(),
														saleOrderLine.getDescription(), 
														qty, 
														unit);
		
		purchaseOrderLine.setIsTitleLine(saleOrderLine.getIsTitleLine());
		this.computeAnalyticDistribution(purchaseOrderLine);
		return purchaseOrderLine;
		
	}
	
	@Override
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, Product product, String productName, String description, BigDecimal qty, Unit unit) throws AxelorException  {
		
		PurchaseOrderLine purchaseOrderLine = super.createPurchaseOrderLine(purchaseOrder, product, productName, description, qty, unit);
		
//		purchaseOrderLine.setAmountInvoiced(BigDecimal.ZERO);
//		
//		purchaseOrderLine.setIsInvoiced(false);
//		purchaseOrderLine.setAmountRemainingToBeInvoiced(purchaseOrderLine.getExTaxTotal());
			
		return purchaseOrderLine;
	}
	
	public PurchaseOrderLine computeAnalyticDistribution(PurchaseOrderLine purchaseOrderLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = purchaseOrderLine.getAnalyticDistributionLineList();
		if((analyticDistributionLineList == null || analyticDistributionLineList.isEmpty()) && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			analyticDistributionLineList = analyticDistributionLineService.generateLines(purchaseOrderLine.getPurchaseOrder().getSupplierPartner(), purchaseOrderLine.getProduct(), purchaseOrderLine.getPurchaseOrder().getCompany(), purchaseOrderLine.getExTaxTotal());
			if(analyticDistributionLineList != null){
				for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
					analyticDistributionLine.setPurchaseOrderLine(purchaseOrderLine);
					analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
					analyticDistributionLine.setDate(generalService.getTodayDate());
				}
				purchaseOrderLine.setAnalyticDistributionLineList(analyticDistributionLineList);
			}
		}
		else if(analyticDistributionLineList != null && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setPurchaseOrderLine(purchaseOrderLine);
				analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
				analyticDistributionLine.setDate(generalService.getTodayDate());
			}
		}
		return purchaseOrderLine;
	}
	
	public PurchaseOrderLine createAnalyticDistributionWithTemplate(PurchaseOrderLine purchaseOrderLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = null;
		analyticDistributionLineList = analyticDistributionLineService.generateLinesWithTemplate(purchaseOrderLine.getAnalyticDistributionTemplate(), purchaseOrderLine.getExTaxTotal());
		if(analyticDistributionLineList != null){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setPurchaseOrderLine(purchaseOrderLine);
			}
		}
		purchaseOrderLine.setAnalyticDistributionLineList(analyticDistributionLineList);
		return purchaseOrderLine;
	}
	
}
