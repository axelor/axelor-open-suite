/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;

public interface SaleOrderStockService {

	enum StockMoveDeliveryStatus {
		NOT_DELIVERY, PARTIAL_DELIVERY, ALL_DELIVERY
	}

	public Location getLocation(Company company);

	/**
	 * Create a delivery stock move from a sale order.
	 * 
	 * @param saleOrder
	 * @return
	 * @throws AxelorException
	 */
	public StockMove createStocksMovesFromSaleOrder(SaleOrder saleOrder) throws AxelorException;

	public StockMove createStockMove(SaleOrder saleOrder, Company company) throws AxelorException;
	
	public StockMoveLine createStockMoveLine(StockMove stockMove, SaleOrderLine saleOrderLine, Company company) throws AxelorException;

	public boolean isStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException;

	/**
	 * Check whether there is at least one stock move not canceled for the sale order.
	 * 
	 * @param saleOrder
	 * @return
	 */
	public boolean existActiveStockMoveForSaleOrder(SaleOrder saleOrder);

	StockMoveDeliveryStatus checkAllSaleOrderLineIsDelivery(SaleOrder saleOrder);
}



