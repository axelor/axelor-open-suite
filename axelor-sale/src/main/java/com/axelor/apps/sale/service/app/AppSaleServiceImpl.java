/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.app;

import com.axelor.apps.base.db.AppSale;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.AppSaleRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppSaleServiceImpl extends AppBaseServiceImpl implements AppSaleService {

  @Inject private AppSaleRepository appSaleRepo;

  @Inject private CompanyRepository companyRepo;

  @Inject private SaleConfigRepository saleConfigRepo;

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
