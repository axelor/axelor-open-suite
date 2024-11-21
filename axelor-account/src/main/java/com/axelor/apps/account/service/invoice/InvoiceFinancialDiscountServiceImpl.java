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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class InvoiceFinancialDiscountServiceImpl implements InvoiceFinancialDiscountService {

  protected InvoiceService invoiceService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected FinancialDiscountService financialDiscountService;

  @Inject
  public InvoiceFinancialDiscountServiceImpl(
      InvoiceService invoiceService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      FinancialDiscountService financialDiscountService) {
    this.invoiceService = invoiceService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.financialDiscountService = financialDiscountService;
  }

  @Override
  public void setFinancialDiscountInformations(Invoice invoice) {

    Objects.requireNonNull(invoice);

    if (invoice.getFinancialDiscount() != null) {
      FinancialDiscount financialDiscount = invoice.getFinancialDiscount();
      invoice.setLegalNotice(financialDiscount.getLegalNotice());
      invoice.setFinancialDiscountRate(financialDiscount.getDiscountRate());
      invoice.setFinancialDiscountTotalAmount(
          this.computeFinancialDiscountTotalAmount(financialDiscount, invoice));
      invoice.setRemainingAmountAfterFinDiscount(
          invoice.getInTaxTotal().subtract(invoice.getFinancialDiscountTotalAmount()));

      if (invoice.getDueDate() != null) {
        invoice.setFinancialDiscountDeadlineDate(
            invoiceService.getFinancialDiscountDeadlineDate(invoice, financialDiscount));
      }
    } else {
      resetFinancialDiscountInformations(invoice);
    }
  }

  protected BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount, Invoice invoice) {
    return financialDiscountService.computeFinancialDiscountTotalAmount(
        financialDiscount, invoice.getInTaxTotal(), invoice.getTaxTotal(), invoice.getCurrency());
  }

  @Override
  public void resetFinancialDiscountInformations(Invoice invoice) {

    Objects.requireNonNull(invoice);

    invoice.setLegalNotice(null);
    invoice.setFinancialDiscountRate(BigDecimal.ZERO);
    invoice.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    invoice.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
  }

  @Override
  public List<InvoiceTerm> updateFinancialDiscount(Invoice invoice) {
    invoice.getInvoiceTermList().stream()
        .filter(it -> it.getAmountRemaining().compareTo(it.getAmount()) == 0)
        .forEach(it -> invoiceTermFinancialDiscountService.computeFinancialDiscount(it, invoice));

    return invoice.getInvoiceTermList();
  }
}
