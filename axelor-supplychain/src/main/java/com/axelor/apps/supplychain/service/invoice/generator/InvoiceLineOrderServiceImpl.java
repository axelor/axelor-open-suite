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
package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.interfaces.OrderLineTax;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class InvoiceLineOrderServiceImpl implements InvoiceLineOrderService {

  protected TaxService taxService;
  protected AppBaseService appBaseService;

  @Inject
  public InvoiceLineOrderServiceImpl(TaxService taxService, AppBaseService appBaseService) {
    this.taxService = taxService;
    this.appBaseService = appBaseService;
  }

  public InvoiceLineGenerator getInvoiceLineGeneratorWithComputedTaxPrice(
      Invoice invoice,
      Product invoicingProduct,
      BigDecimal percentToInvoice,
      OrderLineTax orderLineTax) {

    TaxLine taxLine = orderLineTax.getTaxLine();
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();

    BigDecimal price =
        percentToInvoice
            .multiply(orderLineTax.getExTaxBase())
            .divide(
                new BigDecimal("100"), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);

    BigDecimal lineAmountToInvoice = price.setScale(scale, RoundingMode.HALF_UP);

    BigDecimal lineAmountToInvoiceInclTax =
        taxService.convertUnitPrice(
            invoicingProduct.getInAti(), orderLineTax.getTaxLine(), price, scale);

    return new InvoiceLineGenerator(
        invoice,
        invoicingProduct,
        invoicingProduct.getName(),
        lineAmountToInvoice,
        lineAmountToInvoiceInclTax,
        invoice.getInAti() ? lineAmountToInvoiceInclTax : lineAmountToInvoice,
        invoicingProduct.getDescription(),
        BigDecimal.ONE,
        invoicingProduct.getUnit(),
        taxLine,
        InvoiceLineGenerator.DEFAULT_SEQUENCE,
        BigDecimal.ZERO,
        PriceListLineRepository.AMOUNT_TYPE_NONE,
        lineAmountToInvoice,
        null,
        false) {
      @Override
      public List<InvoiceLine> creates() throws AxelorException {

        InvoiceLine invoiceLine = this.createInvoiceLine();

        List<InvoiceLine> invoiceLines = new ArrayList<>();
        invoiceLines.add(invoiceLine);

        return invoiceLines;
      }
    };
  }
}
