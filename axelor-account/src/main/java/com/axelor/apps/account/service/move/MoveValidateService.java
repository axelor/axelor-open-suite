/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveValidateService {

  /**
   * In move lines, fill the dates field and the partner if they are missing, and fill the counter.
   *
   * @param move
   */
  void completeMoveLines(Move move);

  void checkPreconditions(Move move) throws AxelorException;

  /**
   * Valider une écriture comptable.
   *
   * @param move
   * @throws AxelorException
   */
  void validate(Move move) throws AxelorException;

  /**
   * Valider une écriture comptable.
   *
   * @param move
   * @throws AxelorException
   */
  void validate(Move move, boolean updateCustomerAccount) throws AxelorException;

  /**
   * This method may generate fixed asset for each moveLine of move. It will generate if
   * moveLine.fixedAssetCategory != null AND moveLine.account.accountType.technicalTypeSelect =
   * 'immobilisation'
   *
   * @param move
   * @throws AxelorException
   * @throws NullPointerException if move is null or if a line does not have an account
   */
  void generateFixedAssetMoveLine(Move move) throws AxelorException;

  void validateWellBalancedMove(Move move) throws AxelorException;

  void updateValidateStatus(Move move, boolean daybook) throws AxelorException;

  void updateInDayBookMode(Move move) throws AxelorException;

  /**
   * Get the distinct partners of an account move that impact the partner balances
   *
   * @param move
   * @return A list of partner
   */
  List<Partner> getPartnerOfMoveBeforeUpdate(Move move);

  /**
   * Method that freeze the account and partner fields on move lines
   *
   * @param move
   */
  void freezeAccountAndPartnerFieldsOnMoveLines(Move move);

  boolean validateMultiple(List<? extends Move> moveList);

  void simulateMultiple(List<? extends Move> moveList) throws AxelorException;

  void validateMultiple(Query<Move> moveListQuery) throws AxelorException;
}
