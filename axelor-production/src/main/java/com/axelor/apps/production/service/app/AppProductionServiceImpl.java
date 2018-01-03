/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
