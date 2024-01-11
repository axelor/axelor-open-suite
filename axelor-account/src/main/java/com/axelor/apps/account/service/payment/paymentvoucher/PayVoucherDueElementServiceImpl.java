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

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PayVoucherDueElementRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.inject.Inject;

public class PayVoucherDueElementServiceImpl implements PayVoucherDueElementService {

  protected PayVoucherDueElementRepository payVoucherDueElementRepository;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected PaymentVoucherToolService paymentVoucherToolService;

  private final int RETURN_SCALE = 2;

  @Inject
  public PayVoucherDueElementServiceImpl(
      PayVoucherDueElementRepository payVoucherDueElementRepository,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      PaymentVoucherToolService paymentVoucherToolService) {
    this.payVoucherDueElementRepository = payVoucherDueElementRepository;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.paymentVoucherToolService = paymentVoucherToolService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PayVoucherDueElement updateDueElementWithFinancialDiscount(
      PayVoucherDueElement payVoucherDueElement, PaymentVoucher paymentVoucher)
      throws AxelorException {
    payVoucherDueElement.setPaymentVoucher(paymentVoucher);
    InvoiceTerm invoiceTerm = payVoucherDueElement.getInvoiceTerm();

    if (this.applyFinancialDiscount(invoiceTerm, paymentVoucher)
        && !paymentVoucherToolService.isMultiCurrency(paymentVoucher)) {
      payVoucherDueElement.setApplyFinancialDiscount(true);
      payVoucherDueElement.setFinancialDiscount(invoiceTerm.getFinancialDiscount());

      BigDecimal ratioPaid = BigDecimal.ONE;
      if (payVoucherDueElement.getInvoiceTerm().getAmount().compareTo(BigDecimal.ZERO) > 0) {
        ratioPaid =
            payVoucherDueElement
                .getInvoiceTerm()
                .getAmountRemaining()
                .divide(
                    payVoucherDueElement.getInvoiceTerm().getAmount(), 10, RoundingMode.HALF_UP);
      }
      payVoucherDueElement.setFinancialDiscountTotalAmount(
          invoiceTerm
              .getFinancialDiscountAmount()
              .multiply(ratioPaid)
              .setScale(RETURN_SCALE, RoundingMode.HALF_UP));
      payVoucherDueElement.setFinancialDiscountTaxAmount(
          invoiceTermService
              .getFinancialDiscountTaxAmount(invoiceTerm)
              .multiply(ratioPaid)
              .setScale(RETURN_SCALE, RoundingMode.HALF_UP));
      payVoucherDueElement.setFinancialDiscountAmount(
          payVoucherDueElement
              .getFinancialDiscountTotalAmount()
              .subtract(payVoucherDueElement.getFinancialDiscountTaxAmount()));
      payVoucherDueElement.setFinancialDiscountDeadlineDate(
          invoiceTerm.getFinancialDiscountDeadlineDate());
    }

    return payVoucherDueElement;
  }

  @Override
  public boolean applyFinancialDiscount(InvoiceTerm invoiceTerm, PaymentVoucher paymentVoucher) {
    return invoiceTerm.getFinancialDiscount() != null
        && invoiceTerm.getFinancialDiscountDeadlineDate() != null
        && invoiceTerm.getFinancialDiscountDeadlineDate().compareTo(paymentVoucher.getPaymentDate())
            >= 0;
  }

  @Override
  public PayVoucherDueElement updateAmounts(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException {
    if (payVoucherDueElement != null && !payVoucherDueElement.getApplyFinancialDiscount()) {
      payVoucherDueElement.setFinancialDiscountAmount(BigDecimal.ZERO);
      payVoucherDueElement.setFinancialDiscountTaxAmount(BigDecimal.ZERO);
      payVoucherDueElement.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    } else if (payVoucherDueElement != null
        && payVoucherDueElement.getApplyFinancialDiscount()
        && payVoucherDueElement.getPaymentVoucher() != null) {
      updateDueElementWithFinancialDiscount(
          payVoucherDueElement, payVoucherDueElement.getPaymentVoucher());
    }
    return payVoucherDueElement;
  }
}
