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
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;

public class LoanSimulationServiceImpl implements LoanSimulationService {

  protected static final MathContext MC = MathContext.DECIMAL64;
  protected static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  protected static final BigDecimal TWELVE = BigDecimal.valueOf(12);

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public LoanSimulationServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
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

  @Override
  public BigDecimal computeMonthlyPayment(Loan loan) {
    BigDecimal t = monthlyRate(loan);
    BigDecimal capital = loan.getSimulatedAmount();
    BigDecimal n = BigDecimal.valueOf(loan.getDurationInMonth());
    BigDecimal payment;
    switch (loan.getComputationModeSelect()) {
      case LoanRepository.COMPUTATION_MODE_CONSTANT_CAPITAL:
        // first (highest) installment: capital share + interest on the whole capital
        payment = capital.divide(n, MC).add(capital.multiply(t));
        break;
      case LoanRepository.COMPUTATION_MODE_IN_FINE:
        payment = capital.multiply(t);
        break;
      default:
        payment = annuity(capital, t, loan.getDurationInMonth());
    }
    return scale(loan, payment.add(insurance(loan)));
  }

  @Override
  public BigDecimal computeBorrowableCapital(Loan loan) {
    BigDecimal t = monthlyRate(loan);
    BigDecimal n = BigDecimal.valueOf(loan.getDurationInMonth());
    BigDecimal net = loan.getSimulatedMonthlyPayment().subtract(insurance(loan));
    BigDecimal capital;
    switch (loan.getComputationModeSelect()) {
      case LoanRepository.COMPUTATION_MODE_CONSTANT_CAPITAL:
        // net = C/n + C*t = C*(1/n + t)
        capital = net.divide(BigDecimal.ONE.divide(n, MC).add(t), MC);
        break;
      case LoanRepository.COMPUTATION_MODE_IN_FINE:
        capital = t.signum() == 0 ? BigDecimal.ZERO : net.divide(t, MC);
        break;
      default:
        capital = inverseAnnuity(net, t, loan.getDurationInMonth());
    }
    return scale(loan, capital);
  }

  protected BigDecimal annuity(BigDecimal capital, BigDecimal t, int n) {
    if (t.signum() == 0) {
      return capital.divide(BigDecimal.valueOf(n), MC);
    }
    BigDecimal factor = BigDecimal.ONE.divide(BigDecimal.ONE.add(t).pow(n), MC);
    return capital.multiply(t).divide(BigDecimal.ONE.subtract(factor), MC);
  }

  protected BigDecimal inverseAnnuity(BigDecimal payment, BigDecimal t, int n) {
    if (t.signum() == 0) {
      return payment.multiply(BigDecimal.valueOf(n));
    }
    BigDecimal factor = BigDecimal.ONE.divide(BigDecimal.ONE.add(t).pow(n), MC);
    return payment.multiply(BigDecimal.ONE.subtract(factor)).divide(t, MC);
  }
}
