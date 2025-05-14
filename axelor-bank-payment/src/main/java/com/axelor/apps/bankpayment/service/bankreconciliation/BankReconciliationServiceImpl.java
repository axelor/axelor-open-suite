/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BankReconciliationServiceImpl implements BankReconciliationService {
  protected BankStatementLineRepository bankStatementLineRepository;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationServiceImpl(
      BankStatementLineRepository bankStatementLineRepository,
      BankReconciliationLineRepository bankReconciliationLineRepository,
      CurrencyScaleService currencyScaleService) {
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BankReconciliation onChangeBankStatement(BankReconciliation bankReconciliation)
      throws AxelorException {
    boolean uniqueBankDetails = true;
    BankDetails bankDetails = null;
    bankReconciliation.setToDate(bankReconciliation.getBankStatement().getToDate());
    bankReconciliation.setFromDate(bankReconciliation.getBankStatement().getFromDate());
    List<BankStatementLine> bankStatementLines =
        bankStatementLineRepository
            .findByBankStatement(bankReconciliation.getBankStatement())
            .fetch();
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      if (bankDetails == null) {
        bankDetails = bankStatementLine.getBankDetails();
      }
      // If it is still null
      if (bankDetails == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                BankPaymentExceptionMessage.BANK_RECONCILIATION_BANK_STATEMENT_NO_BANK_DETAIL));
      }
      if (!bankDetails.equals(bankStatementLine.getBankDetails())) {
        uniqueBankDetails = false;
      }
    }
    if (uniqueBankDetails) {
      bankReconciliation.setBankDetails(bankDetails);
      bankReconciliation.setCashAccount(bankDetails.getBankAccount());
      bankReconciliation.setJournal(bankDetails.getJournal());
      Currency currency = bankDetails.getCurrency();
      if (currency != null) {
        bankReconciliation.setCurrency(currency);
      }
    } else {
      bankReconciliation.setBankDetails(null);
    }
    return bankReconciliation;
  }

  @Override
  @Transactional
  public void mergeSplitedReconciliationLines(BankReconciliation bankReconciliation) {
    List<BankReconciliationLine> bankReconciliationLineList =
        bankReconciliationLineRepository
            .all()
            .filter("self.bankReconciliation = :br AND self.moveLine IS NULL")
            .bind("br", bankReconciliation)
            .order("id")
            .fetch();
    List<BankReconciliationLine> alreadyMergedBankReconciliationLineList =
        new ArrayList<BankReconciliationLine>();
    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLineList) {
      BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
      List<BankReconciliationLine> splitedBankReconciliationLines =
          bankReconciliationLineRepository
              .all()
              .filter(
                  "self.bankReconciliation = :br AND self.moveLine IS NULL AND self.bankStatementLine = :bankStatement AND self.id != :id")
              .bind("br", bankReconciliation)
              .bind("bankStatement", bankStatementLine)
              .bind("id", bankReconciliationLine.getId())
              .fetch();
      if (!splitedBankReconciliationLines.isEmpty()
          && !alreadyMergedBankReconciliationLineList.contains(bankReconciliationLine)) {
        for (BankReconciliationLine bankReconciliationLineToMerge :
            splitedBankReconciliationLines) {
          bankReconciliation.removeBankReconciliationLineListItem(bankReconciliationLineToMerge);
          bankReconciliationLineRepository.remove(bankReconciliationLineToMerge);
          alreadyMergedBankReconciliationLineList.add(bankReconciliationLineToMerge);
        }
        if (bankReconciliationLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
          bankReconciliationLine.setCredit(
              currencyScaleService.getScaledValue(
                  bankStatementLine, bankStatementLine.getAmountRemainToReconcile()));
        } else {
          bankReconciliationLine.setDebit(
              currencyScaleService.getScaledValue(
                  bankStatementLine, bankStatementLine.getAmountRemainToReconcile()));
        }
        bankReconciliationLineRepository.save(bankReconciliationLine);
      }
    }
  }
}
