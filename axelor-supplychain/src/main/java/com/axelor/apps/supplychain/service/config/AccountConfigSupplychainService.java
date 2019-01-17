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
package com.axelor.apps.supplychain.service.config;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class AccountConfigSupplychainService extends AccountConfigService {

  public Account getForecastedInvCustAccount(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getForecastedInvCustAccount() == null) {
      throw new AxelorException(
          accountConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.FORECASTED_INVOICE_CUSTOMER_ACCOUNT),
          accountConfig.getCompany().getName());
    }
    return accountConfig.getForecastedInvCustAccount();
  }

  public Account getForecastedInvSuppAccount(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getForecastedInvSuppAccount() == null) {
      throw new AxelorException(
          accountConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.FORECASTED_INVOICE_SUPPLIER_ACCOUNT),
          accountConfig.getCompany().getName());
    }
    return accountConfig.getForecastedInvSuppAccount();
  }
}
