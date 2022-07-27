package com.axelor.apps.account.service.move.control.accounting.journal;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingJournalControlService {

  /**
   * Method that checks if journal is inactive
   *
   * @param move
   * @throws AxelorException
   */
  void checkInactiveJournal(Move move) throws AxelorException;

  /**
   * Method that checks if functional origin is authorized in move.journal
   *
   * @param move
   * @throws AxelorException
   */
  void checkAuthorizedFunctionalOrigin(Move move) throws AxelorException;
}
