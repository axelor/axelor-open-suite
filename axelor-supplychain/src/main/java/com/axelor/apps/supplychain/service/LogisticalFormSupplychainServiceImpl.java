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

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.LogisticalFormServiceImpl;

public class LogisticalFormSupplychainServiceImpl extends LogisticalFormServiceImpl
		implements LogisticalFormSupplychainService {

	@Override
	public void addDetailLines(LogisticalForm logisticalForm, StockMove stockMove) {
		if (stockMove.getStockMoveLineList() != null) {
			Map<StockMoveLine, BigDecimal> stockMoveLineMap = getStockMoveLineQtyMap(logisticalForm);

			for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
				BigDecimal qty = stockMoveLineMap.getOrDefault(stockMoveLine, BigDecimal.ZERO);
				SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();

				if (qty.compareTo(stockMoveLine.getRealQty()) >= 0 || saleOrderLine != null
						&& saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_TITLE) {
					continue;
				}

				LogisticalFormLine logisticalFormLine = createDetailLine(logisticalForm, stockMoveLine,
						stockMoveLine.getRealQty().subtract(qty));
				logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
			}
		}
	}

}
