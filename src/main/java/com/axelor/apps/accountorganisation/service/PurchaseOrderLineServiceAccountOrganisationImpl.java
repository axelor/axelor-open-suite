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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.exception.AxelorException;

public class PurchaseOrderLineServiceAccountOrganisationImpl extends PurchaseOrderLineServiceSupplychainImpl  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineServiceAccountOrganisationImpl.class); 
	
	@Override
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		LOG.debug("Création d'une ligne de commande fournisseur pour le produit : {}",
				new Object[] { saleOrderLine.getProductName() });
		
		PurchaseOrderLine purchaseOrderLine = super.createPurchaseOrderLine(
				purchaseOrder, 
				saleOrderLine.getProduct(), 
				saleOrderLine.getDescription(), 
//				productVariantService.copyProductVariant(saleOrderLine.getProductVariant(), false), TODO doit disparaître
				null,
				saleOrderLine.getQty(), 
				saleOrderLine.getUnit());
		
		purchaseOrderLine.setTask(saleOrderLine.getTask());
		
		return purchaseOrderLine;
		
	}
	
	@Override
	public BigDecimal getSalePrice(PurchaseOrder purchaseOrder, BigDecimal price) throws AxelorException  {
		
		Project project = purchaseOrder.getProject();
		
		if(project != null)  {
			
			return project.getMarginCoef().multiply(price);
			
		}
		
		return price;  
		
	}
	
	
}
