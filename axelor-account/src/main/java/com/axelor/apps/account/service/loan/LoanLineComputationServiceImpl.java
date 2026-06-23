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
    int mode = loan.getComputationModeSelect() == null ? 0 : loan.getComputationModeSelect();
    switch (mode) {
      case LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT:
        return computeConstantPayment(loan);
      case LoanRepository.COMPUTATION_MODE_CONSTANT_CAPITAL:
        return computeConstantCapital(loan);
      case LoanRepository.COMPUTATION_MODE_IN_FINE:
        return computeInFine(loan);
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

  protected BigDecimal computeConstantMonthlyPayment(Loan loan) {
    BigDecimal t = monthlyRate(loan);
    BigDecimal capital = loan.getAmount();
    int n = loan.getDurationInMonth();
    if (t.signum() == 0) {
      return scale(loan, capital.divide(BigDecimal.valueOf(n), MC));
    }
    BigDecimal onePlusTPowN = BigDecimal.ONE.add(t).pow(n);
    BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(onePlusTPowN, MC));
    return scale(loan, capital.multiply(t).divide(denominator, MC));
  }

  protected List<LoanLine> computeConstantPayment(Loan loan) {
    BigDecimal t = monthlyRate(loan);
    BigDecimal payment = computeConstantMonthlyPayment(loan);
    return buildLines(
        loan,
        (i, rdBefore) -> scale(loan, rdBefore.multiply(t)),
        (i, rdBefore, interest) -> scale(loan, payment.subtract(interest)));
  }

  protected List<LoanLine> computeConstantCapital(Loan loan) {
    BigDecimal t = monthlyRate(loan);
    BigDecimal capitalShare =
        scale(loan, loan.getAmount().divide(BigDecimal.valueOf(loan.getDurationInMonth()), MC));
    return buildLines(
        loan,
        (i, rdBefore) -> scale(loan, rdBefore.multiply(t)),
        (i, rdBefore, interest) -> capitalShare);
  }

  protected List<LoanLine> computeInFine(Loan loan) {
    BigDecimal t = monthlyRate(loan);
    return buildLines(
        loan,
        (i, rdBefore) -> scale(loan, rdBefore.multiply(t)),
        (i, rdBefore, interest) -> BigDecimal.ZERO);
  }

  /** Builds n lines applying interest/capital functions; the last line absorbs the residual. */
  protected List<LoanLine> buildLines(Loan loan, InterestFn interestFn, CapitalFn capitalFn) {
    List<LoanLine> lines = new ArrayList<>();
    int n = loan.getDurationInMonth();
    BigDecimal rdBefore = loan.getAmount();
    BigDecimal ins = insurance(loan);
    for (int i = 1; i <= n; i++) {
      BigDecimal interest = interestFn.apply(i, rdBefore);
      BigDecimal capital = (i == n) ? rdBefore : capitalFn.apply(i, rdBefore, interest);
      BigDecimal total = scale(loan, interest.add(capital).add(ins));
      BigDecimal rdAfter = scale(loan, rdBefore.subtract(capital));
      LoanLine line = new LoanLine();
      line.setInstallmentDate(loan.getFirstInstallmentDate().plusMonths(i - 1L));
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
