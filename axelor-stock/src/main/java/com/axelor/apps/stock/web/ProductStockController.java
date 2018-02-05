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
package com.axelor.apps.stock.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
		
		List<Map<String,Object>> stocks = stockMoveService.getStockPerDate(locationId, productId, fromDate, toDate);
		response.setValue("$stockPerDayList", stocks);
		
	}
}
