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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface BankReconciliationService {

  void generateMovesAutoAccounting(BankReconciliation bankReconciliation) throws AxelorException;

  Move generateMove(
      BankReconciliationLine bankReconciliationLine, BankStatementRule bankStatementRule)
      throws AxelorException;

  BankReconciliation computeBalances(BankReconciliation bankReconciliation);

  void compute(BankReconciliation bankReconciliation);

  BankReconciliation saveBR(BankReconciliation bankReconciliation);

  String createDomainForBankDetails(BankReconciliation bankReconciliation);

  void loadBankStatement(BankReconciliation bankReconciliation);

  void loadBankStatement(BankReconciliation bankReconciliation, boolean includeBankStatement);

  String getJournalDomain(BankReconciliation bankReconciliation);

  String getCashAccountDomain(BankReconciliation bankReconciliation);

  Journal getJournal(BankReconciliation bankReconciliation);

  Account getCashAccount(BankReconciliation bankReconciliation);

  String getAccountDomain(BankReconciliation bankReconciliation);

  String getRequestMoveLines(BankReconciliation bankReconciliation);

  Map<String, Object> getBindRequestMoveLine(BankReconciliation bankReconciliation)
      throws AxelorException;

  BankReconciliation reconciliateAccordingToQueries(BankReconciliation bankReconciliation)
      throws AxelorException;

  void unreconcileLines(List<BankReconciliationLine> bankReconciliationLines);

  void unreconcileLine(BankReconciliationLine bankReconciliationLine);

  BankReconciliation computeInitialBalance(BankReconciliation bankReconciliation);

  BankReconciliation computeEndingBalance(BankReconciliation bankReconciliation);

  String printNewBankReconciliation(BankReconciliation bankReconciliation) throws AxelorException;

  BankReconciliationLine setSelected(BankReconciliationLine bankReconciliationLineContext);

  String createDomainForMoveLine(BankReconciliation bankReconciliation) throws AxelorException;

  BankReconciliation onChangeBankStatement(BankReconciliation bankReconciliation)
      throws AxelorException;

  void checkReconciliation(List<MoveLine> moveLines, BankReconciliation br) throws AxelorException;

  BankReconciliation reconcileSelected(BankReconciliation bankReconciliation)
      throws AxelorException;

  String getDomainForWizard(
      BankReconciliation bankReconciliation,
      BigDecimal bankStatementCredit,
      BigDecimal bankStatementDebit);

  BigDecimal getSelectedMoveLineTotal(
      BankReconciliation bankReconciliation, List<LinkedHashMap> toReconcileMoveLineSet);

  void mergeSplitedReconciliationLines(BankReconciliation bankReconciliation);

  boolean getIsCorrectButtonHidden(BankReconciliation bankReconciliation) throws AxelorException;

  String getCorrectedLabel(LocalDateTime correctedDateTime, User correctedUser)
      throws AxelorException;

  void correct(BankReconciliation bankReconciliation, User user);

  BigDecimal computeBankReconciliationLinesSelection(BankReconciliation bankReconciliation);

  BigDecimal computeUnreconciledMoveLinesSelection(BankReconciliation bankReconciliation)
      throws AxelorException;

  void checkAccountBeforeAutoAccounting(
      BankStatementRule bankStatementRule, BankReconciliation bankReconciliation)
      throws AxelorException;
}
