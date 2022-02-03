/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayVoucherElementToPayService {

  protected CurrencyService currencyService;
  protected PayVoucherElementToPayRepository payVoucherElementToPayRepo;

  @Inject
  public PayVoucherElementToPayService(
      CurrencyService currencyService,
      PayVoucherElementToPayRepository payVoucherElementToPayRepo) {
    this.currencyService = currencyService;
    this.payVoucherElementToPayRepo = payVoucherElementToPayRepo;
  }

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Generic method for creating invoice to pay lines (2nd O2M in the view)
   *
   * @param pv
   * @param seq
   * @return
   */
  public PayVoucherElementToPay createPayVoucherElementToPay(
      PaymentVoucher pv,
      int seq,
      Invoice invoice,
      MoveLine ml,
      BigDecimal totalAmount,
      BigDecimal remainingAmount,
      BigDecimal amountToPay) {

    log.debug("In  createPayVoucherElementToPay....");

    if (pv != null && ml != null) {
      PayVoucherElementToPay piToPay = new PayVoucherElementToPay();
      piToPay.setSequence(seq);
      piToPay.setMoveLine(ml);
      piToPay.setTotalAmount(totalAmount);
      piToPay.setRemainingAmount(remainingAmount);
      piToPay.setAmountToPay(amountToPay);
      piToPay.setPaymentVoucher(pv);

      log.debug("End createPayVoucherElementToPay IF.");

      return piToPay;
    } else {
      log.debug("End createPayVoucherElementToPay ELSE.");
      return null;
    }
  }

  @Transactional(rollbackOn = AxelorException.class)
  public void updateAmountToPayCurrency(PayVoucherElementToPay elementToPay)
      throws AxelorException {
    Currency paymentVoucherCurrency = elementToPay.getPaymentVoucher().getCurrency();
    BigDecimal amountToPayCurrency =
        currencyService.getAmountCurrencyConvertedAtDate(
            elementToPay.getCurrency(),
            paymentVoucherCurrency,
            elementToPay.getAmountToPay(),
            elementToPay.getPaymentVoucher().getPaymentDate());
    elementToPay.setAmountToPayCurrency(amountToPayCurrency);
    elementToPay.setRemainingAmountAfterPayment(
        elementToPay.getRemainingAmount().subtract(elementToPay.getAmountToPay()));
    payVoucherElementToPayRepo.save(elementToPay);
  }
}
