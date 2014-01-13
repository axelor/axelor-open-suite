/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class AccountManagementService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountManagementService.class);

	@Inject
	private TaxService taxService;
	
	@Inject
	private FiscalPositionService fiscalPositionService;
	

	/**
	 * Obtenir la bonne configuration comptable en fonction du produit et de la société.
	 * 
	 * @param product
	 * @param company
	 * @return
	 * @throws AxelorException 
	 */
	public AccountManagement getAccountManagement(Product product, Company company) throws AxelorException{
		
		AccountManagement accountManagement = null;
		
		if (product.getAccountManagementList() != null && !product.getAccountManagementList().isEmpty())  {
			accountManagement = this.getAccountManagement(product.getAccountManagementList(), company);
		}
		
		if (accountManagement == null && product.getProductFamily() != null) {
			accountManagement = this.getAccountManagement(product.getProductFamily(), company);
		}
		
		if (accountManagement == null)  {
			throw new AxelorException(String.format("Configuration comptable absente du produit : %s (société : %s)", product.getCode(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountManagement;
		
	}
	
	
	/**
	 * Obtenir la bonne configuration comptable en fonction de la famille de produit et de la société
	 * 
	 * @param productFamily
	 * @param company
	 * @return
	 * @throws AxelorException 
	 */
	public AccountManagement getAccountManagement(ProductFamily productFamily, Company company) throws AxelorException{
		
		if(productFamily.getAccountManagementList() != null && !productFamily.getAccountManagementList().isEmpty())  {
			return this.getAccountManagement(productFamily.getAccountManagementList(), company);
		}
		
		return null;
		
	}
	
	
	
	/**
	 * Obtenir la bonne configuration comptable en fonction de la société.
	 * 
	 * @param accountManagements
	 * @param company
	 * @return
	 */
	public AccountManagement getAccountManagement(List<AccountManagement> accountManagements, Company company){
		
		for (AccountManagement accountManagement : accountManagements){
			if (accountManagement.getCompany().equals(company)){
				LOG.debug("Obtention de la configuration comptable => société: {}",	company.getName());
				
				return accountManagement;
			}
		}
		return null;
		
	}
	
	
	
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
	
	
	
	/**
	 * Obtenir le compte comptable d'une taxe.
	 * 
	 * @param product
	 * @param company
	 * @param isPurchase
	 * @return
	 * @throws AxelorException 
	 */
	public Tax getProductTax(Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase) throws AxelorException{
		
		LOG.debug("Obtention du compte comptable pour le produit {} (société : {}, achat ? {})",
			new Object[]{product.getCode(), company.getName(), isPurchase});
		
		return fiscalPositionService.getTax(
					fiscalPosition,
					this.getProductTax(
						this.getAccountManagement(product, company), 
						isPurchase));
			
	}
	
	
	/**
	 * Obtenir le compte comptable d'une taxe.
	 * 
	 * @param product
	 * @param company
	 * @param isPurchase
	 * @return
	 */
	public Tax getProductTax(AccountManagement accountManagement, boolean isPurchase){
		
		if(isPurchase)  { return accountManagement.getPurchaseTax(); }
		else { return accountManagement.getSaleTax(); }
			
	}
	
	
	
	/**
	 * Obtenir la version de taxe d'un produit.
	 * 
	 * @param product
	 * @param amendment
	 * @return
	 * @throws AxelorException 
	 */
	public TaxLine getTaxLine(LocalDate date, Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase) throws AxelorException {

		TaxLine taxLine = taxService.getTaxLine(this.getProductTax(product, company, fiscalPosition, isPurchase), date);
		if(taxLine != null)  {
			return taxLine;
		}

		throw new AxelorException(String.format("Aucune taxe trouvée pour le produit %s", product.getCode()), IException.CONFIGURATION_ERROR);
		
	}
	
	
}
