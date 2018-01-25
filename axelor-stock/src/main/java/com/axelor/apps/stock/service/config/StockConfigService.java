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
package com.axelor.apps.stock.service.config;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class StockConfigService
{
	
	public StockConfig getStockConfig(Company company) throws AxelorException  {
		
		StockConfig stockConfig = company.getStockConfig();
		
		if (stockConfig == null) {
			throw new AxelorException(company, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_1), company.getName());
		}
		
		return stockConfig;
		
	}
	
	
	
	/******************************** STOCK LOCATION ********************************************/
	
	public StockLocation getInventoryVirtualStockLocation(StockConfig stockConfig) throws AxelorException  {
		
		if (stockConfig.getInventoryVirtualStockLocation() == null) {
			throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_2), stockConfig.getCompany().getName());
		}
		
		return stockConfig.getInventoryVirtualStockLocation();
		
	}
	
	public StockLocation getSupplierVirtualStockLocation(StockConfig stockConfig) throws AxelorException  {
		
		if (stockConfig.getSupplierVirtualStockLocation() == null) {
			throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_3), stockConfig.getCompany().getName());
		}
		
		return stockConfig.getSupplierVirtualStockLocation();
		
	}
	
	public StockLocation getCustomerVirtualStockLocation(StockConfig stockConfig) throws AxelorException  {
		
		if (stockConfig.getCustomerVirtualStockLocation() == null) {
			throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_4), stockConfig.getCompany().getName());
		}
		
		return stockConfig.getCustomerVirtualStockLocation();
		
	}

	public StockLocation getDefaultStockLocation(StockConfig stockConfig) throws AxelorException  {

		if (stockConfig.getDefaultStockLocation() == null) {
			throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_5), stockConfig.getCompany().getName());
		}

		return stockConfig.getDefaultStockLocation();

	}
}
