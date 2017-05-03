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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.apps.stock.db.repo.MinStockRulesRepository;
import com.axelor.apps.supplychain.service.MinStockRulesServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MinStockRulesServiceProductionImpl extends MinStockRulesServiceSupplychainImpl {

	public void generateOrder(Product product, BigDecimal qty, LocationLine locationLine, int type) throws AxelorException {
		Location location = locationLine.getLocation();
		if (location == null) {return;}
		MinStockRules minStockRules = this.getMinStockRules(product, location, type);
		if (minStockRules == null) {return;}
	    if (minStockRules.getOrderAlertSelect() == MinStockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER) {
	    	this.generateProductionOrder(product, qty, locationLine, type, minStockRules);
		}
		else {
			this.generatePurchaseOrder(product, qty, locationLine, type);
		}
    }
    public void generateProductionOrder(Product product, BigDecimal qty, LocationLine locationLine, int type, MinStockRules minStockRules) throws AxelorException {
		if(this.useMinStockRules(locationLine, this.getMinStockRules(product, locationLine.getLocation(), type), qty, type)) {
			BigDecimal qtyToProduce = this.getQtyToOrder(qty, locationLine, type, minStockRules);
			Beans.get(ProductionOrderService.class).generateProductionOrder(product, null, qtyToProduce, LocalDateTime.now());
		}
	}
}
