/**
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
package com.axelor.apps.stock.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class ProductStockController {
	
	@Inject
	private StockMoveService stockMoveService;

	public void setStockPerDay(ActionRequest request, ActionResponse response) {
	
		Context context = request.getContext();
		
		Long productId = Long.parseLong(context.get("id").toString());
		Long locationId = Long.parseLong(context.get("locationId").toString());
		LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
		LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());
		
		Set<Map<String, Object>> stockPerDay = new HashSet<Map<String,Object>>();
		Map<LocalDate, BigDecimal> stockMap = stockMoveService.getStockPerDate(locationId, productId, fromDate, toDate);
		TreeSet<LocalDate> keys = new TreeSet<LocalDate>();
		keys.addAll(stockMap.keySet());
		for (LocalDate date : keys) {
			Map<String, Object> perDateMap = new HashMap<String, Object>();
			perDateMap.put("$date", date);
			perDateMap.put("$qty", stockMap.get(date));
			stockPerDay.add(perDateMap);
		}
		
		response.setValue("$stockPerDaySet", stockPerDay);
		
	}
}
