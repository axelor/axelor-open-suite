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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.LoanLineRepository;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoanLineMoveServiceImpl implements LoanLineMoveService {

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected LoanLineRepository loanLineRepository;

  @Inject
  public LoanLineMoveServiceImpl(
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      LoanLineRepository loanLineRepository) {
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
    this.loanLineRepository = loanLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateMove(LoanLine loanLine) throws AxelorException {
    Objects.requireNonNull(loanLine);
    Loan loan = loanLine.getLoan();
    checkAccounts(loan, loanLine);

    Company company = loan.getCompany();
    LocalDate date = loanLine.getInstallmentDate();
    String origin = loan.getReference();

    Move move =
        moveCreateService.createMove(
            loan.getJournal(),
            company,
            loan.getCurrency(),
            loan.getPartner(),
            date,
            date,
            null,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_LOAN,
            origin,
            origin,
            null);

    List<MoveLine> moveLines = new ArrayList<>();
    int counter = 1;
    counter =
        addDebitLine(
            move,
            moveLines,
            loan,
            loan.getBorrowingDebtAccount(),
            loanLine.getCapitalAmount(),
            date,
            counter);
    counter =
        addDebitLine(
            move,
            moveLines,
            loan,
            loan.getInterestExpenseAccount(),
            loanLine.getInterestAmount(),
            date,
            counter);
    counter =
        addDebitLine(
            move,
            moveLines,
            loan,
            loan.getInsuranceExpenseAccount(),
            loanLine.getInsuranceAmount(),
            date,
            counter);

    MoveLine creditMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            loan.getPartner(),
            loan.getBankAccount(),
            loanLine.getTotalAmount(),
            false,
            date,
            counter,
            origin,
            origin);
    moveLines.add(creditMoveLine);

    if (move.getMoveLineList() == null) {
      move.setMoveLineList(new ArrayList<>());
    }
    move.getMoveLineList().addAll(moveLines);

    return moveRepository.save(move);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move postInstallment(LoanLine loanLine, boolean isBatch) throws AxelorException {
    Objects.requireNonNull(loanLine);
    Loan loan = loanLine.getLoan();

    if (loan == null
        || loan.getStatusSelect() == null
        || loan.getStatusSelect() < LoanRepository.STATUS_VALIDATED) {
      throw new AxelorException(
          LoanLine.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_NOT_VALIDATED));
    }
    if (loanLine.getAccountMove() != null) {
      throw new AxelorException(
          LoanLine.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_LINE_ALREADY_POSTED));
    }
    if (hasEarlierPlannedLine(loanLine)) {
      throw new AxelorException(
          LoanLine.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_LINE_NOT_SEQUENTIAL));
    }

    Move move = generateMove(loanLine);
    moveValidateService.accounting(move);

    loanLine.setAccountMove(move);
    updateLoanAfterPosting(loan, loanLine);
    loanLineRepository.save(loanLine);

    return move;
  }

  protected int addDebitLine(
      Move move,
      List<MoveLine> moveLines,
      Loan loan,
      Account account,
      BigDecimal amount,
      LocalDate date,
      int counter)
      throws AxelorException {
    if (amount == null || amount.signum() == 0) {
      return counter;
    }
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            loan.getPartner(),
            account,
            amount,
            true,
            date,
            counter,
            loan.getReference(),
            loan.getReference());
    moveLines.add(moveLine);
    return counter + 1;
  }

  /**
   * Switches the loan to Ongoing on the first booked installment, sets its remaining debt to the
   * remaining debt after this installment, and closes it once no planned installment remains.
   */
  protected void updateLoanAfterPosting(Loan loan, LoanLine loanLine) {
    if (loan.getStatusSelect() == LoanRepository.STATUS_VALIDATED) {
      loan.setStatusSelect(LoanRepository.STATUS_ONGOING);
    }
    loan.setRemainingDebt(loanLine.getRemainingDebtAfter());

    long remainingPlanned =
        loanLineRepository
            .all()
            .filter("self.loan = :loan AND self.accountMove IS NULL AND self.id != :id")
            .bind("loan", loan)
            .bind("id", loanLine.getId())
            .count();
    if (remainingPlanned == 0) {
      loan.setStatusSelect(LoanRepository.STATUS_CLOSED);
    }
  }

  protected boolean hasEarlierPlannedLine(LoanLine loanLine) {
    return loanLineRepository
            .all()
            .filter(
                "self.loan = :loan AND self.accountMove IS NULL AND self.installmentDate < :date")
            .bind("loan", loanLine.getLoan())
            .bind("date", loanLine.getInstallmentDate())
            .count()
        > 0;
  }

  protected void checkAccounts(Loan loan, LoanLine loanLine) throws AxelorException {
    boolean missing =
        loan == null
            || loan.getJournal() == null
            || loan.getBankAccount() == null
            || (isPositive(loanLine.getCapitalAmount()) && loan.getBorrowingDebtAccount() == null)
            || (isPositive(loanLine.getInterestAmount())
                && loan.getInterestExpenseAccount() == null)
            || (isPositive(loanLine.getInsuranceAmount())
                && loan.getInsuranceExpenseAccount() == null);
    if (missing) {
      throw new AxelorException(
          LoanLine.class,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LOAN_ACCOUNT_MISSING),
          loan != null && loan.getReference() != null ? loan.getReference() : "");
    }
  }

  protected boolean isPositive(BigDecimal amount) {
    return amount != null && amount.signum() != 0;
  }
}
