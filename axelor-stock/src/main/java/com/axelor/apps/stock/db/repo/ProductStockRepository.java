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
package com.axelor.apps.stock.db.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;


public class ProductStockRepository extends ProductBaseRepository {
	
	@Inject
	private StockMoveService stockMoveService;
	
	@Override
	public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
		if (!context.containsKey("fromStockWizard")) {
			return json;
		}
		try {
			Long productId = (Long) json.get("id");
			Long locationId = Long.parseLong(context.get("locationId").toString());
			LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
			LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());
			Map<LocalDate, BigDecimal> stockMap = stockMoveService.getStockPerDate(locationId, productId, fromDate, toDate);
			
			if (stockMap != null && !stockMap.isEmpty()) {
				LocalDate minDate = null;
				LocalDate maxDate = null;
				for (LocalDate date : stockMap.keySet()){
					if (minDate == null || stockMap.get(date).compareTo(stockMap.get(minDate)) > 0
							|| stockMap.get(date).compareTo(stockMap.get(minDate)) == 0 && date.isAfter(minDate)) {
						minDate = date;
					}
					if (maxDate == null || stockMap.get(date).compareTo(stockMap.get(maxDate)) < 0
							|| stockMap.get(date).compareTo(stockMap.get(minDate)) == 0 && date.isBefore(maxDate)) {
						maxDate = date;
					}
				}
				json.put("$stockMinDate", minDate);
				json.put("$stockMin", stockMap.get(minDate));
				json.put("$stockMaxDate", maxDate);
				json.put("$stockMax", stockMap.get(maxDate));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}
	
}
