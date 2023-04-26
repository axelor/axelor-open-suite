package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import java.util.Map;

public interface MoveDefaultService {

  /**
   * Set default values of move.
   *
   * @param move
   * @return Map of modified fields.
   */
  Map<String, Object> setDefaultMoveValues(Move move);

  /**
   * Set default currency value for move. Note: this method is called in setDefaultMoveValues method
   *
   * @param move
   * @return Map of modified fields.
   */
  Map<String, Object> setDefaultCurrency(Move move);
}
