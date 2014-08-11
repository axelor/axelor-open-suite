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
package com.axelor.apps.stock.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.Location;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class StockConfigService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockConfigService.class);

	
	public StockConfig getStockConfig(Company company) throws AxelorException  {
		
		StockConfig stockConfig = company.getStockConfig();
		
		if(stockConfig == null)  {
			throw new AxelorException(String.format("Veuillez configurer le module stock pour la société %s",
					company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return stockConfig;
		
	}
	
	
	
	/******************************** LOCATION ********************************************/
	
	public Location getInventoryVirtualLocation(StockConfig stockConfig) throws AxelorException  {
		
		if(stockConfig.getInventoryVirtualLocation() == null)  {
			throw new AxelorException(String.format("Veuillez configurer un Emplacement Virtuel Inventaire pour la société %s",
					stockConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return stockConfig.getInventoryVirtualLocation();
		
	}
	
	public Location getSupplierVirtualLocation(StockConfig stockConfig) throws AxelorException  {
		
		if(stockConfig.getSupplierVirtualLocation() == null)  {
			throw new AxelorException(String.format("Veuillez configurer un Emplacement Virtuel Fournisseur pour la société %s",
					stockConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return stockConfig.getSupplierVirtualLocation();
		
	}
	
	public Location getCustomerVirtualLocation(StockConfig stockConfig) throws AxelorException  {
		
		if(stockConfig.getCustomerVirtualLocation() == null)  {
			throw new AxelorException(String.format("Veuillez configurer un Emplacement Virtuel Client pour la société %s",
					stockConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return stockConfig.getCustomerVirtualLocation();
		
	}
	
}
