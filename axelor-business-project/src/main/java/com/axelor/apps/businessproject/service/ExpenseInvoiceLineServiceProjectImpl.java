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

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.service.extracharges.ExtraChargeConstants;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
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

    List<InvoiceLine> lines = new ArrayList<>();

    // Only create standard lines if invoiceFeeOnly is NOT true
    if (!Boolean.TRUE.equals(expenseLine.getInvoiceFeeOnly())) {
      if (Boolean.TRUE.equals(expenseLine.getIsIndividualItem())) {
        log.debug(
            "Creating individual item expense invoice line for expense line with ID: {}",
            expenseLine.getId());
        lines.addAll(createIndividualExpenseItemLine(invoice, expenseLine, priority));
      } else {
        lines.addAll(super.createInvoiceLine(invoice, expenseLine, priority));
      }
    } else {
      log.debug(
          "invoiceFeeOnly is true for expense line ID: {}. Skipping standard product line.",
          expenseLine.getId());
    }

    lines.forEach(
        line -> {
          line.setSourceType(ExtraChargeConstants.EXPENSE_INVOICE_LINE_SOURCE_TYPE);
          line.setSourceIds(expenseLine.getId().toString());
        });

    // We no longer check showChargedFee but if fee is applied
    if (expenseLine.getFee() != null && expenseLine.getFee().compareTo(BigDecimal.ZERO) > 0) {
      log.debug("Fee detected ({}). Generating charged fee line.", expenseLine.getFee());

      InvoiceLine chargedFeeLine = addChargedFeeLine(invoice, expenseLine);
      if (chargedFeeLine != null) {
        lines.add(chargedFeeLine);
      }
    }

    return lines;
  }

  private InvoiceLine addChargedFeeLine(Invoice invoice, ExpenseLine expenseLine)
      throws AxelorException {

    String productCode = ExtraChargeConstants.EXPENSE_CHARGED_FEE_INVOICE_LINE_SOURCE_TYPE;
    Product chargedFeeProduct =
        Beans.get(ProductRepository.class)
            .all()
            .filter("self.code = :code")
            .bind("code", productCode)
            .fetchOne();

    if (chargedFeeProduct == null) {
      log.error("Fee Product '{}' missing. Cannot create fee line.", productCode);
      return null;
    }

    BigDecimal qty =
        expenseLine
            .getFee()
            .divide(new BigDecimal(100), 4, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP);
    BigDecimal unitPrice = expenseLine.getUntaxedAmount().setScale(2, RoundingMode.HALF_UP);

    InvoiceLine line = new InvoiceLine();
    line.setInvoice(invoice);
    line.setProduct(chargedFeeProduct);
    line.setProductName(chargedFeeProduct.getName());
    mapAccountingAndTaxes(line, chargedFeeProduct, invoice);

    if (expenseLine.getItemProductName() != null) {
      line.setDescription(expenseLine.getItemProductName());
    } else {
      line.setDescription(expenseLine.getExpenseProduct().getName());
    }

    line.setQty(qty);
    line.setPrice(unitPrice);
    line.setUnit(chargedFeeProduct.getUnit());
    line.setSourceType(ExtraChargeConstants.EXPENSE_CHARGED_FEE_INVOICE_LINE_SOURCE_TYPE);
    line.setSourceIds(expenseLine.getId().toString());

    log.debug(
        "Successfully created a charged fee InvoiceLine with sourceType: {}, Qty: {}, and Unit: {}",
        line.getSourceType(),
        qty,
        unitPrice);
    return line;
  }

  private List<InvoiceLine> createIndividualExpenseItemLine(
      Invoice invoice, ExpenseLine expenseLine, int priority) throws AxelorException {

    List<InvoiceLine> lines = new ArrayList<>();
    InvoiceLine line = new InvoiceLine();
    line.setInvoice(invoice);

    // set product info
    Product product = resolveExpenseProduct(expenseLine);
    line.setProduct(product);
    mapAccountingAndTaxes(line, product, invoice);

    line.setProductName(expenseLine.getItemProductName());
    line.setQty(expenseLine.getItemQty().setScale(2, RoundingMode.HALF_UP));
    line.setUnit(expenseLine.getItemUnit());
    line.setPrice(expenseLine.getItemUnitPrice().setScale(2, RoundingMode.HALF_UP));
    line.setSourceType(ExtraChargeConstants.EXPENSE_INVOICE_LINE_SOURCE_TYPE);
    lines.add(line);

    return lines;
  }

  private void mapAccountingAndTaxes(InvoiceLine line, Product product, Invoice invoice) {
    Company company = invoice.getCompany();

    // Try Product first
    AccountManagement am = findAccountManagement(product.getAccountManagementList(), company);

    // Fallback to Product Family if the product itself isn't configured
    if (am == null && product.getProductFamily() != null) {
      am = findAccountManagement(product.getProductFamily().getAccountManagementList(), company);
    }

    if (am != null) {
      line.setAccount(am.getSaleAccount());
      if (CollectionUtils.isNotEmpty(am.getSaleTaxSet())) {
        Set<TaxLine> taxLines = new HashSet<>();
        for (Tax tax : am.getSaleTaxSet()) {
          Beans.get(TaxLineRepository.class)
              .all()
              .filter("self.tax = :tax")
              .bind("tax", tax)
              .fetch()
              .stream()
              .findFirst()
              .ifPresent(taxLines::add);
        }
        line.setTaxLineSet(taxLines);
      }
    }
  }

  private AccountManagement findAccountManagement(List<AccountManagement> amList, Company company) {
    if (CollectionUtils.isEmpty(amList)) return null;
    return amList.stream().filter(am -> am.getCompany().equals(company)).findFirst().orElse(null);
  }

  private Product resolveExpenseProduct(ExpenseLine expenseLine) throws AxelorException {
    Product product = expenseLine.getExpenseProduct();

    if (product == null) {
      log.warn("Expense Product missing for line {}, using fallback", expenseLine.getId());

      product =
          Beans.get(ProductRepository.class)
              .findByCode(ExtraChargeConstants.INDIVIDUAL_ITEM_PRODUCT_CODE);

      if (product == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            "No product found for code %s",
            ExtraChargeConstants.INDIVIDUAL_ITEM_PRODUCT_CODE);
      }
    }
    return product;
  }
}
