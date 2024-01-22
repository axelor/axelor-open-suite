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
package com.axelor.apps.bankpayment.service.app;

import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.repo.BankPaymentConfigRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppBankPayment;
import com.axelor.studio.db.repo.AppBankPaymentRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppBankPaymentServiceImpl extends AppBaseServiceImpl implements AppBankPaymentService {

  protected AppBankPaymentRepository appBankPaymentRepo;
  protected BankPaymentConfigRepository bankPaymentConfigRepo;
  protected CompanyRepository companyRepo;

  @Inject
  public AppBankPaymentServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      AppBankPaymentRepository appBankPaymentRepo,
      BankPaymentConfigRepository bankPaymentConfigRepo,
      CompanyRepository companyRepo) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.appBankPaymentRepo = appBankPaymentRepo;
    this.bankPaymentConfigRepo = bankPaymentConfigRepo;
    this.companyRepo = companyRepo;
  }

  @Override
  public AppBankPayment getAppBankPayment() {
    return appBankPaymentRepo.all().fetchOne();
  }

  @Override
  @Transactional
  public void generateBankPaymentConfigurations() {
    List<Company> companies = companyRepo.all().filter("self.bankPaymentConfig IS NULL").fetch();

    for (Company company : companies) {
      BankPaymentConfig config = new BankPaymentConfig();
      config.setCompany(company);
      bankPaymentConfigRepo.save(config);
    }
  }
}
