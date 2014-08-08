/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.stock.db.IMinStockRules;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MinStockRulesServiceImpl implements MinStockRulesService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(MinStockRulesServiceImpl.class); 

	@Inject
	private StockConfigService stockConfigService;
	
	private LocalDate today;
	
	@Inject
	public MinStockRulesServiceImpl() {

		this.today = GeneralService.getTodayDate();
		this.user = AuthUtils.getUser();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generatePurchaseOrder(Product product, BigDecimal qty, LocationLine locationLine, int type) throws AxelorException  {
		
		Location location = locationLine.getLocation();
		
		//TODO à supprimer après suppression des variantes
		if(location == null)  {
			return;
		}
		
		MinStockRules minStockRules = this.getMinStockRules(product, location, type);
		
		if(minStockRules == null)  {
			return;
		}
		
		if(this.useMinStockRules(locationLine, minStockRules, qty, type))  {
			
			if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_ALERT)  {
				
				//TODO
			}
			
			
		}
		
	}
	
	
	public boolean useMinStockRules(LocationLine locationLine, MinStockRules minStockRules, BigDecimal qty, int type)  {
		
		BigDecimal currentQty = locationLine.getCurrentQty();
		BigDecimal futureQty = locationLine.getFutureQty();
		
		BigDecimal minQty = minStockRules.getMinQty();
		
		if(type == IMinStockRules.TYPE_CURRENT)  {
			
			if(currentQty.compareTo(minQty) >= 0 && (currentQty.subtract(qty)).compareTo(minQty) == -1)  {
				return true;
			}
			
		}
		else  if(type == IMinStockRules.TYPE_FUTURE){
			
			if(futureQty.compareTo(minQty) >= 0 && (futureQty.subtract(qty)).compareTo(minQty) == -1)  {
				return true;
			}
			
		}
		return false;
		
	}
	
	public MinStockRules getMinStockRules(Product product, Location location, int type)  {
		
		return MinStockRules.filter("self.product = ?1 AND self.location = ?2 AND self.typeSelect = ?3", product, location, type).fetchOne();
		
		//TODO , plusieurs régles min de stock par produit (achat a 500 et production a 100)...
		
	}
	
	
}
