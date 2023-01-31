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
import java.util.Collections;
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
      Set<InvoiceProductStatement> invoiceProductStatementList =
          getInvoiceProductStatements(invoice);
      return computeStatement(productTypes, invoiceProductStatementList);
    }
    return "";
  }

  protected Set<InvoiceProductStatement> getInvoiceProductStatements(Invoice invoice)
      throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    return accountConfig.getStatementsForItemsCategoriesSet();
  }

  protected String computeStatement(
      Set<String> productTypes, Set<InvoiceProductStatement> invoiceProductStatementList) {
    if (invoiceProductStatementList.isEmpty()) {
      return "";
    }
    String statement = getCorrespondingStatement(productTypes, invoiceProductStatementList);
    if (statement.isEmpty()) {
      statement = getStatementForOneType(productTypes, invoiceProductStatementList);
    }
    return statement;
  }

  protected String getCorrespondingStatement(
      Set<String> productTypes, Set<InvoiceProductStatement> invoiceProductStatementList) {
    String statement = "";
    for (InvoiceProductStatement invoiceProductStatement : invoiceProductStatementList) {
      Set<String> result = getTypesList(invoiceProductStatement);
      if (result.equals(productTypes)) {
        statement = invoiceProductStatement.getStatement();
      }
    }
    return statement;
  }

  protected String getStatementForOneType(
      Set<String> productTypes, Set<InvoiceProductStatement> invoiceProductStatementList) {
    if (productTypes.size() != 1) {
      return "";
    }
    int min = getTypeListMinSize(invoiceProductStatementList);
    List<String> statementList = new ArrayList<>();
    for (InvoiceProductStatement invoiceProductStatement : invoiceProductStatementList) {
      Set<String> result = getTypesList(invoiceProductStatement);
      if (result.containsAll(productTypes) && result.size() == min) {
        statementList.add(invoiceProductStatement.getStatement());
      }
    }
    Collections.sort(statementList);
    return statementList.stream().findFirst().orElse("");
  }

  protected int getTypeListMinSize(Set<InvoiceProductStatement> invoiceProductStatementList) {
    int min = 100;
    for (InvoiceProductStatement invoiceProductStatement : invoiceProductStatementList) {
      Set<String> result = getTypesList(invoiceProductStatement);
      if (result.size() < min) {
        min = result.size();
      }
    }
    return min;
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
