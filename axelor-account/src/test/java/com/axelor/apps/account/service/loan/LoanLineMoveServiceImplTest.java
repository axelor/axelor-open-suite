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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import com.axelor.apps.account.db.repo.LoanLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoanLineMoveServiceImplTest {

  private MoveCreateService moveCreateService;
  private MoveLineCreateService moveLineCreateService;
  private MoveValidateService moveValidateService;
  private MoveRepository moveRepository;
  private LoanLineRepository loanLineRepository;

  private LoanLineMoveServiceImpl service;

  private Journal journal;
  private Currency currency;
  private Partner partner;
  private Account debtAccount;
  private Account interestAccount;
  private Account insuranceAccount;
  private Account bankAccount;

  @BeforeEach
  void setUp() throws AxelorException {
    moveCreateService = mock(MoveCreateService.class);
    moveLineCreateService = mock(MoveLineCreateService.class);
    moveValidateService = mock(MoveValidateService.class);
    moveRepository = mock(MoveRepository.class);
    loanLineRepository = mock(LoanLineRepository.class);
    service =
        new LoanLineMoveServiceImpl(
            moveCreateService,
            moveLineCreateService,
            moveValidateService,
            moveRepository,
            loanLineRepository);

    journal = new Journal();
    currency = new Currency();
    partner = new Partner();
    debtAccount = new Account();
    interestAccount = new Account();
    insuranceAccount = new Account();
    bankAccount = new Account();

    when(moveLineCreateService.createMoveLine(
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.anyBoolean(),
            any(),
            anyInt(),
            any(),
            any()))
        .thenReturn(new MoveLine());
    when(moveRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private Loan loan() {
    Loan loan = new Loan();
    loan.setReference("EMP0001");
    loan.setCompany(new Company());
    loan.setCurrency(currency);
    loan.setPartner(partner);
    loan.setJournal(journal);
    loan.setBorrowingDebtAccount(debtAccount);
    loan.setInterestExpenseAccount(interestAccount);
    loan.setInsuranceExpenseAccount(insuranceAccount);
    loan.setBankAccount(bankAccount);
    return loan;
  }

  private LoanLine line(
      Loan loan, String capital, String interest, String insurance, String total) {
    LoanLine line = new LoanLine();
    line.setLoan(loan);
    line.setInstallmentDate(LocalDate.of(2026, 1, 31));
    line.setCapitalAmount(new BigDecimal(capital));
    line.setInterestAmount(new BigDecimal(interest));
    line.setInsuranceAmount(new BigDecimal(insurance));
    line.setTotalAmount(new BigDecimal(total));
    return line;
  }

  @Test
  void generateMove_createsBalancedEntryWithFourLines() throws AxelorException {
    Loan loan = loan();
    LoanLine loanLine = line(loan, "100.00", "20.00", "5.00", "125.00");
    Move move = new Move();
    mockCreateMove(loan, move);

    Move result = service.generateMove(loanLine);

    assertSame(move, result);
    verify(moveRepository).save(move);

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
    ArgumentCaptor<Boolean> isDebitCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(moveLineCreateService, times(4))
        .createMoveLine(
            eq(move),
            eq(partner),
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

    // Debit: 164 capital, 661 interest, 616 insurance
    assertSame(debtAccount, accounts.get(0));
    assertEquals(new BigDecimal("100.00"), amounts.get(0));
    assertEquals(Boolean.TRUE, isDebits.get(0));
    assertSame(interestAccount, accounts.get(1));
    assertEquals(new BigDecimal("20.00"), amounts.get(1));
    assertEquals(Boolean.TRUE, isDebits.get(1));
    assertSame(insuranceAccount, accounts.get(2));
    assertEquals(new BigDecimal("5.00"), amounts.get(2));
    assertEquals(Boolean.TRUE, isDebits.get(2));
    // Credit: 512 total
    assertSame(bankAccount, accounts.get(3));
    assertEquals(new BigDecimal("125.00"), amounts.get(3));
    assertEquals(Boolean.FALSE, isDebits.get(3));

    // Balanced entry: sum of debits equals the credit.
    assertEquals(amounts.get(3), amounts.get(0).add(amounts.get(1)).add(amounts.get(2)));
  }

  @Test
  void generateMove_withoutInsurance_skipsInsuranceLine() throws AxelorException {
    Loan loan = loan();
    LoanLine loanLine = line(loan, "100.00", "20.00", "0.00", "120.00");
    Move move = new Move();
    mockCreateMove(loan, move);

    service.generateMove(loanLine);

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(moveLineCreateService, times(3))
        .createMoveLine(
            eq(move),
            eq(partner),
            accountCaptor.capture(),
            any(),
            org.mockito.ArgumentMatchers.anyBoolean(),
            any(),
            anyInt(),
            any(),
            any());
    org.junit.jupiter.api.Assertions.assertFalse(
        accountCaptor.getAllValues().contains(insuranceAccount));
  }

  @Test
  void generateMove_usesLoanFunctionalOrigin() throws AxelorException {
    Loan loan = loan();
    LoanLine loanLine = line(loan, "100.00", "20.00", "5.00", "125.00");
    Move move = new Move();
    mockCreateMove(loan, move);

    service.generateMove(loanLine);

    verify(moveCreateService)
        .createMove(
            eq(journal),
            eq(loan.getCompany()),
            eq(currency),
            eq(partner),
            eq(loanLine.getInstallmentDate()),
            eq(loanLine.getInstallmentDate()),
            isNull(),
            isNull(),
            eq(MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC),
            eq(MoveRepository.FUNCTIONAL_ORIGIN_LOAN),
            eq("EMP0001"),
            eq("EMP0001"),
            isNull());
  }

  private void mockCreateMove(Loan loan, Move move) throws AxelorException {
    when(moveCreateService.createMove(
            any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any(),
            any(), any()))
        .thenReturn(move);
  }
}
