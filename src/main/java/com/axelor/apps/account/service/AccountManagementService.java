package com.axelor.apps.account.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class AccountManagementService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountManagementService.class);

	@Inject
	private VatService vs;
	
	public AccountManagementService() {
		
		this.vs = new VatService();
		
	}	
	

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
				LOG.debug("Obtention de la configuration comptable {} => société: {}",	company.getName());
				
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
	 * Obtenir le compte comptable d'une TVA.
	 * 
	 * @param product
	 * @param company
	 * @param isPurchase
	 * @return
	 * @throws AxelorException 
	 */
	public Vat getProductVat(Product product, Company company, boolean isPurchase) throws AxelorException{
		
		LOG.debug("Obtention du compte comptable pour le produit {} (société : {}, achat ? {})",
			new Object[]{product.getCode(), company, isPurchase});
		
		return this.getProductVat(
				this.getAccountManagement(product, company), 
				isPurchase);
			
	}
	
	
	/**
	 * Obtenir le compte comptable d'une TVA.
	 * 
	 * @param product
	 * @param company
	 * @param isPurchase
	 * @return
	 */
	public Vat getProductVat(AccountManagement accountManagement, boolean isPurchase){
		
		if(isPurchase)  { return accountManagement.getPurchaseVat(); }
		else { return accountManagement.getSaleVat(); }
			
	}
	
	
	
	/**
	 * Obtenir la version de TVA d'un produit.
	 * 
	 * @param product
	 * @param amendment
	 * @return
	 * @throws AxelorException 
	 */
	public VatLine getVatLine(LocalDate date, Product product, Company company, boolean isPurchase) throws AxelorException {

		VatLine vatLine = vs.getVatLine(this.getProductVat(product, company, isPurchase), date);
		if(vatLine != null)  {
			return vatLine;
		}

		throw new AxelorException(String.format("Aucune TVA trouvée pour le produit %s", product.getCode()), IException.CONFIGURATION_ERROR);
		
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
	public Account getAccount(Tax tax, Company company, boolean isPurchase) throws AxelorException{
		
		LOG.debug("Obtention du compte comptable pour la taxe {} (société : {}, achat ? {})",
			new Object[]{tax, company, isPurchase});
		
		AccountManagement accountManagement = this.getAccountManagement(tax.getAccountManagementList(), company);
		
		if (accountManagement == null)  {
			throw new AxelorException(String.format("Configuration comptable absente de la taxe : %s (société : %s)", tax.getName(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return this.getProductAccount(accountManagement, isPurchase);
			
	}
	
	
	
	/**
	 * Obtenir la bonne configuration comptable en fonction de la société.
	 * 
	 * @param accountManagements
	 * @param company
	 * @return
	 * @throws AxelorException 
	 */
	public AccountManagement getAccountManagement(Tax tax, Company company) throws AxelorException{
		
		
		if (tax.getAccountManagementList() == null || tax.getAccountManagementList().isEmpty())  {
			throw new AxelorException(String.format("Configuration comptable absente de la taxe : %s (société : %s)", tax.getName(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		return this.getAccountManagement(tax.getAccountManagementList(), company);
		
	}
	
	
}
