/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.crm.db.CrmConfig;
import com.axelor.apps.crm.db.repo.CrmConfigRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AppCrmServiceImpl implements AppCrmService {

  @Inject private CompanyRepository companyRepo;

  @Inject private CrmConfigRepository crmConfigRepo;

  @Override
  @Transactional
  public void generateCrmConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.crmConfig is null").fetch();

    for (Company company : companies) {
      CrmConfig crmConfig = new CrmConfig();
      crmConfig.setCompany(company);
      crmConfigRepo.save(crmConfig);
    }
  }
}
