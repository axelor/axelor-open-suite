package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;

public interface MoveDefaultRecordService {

  /**
   * Set default values of move.
   *
   * @param move
   * @return modified move.
   */
  Move setDefaultMoveValues(Move move);

  /**
   * Set default currency value for move. Note: this method is called in setDefaultMoveValues method
   *
   * @param move
   * @return Modified move.
   */
  Move setDefaultCurrency(Move move);
}
