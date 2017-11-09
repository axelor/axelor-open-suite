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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.tool.StringTool;
import com.axelor.inject.Beans;

public class LogisticalFormLineServiceImpl implements LogisticalFormLineService {

	@Override
	public BigDecimal getRemainingQty(LogisticalFormLine logisticalFormLine) {
		LogisticalForm logisticalForm = logisticalFormLine.getLogisticalForm();
		StockMoveLine stockMoveLine = logisticalFormLine.getStockMoveLine();

		if (logisticalForm == null || stockMoveLine == null) {
			return BigDecimal.ZERO;
		}

		Map<StockMoveLine, BigDecimal> stockMoveLineMap = Beans.get(LogisticalFormService.class)
				.getStockMoveLineQtyMap(logisticalForm);
		BigDecimal qty = stockMoveLineMap.getOrDefault(stockMoveLine, BigDecimal.ZERO);
		return stockMoveLine.getRealQty().subtract(qty);
	}

	@Override
	public String getStockMoveLineDomain(LogisticalFormLine logisticalFormLine) {
		long partnerId = 0;
		List<String> domainList = new ArrayList<>();
		LogisticalForm logisticalForm = logisticalFormLine.getLogisticalForm();

		if (logisticalForm != null) {
			Partner deliverToCustomer = logisticalForm.getDeliverToCustomer();

			if (deliverToCustomer != null) {
				partnerId = deliverToCustomer.getId();
			}
		}

		domainList.add(String.format("self.stockMove.partner.id = %d", partnerId));

		List<StockMoveLine> fullySpreadStockMoveLineList = Beans.get(LogisticalFormService.class)
				.getFullySpreadStockMoveLineList(logisticalForm);

		if (!fullySpreadStockMoveLineList.isEmpty()) {
			String idListString = StringTool.getIdListString(fullySpreadStockMoveLineList);
			domainList.add(String.format("self.id NOT IN (%s)", idListString));
		}

		return domainList.stream().map(domain -> String.format("(%s)", domain)).collect(Collectors.joining(" AND "));
	}

}
