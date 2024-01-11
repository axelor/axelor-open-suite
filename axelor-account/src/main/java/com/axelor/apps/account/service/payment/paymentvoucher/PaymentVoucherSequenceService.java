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
package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherSequenceService {

  protected SequenceService sequenceService;
  protected PaymentModeService paymentModeService;

  @Inject
  public PaymentVoucherSequenceService(
      SequenceService sequenceService, PaymentModeService paymentModeService) {

    this.sequenceService = sequenceService;
    this.paymentModeService = paymentModeService;
  }

  public void setReference(PaymentVoucher paymentVoucher) throws AxelorException {

    if (Strings.isNullOrEmpty(paymentVoucher.getRef())) {

      paymentVoucher.setRef(this.getReference(paymentVoucher));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public String getReference(PaymentVoucher paymentVoucher) throws AxelorException {

    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    Company company = paymentVoucher.getCompany();

    return sequenceService.getSequenceNumber(
        paymentModeService.getPaymentModeSequence(
            paymentMode, company, paymentVoucher.getCompanyBankDetails()),
        PaymentVoucher.class,
        "ref");
  }

  public void setReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)
      throws AxelorException {

    if (journal.getEditReceiptOk()) {

      paymentVoucher.setReceiptNo(this.getReceiptNo(paymentVoucher, company, journal));
    }
  }

  public String getReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)
      throws AxelorException {

    return sequenceService.getSequenceNumber(
        SequenceRepository.PAYMENT_VOUCHER_RECEIPT_NUMBER,
        company,
        PaymentVoucher.class,
        "receiptNo");
  }

  public void checkReceipt(PaymentVoucher paymentVoucher) throws AxelorException {

    Company company = paymentVoucher.getCompany();

    if (!sequenceService.hasSequence(SequenceRepository.PAYMENT_VOUCHER_RECEIPT_NUMBER, company)) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_SEQUENCE_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }
  }
}
