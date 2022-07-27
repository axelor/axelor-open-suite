package com.axelor.apps.account.db.repo.listener;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.control.MovePreSaveControlService;
import com.axelor.apps.account.service.move.control.accounting.balance.MoveAccountingBalanceControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.MoveAccountingMoveLineControlService;
import com.axelor.apps.account.service.move.control.moveline.MoveLineChangeControlService;
import com.axelor.apps.account.service.move.control.moveline.MoveLinePreSaveControlService;
import com.axelor.apps.account.service.moveline.MoveLineCompletionService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineListener {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @PrePersist
  @PreUpdate
  public void beforeSave(MoveLine moveLine) throws AxelorException {

    log.debug("Applying pre-save operations on moveLine {}", moveLine);

    // Control and completion to do in any cases
    Beans.get(MoveLinePreSaveControlService.class).checkValidity(moveLine);
    Beans.get(MoveLineCompletionService.class).completeAnalyticMoveLine(moveLine);

    // Operations to do if the move line is related to a move
    Move move = moveLine.getMove();
    if (move != null) {

      Beans.get(MovePreSaveControlService.class).checkValidity(move);

      if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
          || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED) {

        Beans.get(MoveLineCompletionService.class).freezeAccountAndPartnerFields(moveLine);

        // Control to do only in these cases because of conflict with opening/closure batch
        if (move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE
            && move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_OPENING) {

          Beans.get(MoveAccountingMoveLineControlService.class).controlAccounting(moveLine);
        }
      }

      if (move.getStatusSelect() != MoveRepository.STATUS_NEW
          && move.getStatusSelect() != MoveRepository.STATUS_CANCELED) {
        Beans.get(MoveAccountingBalanceControlService.class).checkWellBalanced(move);
      }
    }

    log.debug("Applied pre-save operations");
  }

  @PreRemove
  public void beforeDelete(MoveLine moveLine) throws AxelorException {
    log.debug("Applying pre-delete operations on moveLine {}", moveLine);
    Move move = moveLine.getMove();

    if (move != null) {
      Beans.get(MoveLineChangeControlService.class).checkIllegalRemoval(moveLine);
      if (move.getStatusSelect() != MoveRepository.STATUS_NEW
          && move.getStatusSelect() != MoveRepository.STATUS_CANCELED) {
        Beans.get(MoveAccountingBalanceControlService.class).checkWellBalanced(move);
      }
    }

    log.debug("Applied pre-delete operations");
  }
}
