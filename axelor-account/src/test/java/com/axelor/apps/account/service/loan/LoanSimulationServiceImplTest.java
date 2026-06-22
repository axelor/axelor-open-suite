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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanSimulationServiceImplTest {

  private LoanSimulationServiceImpl service;

  @BeforeEach
  void setUp() {
    CurrencyScaleService scale = mock(CurrencyScaleService.class);
    when(scale.getCurrencyScale(nullable(Currency.class))).thenReturn(2);
    when(scale.getScaledValue(any(BigDecimal.class), anyInt()))
        .thenAnswer(i -> ((BigDecimal) i.getArgument(0)).setScale(2, RoundingMode.HALF_UP));
    service = new LoanSimulationServiceImpl(scale);
  }

  private Loan loan(int mode, String rate, int duration, String insurance) {
    Loan loan = new Loan();
    loan.setCompany(new Company());
    loan.setCurrency(new Currency());
    loan.setComputationModeSelect(mode);
    loan.setAnnualInterestRate(new BigDecimal(rate));
    loan.setDurationInMonth(duration);
    loan.setMonthlyInsuranceAmount(new BigDecimal(insurance));
    return loan;
  }

  private void assertAmount(String expected, BigDecimal actual) {
    assertEquals(0, new BigDecimal(expected).compareTo(actual), "got " + actual);
  }

  @Test
  void constantPayment_monthlyPaymentIncludesInsurance() {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT, "3.6", 12, "50");
    loan.setSimulatedAmount(new BigDecimal("100000"));
    assertAmount("8546.73", service.computeMonthlyPayment(loan));
  }

  @Test
  void constantPayment_borrowableCapitalIsInverse() {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT, "3.6", 12, "50");
    loan.setSimulatedMonthlyPayment(new BigDecimal("8546.73"));
    BigDecimal c = service.computeBorrowableCapital(loan);
    assertTrue(c.subtract(new BigDecimal("100000")).abs().compareTo(new BigDecimal("0.50")) <= 0);
  }

  @Test
  void inFine_monthlyPaymentIsInterestPlusInsurance() {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_IN_FINE, "3.6", 12, "50");
    loan.setSimulatedAmount(new BigDecimal("100000"));
    // interest 100000 * 0.003 = 300 + insurance 50
    assertAmount("350.00", service.computeMonthlyPayment(loan));
  }

  @Test
  void constantCapital_monthlyPaymentIsFirstInstallment() {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_CONSTANT_CAPITAL, "3.6", 12, "50");
    loan.setSimulatedAmount(new BigDecimal("100000"));
    // first installment: 100000/12 + 100000*0.003 + 50 = 8333.33 + 300 + 50
    assertAmount("8683.33", service.computeMonthlyPayment(loan));
  }

  @Test
  void zeroRate_constantPayment() {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT, "0", 12, "0");
    loan.setSimulatedAmount(new BigDecimal("60000"));
    assertAmount("5000.00", service.computeMonthlyPayment(loan));
    loan.setSimulatedMonthlyPayment(new BigDecimal("5000"));
    assertAmount("60000.00", service.computeBorrowableCapital(loan));
  }
}
