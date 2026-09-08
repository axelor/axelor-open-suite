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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanManagementConfig;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class LoanValidateServiceImplTest {

  private LoanRepository loanRepository;
  private LoanService loanService;
  private LoanLineGenerationService loanLineGenerationService;

  private LoanValidateServiceImpl service;
  private Company company;
  private LoanManagementConfig config;

  @BeforeEach
  void setUp() throws AxelorException {
    loanRepository = mock(LoanRepository.class);
    loanService = mock(LoanService.class);
    loanLineGenerationService = mock(LoanLineGenerationService.class);
    service = new LoanValidateServiceImpl(loanRepository, loanService, loanLineGenerationService);
    company = new Company();
    config = new LoanManagementConfig();
    config.setJournal(new Journal());
    config.setBorrowingDebtAccount(new Account());
    config.setInterestExpenseAccount(new Account());
    config.setInsuranceExpenseAccount(new Account());
    config.setBankAccount(new Account());
    config.setAccruedInterestAccount(new Account());
    config.setPrepaidExpenseAccount(new Account());
    when(loanService.generateReference(ArgumentMatchers.any())).thenReturn("EMP0001");
  }

  private Loan draftLoan() {
    Loan loan = new Loan();
    loan.setStatusSelect(LoanRepository.STATUS_DRAFT);
    loan.setCompany(company);
    loan.setLoanManagementConfig(config);
    return loan;
  }

  @Test
  void validate_draftLoan_setsValidatedAndReference() throws AxelorException {
    Loan loan = draftLoan();

    service.validate(loan);

    assertEquals(LoanRepository.STATUS_VALIDATED, loan.getStatusSelect().intValue());
    assertEquals("EMP0001", loan.getReference());
  }

  @Test
  void validate_draftLoanWithReference_keepsExistingReference() throws AxelorException {
    Loan loan = draftLoan();
    loan.setReference("EXISTING");

    service.validate(loan);

    assertEquals("EXISTING", loan.getReference());
  }

  // Note: the guard branches (non-draft status, missing config) throw an AxelorException built with
  // I18n.get(...), which loads messages from the DB. In a unit test without a JPA EntityManager
  // that
  // call fails once the I18n bundle has been initialized by another test in the module suite,
  // making
  // such assertThrows tests flaky. AOS service unit tests avoid exercising I18n-message paths; the
  // guards are covered functionally instead.
}
