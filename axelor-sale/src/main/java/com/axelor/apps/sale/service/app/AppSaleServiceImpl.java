package com.axelor.apps.sale.service.app;

import com.axelor.apps.base.db.AppSale;
import com.axelor.apps.base.db.repo.AppSaleRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppSaleServiceImpl extends AppBaseServiceImpl implements AppSaleService {
	
	@Inject
	private AppSaleRepository appSaleRepo;
	
	@Override
	public AppSale getAppSale() {
		return appSaleRepo.all().fetchOne();
	}
	
}
