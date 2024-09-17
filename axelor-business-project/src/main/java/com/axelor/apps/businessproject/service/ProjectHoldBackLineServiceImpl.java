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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.db.ProjectHoldBackLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProjectHoldBackLineServiceImpl implements ProjectHoldBackLineService {

  @Override
  public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<InvoiceLine> invoiceLines)
      throws AxelorException {

    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList();
    if (projectHoldBackLineList == null || projectHoldBackLineList.isEmpty()) {
      return new ArrayList<>();
    }

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ProjectHoldBackLine projectHoldBackLine : projectHoldBackLineList) {
      invoiceLineList.addAll(
          this.createInvoiceLine(
              invoice,
              projectHoldBackLine,
              invoice.getInvoiceLineList().size() * 100 + count,
              invoiceLines));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLine(
      Invoice invoice,
      ProjectHoldBackLine projectHoldBackLine,
      int priority,
      List<InvoiceLine> invoiceLineList)
      throws AxelorException {

    BigDecimal price = calculateHoldBackLinePrice(invoiceLineList, projectHoldBackLine);

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct(),
            projectHoldBackLine.getProjectHoldBack().getName(),
            price,
            price,
            price,
            null,
            BigDecimal.ONE,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct().getUnit(),
            null,
            priority,
            BigDecimal.ZERO,
            0,
            null,
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

    return invoiceLineGenerator.creates();
  }

  protected BigDecimal calculateHoldBackLinePrice(
      List<InvoiceLine> invoiceLineList, ProjectHoldBackLine projectHoldBackLine) {
    BigDecimal price;
    BigDecimal percentage = projectHoldBackLine.getPercentage();
    Set<Product> products = projectHoldBackLine.getProjectHoldBack().getProductsHeldBackSet();
    if (products == null || products.isEmpty()) {
      price =
          invoiceLineList.stream()
              .map(InvoiceLine::getExTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    } else {
      price =
          invoiceLineList.stream()
              .filter(invLine -> products.contains(invLine.getProduct()))
              .map(InvoiceLine::getExTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    }

    return price.negate();
  }
}
