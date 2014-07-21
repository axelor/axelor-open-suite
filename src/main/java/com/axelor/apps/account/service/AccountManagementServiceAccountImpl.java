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
package com.axelor.apps.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.exception.AxelorException;

public class AccountManagementServiceAccountImpl extends AccountManagementServiceImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountManagementServiceAccountImpl.class);


	/**
	 * Obtenir le compte comptable d'un produit.
	 * 
	 * @param product
	 * @param company
	 * @param isPurchase
	 * @return
	 * @throws AxelorException 
	 */
	public Account getProductAccount(Product product, Company company, boolean isPurchase) throws AxelorException{
		
		LOG.debug("Obtention du compte comptable pour le produit {} (société : {}, achat ? {})",
			new Object[]{product, company, isPurchase});
		
		return this.getProductAccount(
				this.getAccountManagement(product, company), 
				isPurchase);
			
	}
	
	
	/**
	 * Obtenir le compte comptable d'un produit.
	 * 
	 * @param product
	 * @param company
	 * @param isPurchase
	 * @return
	 */
	public Account getProductAccount(AccountManagement accountManagement, boolean isPurchase){
		
		if(isPurchase)  { return accountManagement.getPurchaseAccount(); }
		else { return accountManagement.getSaleAccount(); }
			
	}
	
	
	
	
	
	
}
