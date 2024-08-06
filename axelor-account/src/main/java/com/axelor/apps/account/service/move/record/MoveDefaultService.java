/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;

public interface MoveDefaultService {

  /**
   * Set default values of move.
   *
   * @param move
   * @return Map of modified fields.
   */
  void setDefaultValues(Move move);

  /**
   * Set default currency value for move. Note: this method is called in setDefaultMoveValues method
   *
   * @param move
   * @return Map of modified fields.
   */
  void setDefaultCurrency(Move move);

  /**
   * Set default currency code for move.
   *
   * @param move
   */
  void setDefaultCurrencyOnChange(Move move);

  void setDefaultFiscalPositionOnChange(Move move) throws AxelorException;
}
