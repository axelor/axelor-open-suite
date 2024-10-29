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
package com.axelor.apps.account.service.invoice.tax;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class InvoiceTaxComputeServiceImpl implements InvoiceTaxComputeService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public InvoiceTaxComputeServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void recomputeInvoiceTaxAmounts(Invoice invoice) {
    // In the invoice currency
    invoice.setTaxTotal(BigDecimal.ZERO);
    invoice.setInTaxTotal(BigDecimal.ZERO);

    // In the company accounting currency
    invoice.setCompanyTaxTotal(BigDecimal.ZERO);
    invoice.setCompanyInTaxTotal(BigDecimal.ZERO);

    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      // In the invoice currency
      invoice.setTaxTotal(
          currencyScaleService.getScaledValue(
              invoice, invoice.getTaxTotal().add(invoiceLineTax.getTaxTotal())));

      // In the company accounting currency
      invoice.setCompanyTaxTotal(
          currencyScaleService.getCompanyScaledValue(
              invoice, invoice.getCompanyTaxTotal().add(invoiceLineTax.getCompanyTaxTotal())));
    }

    // In the invoice currency
    invoice.setInTaxTotal(
        currencyScaleService.getScaledValue(
            invoice, invoice.getExTaxTotal().add(invoice.getTaxTotal())));

    // In the company accounting currency
    invoice.setCompanyInTaxTotal(
        currencyScaleService.getCompanyScaledValue(
            invoice, invoice.getCompanyExTaxTotal().add(invoice.getCompanyTaxTotal())));
    invoice.setCompanyInTaxTotalRemaining(invoice.getCompanyInTaxTotal());

    invoice.setAmountRemaining(invoice.getInTaxTotal());
  }
}
