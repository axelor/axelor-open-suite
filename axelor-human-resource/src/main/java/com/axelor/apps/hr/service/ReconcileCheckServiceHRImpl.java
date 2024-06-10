package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.reconcile.ReconcileCheckServiceImpl;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;

public class ReconcileCheckServiceHRImpl extends ReconcileCheckServiceImpl {

  @Inject
  public ReconcileCheckServiceHRImpl(
      CurrencyScaleService currencyScaleService,
      InvoiceTermPfpService invoiceTermPfpService,
      InvoiceTermToolService invoiceTermToolService,
      MoveLineToolService moveLineToolService) {
    super(currencyScaleService, invoiceTermPfpService, invoiceTermToolService, moveLineToolService);
  }

  @Override
  protected boolean isMissingTax(MoveLine it) {
    return it.getMove().getExpense() == null && super.isMissingTax(it);
  }
}
