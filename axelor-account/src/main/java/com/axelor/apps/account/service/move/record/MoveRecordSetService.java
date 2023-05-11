/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import java.util.Map;

public interface MoveRecordSetService {

  /**
   * Set the payment mode of move.
   *
   * <p>Note: This method can set paymentMode to null even if it was not before.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setPaymentMode(Move move);

  /**
   * Set the paymentCondition of move.
   *
   * <p>Note: This method can set paymentCondition to null even if it was not before.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setPaymentCondition(Move move);

  /**
   * Set the partnerBankDetails of move.
   *
   * <p>Note: This method can set partnerBankDetails to null even if it was not before.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setPartnerBankDetails(Move move);

  /**
   * Set the currency of move by using the move.partner.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setCurrencyByPartner(Move move);

  /**
   * Set the currencyCode of the move by using current currency
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setCurrencyCode(Move move);

  /**
   * Set the journal of the move by using the move company
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setJournal(Move move);

  /**
   * Set the functionOriginSelect of the move
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setFunctionalOriginSelect(Move move);

  Map<String, Object> setPeriod(Move move) throws AxelorException;

  Map<String, Object> setMoveLineDates(Move move) throws AxelorException;

  Map<String, Object> setCompanyBankDetails(Move move) throws AxelorException;

  Map<String, Object> setMoveLineOriginDates(Move move) throws AxelorException;

  Map<String, Object> setOriginOnMoveLineList(Move move) throws AxelorException;
}
