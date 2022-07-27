package com.axelor.apps.account.service.move.control.accounting;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.control.accounting.account.MoveAccountingAccountControlService;
import com.axelor.apps.account.service.move.control.accounting.analytic.MoveAccountingAnalyticControlService;
import com.axelor.apps.account.service.move.control.accounting.balance.MoveAccountingBalanceControlService;
import com.axelor.apps.account.service.move.control.accounting.journal.MoveAccountingJournalControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.MoveAccountingMoveLineControlService;
import com.axelor.apps.account.service.move.control.moveline.MoveLinePreSaveControlService;
import com.axelor.apps.account.service.move.control.period.MovePeriodControlService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingControlServiceImpl implements MoveAccountingControlService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected MoveAccountingNullControlService moveAccountingNullControlService;
  protected MoveAccountingMoveLineControlService moveAccountingMoveLineControlService;
  protected MoveAccountingBalanceControlService moveAccountingBalanceControlService;
  protected MovePeriodControlService movePeriodControlService;
  protected MoveLinePreSaveControlService moveLinePreSaveControlService;

  protected MoveAccountingAccountControlService moveAccountingAccountControlService;
  protected MoveAccountingAnalyticControlService moveAccountingAnalyticControlService;
  protected MoveAccountingJournalControlService moveAccountingJournalControlService;

  @Inject
  public MoveAccountingControlServiceImpl(
      MoveAccountingMoveLineControlService moveAccountingMoveLineControlService,
      MoveAccountingNullControlService moveAccountingNullControlService,
      MoveAccountingBalanceControlService moveAccountingBalanceControlService,
      MovePeriodControlService movePeriodControlService,
      MoveLinePreSaveControlService moveLinePreSaveControlService,
      MoveAccountingAccountControlService moveAccountingAccountControlService,
      MoveAccountingAnalyticControlService moveAccountingAnalyticControlService,
      MoveAccountingJournalControlService moveAccountingJournalControlService) {

    this.moveAccountingMoveLineControlService = moveAccountingMoveLineControlService;
    this.moveAccountingNullControlService = moveAccountingNullControlService;
    this.moveAccountingBalanceControlService = moveAccountingBalanceControlService;
    this.movePeriodControlService = movePeriodControlService;
    this.moveLinePreSaveControlService = moveLinePreSaveControlService;
    this.moveAccountingAccountControlService = moveAccountingAccountControlService;
    this.moveAccountingAnalyticControlService = moveAccountingAnalyticControlService;
    this.moveAccountingJournalControlService = moveAccountingJournalControlService;
  }

  @Override
  public void controlAccounting(Move move) throws AxelorException {
    log.debug("Controlling accounting of move {}", move);
    Objects.requireNonNull(move);

    moveAccountingNullControlService.checkNullFields(move);

    movePeriodControlService.checkAuthorizationOnClosedPeriod(move);
    movePeriodControlService.checkClosedPeriod(move);

    moveAccountingAccountControlService.checkInactiveAccount(move);
    moveAccountingJournalControlService.checkInactiveJournal(move);
    moveAccountingJournalControlService.checkAuthorizedFunctionalOrigin(move);

    moveAccountingAnalyticControlService.checkInactiveAnalyticAccount(move);
    moveAccountingAnalyticControlService.checkInactiveAnalyticJournal(move);
  }

  @Override
  public void deepControlAccounting(Move move) throws AxelorException {

    log.debug("Deep controlling accounting of move {}", move);

    controlAccounting(move);

    if (move.getMoveLineList() != null) {

      for (MoveLine moveLine : move.getMoveLineList()) {

        moveLinePreSaveControlService.checkValidity(moveLine);

        // Control to do only in these cases because of conflict with opening/closure batch
        if (move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE
            && move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_OPENING) {
          moveAccountingMoveLineControlService.controlAccounting(moveLine);
        }
      }
    }
    moveAccountingBalanceControlService.checkWellBalanced(move);
  }
}
