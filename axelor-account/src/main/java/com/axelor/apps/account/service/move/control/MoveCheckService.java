package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface MoveCheckService {

  /**
   * Method that checks if there are any related cutoff moves to move.
   *
   * @param move
   * @return true if there is any, else false
   */
  boolean checkRelatedCutoffMoves(Move move);

  /**
   * Method that check if period and status are closed of the move.
   *
   * <p>The result map will have two entries "$simulatedPeriodClosed" and "$periodClosed" with their
   * respective result. It is done like this in order to exploit the result in the view form of
   * move.
   *
   * @param move
   * @return generated map
   * @throws AxelorException
   */
  Map<String, Object> checkPeriodAndStatus(Move move) throws AxelorException;

  void checkPeriodPermission(Move move) throws AxelorException;

  void checkDates(Move move) throws AxelorException;

  void checkRemovedLines(Move move) throws AxelorException;

  void checkAnalyticAccount(Move move) throws AxelorException;

  void checkPartnerCompatible(Move move) throws AxelorException;

  void checkDuplicatedMoveOrigin(Move move) throws AxelorException;

  void checkOrigin(Move move) throws AxelorException;
}
