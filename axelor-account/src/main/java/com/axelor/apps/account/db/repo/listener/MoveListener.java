package com.axelor.apps.account.db.repo.listener;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCompletionService;
import com.axelor.apps.account.service.move.MoveCustAccountService;
import com.axelor.apps.account.service.move.control.MovePreSaveControlService;
import com.axelor.apps.account.service.move.control.accounting.MoveAccountingControlService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import javax.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveListener {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveAccountingControlService moveAccountingControlService;
  protected MoveCompletionService moveCompletionService;
  protected MovePreSaveControlService movePreSaveControlService;

  @Inject
  public MoveListener(
      MoveAccountingControlService moveAccountingControlService,
      MoveCompletionService moveCompletionService,
      MovePreSaveControlService movePreSaveControlService,
      MoveCustAccountService moveCustAccountService) {
    this.moveAccountingControlService = moveAccountingControlService;
    this.moveCompletionService = moveCompletionService;
    this.movePreSaveControlService = movePreSaveControlService;
  }

  @PrePersist
  public void beforeSave(Move move) throws AxelorException {

    log.debug("Applying pre-persist operations on move {}", move);

    movePreSaveControlService.checkValidity(move);

    if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
        || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED
        || move.getStatusSelect() == MoveRepository.STATUS_VALIDATED) {
      moveAccountingControlService.controlAccounting(move);
    }

    log.debug("Applied pre-persist operations");
  }
}
