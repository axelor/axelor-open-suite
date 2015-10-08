/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PurchaseOrderLineServiceSupplychainImpl extends PurchaseOrderLineServiceImpl  {
	
	@Inject
	protected AnalyticDistributionLineService analyticDistributionLineService;
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineServiceSupplychainImpl.class); 
	
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		LOG.debug("Création d'une ligne de commande fournisseur pour le produit : {}",
				new Object[] { saleOrderLine.getProductName() });
		
		return super.createPurchaseOrderLine(
				purchaseOrder, 
				saleOrderLine.getProduct(), 
				saleOrderLine.getDescription(), 
				saleOrderLine.getQty(), 
				saleOrderLine.getUnit());
		
	}
	
	@Override
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, Product product, String description, BigDecimal qty, Unit unit) throws AxelorException  {
		
		PurchaseOrderLine purchaseOrderLine = super.createPurchaseOrderLine(purchaseOrder, product, description, qty, unit);
		
//		purchaseOrderLine.setAmountInvoiced(BigDecimal.ZERO);
//		
//		purchaseOrderLine.setIsInvoiced(false);
//		purchaseOrderLine.setAmountRemainingToBeInvoiced(purchaseOrderLine.getExTaxTotal());
			
		return purchaseOrderLine;
	}
	
	public PurchaseOrderLine computeAnalyticDistribution(PurchaseOrderLine purchaseOrderLine){
		List<AnalyticDistributionLine> analyticDistributionLineList = purchaseOrderLine.getAnalyticDistributionLineList();
		if(analyticDistributionLineList != null){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				if(analyticDistributionLine.getPurchaseOrderLine() == null){
					analyticDistributionLine.setPurchaseOrderLine(purchaseOrderLine);
				}
				analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
				analyticDistributionLine.setDate(generalService.getTodayDate());
			}
		}
		return purchaseOrderLine;
	}
	
}
