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
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.db.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanConsistencyServiceImplTest {

  private MoveLineRepository moveLineRepository;
  private LoanConsistencyServiceImpl service;
  private Account debtAccount;

  @BeforeEach
  void setUp() {
    moveLineRepository = mock(MoveLineRepository.class);
    CurrencyScaleService scale = mock(CurrencyScaleService.class);
    when(scale.getCurrencyScale(nullable(Currency.class))).thenReturn(2);
    when(scale.getScaledValue(any(BigDecimal.class), anyInt()))
        .thenAnswer(i -> ((BigDecimal) i.getArgument(0)).setScale(2, RoundingMode.HALF_UP));
    service = new LoanConsistencyServiceImpl(moveLineRepository, scale);
    debtAccount = new Account();
  }

  private Loan loanWithLines() {
    Loan loan = new Loan();
    loan.setCurrency(new Currency());
    loan.setAmount(new BigDecimal("100000"));
    loan.setBorrowingDebtAccount(debtAccount);
    loan.setLineList(new java.util.ArrayList<>());
    LoanLine booked = line("8196.73");
    Move move = new Move();
    move.setId(1L);
    booked.setAccountMove(move);
    loan.getLineList().add(booked);
    loan.getLineList().add(line("91803.27"));
    return loan;
  }

  private LoanLine line(String capital) {
    LoanLine line = new LoanLine();
    line.setCapitalAmount(new BigDecimal(capital));
    return line;
  }

  private MoveLine moveLine(String debit, String credit) {
    MoveLine moveLine = new MoveLine();
    moveLine.setDebit(new BigDecimal(debit));
    moveLine.setCredit(new BigDecimal(credit));
    return moveLine;
  }

  @SuppressWarnings("unchecked")
  private void mockAccountMoveLines(List<MoveLine> moveLineList) {
    Query<MoveLine> query = mock(Query.class, RETURNS_SELF);
    when(moveLineRepository.all()).thenReturn(query);
    when(query.fetch()).thenReturn(moveLineList);
  }

  @Test
  void computeTheoreticalOutstanding_sumsNonBookedCapital() {
    Loan loan = loanWithLines();

    assertEquals(
        0, new BigDecimal("91803.27").compareTo(service.computeTheoreticalOutstanding(loan)));
  }

  @Test
  void computeGap_isZeroWhenAccountingMatchesSchedule() {
    Loan loan = loanWithLines();
    // account 164 debit for the loan = capital booked (8196.73) -> outstanding 100000 - 8196.73
    mockAccountMoveLines(List.of(moveLine("8196.73", "0.00")));

    assertEquals(0, BigDecimal.ZERO.compareTo(service.computeGap(loan)));
  }

  @Test
  void computeGap_nonZeroWhenAccountingDivergesFromSchedule() {
    Loan loan = loanWithLines();
    // account 164 debit diverges (9000 instead of 8196.73): outstanding 100000 - 9000 = 91000
    mockAccountMoveLines(List.of(moveLine("9000.00", "0.00")));

    // 91803.27 - 91000.00
    assertEquals(0, new BigDecimal("803.27").compareTo(service.computeGap(loan)));
  }
}
