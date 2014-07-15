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
package com.axelor.apps.purchase.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class PurchaseConfigService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseConfigService.class);

	
	public PurchaseConfig getPurchaseConfig(Company company) throws AxelorException  {
		
		PurchaseConfig purchaseConfig = company.getPurchaseConfig();
		
		if(purchaseConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer le module Achat pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return purchaseConfig;
		
	}
	
	
}
