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
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InvoicePaymentComputeServiceImpl implements InvoicePaymentComputeService {

  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService;
  protected InvoiceTermFilterService invoiceTermFilterService;
  protected InvoicePaymentToolService invoicePaymentToolService;

  @Inject
  public InvoicePaymentComputeServiceImpl(
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      InvoiceTermFilterService invoiceTermFilterService,
      InvoicePaymentToolService invoicePaymentToolService) {
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoicePaymentFinancialDiscountService = invoicePaymentFinancialDiscountService;
    this.invoiceTermFilterService = invoiceTermFilterService;
    this.invoicePaymentToolService = invoicePaymentToolService;
  }

  @Override
  public List<Long> computeDataForFinancialDiscount(InvoicePayment invoicePayment, Long invoiceId)
      throws AxelorException {
    List<Long> invoiceTermIdList = null;

    if (invoiceId > 0) {
      List<InvoiceTerm> invoiceTerms =
          invoiceTermFilterService.getUnpaidInvoiceTermsFiltered(invoicePayment.getInvoice());

      invoiceTermIdList =
          invoiceTerms.stream().map(InvoiceTerm::getId).collect(Collectors.toList());

      if (!invoicePayment.getApplyFinancialDiscount()
          && invoicePayment.getCurrency().equals(invoicePayment.getCompanyCurrency())
          && invoicePayment.getTotalAmountWithFinancialDiscount().signum() > 0) {
        invoicePayment.setAmount(invoicePayment.getTotalAmountWithFinancialDiscount());
      }
      invoicePayment.clearInvoiceTermPaymentList();
      BigDecimal companyAvailableAmount =
          invoicePaymentToolService.computeCompanyAmount(
              invoicePayment.getAmount(),
              invoicePayment.getCurrency(),
              invoicePayment.getCompanyCurrency(),
              invoicePayment.getPaymentDate());
      invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
          invoicePayment,
          invoiceTerms,
          companyAvailableAmount,
          invoicePayment.getAmount(),
          companyAvailableAmount);

      invoicePaymentFinancialDiscountService.computeFinancialDiscount(invoicePayment);
    }
    return invoiceTermIdList;
  }
}
