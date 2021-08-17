package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.common.ObjectUtils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BankReconciliationLineController {

  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationService bankReconciliationService;
  protected BankReconciliationLineService bankReconciliationLineService;

  @Inject
  public BankReconciliationLineController(
      BankReconciliationLineRepository bankReconciliationLineRepository,
      MoveLineRepository moveLineRepository,
      BankReconciliationService bankReconciliationService,
      BankReconciliationLineService bankReconciliationLineService) {
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.moveLineRepository = moveLineRepository;
    this.bankReconciliationService = bankReconciliationService;
    this.bankReconciliationLineService = bankReconciliationLineService;
  }

  public void unreconcileUnselectedReconcileSelected(
      ActionRequest request, ActionResponse response) {
    BankReconciliationLine bankReconciliationLineContext =
        request.getContext().asType(BankReconciliationLine.class);
    MoveLine moveLine = bankReconciliationLineContext.getMoveLine();
    BankReconciliationLine bankReconciliationLineDatabase =
        bankReconciliationLineRepository.find(bankReconciliationLineContext.getId());

    if (ObjectUtils.notEmpty(bankReconciliationLineDatabase.getMoveLine()))
      bankReconciliationService.unreconcileLine(bankReconciliationLineDatabase);

    if (ObjectUtils.notEmpty(moveLine))
      bankReconciliationLineService.reconcileBRLAndMoveLine(
          bankReconciliationLineRepository.find(bankReconciliationLineContext.getId()), moveLine);

    response.setReload(true);
  }
}
