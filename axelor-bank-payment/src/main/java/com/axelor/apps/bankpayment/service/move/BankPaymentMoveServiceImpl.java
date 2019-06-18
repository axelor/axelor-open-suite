package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveDueService;
import com.axelor.apps.account.service.move.MoveExcessPaymentService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.LinkedHashMap;

public class BankPaymentMoveServiceImpl extends MoveService {

  @Inject
  public BankPaymentMoveServiceImpl(
      AppAccountService appAccountService,
      MoveLineService moveLineService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveToolService moveToolService,
      MoveRemoveService moveRemoveService,
      ReconcileService reconcileService,
      MoveDueService moveDueService,
      PaymentService paymentService,
      MoveExcessPaymentService moveExcessPaymentService,
      MoveRepository moveRepository,
      AccountConfigService accountConfigService) {
    super(
        appAccountService,
        moveLineService,
        moveCreateService,
        moveValidateService,
        moveToolService,
        moveRemoveService,
        reconcileService,
        moveDueService,
        paymentService,
        moveExcessPaymentService,
        moveRepository,
        accountConfigService);
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Move generateReverse(Move move, LinkedHashMap<String, Object> assistantMap)
      throws AxelorException {
    Move newMove = super.generateReverse(move, assistantMap);

    boolean isHiddenMoveLinesInBankReconcilliation =
        (boolean) assistantMap.get("isHiddenMoveLinesInBankReconcilliation");
    if (isHiddenMoveLinesInBankReconcilliation) {
      move = this.updateBankAmountReconcile(move);
      newMove = this.updateBankAmountReconcile(newMove);
    }
    return newMove;
  }

  protected Move updateBankAmountReconcile(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setBankReconciledAmount(moveLine.getDebit().add(moveLine.getCredit()));
    }
    return move;
  }
}
