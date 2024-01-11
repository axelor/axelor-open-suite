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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountChart;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.repo.AccountChartRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountChartService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class AccountChartController {

  public void installChart(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    AccountChart act =
        Beans.get(AccountChartRepository.class).find(accountConfig.getAccountChart().getId());
    Company company = Beans.get(CompanyRepository.class).find(accountConfig.getCompany().getId());
    accountConfig = Beans.get(AccountConfigRepository.class).find(accountConfig.getId());
    List<? extends Account> accountList =
        Beans.get(AccountRepository.class)
            .all()
            .filter("self.company.id = ?1 AND self.parentAccount != null", company.getId())
            .fetch();

    if (accountList.isEmpty()) {
      if (Beans.get(AccountChartService.class).installAccountChart(act, company, accountConfig))
        response.setInfo(I18n.get(AccountExceptionMessage.ACCOUNT_CHART_1));
      else response.setInfo(I18n.get(AccountExceptionMessage.ACCOUNT_CHART_2));
      response.setReload(true);

    } else response.setInfo(I18n.get(AccountExceptionMessage.ACCOUNT_CHART_3));
  }
}
