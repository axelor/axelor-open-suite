/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.apps.purchase.db.repo.PurchaseConfigRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppPurchase;
import com.axelor.studio.db.repo.AppPurchaseRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppPurchaseServiceImpl extends AppBaseServiceImpl implements AppPurchaseService {

  protected AppPurchaseRepository appPurchaseRepo;

  protected CompanyRepository companyRepo;

  protected PurchaseConfigRepository purchaseConfigRepo;

  @Inject
  public AppPurchaseServiceImpl(
      AppRepository appRepo, MetaFiles metaFiles, AppVersionService appVersionService) {
    super(appRepo, metaFiles, appVersionService);
  }

  @Override
  public AppPurchase getAppPurchase() {
    return appPurchaseRepo.all().fetchOne();
  }

  @Override
  @Transactional
  public void generatePurchaseConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.purchaseConfig is null").fetch();

    for (Company company : companies) {
      PurchaseConfig purchaseConfig = new PurchaseConfig();
      purchaseConfig.setCompany(company);
      purchaseConfigRepo.save(purchaseConfig);
    }
  }
}
