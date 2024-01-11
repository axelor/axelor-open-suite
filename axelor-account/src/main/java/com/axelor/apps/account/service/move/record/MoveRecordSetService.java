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
import java.util.Map;

public interface MoveRecordSetService {

  void setPaymentMode(Move move);

  void setPaymentCondition(Move move) throws AxelorException;

  void setPartnerBankDetails(Move move);

  void setCurrencyByPartner(Move move);

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
  void setJournal(Move move);

  void setFunctionalOriginSelect(Move move);

  void setPeriod(Move move);

  void setCompanyBankDetails(Move move) throws AxelorException;
}
