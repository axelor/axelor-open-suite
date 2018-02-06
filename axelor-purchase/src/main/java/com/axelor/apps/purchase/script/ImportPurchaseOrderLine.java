/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.script;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class ImportPurchaseOrderLine {

	public Object importPurchaseOrderLine(Object bean, Map<String,Object> values) throws AxelorException {
		assert bean instanceof PurchaseOrderLine;

		PurchaseOrderLine purchaseOrderLine = (PurchaseOrderLine) bean;
		PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		BigDecimal companyInTaxTotal = BigDecimal.ZERO;
		BigDecimal priceDiscounted = purchaseOrderLineService.computeDiscount(purchaseOrderLine);
		BigDecimal taxRate = BigDecimal.ZERO;

		if(purchaseOrderLine.getTaxLine() != null)  {  taxRate = purchaseOrderLine.getTaxLine().getValue();  }

		if(!purchaseOrder.getInAti()){
			exTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
			inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
			companyExTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
			companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate));
		}
		else {
			inTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
			exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
			companyInTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(inTaxTotal, purchaseOrder);
			companyExTaxTotal = companyInTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
		}

		purchaseOrderLine.setDiscountAmount(priceDiscounted);
		purchaseOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
		purchaseOrderLine.setCompanyInTaxTotal(companyInTaxTotal);
		purchaseOrderLine.setExTaxTotal(exTaxTotal);
		purchaseOrderLine.setInTaxTotal(inTaxTotal);
		purchaseOrderLine.setSaleMinPrice(purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
		purchaseOrderLine.setSalePrice(purchaseOrderLineService.getSalePrice(purchaseOrder, purchaseOrderLine.getProduct(), purchaseOrderLine.getPrice()));
		return purchaseOrderLine;
	}
}
