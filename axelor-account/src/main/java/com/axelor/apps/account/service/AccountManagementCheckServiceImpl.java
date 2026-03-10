/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;

public class AccountManagementCheckServiceImpl implements AccountManagementCheckService {

  protected final AccountManagementRepository accountManagementRepository;

  @Inject
  public AccountManagementCheckServiceImpl(
      AccountManagementRepository accountManagementRepository) {
    this.accountManagementRepository = accountManagementRepository;
  }

  @Override
  public void checkDuplicateAccountManagement(
      AccountManagement accountManagement, PaymentMode paymentMode) throws AxelorException {

    if (accountManagement.getInterbankCodeLine() != null
        || accountManagement.getBankDetails() == null
        || accountManagement.getCompany() == null) {
      return;
    }

    boolean duplicateExists =
        accountManagementRepository
                .all()
                .filter(
                    "self.bankDetails = :bankDetails "
                        + "AND self.paymentMode = :paymentMode "
                        + "AND self.company = :company "
                        + "AND self.interbankCodeLine IS NULL "
                        + "AND self.id != :currentId")
                .bind("bankDetails", accountManagement.getBankDetails())
                .bind("paymentMode", paymentMode)
                .bind("company", accountManagement.getCompany())
                .bind(
                    "currentId", accountManagement.getId() != null ? accountManagement.getId() : 0L)
                .count()
            > 0;

    if (duplicateExists) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_ALREADY_EXISTS),
          accountManagement.getBankDetails().getFullName(),
          paymentMode.getName(),
          accountManagement.getCompany().getName());
    }
  }
}
