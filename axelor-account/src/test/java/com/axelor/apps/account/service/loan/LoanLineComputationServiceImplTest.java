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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanLineComputationServiceImplTest {

  private LoanLineComputationServiceImpl service;

  @BeforeEach
  void setUp() {
    CurrencyScaleService scale = mock(CurrencyScaleService.class);
    when(scale.getCurrencyScale(nullable(Currency.class))).thenReturn(2);
    when(scale.getScaledValue(any(BigDecimal.class), anyInt()))
        .thenAnswer(i -> ((BigDecimal) i.getArgument(0)).setScale(2, RoundingMode.HALF_UP));
    service = new LoanLineComputationServiceImpl(scale);
  }

  private Loan loan(int mode) {
    Loan loan = new Loan();
    loan.setCompany(new Company());
    loan.setCurrency(new Currency());
    loan.setAmount(new BigDecimal("100000"));
    loan.setAnnualInterestRate(new BigDecimal("3.6"));
    loan.setDurationInMonth(12);
    loan.setMonthlyInsuranceAmount(new BigDecimal("50"));
    loan.setFirstInstallmentDate(LocalDate.of(2026, 2, 1));
    loan.setComputationModeSelect(mode);
    return loan;
  }

  private void assertAmount(String expected, BigDecimal actual) {
    assertEquals(0, new BigDecimal(expected).compareTo(actual), "got " + actual);
  }

  @Test
  void constantPayment_matchesExcelOracle() throws AxelorException {
    List<LoanLine> lines =
        service.computeLines(loan(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT));

    assertEquals(12, lines.size());
    // Line 1
    assertAmount("100000.00", lines.get(0).getRemainingDebtBefore());
    assertAmount("300.00", lines.get(0).getInterestAmount());
    assertAmount("8196.73", lines.get(0).getCapitalAmount());
    assertAmount("50", lines.get(0).getInsuranceAmount());
    assertAmount("8546.73", lines.get(0).getTotalAmount());
    assertAmount("91803.27", lines.get(0).getRemainingDebtAfter());
    assertEquals(LocalDate.of(2026, 2, 1), lines.get(0).getInstallmentDate());
    // Line 12 (absorbs residual)
    LoanLine last = lines.get(11);
    assertAmount("25.41", last.getInterestAmount());
    assertAmount("8471.27", last.getCapitalAmount());
    assertAmount("8546.68", last.getTotalAmount());
    assertAmount("0.00", last.getRemainingDebtAfter());
    assertEquals(LocalDate.of(2027, 1, 1), last.getInstallmentDate());
    // Totals
    BigDecimal totalInterest =
        lines.stream().map(LoanLine::getInterestAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertAmount("1960.71", totalInterest);
  }

  @Test
  void constantCapital_matchesOracle() throws AxelorException {
    List<LoanLine> lines =
        service.computeLines(loan(LoanRepository.COMPUTATION_MODE_CONSTANT_CAPITAL));
    assertEquals(12, lines.size());
    assertAmount("300.00", lines.get(0).getInterestAmount());
    assertAmount("8333.33", lines.get(0).getCapitalAmount());
    assertAmount("8683.33", lines.get(0).getTotalAmount());
    assertAmount("91666.67", lines.get(0).getRemainingDebtAfter());
    LoanLine last = lines.get(11);
    assertAmount("8333.37", last.getCapitalAmount());
    assertAmount("0.00", last.getRemainingDebtAfter());
    BigDecimal totalInterest =
        lines.stream().map(LoanLine::getInterestAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertAmount("1950.00", totalInterest);
  }

  @Test
  void inFine_matchesOracle() throws AxelorException {
    List<LoanLine> lines = service.computeLines(loan(LoanRepository.COMPUTATION_MODE_IN_FINE));
    assertEquals(12, lines.size());
    assertAmount("0.00", lines.get(0).getCapitalAmount());
    assertAmount("300.00", lines.get(0).getInterestAmount());
    assertAmount("100000.00", lines.get(0).getRemainingDebtAfter());
    LoanLine last = lines.get(11);
    assertAmount("100000.00", last.getCapitalAmount());
    assertAmount("100350.00", last.getTotalAmount());
    assertAmount("0.00", last.getRemainingDebtAfter());
    BigDecimal totalInterest =
        lines.stream().map(LoanLine::getInterestAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertAmount("3600.00", totalInterest);
  }

  @Test
  void zeroRate_constantPayment_splitsCapitalEvenly() throws AxelorException {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT);
    loan.setAnnualInterestRate(BigDecimal.ZERO);
    loan.setMonthlyInsuranceAmount(BigDecimal.ZERO);
    List<LoanLine> lines = service.computeLines(loan);
    assertAmount("8333.33", lines.get(0).getCapitalAmount());
    assertAmount("0.00", lines.get(0).getInterestAmount());
    assertAmount("0.00", lines.get(11).getRemainingDebtAfter());
  }

  @Test
  void computeLinesFrom_reamortizesRemainingBalanceToZero() throws AxelorException {
    Loan loan = loan(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT);

    List<LoanLine> lines =
        service.computeLinesFrom(loan, new BigDecimal("50000"), LocalDate.of(2026, 7, 1), 6);

    assertEquals(6, lines.size());
    assertEquals(LocalDate.of(2026, 7, 1), lines.get(0).getInstallmentDate());
    assertAmount("50000", lines.get(0).getRemainingDebtBefore());
    assertAmount("150.00", lines.get(0).getInterestAmount());
    assertAmount("0.00", lines.get(5).getRemainingDebtAfter());
  }
}
