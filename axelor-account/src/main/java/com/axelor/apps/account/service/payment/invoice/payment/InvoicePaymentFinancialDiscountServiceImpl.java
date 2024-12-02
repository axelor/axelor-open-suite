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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoicePaymentFinancialDiscountServiceImpl
    implements InvoicePaymentFinancialDiscountService {
  protected InvoiceTermToolService invoiceTermToolService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected CurrencyScaleService currencyScaleService;
  protected InvoiceTermPaymentToolService invoiceTermPaymentToolService;
  protected InvoiceTermFilterService invoiceTermFilterService;

  @Inject
  public InvoicePaymentFinancialDiscountServiceImpl(
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermPaymentToolService invoiceTermPaymentToolService,
      InvoiceTermFilterService invoiceTermFilterService) {
    this.invoiceTermToolService = invoiceTermToolService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermPaymentToolService = invoiceTermPaymentToolService;
    this.invoiceTermFilterService = invoiceTermFilterService;
  }

  @Override
  public void computeFinancialDiscount(InvoicePayment invoicePayment) {

    computeFinancialDiscountFields(invoicePayment);

    if (invoicePayment.getFinancialDiscountDeadlineDate() == null
        || (invoicePayment.getPaymentDate() != null
            && invoicePayment
                .getFinancialDiscountDeadlineDate()
                .isBefore(invoicePayment.getPaymentDate()))) {
      this.resetFinancialDiscount(invoicePayment);
    }
  }

  @Override
  public void computeFinancialDiscountFields(InvoicePayment invoicePayment) {
    if (CollectionUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())
        || invoiceTermPaymentToolService.isPartialPayment(invoicePayment)) {
      if (invoicePayment.getApplyFinancialDiscount()) {
        this.resetFinancialDiscount(invoicePayment);
      }

      return;
    }

    List<InvoiceTermPayment> invoiceTermPaymentList =
        invoicePayment.getInvoiceTermPaymentList().stream()
            .filter(
                it ->
                    it.getInvoiceTerm() != null
                        && it.getInvoiceTerm().getApplyFinancialDiscount()
                        && !invoiceTermToolService.isPartiallyPaid(it.getInvoiceTerm()))
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(invoiceTermPaymentList)) {
      this.resetFinancialDiscount(invoicePayment);

      return;
    }

    if (!invoicePayment.getManualChange()) {
      invoicePayment.setApplyFinancialDiscount(true);
    }

    invoicePayment.setFinancialDiscount(
        invoiceTermPaymentList.get(0).getInvoiceTerm().getFinancialDiscount());
    invoicePayment.setFinancialDiscountTotalAmount(
        this.getFinancialDiscountTotalAmount(invoiceTermPaymentList));
    invoicePayment.setFinancialDiscountTaxAmount(
        this.getFinancialDiscountTaxAmount(invoiceTermPaymentList));
    invoicePayment.setFinancialDiscountAmount(
        invoicePayment
            .getFinancialDiscountTotalAmount()
            .subtract(invoicePayment.getFinancialDiscountTaxAmount()));
    invoicePayment.setTotalAmountWithFinancialDiscount(
        invoicePayment.getAmount().add(invoicePayment.getFinancialDiscountTotalAmount()));

    LocalDate financialDiscountDeadlineDate =
        this.getFinancialDiscountDeadlineDate(invoiceTermPaymentList);

    invoicePayment.setFinancialDiscountDeadlineDate(financialDiscountDeadlineDate);
  }

  protected void resetFinancialDiscount(InvoicePayment invoicePayment) {
    invoicePayment.setApplyFinancialDiscount(false);
    invoicePayment.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountTaxAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountAmount(BigDecimal.ZERO);
    invoicePayment.setTotalAmountWithFinancialDiscount(BigDecimal.ZERO);
  }

  protected BigDecimal getFinancialDiscountTotalAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .map(InvoiceTermPayment::getFinancialDiscountAmount)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .setScale(this.findScale(invoiceTermPaymentList), RoundingMode.HALF_UP);
  }

  protected BigDecimal getFinancialDiscountTaxAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .filter(it -> it.getInvoiceTerm().getAmountRemainingAfterFinDiscount().signum() > 0)
        .map(
            it -> {
              try {
                return invoiceTermFinancialDiscountService
                    .getFinancialDiscountTaxAmount(it.getInvoiceTerm())
                    .multiply(it.getPaidAmount())
                    .divide(
                        it.getInvoiceTerm().getAmountRemainingAfterFinDiscount(),
                        AppBaseService.COMPUTATION_SCALING,
                        RoundingMode.HALF_UP);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .setScale(this.findScale(invoiceTermPaymentList), RoundingMode.HALF_UP);
  }

  protected int findScale(List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .map(InvoiceTermPayment::getInvoiceTerm)
        .map(currencyScaleService::getScale)
        .findAny()
        .orElse(currencyScaleService.getScale());
  }

  protected LocalDate getFinancialDiscountDeadlineDate(
      List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .map(InvoiceTermPayment::getInvoiceTerm)
        .map(InvoiceTerm::getFinancialDiscountDeadlineDate)
        .min(LocalDate::compareTo)
        .orElse(null);
  }
}
