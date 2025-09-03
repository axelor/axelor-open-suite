/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.loader.AppVersionService;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppSaleServiceImpl extends AppBaseServiceImpl implements AppSaleService {

  protected AppSaleRepository appSaleRepo;

  protected CompanyRepository companyRepo;

  protected SaleConfigRepository saleConfigRepo;

  @Inject
  public AppSaleServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      AppSettingsStudioService appSettingsService,
      MetaModuleRepository metaModuleRepo,
      MetaFileRepository metaFileRepo,
      AppSaleRepository appSaleRepo,
      CompanyRepository companyRepo,
      SaleConfigRepository saleConfigRepo) {
    super(appRepo, metaFiles, appVersionService, appSettingsService, metaModuleRepo, metaFileRepo);
    this.appSaleRepo = appSaleRepo;
    this.companyRepo = companyRepo;
    this.saleConfigRepo = saleConfigRepo;
  }

  @Override
  public AppSale getAppSale() {
    return appSaleRepo.all().fetchOne();
  }

  @Override
  @Transactional
  public void generateSaleConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.saleConfig is null").fetch();

    for (Company company : companies) {
      SaleConfig saleConfig = new SaleConfig();
      saleConfig.setCompany(company);
      saleConfigRepo.save(saleConfig);
    }
  }
}
