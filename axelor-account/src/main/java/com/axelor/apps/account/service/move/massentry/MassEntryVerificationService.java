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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface MassEntryVerificationService {

  void checkPreconditionsMassEntry(
      Move move, int temporaryMoveNumber, List<Move> massEntryMoveList, boolean manageCutOff)
      throws AxelorException;

  void checkChangesMassEntryMoveLine(
      MoveLineMassEntry moveLine,
      Move parentMove,
      MoveLineMassEntry newMoveLine,
      boolean manageCutOff)
      throws AxelorException;

  void checkDateMassEntryMove(Move move, int temporaryMoveNumber) throws AxelorException;

  void checkCurrencyRateMassEntryMove(Move move, int temporaryMoveNumber);

  void checkOriginDateMassEntryMove(Move move, int temporaryMoveNumber);

  void checkOriginMassEntryMoveLines(
      Move move, int temporaryMoveNumber, List<Move> massEntryMoveList);

  void checkPartnerMassEntryMove(Move move, int temporaryMoveNumber);

  void checkWellBalancedMove(Move move, int temporaryMoveNumber);

  void checkAccountAnalytic(Move move, int temporaryMoveNumber);

  void setErrorMassEntryMoveLines(
      Move move, int temporaryMoveNumber, String fieldName, String errorMessage);

  void verifyCompanyBankDetails(
      Move move, Company company, BankDetails companyBankDetails, Journal journal)
      throws AxelorException;
}
