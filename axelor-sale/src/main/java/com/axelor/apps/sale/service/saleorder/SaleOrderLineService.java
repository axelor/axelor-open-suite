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
package com.axelor.apps.sale.service.saleorder;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public interface SaleOrderLineService {


	/**
     * Update all fields of the sale order line from the product.
	 * @param saleOrderLine
	 * @param saleOrder
	 */
	void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

	/**
	 * Compute totals from a sale order line
	 * @param saleOrder
	 * @param saleOrderLine
	 * @return
	 * @throws AxelorException
	 */
	public Map<String, BigDecimal> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException;

	/**
	 * Compute the excluded tax total amount of a sale order line.
	 *
	 * @param quantity
	 *          The quantity.
	 * @param price
	 *          The unit price.
	 * @return
	 * 			The excluded tax total amount.
	 */
	public BigDecimal computeAmount(SaleOrderLine saleOrderLine);
	
	public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price);

	public BigDecimal getUnitPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException;

	public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException;

	public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder) throws AxelorException;

	public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException;

	public PriceListLine getPriceListLine(SaleOrderLine saleOrderLine, PriceList priceList);

	public BigDecimal computeDiscount(SaleOrderLine saleOrderLine);

	public BigDecimal convertUnitPrice(Product product, TaxLine taxLine, BigDecimal price, SaleOrder saleOrder);

	public Map<String,Object> getDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price);

	public int getDiscountTypeSelect(SaleOrder saleOrder, SaleOrderLine saleOrderLine);
	
	public Unit getSaleUnit(SaleOrderLine saleOrderLine);
	
	public boolean unitPriceShouldBeUpdate(SaleOrder saleOrder, Product product);
	
	public BigDecimal computeTotalPack(SaleOrderLine saleOrderLine);
	
	public SaleOrder getSaleOrder(Context context);
	
	public void computeSubMargin(SaleOrderLine saleOrderLine) throws AxelorException;

	public BigDecimal getAvailableStock(SaleOrderLine saleOrderLine);
	
	public void checkMultipleQty(SaleOrderLine saleOrderLine, ActionResponse response);

}
