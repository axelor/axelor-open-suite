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
import static org.mockito.Mockito.mock;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanManagementConfig;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanServiceImplTest {

  private LoanServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new LoanServiceImpl(mock(SequenceService.class));
  }

  private LoanManagementConfig config() {
    LoanManagementConfig c = new LoanManagementConfig();
    c.setJournal(new Journal());
    c.setBorrowingDebtAccount(new Account());
    c.setInterestExpenseAccount(new Account());
    c.setInsuranceExpenseAccount(new Account());
    c.setBankAccount(new Account());
    c.setAccruedInterestAccount(new Account());
    c.setPrepaidExpenseAccount(new Account());
    c.setAnnualInterestRate(new BigDecimal("3.6"));
    c.setComputationModeSelect(LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT);
    c.setDurationInMonth(12);
    c.setMonthlyInsuranceAmount(new BigDecimal("50"));
    return c;
  }

  @Test
  void copy_setsAccountsAndDefaultConditions() {
    Loan loan = new Loan();
    LoanManagementConfig c = config();
    loan.setLoanManagementConfig(c);

    service.copyManagementConfigToLoan(loan);

    assertEquals(c.getJournal(), loan.getJournal());
    assertEquals(c.getBorrowingDebtAccount(), loan.getBorrowingDebtAccount());
    assertEquals(c.getPrepaidExpenseAccount(), loan.getPrepaidExpenseAccount());
    assertEquals(0, new BigDecimal("3.6").compareTo(loan.getAnnualInterestRate()));
    assertEquals(
        LoanRepository.COMPUTATION_MODE_CONSTANT_PAYMENT,
        loan.getComputationModeSelect().intValue());
    assertEquals(12, loan.getDurationInMonth().intValue());
    assertEquals(0, new BigDecimal("50").compareTo(loan.getMonthlyInsuranceAmount()));
  }

  @Test
  void copy_overwritesExistingConditionsFromConfig() {
    Loan loan = new Loan();
    loan.setLoanManagementConfig(config());
    loan.setAnnualInterestRate(new BigDecimal("2.0"));
    loan.setDurationInMonth(24);

    service.copyManagementConfigToLoan(loan);

    assertEquals(0, new BigDecimal("3.6").compareTo(loan.getAnnualInterestRate()));
    assertEquals(12, loan.getDurationInMonth().intValue());
  }

  @Test
  void copy_nullConfig_clearsAccounts() {
    Loan loan = new Loan();
    loan.setJournal(new Journal());
    loan.setLoanManagementConfig(null);

    service.copyManagementConfigToLoan(loan);

    assertNull(loan.getJournal());
  }

  @Test
  void resetForCopy_resetsStatusReferenceAndSchedule() {
    Loan loan = new Loan();
    loan.setStatusSelect(LoanRepository.STATUS_VALIDATED);
    loan.setReference("EMP0001");
    loan.addLineListItem(new com.axelor.apps.account.db.LoanLine());

    service.resetForCopy(loan);

    assertEquals(LoanRepository.STATUS_DRAFT, loan.getStatusSelect().intValue());
    assertNull(loan.getReference());
    assertTrue(loan.getLineList().isEmpty());
  }
}
