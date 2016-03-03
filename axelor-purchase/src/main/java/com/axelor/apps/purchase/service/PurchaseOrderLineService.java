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
package com.axelor.apps.purchase.service;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;

public interface PurchaseOrderLineService{


	public BigDecimal getUnitPrice(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, TaxLine taxLine) throws AxelorException;

	public BigDecimal getMinSalePrice(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

	public BigDecimal getSalePrice(PurchaseOrder purchaseOrder, Product product, BigDecimal price) throws AxelorException;

	public TaxLine getTaxLine(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

	public BigDecimal computePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine);

	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, PurchaseOrder purchaseOrder) throws AxelorException;

	public PriceListLine getPriceListLine(PurchaseOrderLine purchaseOrderLine, PriceList priceList);

	public BigDecimal computeDiscount(PurchaseOrderLine purchaseOrderLine);

	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, Product product, String productName, String description,
			BigDecimal qty, Unit unit) throws AxelorException;

	public BigDecimal getQty(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine);

	public SupplierCatalog getSupplierCatalog(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine);

	public SupplierCatalog getSupplierCatalog(Product product, Partner supplierPartner);

	public BigDecimal convertUnitPrice(Product product, TaxLine taxLine, BigDecimal price, PurchaseOrder purchaseOrder);

	public Map<String,Object> getDiscount(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, BigDecimal price);
	
	public int getDiscountTypeSelect(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder);

	public Unit getPurchaseUnit(PurchaseOrderLine purchaseOrderLine);
	
	public boolean unitPriceShouldBeUpdate(PurchaseOrder purchaseOrder, Product product);


}
