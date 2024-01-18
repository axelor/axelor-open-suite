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

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayVoucherElementToPayService {

  protected CurrencyService currencyService;
  protected PayVoucherElementToPayRepository payVoucherElementToPayRepo;
  protected AccountConfigService accountConfigService;
  protected InvoiceTermService invoiceTermService;

  private final int RETURN_SCALE = 2;
  private final int CALCULATION_SCALE = 10;

  @Inject
  public PayVoucherElementToPayService(
      CurrencyService currencyService,
      PayVoucherElementToPayRepository payVoucherElementToPayRepo,
      AccountConfigService accountConfigService,
      InvoiceTermService invoiceTermService) {
    this.currencyService = currencyService;
    this.payVoucherElementToPayRepo = payVoucherElementToPayRepo;
    this.accountConfigService = accountConfigService;
    this.invoiceTermService = invoiceTermService;
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
  }

  @Transactional(rollbackOn = {Exception.class})
  public PayVoucherElementToPay updateElementToPayWithFinancialDiscount(
      PayVoucherElementToPay payVoucherElementToPay,
      PayVoucherDueElement payVoucherDueElement,
      PaymentVoucher paymentVoucher)
      throws AxelorException {
    if (!payVoucherDueElement.getApplyFinancialDiscount()
        || payVoucherDueElement.getFinancialDiscount() == null) {
      return payVoucherElementToPay;
    }

    FinancialDiscount financialDiscount = payVoucherDueElement.getFinancialDiscount();
    LocalDate financialDiscountDeadlineDate =
        payVoucherDueElement.getFinancialDiscountDeadlineDate();
    if (financialDiscountDeadlineDate.compareTo(paymentVoucher.getPaymentDate()) >= 0) {
      payVoucherElementToPay.setApplyFinancialDiscount(true);
      payVoucherElementToPay.setFinancialDiscount(financialDiscount);
      payVoucherElementToPay.setFinancialDiscountDeadlineDate(financialDiscountDeadlineDate);
      payVoucherElementToPay.setFinancialDiscountAmount(
          payVoucherDueElement.getFinancialDiscountAmount());
      payVoucherElementToPay.setFinancialDiscountTaxAmount(
          payVoucherDueElement.getFinancialDiscountTaxAmount());
      payVoucherElementToPay.setFinancialDiscountTotalAmount(
          payVoucherDueElement.getFinancialDiscountTotalAmount());

      this.updateFinancialDiscount(payVoucherElementToPay);
    }

    return payVoucherElementToPay;
  }

  public void updateFinancialDiscount(PayVoucherElementToPay payVoucherElementToPay)
      throws AxelorException {
    if (!payVoucherElementToPay.getApplyFinancialDiscount()
        || payVoucherElementToPay.getFinancialDiscount() == null
        || (payVoucherElementToPay.getInvoiceTerm() != null
            && payVoucherElementToPay.getInvoiceTerm().getAmountRemainingAfterFinDiscount().signum()
                == 0)) {
      return;
    }

    InvoiceTerm invoiceTerm = payVoucherElementToPay.getInvoiceTerm();

    BigDecimal percentagePaid =
        payVoucherElementToPay
            .getAmountToPay()
            .divide(
                invoiceTerm.getRemainingAmountAfterFinDiscount(),
                CALCULATION_SCALE,
                RoundingMode.HALF_UP);

    payVoucherElementToPay.setFinancialDiscountTotalAmount(
        invoiceTerm
            .getFinancialDiscountAmount()
            .multiply(percentagePaid)
            .setScale(RETURN_SCALE, RoundingMode.HALF_UP));
    payVoucherElementToPay.setFinancialDiscountTaxAmount(
        invoiceTermService
            .getFinancialDiscountTaxAmount(payVoucherElementToPay.getInvoiceTerm())
            .multiply(percentagePaid)
            .setScale(RETURN_SCALE, RoundingMode.HALF_UP));
    payVoucherElementToPay.setFinancialDiscountAmount(
        payVoucherElementToPay
            .getFinancialDiscountTotalAmount()
            .subtract(payVoucherElementToPay.getFinancialDiscountTaxAmount()));
    payVoucherElementToPay.setTotalAmountWithFinancialDiscount(
        payVoucherElementToPay
            .getAmountToPay()
            .add(payVoucherElementToPay.getFinancialDiscountTotalAmount()));
  }
}
