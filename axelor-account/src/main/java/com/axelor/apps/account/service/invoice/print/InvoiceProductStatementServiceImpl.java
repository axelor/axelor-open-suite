/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice.print;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceProductStatement;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvoiceProductStatementServiceImpl implements InvoiceProductStatementService {
  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceProductStatementServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  public String getInvoiceProductStatement(Invoice invoice) throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (invoiceLineList == null
        || invoiceLineList.isEmpty()
        || !accountConfig.getDisplayItemsCategoriesOnPrinting()) {
      return "";
    }

    Set<String> productTypes = getProductTypes(invoiceLineList);
    return getStatement(invoice, productTypes);
  }

  protected String getStatement(Invoice invoice, Set<String> productTypes) throws AxelorException {
    if (!productTypes.isEmpty()) {
      List<InvoiceProductStatement> invoiceProductStatementList =
          getInvoiceProductStatements(invoice);
      return computeStatement(productTypes, invoiceProductStatementList);
    }
    return "";
  }

  protected List<InvoiceProductStatement> getInvoiceProductStatements(Invoice invoice)
      throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    return accountConfig.getStatementsForItemsCategoriesList();
  }

  protected String computeStatement(
      Set<String> productTypes, List<InvoiceProductStatement> invoiceProductStatementList) {
    if (invoiceProductStatementList.isEmpty()) {
      return "";
    }
    String statement = getCorrespondingStatement(productTypes, invoiceProductStatementList);
    if (statement.isEmpty()) {
      List<InvoiceProductStatement> minSizeCorrespondingInvoiceProductStatements =
          getCorrectInvoiceProductStatements(productTypes, invoiceProductStatementList);
      statement =
          minSizeCorrespondingInvoiceProductStatements.stream()
              .findFirst()
              .map(InvoiceProductStatement::getStatement)
              .orElse("");
    }
    return statement;
  }

  protected List<InvoiceProductStatement> getCorrectInvoiceProductStatements(
      Set<String> productTypes, List<InvoiceProductStatement> invoiceProductStatementList) {
    List<InvoiceProductStatement> correspondingInvoiceProductStatements =
        getCorrespondingInvoiceProductStatements(productTypes, invoiceProductStatementList);
    int min = getMinSize(correspondingInvoiceProductStatements);
    List<InvoiceProductStatement> correspondingInvoiceProductStatementsWithMinSize =
        getCorrespondingInvoiceProductStatementsWithMinSize(
            correspondingInvoiceProductStatements, min);
    correspondingInvoiceProductStatementsWithMinSize.sort(
        Comparator.comparing(InvoiceProductStatement::getId));
    return correspondingInvoiceProductStatementsWithMinSize;
  }

  protected List<InvoiceProductStatement> getCorrespondingInvoiceProductStatementsWithMinSize(
      List<InvoiceProductStatement> correspondingInvoiceProductStatements, int min) {
    List<InvoiceProductStatement> minSizeCorrespondingInvoiceProductStatements = new ArrayList<>();
    for (InvoiceProductStatement invoiceProductStatement : correspondingInvoiceProductStatements) {
      int typesListSize = getTypesList(invoiceProductStatement).size();
      if (typesListSize == min) {
        minSizeCorrespondingInvoiceProductStatements.add(invoiceProductStatement);
      }
    }
    return minSizeCorrespondingInvoiceProductStatements;
  }

  protected int getMinSize(List<InvoiceProductStatement> correspondingInvoiceProductStatements) {
    int min = 1000;
    for (InvoiceProductStatement invoiceProductStatement : correspondingInvoiceProductStatements) {
      int typesListSize = getTypesList(invoiceProductStatement).size();
      if (typesListSize < min) {
        min = typesListSize;
      }
    }
    return min;
  }

  protected List<InvoiceProductStatement> getCorrespondingInvoiceProductStatements(
      Set<String> productTypes, List<InvoiceProductStatement> invoiceProductStatementList) {
    List<InvoiceProductStatement> correspondingStatements = new ArrayList<>();
    for (InvoiceProductStatement invoiceProductStatement : invoiceProductStatementList) {
      Set<String> result = getTypesList(invoiceProductStatement);
      if (result.stream().anyMatch(productTypes::contains)) {
        correspondingStatements.add(invoiceProductStatement);
      }
    }
    return correspondingStatements;
  }

  protected String getCorrespondingStatement(
      Set<String> productTypes, List<InvoiceProductStatement> invoiceProductStatementList) {
    String statement = "";
    for (InvoiceProductStatement invoiceProductStatement : invoiceProductStatementList) {
      Set<String> result = getTypesList(invoiceProductStatement);
      if (result.equals(productTypes)) {
        statement = invoiceProductStatement.getStatement();
      }
    }
    return statement;
  }

  protected Set<String> getTypesList(InvoiceProductStatement invoiceProductStatement) {
    return Stream.of(invoiceProductStatement.getTypeList().trim().split(", "))
        .collect(Collectors.toSet());
  }

  protected Set<String> getProductTypes(List<InvoiceLine> invoiceLineList) {
    Set<String> productTypes = new HashSet<>();
    for (InvoiceLine invoiceLine : invoiceLineList) {
      Product product = invoiceLine.getProduct();
      if (product != null) {
        productTypes.add(product.getProductTypeSelect());
      }
    }
    return productTypes;
  }
}
