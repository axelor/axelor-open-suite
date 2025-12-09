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
package com.axelor.apps.contract.service.record;

import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ContractLineRecordSetServiceImpl implements ContractLineRecordSetService {

  protected AppAccountService appAccountService;
  protected CurrencyService currencyService;

  @Inject
  public ContractLineRecordSetServiceImpl(
      AppAccountService appAccountService, CurrencyService currencyService) {
    this.appAccountService = appAccountService;
    this.currencyService = currencyService;
  }

  @Override
  public void setCompanyExTaxTotal(
      AnalyticLineModel analyticLineModel, ContractLine contractLine, Contract contract)
      throws AxelorException {
    if (contractLine == null || analyticLineModel == null || contract == null) {
      return;
    }

    BigDecimal companyAmount =
        currencyService.getAmountCurrencyConvertedAtDate(
            contract.getCurrency(),
            Optional.of(analyticLineModel)
                .map(AnalyticLineModel::getCompany)
                .map(Company::getCurrency)
                .orElse(null),
            contractLine.getExTaxTotal(),
            appAccountService.getTodayDate(analyticLineModel.getCompany()));
    analyticLineModel.setLineAmount(companyAmount);
  }
}
