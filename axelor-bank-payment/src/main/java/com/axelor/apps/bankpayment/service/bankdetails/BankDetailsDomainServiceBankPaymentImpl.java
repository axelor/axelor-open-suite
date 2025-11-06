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
import com.axelor.apps.account.service.BankDetailsDomainServiceAccountImpl;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class BankDetailsDomainServiceBankPaymentImpl extends BankDetailsDomainServiceAccountImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;
  protected AppBankPaymentService appBankPaymentService;

  @Inject
  public BankDetailsDomainServiceBankPaymentImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService,
      AppBankPaymentService appBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
    this.appBankPaymentService = appBankPaymentService;
  }

  @Override
  public String createDomainForBankDetails(
      Partner partner, PaymentMode paymentMode, Company company) {
    String domain = super.createDomainForBankDetails(partner, paymentMode, company);

    if (partner == null
        || paymentMode == null
        || !appBankPaymentService.getAppBankPayment().getManageDirectDebitPayment()
        || ObjectUtils.isEmpty(partner.getBankDetailsList())) {
      return domain;
    }

    List<BankDetails> bankDetailsList =
        bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
            paymentMode, partner, company);
    if (paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD) {
      domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
    }
    return domain;
  }
}
