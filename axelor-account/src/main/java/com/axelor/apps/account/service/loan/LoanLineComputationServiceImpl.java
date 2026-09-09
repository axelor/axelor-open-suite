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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanLineComputationServiceImpl implements LoanLineComputationService {

  protected static final MathContext MC = MathContext.DECIMAL64;
  protected static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  protected static final BigDecimal TWELVE = BigDecimal.valueOf(12);

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public LoanLineComputationServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public List<LoanLine> computeLines(Loan loan) throws AxelorException {
    return computeLinesFrom(
        loan, loan.getAmount(), loan.getFirstInstallmentDate(), loan.getDurationInMonth());
  }

  @Override
  public List<LoanLine> computeLinesFrom(
      Loan loan, BigDecimal startRemainingDebt, LocalDate firstDate, int count) {
    if (startRemainingDebt == null || firstDate == null || count <= 0) {
      return new ArrayList<>();
    }
    int mode = loan.getComputationModeSelect() == null ? 0 : loan.getComputationModeSelect();
    BigDecimal t = monthlyRate(loan);
    switch (mode) {
      case LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT:
        BigDecimal payment = computeConstantPaymentAmount(loan, startRemainingDebt, count, t);
        return buildLines(
            loan,
            startRemainingDebt,
            firstDate,
            count,
            (i, rdBefore) -> scale(loan, rdBefore.multiply(t)),
            (i, rdBefore, interest) -> scale(loan, payment.subtract(interest)));
      case LoanRepository.COMPUTATION_MODE_CONSTANT_CAPITAL:
        BigDecimal capitalShare =
            scale(loan, startRemainingDebt.divide(BigDecimal.valueOf(count), MC));
        return buildLines(
            loan,
            startRemainingDebt,
            firstDate,
            count,
            (i, rdBefore) -> scale(loan, rdBefore.multiply(t)),
            (i, rdBefore, interest) -> capitalShare);
      case LoanRepository.COMPUTATION_MODE_IN_FINE:
        return buildLines(
            loan,
            startRemainingDebt,
            firstDate,
            count,
            (i, rdBefore) -> scale(loan, rdBefore.multiply(t)),
            (i, rdBefore, interest) -> BigDecimal.ZERO);
      default:
        return new ArrayList<>();
    }
  }

  protected BigDecimal monthlyRate(Loan loan) {
    return loan.getAnnualInterestRate().divide(HUNDRED, MC).divide(TWELVE, MC);
  }

  protected BigDecimal scale(Loan loan, BigDecimal value) {
    return currencyScaleService.getScaledValue(
        value, currencyScaleService.getCurrencyScale(loan.getCurrency()));
  }

  protected BigDecimal insurance(Loan loan) {
    return loan.getMonthlyInsuranceAmount() == null
        ? BigDecimal.ZERO
        : loan.getMonthlyInsuranceAmount();
  }

  protected BigDecimal computeConstantPaymentAmount(
      Loan loan, BigDecimal capital, int n, BigDecimal t) {
    if (t.signum() == 0) {
      return scale(loan, capital.divide(BigDecimal.valueOf(n), MC));
    }
    BigDecimal onePlusTPowN = BigDecimal.ONE.add(t).pow(n);
    BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(onePlusTPowN, MC));
    return scale(loan, capital.multiply(t).divide(denominator, MC));
  }

  /**
   * Builds {@code count} lines from a starting remaining debt and date, applying the interest and
   * capital functions; the last line absorbs the residual to reach a zero remaining debt.
   */
  protected List<LoanLine> buildLines(
      Loan loan,
      BigDecimal startRemainingDebt,
      LocalDate firstDate,
      int count,
      InterestFn interestFn,
      CapitalFn capitalFn) {
    List<LoanLine> lines = new ArrayList<>();
    BigDecimal rdBefore = startRemainingDebt;
    BigDecimal ins = insurance(loan);
    for (int i = 1; i <= count; i++) {
      BigDecimal interest = interestFn.apply(i, rdBefore);
      BigDecimal capital = (i == count) ? rdBefore : capitalFn.apply(i, rdBefore, interest);
      BigDecimal total = scale(loan, interest.add(capital).add(ins));
      BigDecimal rdAfter = scale(loan, rdBefore.subtract(capital));
      LoanLine line = new LoanLine();
      line.setInstallmentDate(firstDate.plusMonths(i - 1L));
      line.setRemainingDebtBefore(rdBefore);
      line.setInterestAmount(interest);
      line.setCapitalAmount(capital);
      line.setInsuranceAmount(ins);
      line.setTotalAmount(total);
      line.setRemainingDebtAfter(rdAfter);
      lines.add(line);
      rdBefore = rdAfter;
    }
    return lines;
  }

  @FunctionalInterface
  protected interface InterestFn {
    BigDecimal apply(int i, BigDecimal rdBefore);
  }

  @FunctionalInterface
  protected interface CapitalFn {
    BigDecimal apply(int i, BigDecimal rdBefore, BigDecimal interest);
  }
}
