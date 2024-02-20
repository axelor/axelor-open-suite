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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class BankReconciliationComputeServiceImpl implements BankReconciliationComputeService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  @Inject
  public BankReconciliationComputeServiceImpl(
      BankReconciliationRepository bankReconciliationRepository,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment) {
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
  }

  @Override
  @Transactional
  public void compute(BankReconciliation bankReconciliation) {
    BigDecimal totalPaid = BigDecimal.ZERO;
    BigDecimal totalCashed = BigDecimal.ZERO;

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      totalPaid = totalPaid.add(bankReconciliationLine.getDebit());
      totalCashed = totalCashed.add(bankReconciliationLine.getCredit());
    }
    bankReconciliation.setComputedBalance(
        currencyScaleServiceBankPayment.getScaledValue(
            bankReconciliation,
            bankReconciliation.getAccountBalance().add(totalCashed).subtract(totalPaid)));

    bankReconciliation.setTotalPaid(
        currencyScaleServiceBankPayment.getScaledValue(bankReconciliation, totalPaid));
    bankReconciliation.setTotalCashed(
        currencyScaleServiceBankPayment.getScaledValue(bankReconciliation, totalCashed));
    bankReconciliationRepository.save(bankReconciliation);
  }

  @Override
  @Transactional
  public BankReconciliation computeInitialBalance(BankReconciliation bankReconciliation) {
    BankDetails bankDetails = bankReconciliation.getBankDetails();
    BankReconciliation previousBankReconciliation =
        bankReconciliationRepository
            .all()
            .filter("self.bankDetails = :bankDetails AND self.id != :id")
            .bind("bankDetails", bankDetails)
            .bind("id", bankReconciliation.getId())
            .order("-id")
            .fetchOne();
    BigDecimal startingBalance = BigDecimal.ZERO;
    if (ObjectUtils.isEmpty(previousBankReconciliation)) {
      BankStatementLine initialsBankStatementLine =
          bankStatementLineAFB120Repository
              .all()
              .filter(
                  "self.bankStatement = :bankStatement AND self.lineTypeSelect = :lineTypeSelect "
                      + "AND self.bankDetails = :bankDetails")
              .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
              .bind("bankStatement", bankReconciliation.getBankStatement())
              .bind("bankDetails", bankDetails)
              .order("sequence")
              .fetchOne();
      startingBalance =
          initialsBankStatementLine.getCredit().subtract(initialsBankStatementLine.getDebit());
    } else {
      if (previousBankReconciliation.getStatusSelect()
          == BankReconciliationRepository.STATUS_VALIDATED) {
        startingBalance = previousBankReconciliation.getEndingBalance();
      } else {
        return null;
      }
    }
    bankReconciliation.setStartingBalance(
        currencyScaleServiceBankPayment.getScaledValue(bankReconciliation, startingBalance));
    return bankReconciliation;
  }

  @Override
  public BankReconciliation computeEndingBalance(BankReconciliation bankReconciliation) {
    BigDecimal endingBalance = BigDecimal.ZERO;
    BigDecimal amount = BigDecimal.ZERO;
    endingBalance = endingBalance.add(bankReconciliation.getStartingBalance());
    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      amount = BigDecimal.ZERO;
      if (bankReconciliationLine.getMoveLine() != null) {
        amount =
            bankReconciliationLine
                .getMoveLine()
                .getDebit()
                .subtract(bankReconciliationLine.getMoveLine().getCredit());
      }
      endingBalance = endingBalance.add(amount);
    }
    bankReconciliation.setEndingBalance(
        currencyScaleServiceBankPayment.getScaledValue(bankReconciliation, endingBalance));
    return bankReconciliation;
  }
}
