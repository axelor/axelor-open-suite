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
package com.axelor.apps.production.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.supplychain.service.StockRulesServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class StockRulesServiceProductionImpl extends StockRulesServiceSupplychainImpl {

	public void generateOrder(Product product, BigDecimal qty, LocationLine locationLine, int type) throws AxelorException {
		StockLocation location = locationLine.getLocation();
		if (location == null) {return;}
		StockRules stockRules = this.getStockRules(product, location, type, StockRulesRepository.USE_CASE_STOCK_CONTROL);
		if (stockRules == null) {return;}
	    if (stockRules.getOrderAlertSelect() == StockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER) {
	    	this.generateProductionOrder(product, qty, locationLine, type, stockRules);
		}
		else {
			this.generatePurchaseOrder(product, qty, locationLine, type);
		}
    }

    public void generateProductionOrder(Product product, BigDecimal qty, LocationLine locationLine, int type, StockRules stockRules) throws AxelorException {
		if(this.useMinStockRules(locationLine, this.getStockRules(product, locationLine.getLocation(), type, StockRulesRepository.USE_CASE_STOCK_CONTROL), qty, type)) {
			BigDecimal qtyToProduce = this.getQtyToOrder(qty, locationLine, type, stockRules);
			Beans.get(ProductionOrderService.class).generateProductionOrder(product, null, qtyToProduce, LocalDateTime.now());
		}
	}
}
