/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountChart;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.repo.AccountChartRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountChartService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class AccountChartController {

  @Inject AccountChartService accountChartsService;

  @Inject CompanyRepository companyRepo;

  @Inject AccountConfigRepository accountConfigRepo;

  @Inject AccountRepository accountRepo;

  @Inject AccountChartRepository accountChartRepo;

  public void installChart(ActionRequest request, ActionResponse response) throws AxelorException {

    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    AccountChart act = accountChartRepo.find(accountConfig.getAccountChart().getId());
    Company company = companyRepo.find(accountConfig.getCompany().getId());
    accountConfig = accountConfigRepo.find(accountConfig.getId());
    List<? extends Account> accountList =
        accountRepo
            .all()
            .filter("self.company.id = ?1 AND self.parentAccount != null", company.getId())
            .fetch();

    if (accountList.isEmpty()) {
      if (accountChartsService.installAccountChart(act, company, accountConfig))
        response.setFlash(I18n.get(IExceptionMessage.ACCOUNT_CHART_1));
      else response.setFlash(I18n.get(IExceptionMessage.ACCOUNT_CHART_2));
      response.setReload(true);

    } else response.setFlash(I18n.get(IExceptionMessage.ACCOUNT_CHART_3));
  }
}
