package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.tool.service.ArchivingToolService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MoveRemoveServiceBankPaymentImpl extends MoveRemoveService {

  @Inject
  public MoveRemoveServiceBankPaymentImpl(
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      ArchivingToolService archivingToolService,
      ReconcileService reconcileService,
      AccountingSituationService accountingSituationService) {
    super(
        moveRepo, moveLineRepo, archivingToolService, reconcileService, accountingSituationService);
  }

  @Override
  protected void checkDaybookMove(Move move) throws Exception {
    super.checkDaybookMove(move);
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getBankReconciledAmount().compareTo(BigDecimal.ZERO) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MOVE_ARCHIVE_NOT_OK),
            move.getReference());
      }
    }
  }
}
