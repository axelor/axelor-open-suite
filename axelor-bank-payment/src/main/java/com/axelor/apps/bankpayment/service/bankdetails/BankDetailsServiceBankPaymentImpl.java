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
package com.axelor.apps.bankpayment.service.bankdetails;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.BankDetailsServiceAccountImpl;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

public class BankDetailsServiceBankPaymentImpl extends BankDetailsServiceAccountImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;
  protected AppBankPaymentService appBankPaymentService;

  @Inject
  public BankDetailsServiceBankPaymentImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService,
      AppBankPaymentService appBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
    this.appBankPaymentService = appBankPaymentService;
  }

  @Override
  public BankDetails getDefaultBankDetails(
      Partner partner, Company company, PaymentMode paymentMode) {
    BankDetails defaultBankDetails = super.getDefaultBankDetails(partner, company, paymentMode);

    if (paymentMode != null
        && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD
        && Boolean.TRUE.equals(
            appBankPaymentService.getAppBankPayment().getManageDirectDebitPayment())) {
      defaultBankDetails =
          bankDetailsBankPaymentService
              .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
              .stream()
              .findFirst()
              .orElse(null);
    }
    return defaultBankDetails;
  }
}
