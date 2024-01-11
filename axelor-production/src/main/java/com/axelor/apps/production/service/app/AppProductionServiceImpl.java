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
package com.axelor.apps.production.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppProduction;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppProductionServiceImpl extends AppBaseServiceImpl implements AppProductionService {

  public static final int DEFAULT_NB_DECIMAL_DIGITS = 2;

  @Inject
  public AppProductionServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
  }

  @Override
  public AppProduction getAppProduction() {
    return Query.of(AppProduction.class).fetchOne();
  }

  @Override
  @Transactional
  public void generateProductionConfigurations() {

    List<Company> companies =
        Query.of(Company.class).filter("self.productionConfig is null").fetch();

    for (Company company : companies) {
      ProductionConfig productionConfig = new ProductionConfig();
      productionConfig.setCompany(company);
      Beans.get(ProductionConfigRepository.class).save(productionConfig);
    }
  }

  @Override
  public int getNbDecimalDigitForBomQty() {

    AppProduction appProduction = getAppProduction();

    if (appProduction != null) {
      return appProduction.getNbDecimalDigitForBomQty();
    }

    return DEFAULT_NB_DECIMAL_DIGITS;
  }

  @Override
  @Transactional
  public void updatePartnerSubcontractTag() {
    JPA.em()
        .createQuery(
            "UPDATE Partner self SET self.isSubcontractor = FALSE WHERE self.isSubcontractor IS TRUE")
        .executeUpdate();
  }
}
