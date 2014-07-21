/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;

public class PurchaseOrderLineServiceSupplychainImpl extends PurchaseOrderLineServiceImpl  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineServiceSupplychainImpl.class); 
	
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		LOG.debug("Création d'une ligne de commande fournisseur pour le produit : {}",
				new Object[] { saleOrderLine.getProductName() });
		
		return super.createPurchaseOrderLine(
				purchaseOrder, 
				saleOrderLine.getProduct(), 
				saleOrderLine.getDescription(), 
//				productVariantService.copyProductVariant(saleOrderLine.getProductVariant(), false), TODO doit disparaître
				null,
				saleOrderLine.getQty(), 
				saleOrderLine.getUnit());
		
	}
	
	@Override
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, Product product, String description, ProductVariant productVariant, BigDecimal qty, Unit unit) throws AxelorException  {
		
		PurchaseOrderLine purchaseOrderLine = super.createPurchaseOrderLine(purchaseOrder, product, description, productVariant, qty, unit);
		
//		purchaseOrderLine.setAmountInvoiced(BigDecimal.ZERO);
//		
//		purchaseOrderLine.setIsInvoiced(false);
//		purchaseOrderLine.setAmountRemainingToBeInvoiced(purchaseOrderLine.getExTaxTotal());
			
		return purchaseOrderLine;
	}
	
	
}
