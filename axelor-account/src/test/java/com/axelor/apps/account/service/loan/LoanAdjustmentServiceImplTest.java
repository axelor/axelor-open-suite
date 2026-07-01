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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.repo.LoanLineRepository;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanAdjustmentServiceImplTest {

  private LoanAdjustmentServiceImpl service;
  private CurrencyScaleService scale;

  @BeforeEach
  void setUp() {
    scale = mock(CurrencyScaleService.class);
    when(scale.getCurrencyScale(nullable(Currency.class))).thenReturn(2);
    when(scale.getScaledValue(any(BigDecimal.class), anyInt()))
        .thenAnswer(i -> ((BigDecimal) i.getArgument(0)).setScale(2, RoundingMode.HALF_UP));
    service =
        new LoanAdjustmentServiceImpl(
            new LoanLineComputationServiceImpl(scale), mock(LoanRepository.class), scale);
  }

  private Loan scheduledLoan() throws AxelorException {
    Loan loan = new Loan();
    loan.setReference("EMP0001");
    loan.setCompany(new Company());
    loan.setCurrency(new Currency());
    loan.setAmount(new BigDecimal("100000"));
    loan.setAnnualInterestRate(new BigDecimal("3.6"));
    loan.setDurationInMonth(12);
    loan.setMonthlyInsuranceAmount(new BigDecimal("50"));
    loan.setFirstInstallmentDate(LocalDate.of(2026, 2, 1));
    loan.setComputationModeSelect(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT);
    loan.setStatusSelect(LoanRepository.STATUS_ONGOING);
    List<LoanLine> lines = new LoanLineComputationServiceImpl(scale).computeLines(loan);
    lines.forEach(line -> line.setLoan(loan));
    loan.setLineList(new ArrayList<>(lines));
    return loan;
  }

  private LoanLine line(Loan loan, int index) {
    return loan.getLineList().get(index);
  }

  private LoanLine lastLine(Loan loan) {
    return loan.getLineList().get(loan.getLineList().size() - 1);
  }

  private boolean isZero(BigDecimal value) {
    return BigDecimal.ZERO.compareTo(value) == 0;
  }

  @Test
  void computeEditedLine_interest_keepsTotalAndAdjustsCapital() throws AxelorException {
    Loan loan = scheduledLoan();
    LoanLine first = line(loan, 0); // interest 300, capital 8196.73, insurance 50, total 8546.73
    first.setInterestAmount(new BigDecimal("400.00"));
    first.setEditedFieldSelect(LoanLineRepository.EDITED_FIELD_INTEREST);

    service.computeEditedLine(first);

    assertEquals(0, new BigDecimal("8546.73").compareTo(first.getTotalAmount()));
    assertEquals(0, new BigDecimal("8096.73").compareTo(first.getCapitalAmount()));
    assertTrue(Boolean.TRUE.equals(first.getIsManuallyModified()));
  }

  @Test
  void computeEditedLine_capital_recomputesTotal() throws AxelorException {
    Loan loan = scheduledLoan();
    LoanLine first = line(loan, 0);
    first.setCapitalAmount(new BigDecimal("9000.00"));
    first.setEditedFieldSelect(LoanLineRepository.EDITED_FIELD_CAPITAL);

    service.computeEditedLine(first);

    assertEquals(0, new BigDecimal("9350.00").compareTo(first.getTotalAmount()));
    assertEquals(0, new BigDecimal("91000.00").compareTo(first.getRemainingDebtAfter()));
  }

  @Test
  void recomputeSchedule_capitalEdit_reamortizesFollowingToZero() throws AxelorException {
    Loan loan = scheduledLoan();
    LoanLine edited = line(loan, 1);
    edited.setCapitalAmount(new BigDecimal("10000.00"));
    edited.setEditedFieldSelect(LoanLineRepository.EDITED_FIELD_CAPITAL);
    service.computeEditedLine(edited);

    service.recomputeSchedule(loan);

    assertEquals(12, loan.getLineList().size());
    assertTrue(isZero(lastLine(loan).getRemainingDebtAfter()));
  }

  @Test
  void recomputeSchedule_insuranceEdit_carriesInsuranceForward() throws AxelorException {
    Loan loan = scheduledLoan();
    LoanLine edited = line(loan, 0);
    edited.setInsuranceAmount(new BigDecimal("80.00"));
    edited.setEditedFieldSelect(LoanLineRepository.EDITED_FIELD_INSURANCE);
    service.computeEditedLine(edited);

    List<LoanLine> result = service.recomputeSchedule(loan);

    assertTrue(
        result.stream()
            .allMatch(l -> new BigDecimal("80.00").compareTo(l.getInsuranceAmount()) == 0));
    assertTrue(isZero(lastLine(loan).getRemainingDebtAfter()));
  }

  @Test
  void defer_capitalize_extendsScheduleAndReachesZero() throws AxelorException {
    Loan loan = scheduledLoan();

    service.defer(loan, 2, true, true, true);

    assertEquals(14, loan.getLineList().size());
    assertTrue(isZero(line(loan, 0).getCapitalAmount()));
    assertTrue(isZero(line(loan, 1).getCapitalAmount()));
    // remaining debt grows over the two deferred months (interest capitalized)
    assertTrue(line(loan, 1).getRemainingDebtAfter().compareTo(new BigDecimal("100000")) > 0);
    assertTrue(isZero(lastLine(loan).getRemainingDebtAfter()));
    assertTrue(loan.getScheduleSnapshot() != null);
  }

  @Test
  void defer_withoutCapitalize_generatesInterestOnlyMonths() throws AxelorException {
    Loan loan = scheduledLoan();

    service.defer(loan, 2, false, false, true);

    assertEquals(14, loan.getLineList().size());
    assertTrue(isZero(line(loan, 0).getCapitalAmount()));
    assertEquals(0, new BigDecimal("300.00").compareTo(line(loan, 0).getInterestAmount()));
    assertEquals(0, new BigDecimal("100000").compareTo(line(loan, 0).getRemainingDebtAfter()));
    assertTrue(isZero(lastLine(loan).getRemainingDebtAfter()));
  }

  @Test
  void cancelDeferral_restoresSnapshot() throws AxelorException {
    Loan loan = scheduledLoan();
    service.defer(loan, 2, true, true, true);

    service.cancelDeferral(loan);

    assertEquals(12, loan.getLineList().size());
    assertEquals(0, new BigDecimal("8546.73").compareTo(line(loan, 0).getTotalAmount()));
    assertNull(loan.getScheduleSnapshot());
  }
}
