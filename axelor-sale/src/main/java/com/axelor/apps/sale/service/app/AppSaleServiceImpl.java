package com.axelor.apps.sale.service.app;

import com.axelor.apps.base.db.AppSale;
import com.axelor.apps.base.db.repo.AppSaleRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppSaleServiceImpl extends AppBaseServiceImpl implements AppSaleService {
	
	private Long appSaleId;
	
	@Inject
	public AppSaleServiceImpl() {
		AppSale appSale = Beans.get(AppSaleRepository.class).all().fetchOne();
		if (appSale != null) {
			appSaleId = appSale.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppSale getAppSale() {
		return Beans.get(AppSaleRepository.class).find(appSaleId);
	}
	
}
