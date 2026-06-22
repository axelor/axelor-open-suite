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
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

public class LoanLineGenerationServiceImpl implements LoanLineGenerationService {

  protected LoanLineComputationService loanLineComputationService;
  protected LoanRepository loanRepository;

  @Inject
  public LoanLineGenerationServiceImpl(
      LoanLineComputationService loanLineComputationService, LoanRepository loanRepository) {
    this.loanLineComputationService = loanLineComputationService;
    this.loanRepository = loanRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateSchedule(Loan loan) throws AxelorException {
    checkRequiredData(loan);
    if (loan.getLineList() != null) {
      loan.getLineList().clear();
    }
    List<LoanLine> lines = loanLineComputationService.computeLines(loan);
    for (LoanLine line : lines) {
      loan.addLineListItem(line);
    }
    loan.setRemainingDebt(loan.getAmount());
    if (!lines.isEmpty()) {
      LoanLine first = lines.get(0);
      loan.setMonthlyPayment(first.getInterestAmount().add(first.getCapitalAmount()));
    }
    loanRepository.save(loan);
  }

  protected void checkRequiredData(Loan loan) throws AxelorException {
    if (loan.getAmount() == null
        || loan.getAmount().signum() <= 0
        || loan.getAnnualInterestRate() == null
        || loan.getDurationInMonth() == null
        || loan.getDurationInMonth() <= 0
        || loan.getComputationModeSelect() == null
        || loan.getComputationModeSelect() == 0
        || loan.getFirstInstallmentDate() == null) {
      throw new AxelorException(
          loan,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LOAN_GENERATION_MISSING_DATA));
    }
  }
}
