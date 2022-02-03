package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class BankReconciliationLineController {

  public void unreconcileUnselectedReconcileSelected(
      ActionRequest request, ActionResponse response) {
    BankReconciliationLine bankReconciliationLineContext =
        request.getContext().asType(BankReconciliationLine.class);
    BankReconciliationLineRepository bankReconciliationLineRepository =
        Beans.get(BankReconciliationLineRepository.class);
    MoveLine moveLine = bankReconciliationLineContext.getMoveLine();
    BankReconciliationLine bankReconciliationLineDatabase =
        bankReconciliationLineRepository.find(bankReconciliationLineContext.getId());

    if (ObjectUtils.notEmpty(bankReconciliationLineDatabase.getMoveLine())) {
      Beans.get(BankReconciliationService.class).unreconcileLine(bankReconciliationLineDatabase);
    }

    if (ObjectUtils.notEmpty(moveLine)) {
      Beans.get(BankReconciliationLineService.class)
          .reconcileBRLAndMoveLine(
              bankReconciliationLineRepository.find(bankReconciliationLineContext.getId()),
              moveLine);
    }

    response.setReload(true);
  }

  public void setSelected(ActionRequest request, ActionResponse response) {
    BankReconciliationLine bankReconciliationLineContext =
        request.getContext().asType(BankReconciliationLine.class);

    bankReconciliationLineContext =
        Beans.get(BankReconciliationService.class).setSelected(bankReconciliationLineContext);
    response.setReload(true);
  }

  @HandleExceptionResponse
  public void setMoveLineDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    BankReconciliationLine bankReconciliationLineContext =
        request.getContext().asType(BankReconciliationLine.class);
    BankReconciliationService bankReconciliationService =
        Beans.get(BankReconciliationService.class);

    String domain =
        bankReconciliationService.createDomainForMoveLine(
            bankReconciliationLineContext.getBankReconciliation());

    response.setAttr("moveLine", "domain", domain);
  }
}
