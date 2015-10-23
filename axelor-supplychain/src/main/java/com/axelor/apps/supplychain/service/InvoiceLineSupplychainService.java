package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.google.inject.Inject;

public class InvoiceLineSupplychainService extends InvoiceLineService{
	
	@Inject
	public InvoiceLineSupplychainService(AccountManagementService accountManagementService, CurrencyService currencyService, PriceListService priceListService, GeneralService generalService, AnalyticDistributionLineService analyticDistributionLineService)  {
		
		super(accountManagementService, currencyService, priceListService, generalService, analyticDistributionLineService);
		
	}
	
	@Override
	public Unit getUnit(Product product, boolean isPurchase){
		if(isPurchase){
			if(product.getPurchasesUnit() != null){
				return product.getPurchasesUnit();
			}
			else{
				return product.getUnit();
			}
		}
		else{
			if(product.getSalesUnit() != null){
				return product.getPurchasesUnit();
			}
			else{
				return product.getUnit();
			}
		}
	}
}
