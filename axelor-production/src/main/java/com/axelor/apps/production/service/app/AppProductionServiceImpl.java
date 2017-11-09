package com.axelor.apps.production.service.app;

import java.util.List;

import com.axelor.apps.base.db.AppProduction;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.AppProductionRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class AppProductionServiceImpl extends AppBaseServiceImpl implements AppProductionService {
	
	@Inject
	private AppProductionRepository appProductionRepo;
	
	@Inject
	private CompanyRepository companyRepo;
	
	@Inject
	private ProductionConfigRepository productionConfigRepo;
	
	@Override
	public AppProduction getAppProduction() {
		return appProductionRepo.all().fetchOne();
	}

	@Override
	@Transactional
	public void generateProductionConfigurations() {
		
		List<Company> companies = companyRepo.all().filter("self.productionConfig is null").fetch();
		
		for (Company company : companies) {
			ProductionConfig productionConfig = new ProductionConfig();
			productionConfig.setCompany(company);
			productionConfigRepo.save(productionConfig);
		}
	}

}
