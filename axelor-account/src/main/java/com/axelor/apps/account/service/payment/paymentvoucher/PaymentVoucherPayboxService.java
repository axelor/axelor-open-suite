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
package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class PaymentVoucherPayboxService {

  protected PaymentVoucherRepository paymentVoucherRepository;

  @Inject
  public PaymentVoucherPayboxService(PaymentVoucherRepository paymentVoucherRepository) {

    this.paymentVoucherRepository = paymentVoucherRepository;
  }

  /**
   * Procédure permettant d'autauriser la confirmation de la saisie paiement
   *
   * @param paymentVoucher Une saisie paiement
   */
  @Transactional
  public void authorizeConfirmPaymentVoucher(
      PaymentVoucher paymentVoucher, String bankCardTransactionNumber, String payboxAmountPaid) {

    paymentVoucher.setPayboxPaidOk(true);
    paymentVoucher.setBankCardTransactionNumber(bankCardTransactionNumber);
    paymentVoucher.setPayboxAmountPaid(
        new BigDecimal(payboxAmountPaid).divide(new BigDecimal("100")));

    paymentVoucherRepository.save(paymentVoucher);
  }
}
