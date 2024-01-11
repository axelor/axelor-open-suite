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
package com.axelor.apps.account.service.app;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppAccount;
import com.axelor.studio.db.AppBudget;
import com.axelor.studio.db.AppInvoice;
import com.axelor.studio.db.repo.AppAccountRepository;
import com.axelor.studio.db.repo.AppBudgetRepository;
import com.axelor.studio.db.repo.AppInvoiceRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppAccountServiceImpl extends AppBaseServiceImpl implements AppAccountService {

  protected AppAccountRepository appAccountRepo;

  protected AppBudgetRepository appBudgetRepo;

  protected AppInvoiceRepository appInvoiceRepo;

  protected AccountConfigRepository accountConfigRepo;

  protected CompanyRepository companyRepo;

  @Inject
  public AppAccountServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      AppAccountRepository appAccountRepo,
      AppBudgetRepository appBudgetRepo,
      AppInvoiceRepository appInvoiceRepo,
      AccountConfigRepository accountConfigRepo,
      CompanyRepository companyRepo) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.appAccountRepo = appAccountRepo;
    this.appBudgetRepo = appBudgetRepo;
    this.appInvoiceRepo = appInvoiceRepo;
    this.accountConfigRepo = accountConfigRepo;
    this.companyRepo = companyRepo;
  }

  @Override
  public AppAccount getAppAccount() {
    return appAccountRepo.all().fetchOne();
  }

  @Override
  public AppBudget getAppBudget() {
    return appBudgetRepo.all().fetchOne();
  }

  @Override
  public AppInvoice getAppInvoice() {
    return appInvoiceRepo.all().fetchOne();
  }

  @Transactional
  @Override
  public void generateAccountConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.accountConfig is null").fetch();

    for (Company company : companies) {
      AccountConfig config = new AccountConfig();
      config.setCompany(company);
      accountConfigRepo.save(config);
    }
  }
}
