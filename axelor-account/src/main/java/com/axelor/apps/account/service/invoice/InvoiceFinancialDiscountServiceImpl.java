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
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class InvoiceFinancialDiscountServiceImpl implements InvoiceFinancialDiscountService {

  protected InvoiceService invoiceService;

  @Inject
  public InvoiceFinancialDiscountServiceImpl(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @Override
  public void setFinancialDiscountInformations(Invoice invoice) {

    Objects.requireNonNull(invoice);

    if (invoice.getFinancialDiscount() != null) {
      FinancialDiscount financialDiscount = invoice.getFinancialDiscount();
      invoice.setLegalNotice(financialDiscount.getLegalNotice());
      invoice.setFinancialDiscountRate(financialDiscount.getDiscountRate());
      invoice.setFinancialDiscountTotalAmount(
          computeFinancialDiscountTotalAmount(invoice, financialDiscount));
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
      Invoice invoice, FinancialDiscount financialDiscount) {

    // the scale is the default scale for a decimal field, so 2.
    int financialDiscountTotalAmountScale = 2;

    return financialDiscount
        .getDiscountRate()
        .multiply(invoice.getInTaxTotal())
        .divide(new BigDecimal(100), financialDiscountTotalAmountScale, RoundingMode.HALF_UP);
  }

  @Override
  public void resetFinancialDiscountInformations(Invoice invoice) {

    Objects.requireNonNull(invoice);

    invoice.setLegalNotice(null);
    invoice.setFinancialDiscountRate(BigDecimal.ZERO);
    invoice.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    invoice.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
  }
}
