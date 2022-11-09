package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class AnalyticMoveLineSupplychainServiceImpl extends AnalyticMoveLineServiceImpl {

  @Inject
  public AnalyticMoveLineSupplychainServiceImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AppAccountService appAccountService,
      AccountManagementServiceAccountImpl accountManagementServiceAccountImpl,
      AccountConfigService accountConfigService,
      AccountRepository accountRepository,
      AppBaseService appBaseService) {
    super(
        analyticMoveLineRepository,
        appAccountService,
        accountManagementServiceAccountImpl,
        accountConfigService,
        accountRepository,
        appBaseService);
    // TODO Auto-generated constructor stub
  }

  @Override
  public AnalyticMoveLine computeAnalyticMoveLine(
      InvoiceLine invoiceLine, Invoice invoice, Company company, AnalyticAccount analyticAccount)
      throws AxelorException {

    AnalyticMoveLine analyticMoveLine =
        super.computeAnalyticMoveLine(invoiceLine, invoice, company, analyticAccount);
    analyticMoveLine.setSaleOrderLine(invoiceLine.getSaleOrderLine());
    analyticMoveLine.setPurchaseOrderLine(invoiceLine.getPurchaseOrderLine());
    return analyticMoveLine;
  }
}
