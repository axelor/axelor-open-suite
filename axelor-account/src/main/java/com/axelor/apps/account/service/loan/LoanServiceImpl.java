/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.loan;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanManagementConfig;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.util.ArrayList;

public class LoanServiceImpl implements LoanService {

  protected SequenceService sequenceService;

  @Inject
  public LoanServiceImpl(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public String generateReference(Loan loan) throws AxelorException {
    LoanManagementConfig config = loan.getLoanManagementConfig();
    String companyName = loan.getCompany() != null ? loan.getCompany().getName() : "";
    Sequence sequence = config != null ? config.getSequence() : null;
    if (sequence == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.LOAN_SEQUENCE_MISSING),
          companyName);
    }
    String reference = sequenceService.getSequenceNumber(sequence, Loan.class, "reference", loan);
    if (reference == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.LOAN_SEQUENCE_MISSING),
          companyName);
    }
    return reference;
  }

  @Override
  public void copyManagementConfigToLoan(Loan loan) {
    LoanManagementConfig config = loan.getLoanManagementConfig();
    loan.setJournal(config == null ? null : config.getJournal());
    loan.setBorrowingDebtAccount(config == null ? null : config.getBorrowingDebtAccount());
    loan.setInterestExpenseAccount(config == null ? null : config.getInterestExpenseAccount());
    loan.setInsuranceExpenseAccount(config == null ? null : config.getInsuranceExpenseAccount());
    loan.setBankAccount(config == null ? null : config.getBankAccount());
    loan.setAccruedInterestAccount(config == null ? null : config.getAccruedInterestAccount());
    loan.setPrepaidExpenseAccount(config == null ? null : config.getPrepaidExpenseAccount());
    loan.setAnnualInterestRate(config == null ? null : config.getAnnualInterestRate());
    loan.setComputationModeSelect(config == null ? null : config.getComputationModeSelect());
    loan.setDurationInMonth(config == null ? null : config.getDurationInMonth());
    loan.setMonthlyInsuranceAmount(config == null ? null : config.getMonthlyInsuranceAmount());
  }

  @Override
  public void resetForCopy(Loan loan) {
    loan.setStatusSelect(LoanRepository.STATUS_DRAFT);
    loan.setReference(null);
    loan.setLineList(new ArrayList<>());
    loan.setMonthlyPayment(null);
    loan.setRemainingDebt(null);
  }
}
