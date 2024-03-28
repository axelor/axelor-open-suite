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
package com.axelor.apps.contract.service.record;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ContractLineRecordSetServiceImpl implements ContractLineRecordSetService {

  protected AppAccountService appAccountService;
  protected CurrencyService currencyService;

  @Inject
  public ContractLineRecordSetServiceImpl(
      AppAccountService appAccountService, CurrencyService currencyService) {
    this.appAccountService = appAccountService;
    this.currencyService = currencyService;
  }

  public void setCompanyExTaxTotal(
      AnalyticLineContractModel analyticLineContractModel, ContractLine contractLine)
      throws AxelorException {
    if (contractLine != null && analyticLineContractModel != null) {
      BigDecimal companyAmount =
          currencyService.getAmountCurrencyConvertedAtDate(
              analyticLineContractModel.getContract().getCurrency(),
              analyticLineContractModel.getCompany().getCurrency(),
              contractLine.getExTaxTotal(),
              appAccountService.getTodayDate(analyticLineContractModel.getCompany()));
      analyticLineContractModel.setCompanyExTaxTotal(companyAmount);
    }
  }
}
