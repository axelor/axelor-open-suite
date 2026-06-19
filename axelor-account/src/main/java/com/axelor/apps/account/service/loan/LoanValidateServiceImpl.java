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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.Objects;

public class LoanValidateServiceImpl implements LoanValidateService {

  protected LoanRepository loanRepository;
  protected LoanService loanService;

  @Inject
  public LoanValidateServiceImpl(LoanRepository loanRepository, LoanService loanService) {
    this.loanRepository = loanRepository;
    this.loanService = loanService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(Loan loan) throws AxelorException {
    Objects.requireNonNull(loan);

    if (loan.getStatusSelect() == null || loan.getStatusSelect() != LoanRepository.STATUS_DRAFT) {
      throw new AxelorException(
          loan,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_NOT_DRAFT));
    }

    LoanManagementConfig config = loan.getLoanManagementConfig();
    if (config == null) {
      throw new AxelorException(
          loan,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LOAN_MANAGEMENT_CONFIG_MISSING),
          loan.getCompany() != null ? loan.getCompany().getName() : "");
    }

    copyConfigToLoan(loan, config);

    if (StringUtils.isEmpty(loan.getReference())) {
      loan.setReference(loanService.generateReference(loan));
    }

    loan.setStatusSelect(LoanRepository.STATUS_VALIDATED);
    loanRepository.save(loan);
  }

  protected void copyConfigToLoan(Loan loan, LoanManagementConfig config) {
    loan.setJournal(config.getJournal());
    loan.setBorrowingDebtAccount(config.getBorrowingDebtAccount());
    loan.setInterestExpenseAccount(config.getInterestExpenseAccount());
    loan.setInsuranceExpenseAccount(config.getInsuranceExpenseAccount());
    loan.setBankAccount(config.getBankAccount());
    loan.setAccruedInterestAccount(config.getAccruedInterestAccount());
    loan.setPrepaidExpenseAccount(config.getPrepaidExpenseAccount());
  }
}
