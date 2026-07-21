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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LoanConsistencyServiceImpl implements LoanConsistencyService {

  protected MoveLineRepository moveLineRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public LoanConsistencyServiceImpl(
      MoveLineRepository moveLineRepository, CurrencyScaleService currencyScaleService) {
    this.moveLineRepository = moveLineRepository;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BigDecimal computeTheoreticalOutstanding(Loan loan) {
    if (loan.getLineList() == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal outstanding =
        loan.getLineList().stream()
            .filter(line -> line.getAccountMove() == null)
            .map(LoanLine::getCapitalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return scale(loan, outstanding);
  }

  @Override
  public BigDecimal computeAccountBalance(Loan loan) {
    Account account = loan.getBorrowingDebtAccount();
    BigDecimal amount = nz(loan.getAmount());
    if (account == null || loan.getLineList() == null) {
      return scale(loan, amount);
    }
    // Capital actually repaid in accounting = net debit of this loan's entries on account 164.
    List<Long> moveIds =
        loan.getLineList().stream()
            .map(LoanLine::getAccountMove)
            .filter(Objects::nonNull)
            .map(Move::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    BigDecimal repaidCapital = BigDecimal.ZERO;
    if (!moveIds.isEmpty()) {
      List<MoveLine> moveLineList =
          moveLineRepository
              .all()
              .filter("self.account = :account AND self.move.id IN (:moveIds)")
              .bind("account", account)
              .bind("moveIds", moveIds)
              .fetch();
      repaidCapital =
          moveLineList.stream()
              .map(moveLine -> nz(moveLine.getDebit()).subtract(nz(moveLine.getCredit())))
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // Outstanding capital seen from the loan's accounting entries.
    return scale(loan, amount.subtract(repaidCapital));
  }

  @Override
  public BigDecimal computeGap(Loan loan) {
    return scale(loan, computeTheoreticalOutstanding(loan).subtract(computeAccountBalance(loan)));
  }

  protected BigDecimal nz(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  protected BigDecimal scale(Loan loan, BigDecimal value) {
    return currencyScaleService.getScaledValue(
        value, currencyScaleService.getCurrencyScale(loan.getCurrency()));
  }
}
