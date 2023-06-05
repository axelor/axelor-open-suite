/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.config;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class AccountConfigHRService extends AccountConfigService {

  public Journal getExpenseJournal(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getExpenseJournal() == null) {
      throw new AxelorException(
          accountConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_JOURNAL),
          accountConfig.getCompany().getName());
    }
    return accountConfig.getExpenseJournal();
  }

  public Account getExpenseEmployeeAccount(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getEmployeeAccount() == null) {
      throw new AxelorException(
          accountConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_ACCOUNT),
          accountConfig.getCompany().getName());
    }
    return accountConfig.getEmployeeAccount();
  }

  public Account getExpenseTaxAccount(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getExpenseTaxAccount() == null) {
      throw new AxelorException(
          accountConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_ACCOUNT_TAX),
          accountConfig.getCompany().getName());
    }
    return accountConfig.getExpenseTaxAccount();
  }
}
