/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplyChainConfigRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppSupplychain;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.db.repo.AppSupplychainRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppSupplychainServiceImpl extends AppBaseServiceImpl implements AppSupplychainService {

  protected AppSupplychainRepository appSupplychainRepo;

  protected CompanyRepository companyRepo;

  protected SupplyChainConfigRepository supplyChainConfigRepo;

  @Inject
  public AppSupplychainServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      AppSupplychainRepository appSupplychainRepo,
      CompanyRepository companyRepo,
      SupplyChainConfigRepository supplyChainConfigRepo) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.appSupplychainRepo = appSupplychainRepo;
    this.companyRepo = companyRepo;
    this.supplyChainConfigRepo = supplyChainConfigRepo;
  }

  @Override
  public AppSupplychain getAppSupplychain() {
    return appSupplychainRepo.all().fetchOne();
  }

  @Override
  @Transactional
  public void generateSupplychainConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.supplyChainConfig is null").fetch();

    for (Company company : companies) {
      SupplyChainConfig supplyChainConfig = new SupplyChainConfig();
      supplyChainConfig.setCompany(company);
      supplyChainConfigRepo.save(supplyChainConfig);
    }
  }
}
