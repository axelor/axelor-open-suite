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
package com.axelor.apps.bankpayment.service.bankreconciliation.load;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFilterService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationLoadBankStatementClassicService
    extends BankReconciliationLoadBankStatementAbstractService {

  protected BankReconciliationLineService bankReconciliationLineService;
  protected BankStatementLineFilterService bankStatementLineFilterService;

  @Inject
  public BankReconciliationLoadBankStatementClassicService(
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationService bankReconciliationService,
      BankReconciliationLineService bankReconciliationLineService,
      BankStatementLineFilterService bankStatementLineFilterService) {
    super(bankReconciliationRepository, bankReconciliationService);
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.bankStatementLineFilterService = bankStatementLineFilterService;
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    this.loadBankStatementLines(bankReconciliation, includeBankStatement);
  }

  @Transactional
  protected void loadBankStatementLines(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    List<BankStatementLine> bankStatementLineList =
        getBankStatementLines(bankReconciliation, includeBankStatement);

    if (bankStatementLineList != null) {
      for (BankStatementLine bankStatementLine : bankStatementLineList) {

        bankReconciliation.addBankReconciliationLineListItem(
            bankReconciliationLineService.createBankReconciliationLine(bankStatementLine));
      }
    }
  }

  protected List<BankStatementLine> getBankStatementLines(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();
    String queryFilter =
        bankStatementLineFilterService.getBankStatementLinesFilterWithAmountToReconcile(
            bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement);
    Query<BankStatementLine> bankStatementLinesQuery =
        JPA.all(BankStatementLine.class)
            .bind("bankDetails", bankReconciliation.getBankDetails())
            .bind("currency", bankReconciliation.getCurrency())
            .bind("statusImported", BankStatementRepository.STATUS_IMPORTED)
            .bind("bankStatement", bankStatement)
            .bind("bankStatementFileFormat", bankStatement.getBankStatementFileFormat())
            .order("valueDate")
            .order("sequence");
    List<Long> existingBankStatementLineIds = getExistingBankStatementLines(bankReconciliation);
    if (!CollectionUtils.isEmpty(existingBankStatementLineIds)) {
      queryFilter += " AND self.id NOT IN (:existingBankStatementLines)";
      bankStatementLinesQuery.bind("existingBankStatementLines", existingBankStatementLineIds);
    }
    return bankStatementLinesQuery.filter(queryFilter).fetch();
  }
}
