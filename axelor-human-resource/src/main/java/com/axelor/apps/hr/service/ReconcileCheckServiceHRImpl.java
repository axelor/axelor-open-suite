package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.reconcile.ReconcileCheckServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.common.ObjectUtils;
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
  protected void taxLinePrecondition(Move move) throws AxelorException {
    // Checking also if move expense is null
    if (move.getMoveLineList().stream()
        .anyMatch(
            it ->
                (move.getExpense() == null && ObjectUtils.isEmpty(it.getTaxLineSet()))
                    && ObjectUtils.isEmpty(it.getTaxLineSet())
                    && moveLineToolService.isMoveLineTaxAccount(it)
                    && it.getAccount().getIsTaxAuthorizedOnMoveLine())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          AccountExceptionMessage.RECONCILE_MISSING_TAX,
          move.getReference());
    }
  }
}
