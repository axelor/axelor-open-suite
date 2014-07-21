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
package com.axelor.apps.sale.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class SaleConfigService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SaleConfigService.class);

	
	public SaleConfig getSaleConfig(Company company) throws AxelorException  {
		
		SaleConfig saleConfig = company.getSaleConfig();
		
		if(saleConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer le module vente pour la société %s",
					company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return saleConfig;
		
	}
	
	
	
	
}
