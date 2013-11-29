/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
