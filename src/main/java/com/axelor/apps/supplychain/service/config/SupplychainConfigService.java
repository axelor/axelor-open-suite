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
package com.axelor.apps.supplychain.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.SupplychainConfig;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class SupplychainConfigService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SupplychainConfigService.class);

	
	public SupplychainConfig getSupplychainConfig(Company company) throws AxelorException  {
		
		SupplychainConfig supplychainConfig = company.getSupplychainConfig();
		
		if(supplychainConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer la chaîne d'approvisionnement pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return supplychainConfig;
		
	}
	
	
	
	/******************************** LOCATION ********************************************/
	
	public Location getInventoryVirtualLocation(SupplychainConfig supplychainConfig) throws AxelorException  {
		
		if(supplychainConfig.getInventoryVirtualLocation() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Emplacement Virtuel Inventaire pour la société %s",
					GeneralService.getExceptionAccountingMsg(), supplychainConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return supplychainConfig.getInventoryVirtualLocation();
		
	}
	
	public Location getSupplierVirtualLocation(SupplychainConfig supplychainConfig) throws AxelorException  {
		
		if(supplychainConfig.getSupplierVirtualLocation() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Emplacement Virtuel Fournisseur pour la société %s",
					GeneralService.getExceptionAccountingMsg(), supplychainConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return supplychainConfig.getSupplierVirtualLocation();
		
	}
	
	public Location getCustomerVirtualLocation(SupplychainConfig supplychainConfig) throws AxelorException  {
		
		if(supplychainConfig.getCustomerVirtualLocation() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Emplacement Virtuel Client pour la société %s",
					GeneralService.getExceptionAccountingMsg(), supplychainConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return supplychainConfig.getCustomerVirtualLocation();
		
	}
	
}
