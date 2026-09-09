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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoanClosureServiceImplTest {

  private MoveCreateService moveCreateService;
  private MoveLineCreateService moveLineCreateService;
  private MoveValidateService moveValidateService;
  private MoveReverseService moveReverseService;
  private MoveRepository moveRepository;

  private LoanClosureServiceImpl service;

  private Currency currency;
  private Account interestAccount;
  private Account accruedInterestAccount;
  private Account prepaidAccount;
  private Account insuranceAccount;

  @BeforeEach
  void setUp() throws AxelorException {
    moveCreateService = mock(MoveCreateService.class);
    moveLineCreateService = mock(MoveLineCreateService.class);
    moveValidateService = mock(MoveValidateService.class);
    moveReverseService = mock(MoveReverseService.class);
    moveRepository = mock(MoveRepository.class);
    service =
        new LoanClosureServiceImpl(
            moveCreateService,
            moveLineCreateService,
            moveValidateService,
            moveReverseService,
            moveRepository);

    currency = new Currency();
    currency.setNumberOfDecimals(2);
    interestAccount = new Account();
    accruedInterestAccount = new Account();
    prepaidAccount = new Account();
    insuranceAccount = new Account();

    when(moveLineCreateService.createMoveLine(
            any(), any(), any(), any(), anyBoolean(), any(), anyInt(), any(), any()))
        .thenReturn(new MoveLine());
    when(moveRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private Loan loan() {
    Loan loan = new Loan();
    loan.setReference("EMP0001");
    loan.setCompany(new Company());
    loan.setCurrency(currency);
    loan.setAnnualInterestRate(new BigDecimal("3.6"));
    loan.setJournal(new Journal());
    loan.setInterestExpenseAccount(interestAccount);
    loan.setAccruedInterestAccount(accruedInterestAccount);
    loan.setPrepaidExpenseAccount(prepaidAccount);
    loan.setInsuranceExpenseAccount(insuranceAccount);
    loan.setLineList(new ArrayList<>());
    return loan;
  }

  private LoanLine addLine(Loan loan, LocalDate date, String remainingDebtAfter, String insurance) {
    LoanLine line = new LoanLine();
    line.setLoan(loan);
    line.setInstallmentDate(date);
    line.setRemainingDebtAfter(new BigDecimal(remainingDebtAfter));
    line.setInsuranceAmount(new BigDecimal(insurance));
    loan.getLineList().add(line);
    return line;
  }

  @Test
  void computeAccruedInterest_proratesOnRemainingDebtOver360() {
    Loan loan = loan();
    addLine(loan, LocalDate.of(2026, 12, 1), "8471.27", "50.00");

    BigDecimal accrued = service.computeAccruedInterest(loan, LocalDate.of(2026, 12, 31));

    // 8471.27 * 3.6% * 30 / 360 = 25.41
    assertEquals(new BigDecimal("25.41"), accrued);
  }

  @Test
  void computeAccruedInterest_zeroWhenClosingOnInstallmentDate() {
    Loan loan = loan();
    addLine(loan, LocalDate.of(2026, 12, 1), "8471.27", "50.00");

    assertEquals(BigDecimal.ZERO, service.computeAccruedInterest(loan, LocalDate.of(2026, 12, 1)));
  }

  @Test
  void computePrepaidInsurance_proratesStraddlingInstallment() {
    Loan loan = loan();
    addLine(loan, LocalDate.of(2026, 12, 1), "8471.27", "50.00");
    addLine(loan, LocalDate.of(2027, 1, 1), "0.00", "50.00");

    BigDecimal prepaid = service.computePrepaidInsurance(loan, LocalDate.of(2026, 12, 31));

    // 50.00 * 1 day / 31 days = 1.61
    assertEquals(new BigDecimal("1.61"), prepaid);
  }

  @Test
  void computePrepaidInsurance_zeroWhenNoNextInstallment() {
    Loan loan = loan();
    addLine(loan, LocalDate.of(2026, 12, 1), "8471.27", "50.00");

    assertEquals(
        BigDecimal.ZERO, service.computePrepaidInsurance(loan, LocalDate.of(2026, 12, 31)));
  }

  @Test
  void generateClosureMove_buildsBalancedAdjustmentEntry() throws AxelorException {
    Loan loan = loan();
    addLine(loan, LocalDate.of(2026, 12, 1), "8471.27", "50.00");
    addLine(loan, LocalDate.of(2027, 1, 1), "0.00", "50.00");
    Move move = new Move();
    when(moveCreateService.createMove(
            any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any(),
            any(), any()))
        .thenReturn(move);

    Move result = service.generateClosureMove(loan, LocalDate.of(2026, 12, 31));

    assertSame(move, result);
    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
    ArgumentCaptor<Boolean> isDebitCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(moveLineCreateService, times(4))
        .createMoveLine(
            eq(move),
            any(),
            accountCaptor.capture(),
            amountCaptor.capture(),
            isDebitCaptor.capture(),
            any(),
            anyInt(),
            any(),
            any());

    List<Account> accounts = accountCaptor.getAllValues();
    List<BigDecimal> amounts = amountCaptor.getAllValues();
    List<Boolean> isDebits = isDebitCaptor.getAllValues();

    // Accrued interest: debit 661 / credit 16884
    assertSame(interestAccount, accounts.get(0));
    assertEquals(new BigDecimal("25.41"), amounts.get(0));
    assertEquals(Boolean.TRUE, isDebits.get(0));
    assertSame(accruedInterestAccount, accounts.get(1));
    assertEquals(new BigDecimal("25.41"), amounts.get(1));
    assertEquals(Boolean.FALSE, isDebits.get(1));
    // Prepaid insurance: debit 486 / credit 616
    assertSame(prepaidAccount, accounts.get(2));
    assertEquals(new BigDecimal("1.61"), amounts.get(2));
    assertEquals(Boolean.TRUE, isDebits.get(2));
    assertSame(insuranceAccount, accounts.get(3));
    assertEquals(new BigDecimal("1.61"), amounts.get(3));
    assertEquals(Boolean.FALSE, isDebits.get(3));
  }

  @Test
  void generateClosureMove_returnsNullWhenNothingToAdjust() throws AxelorException {
    Loan loan = loan();

    assertNull(service.generateClosureMove(loan, LocalDate.of(2026, 12, 31)));
  }
}
