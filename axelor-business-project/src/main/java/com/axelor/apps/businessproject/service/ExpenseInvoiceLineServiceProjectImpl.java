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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.businessproject.service.extracharges.ExtraChargeConstants;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpenseInvoiceLineServiceProjectImpl extends ExpenseInvoiceLineServiceImpl {

  private static final Logger log =
      LoggerFactory.getLogger(ExpenseInvoiceLineServiceProjectImpl.class);

  protected AppAccountService appAccountService;

  @Inject
  public ExpenseInvoiceLineServiceProjectImpl(AppAccountService appAccountService) {
    this.appAccountService = appAccountService;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException {

    if (!appAccountService.isApp("business-project")) {
      return super.createInvoiceLines(invoice, expenseLineList, priority);
    }

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ExpenseLine expenseLine : expenseLineList) {
      List<InvoiceLine> lines =
          this.createInvoiceLine(invoice, expenseLine, priority * 100 + count);

      for (InvoiceLine line : lines) {
        line.setProject(expenseLine.getProject());
        line.setExpenseLine(expenseLine);
        invoiceLineList.add(line);
      }
      count += lines.size();
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine, int priority)
      throws AxelorException {

    if (Boolean.TRUE.equals(expenseLine.getShowChargedFee())) {
      List<InvoiceLine> lines = new ArrayList<>();
      BigDecimal originalFee = expenseLine.getFee();
      BigDecimal originalTotalAmountToInvoice = expenseLine.getTotalAmountToInvoice();

      try {
        log.debug("Generating base line (temporarily zeroing fee)");
        expenseLine.setFee(BigDecimal.ZERO);
        expenseLine.setTotalAmountToInvoice(expenseLine.getUntaxedAmount());
        lines.addAll(super.createInvoiceLine(invoice, expenseLine, priority));
      } finally {
        expenseLine.setFee(originalFee);
        expenseLine.setTotalAmountToInvoice(originalTotalAmountToInvoice);
      }

      if (originalFee != null && originalFee.compareTo(BigDecimal.ZERO) > 0) {
        log.debug("Generating charged fee line for fee: {}", originalFee);
        InvoiceLine chargedFeeLine = createChargedFeeLine(invoice, expenseLine, priority + 1);
        if (chargedFeeLine != null) {
          lines.add(chargedFeeLine);
        } else {
          log.error("Failed to create charged fee line (Product might be missing)");
        }
      }
      return lines;
    }

    return super.createInvoiceLine(invoice, expenseLine, priority);
  }

  private InvoiceLine createChargedFeeLine(Invoice invoice, ExpenseLine expenseLine, int priority)
      throws AxelorException {

    String productCode = ExtraChargeConstants.EXPENSE_CHARGED_FEE_INVOICE_LINE_SOURCE_TYPE;
    log.debug("Searching for fee product with code: {}", productCode);
    Product chargedFeeProduct =
        Beans.get(ProductRepository.class)
            .all()
            .filter("self.code = :code")
            .bind("code", productCode)
            .fetchOne();

    if (chargedFeeProduct == null) {
      log.error("Product with code '{}' not found in database.", productCode);
      return null;
    }

    BigDecimal qty = expenseLine.getFee().divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
    BigDecimal unitPrice = expenseLine.getUntaxedAmount();

    InvoiceLine line = new InvoiceLine();
    line.setInvoice(invoice);
    line.setProduct(chargedFeeProduct);
    line.setProductName(chargedFeeProduct.getName());
    line.setQty(qty);
    line.setPrice(unitPrice);
    line.setUnit(chargedFeeProduct.getUnit());
    line.setSourceType(ExtraChargeConstants.EXPENSE_CHARGED_FEE_INVOICE_LINE_SOURCE_TYPE);

    log.debug(
        "Successfully created a charged fee InvoiceLine with sourceType: {}, Qty: {}, and Unit: {}",
        line.getSourceType(),
        qty,
        unitPrice);
    return line;
  }
}
