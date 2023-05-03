package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class MoveLineCheckBankPaymentServiceImpl implements MoveLineCheckBankPaymentService {
  protected MoveLineToolBankPaymentService moveLineToolBankPaymentService;

  @Inject
  public MoveLineCheckBankPaymentServiceImpl(
      MoveLineToolBankPaymentService moveLineToolBankPaymentService) {
    this.moveLineToolBankPaymentService = moveLineToolBankPaymentService;
  }

  @Override
  public void checkBankReconciledAmount(MoveLine moveLine) throws AxelorException {
    if (moveLineToolBankPaymentService.checkBankReconciledAmount(moveLine)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.MOVE_LINE_CHECK_BANK_RECONCILED_AMOUNT));
    }
  }
}
