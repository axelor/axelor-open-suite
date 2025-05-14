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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingConfigTemplateService;
import com.axelor.apps.account.service.YearAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Singleton
public class AccountingConfigTemplateController {

  public void installChart(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    accountConfig = Beans.get(AccountConfigRepository.class).find(accountConfig.getId());

    if (Beans.get(AccountingConfigTemplateService.class).installAccountChart(accountConfig)) {
      response.setInfo(I18n.get(AccountExceptionMessage.ACCOUNT_CHART_1));
    } else {
      response.setInfo(I18n.get(AccountExceptionMessage.ACCOUNT_CHART_2));
    }
    response.setReload(true);
  }

  public void installChartGenerateFiscalYear(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    Company company = accountConfig.getCompany();
    accountConfig = Beans.get(AccountConfigRepository.class).find(accountConfig.getId());

    generateFiscalYearAndPeriod(request);
    Beans.get(AccountingConfigTemplateService.class)
        .installProcess(accountConfig, accountConfig.getAccountingConfigTemplate(), company);

    response.setInfo(
        String.format(
            I18n.get(AccountExceptionMessage.ACCOUNTING_CONFIGURATION_TEMPLATE_IMPORT_SUCCESS),
            company.getName()));
    response.setReload(true);
  }

  public void generateFiscalYearAndPeriodAndReload(ActionRequest request, ActionResponse response)
      throws AxelorException {
    generateFiscalYearAndPeriod(request);
    response.setInfo(
        I18n.get(AccountExceptionMessage.ACCOUNT_FISCAL_YEAR_PERIOD_GENERATION_SUCCESS));
    response.setReload(true);
  }

  protected void generateFiscalYearAndPeriod(ActionRequest request) throws AxelorException {
    Context context = request.getContext();
    AccountConfig accountConfig = context.asType(AccountConfig.class);
    LocalDate fiscalYearFromDate = null;
    LocalDate fiscalYearToDate = null;
    String accountingPeriodDuration = null;

    if (context.get("fiscalYearFromDate") != null) {
      fiscalYearFromDate =
          LocalDate.parse((String) context.get("fiscalYearFromDate"), DateTimeFormatter.ISO_DATE);
    }

    if (context.get("fiscalYearToDate") != null) {
      fiscalYearToDate =
          LocalDate.parse((String) context.get("fiscalYearToDate"), DateTimeFormatter.ISO_DATE);
    }

    if (context.get("accountingPeriodDuration") != null) {
      accountingPeriodDuration = (String) context.get("accountingPeriodDuration");
    }

    if (fiscalYearFromDate == null
        || fiscalYearToDate == null
        || accountingPeriodDuration == null) {
      return;
    }

    Beans.get(YearAccountService.class)
        .generateFiscalYear(
            accountConfig.getCompany(),
            fiscalYearFromDate,
            fiscalYearToDate,
            accountingPeriodDuration,
            fiscalYearToDate.plusDays(1L));
  }
}
