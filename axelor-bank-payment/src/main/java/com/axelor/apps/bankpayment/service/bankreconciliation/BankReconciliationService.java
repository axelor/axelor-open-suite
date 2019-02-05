/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120.BankReconciliationLoadAFB120Service;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class BankReconciliationService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected AccountService accountService;

  @Inject
  public BankReconciliationService(
      BankReconciliationRepository bankReconciliationRepository, AccountService accountService) {

    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
  }

  @Transactional
  public void compute(BankReconciliation bankReconciliation) {

    BigDecimal totalPaid = BigDecimal.ZERO;
    BigDecimal totalCashed = BigDecimal.ZERO;

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      totalPaid = totalPaid.add(bankReconciliationLine.getDebit());
      totalCashed = totalCashed.add(bankReconciliationLine.getCredit());
    }

    bankReconciliation.setTotalPaid(totalPaid);
    bankReconciliation.setTotalCashed(totalCashed);
    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }
    bankReconciliation.setComputedBalance(
        bankReconciliation.getAccountBalance().add(totalCashed).subtract(totalPaid));
  }

  public String createDomainForBankDetails(BankReconciliation bankReconciliation) {

    return Beans.get(BankDetailsService.class)
        .getActiveCompanyBankDetails(
            bankReconciliation.getCompany(), bankReconciliation.getCurrency());
  }

  @Transactional
  public void loadBankStatement(BankReconciliation bankReconciliation) {
    loadBankStatement(bankReconciliation, true);
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        Beans.get(BankReconciliationLoadAFB120Service.class)
            .loadBankStatement(bankReconciliation, includeBankStatement);
        break;

      default:
        Beans.get(BankReconciliationLoadService.class)
            .loadBankStatement(bankReconciliation, includeBankStatement);
    }

    compute(bankReconciliation);

    bankReconciliationRepository.save(bankReconciliation);
  }
}
