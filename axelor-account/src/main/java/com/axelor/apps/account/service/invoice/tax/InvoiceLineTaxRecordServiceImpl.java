/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class InvoiceLineTaxRecordServiceImpl implements InvoiceLineTaxRecordService {

  protected CurrencyService currencyService;

  @Inject
  public InvoiceLineTaxRecordServiceImpl(CurrencyService currencyService) {
    this.currencyService = currencyService;
  }

  @Override
  public void recomputeAmounts(InvoiceLineTax invoiceLineTax, Invoice invoice)
      throws AxelorException {
    invoiceLineTax.setInTaxTotal(computeInTaxTotal(invoiceLineTax));
    invoiceLineTax.setCompanyTaxTotal(computeCompanyTaxTotal(invoiceLineTax, invoice));
    invoiceLineTax.setCompanyInTaxTotal(computeCompanyInTaxTotal(invoiceLineTax));
  }

  protected BigDecimal computeInTaxTotal(InvoiceLineTax invoiceLineTax) {
    if (invoiceLineTax.getTaxTotal().signum() <= 0) {
      return invoiceLineTax.getExTaxBase();
    }
    return invoiceLineTax.getExTaxBase().add(invoiceLineTax.getTaxTotal());
  }

  protected BigDecimal computeCompanyTaxTotal(InvoiceLineTax invoiceLineTax, Invoice invoice)
      throws AxelorException {
    if (invoiceLineTax.getTaxTotal().signum() <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal companyTaxTotal = invoiceLineTax.getTaxTotal();
    Currency invoiceCurrency = Optional.ofNullable(invoice).map(Invoice::getCurrency).orElse(null);
    Currency companyCurrency =
        Optional.ofNullable(invoice)
            .map(Invoice::getCompany)
            .map(Company::getCurrency)
            .orElse(null);

    if (invoiceCurrency == null
        || companyCurrency == null
        || Objects.equals(invoiceCurrency, companyCurrency)) {
      return companyTaxTotal;
    }

    companyTaxTotal =
        currencyService.getAmountCurrencyConvertedAtDate(
            invoiceCurrency,
            companyCurrency,
            invoiceLineTax.getTaxTotal(),
            invoice.getInvoiceDate());

    return companyTaxTotal;
  }

  protected BigDecimal computeCompanyInTaxTotal(InvoiceLineTax invoiceLineTax) {
    if (invoiceLineTax.getCompanyTaxTotal().signum() <= 0) {
      return invoiceLineTax.getCompanyExTaxBase();
    }
    return invoiceLineTax.getCompanyExTaxBase().add(invoiceLineTax.getCompanyTaxTotal());
  }
}
