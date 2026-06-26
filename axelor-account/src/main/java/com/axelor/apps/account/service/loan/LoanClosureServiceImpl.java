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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LoanClosureServiceImpl implements LoanClosureService {

  protected static final int DAY_COUNT_BASIS = 360;
  protected static final int DEFAULT_SCALE = 2;

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveReverseService moveReverseService;
  protected MoveRepository moveRepository;

  @Inject
  public LoanClosureServiceImpl(
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      MoveReverseService moveReverseService,
      MoveRepository moveRepository) {
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveValidateService = moveValidateService;
    this.moveReverseService = moveReverseService;
    this.moveRepository = moveRepository;
  }

  @Override
  public BigDecimal computeAccruedInterest(Loan loan, LocalDate closingDate) {
    LoanLine lastLine = getLastInstallmentBefore(loan, closingDate);
    if (lastLine == null
        || lastLine.getRemainingDebtAfter() == null
        || loan.getAnnualInterestRate() == null) {
      return BigDecimal.ZERO;
    }
    long days = ChronoUnit.DAYS.between(lastLine.getInstallmentDate(), closingDate);
    if (days <= 0) {
      return BigDecimal.ZERO;
    }
    // CRD x annual rate (%) x days / (100 x 360)
    BigDecimal accrued =
        lastLine
            .getRemainingDebtAfter()
            .multiply(loan.getAnnualInterestRate())
            .multiply(BigDecimal.valueOf(days))
            .divide(
                BigDecimal.valueOf(100L * DAY_COUNT_BASIS), getScale(loan), RoundingMode.HALF_UP);
    return accrued;
  }

  @Override
  public BigDecimal computePrepaidInsurance(Loan loan, LocalDate closingDate) {
    LoanLine straddlingLine = getLastInstallmentBefore(loan, closingDate);
    LoanLine nextLine = getFirstInstallmentAfter(loan, closingDate);
    if (straddlingLine == null
        || nextLine == null
        || straddlingLine.getInsuranceAmount() == null
        || straddlingLine.getInsuranceAmount().signum() == 0) {
      return BigDecimal.ZERO;
    }
    long totalDays =
        ChronoUnit.DAYS.between(straddlingLine.getInstallmentDate(), nextLine.getInstallmentDate());
    long daysAfterClosing = ChronoUnit.DAYS.between(closingDate, nextLine.getInstallmentDate());
    if (totalDays <= 0 || daysAfterClosing <= 0) {
      return BigDecimal.ZERO;
    }
    return straddlingLine
        .getInsuranceAmount()
        .multiply(BigDecimal.valueOf(daysAfterClosing))
        .divide(BigDecimal.valueOf(totalDays), getScale(loan), RoundingMode.HALF_UP);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateClosureMove(Loan loan, LocalDate closingDate) throws AxelorException {
    Objects.requireNonNull(loan);
    Objects.requireNonNull(closingDate);

    BigDecimal accruedInterest = computeAccruedInterest(loan, closingDate);
    BigDecimal prepaidInsurance = computePrepaidInsurance(loan, closingDate);
    if (accruedInterest.signum() == 0 && prepaidInsurance.signum() == 0) {
      return null;
    }
    checkAccounts(loan, accruedInterest, prepaidInsurance);

    String origin = loan.getReference();
    Move move =
        moveCreateService.createMove(
            loan.getJournal(),
            loan.getCompany(),
            loan.getCurrency(),
            loan.getPartner(),
            closingDate,
            closingDate,
            null,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_LOAN_CLOSURE,
            origin,
            origin,
            null);

    List<MoveLine> moveLines = new ArrayList<>();
    int counter = 1;
    if (accruedInterest.signum() != 0) {
      counter =
          addLine(
              move,
              moveLines,
              loan,
              loan.getInterestExpenseAccount(),
              accruedInterest,
              true,
              closingDate,
              counter);
      counter =
          addLine(
              move,
              moveLines,
              loan,
              loan.getAccruedInterestAccount(),
              accruedInterest,
              false,
              closingDate,
              counter);
    }
    if (prepaidInsurance.signum() != 0) {
      counter =
          addLine(
              move,
              moveLines,
              loan,
              loan.getPrepaidExpenseAccount(),
              prepaidInsurance,
              true,
              closingDate,
              counter);
      counter =
          addLine(
              move,
              moveLines,
              loan,
              loan.getInsuranceExpenseAccount(),
              prepaidInsurance,
              false,
              closingDate,
              counter);
    }

    if (move.getMoveLineList() == null) {
      move.setMoveLineList(new ArrayList<>());
    }
    move.getMoveLineList().addAll(moveLines);

    return moveRepository.save(move);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move postClosure(Loan loan, LocalDate closingDate) throws AxelorException {
    Objects.requireNonNull(loan);
    Objects.requireNonNull(closingDate);

    if (hasClosureMove(loan, closingDate)) {
      return null;
    }

    Move move = generateClosureMove(loan, closingDate);
    if (move == null) {
      return null;
    }
    moveValidateService.accounting(move);
    moveReverseService.generateReverse(move, false, true, false, closingDate.plusDays(1));

    return move;
  }

  protected int addLine(
      Move move,
      List<MoveLine> moveLines,
      Loan loan,
      Account account,
      BigDecimal amount,
      boolean isDebit,
      LocalDate date,
      int counter)
      throws AxelorException {
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            loan.getPartner(),
            account,
            amount,
            isDebit,
            date,
            counter,
            loan.getReference(),
            loan.getReference());
    moveLines.add(moveLine);
    return counter + 1;
  }

  protected boolean hasClosureMove(Loan loan, LocalDate closingDate) {
    return moveRepository
            .all()
            .filter(
                "self.origin = :origin AND self.date = :date"
                    + " AND self.functionalOriginSelect = :functionalOrigin"
                    + " AND self.company = :company")
            .bind("origin", loan.getReference())
            .bind("date", closingDate)
            .bind("functionalOrigin", MoveRepository.FUNCTIONAL_ORIGIN_LOAN_CLOSURE)
            .bind("company", loan.getCompany())
            .count()
        > 0;
  }

  protected void checkAccounts(Loan loan, BigDecimal accruedInterest, BigDecimal prepaidInsurance)
      throws AxelorException {
    boolean missing =
        loan.getJournal() == null
            || (accruedInterest.signum() != 0
                && (loan.getInterestExpenseAccount() == null
                    || loan.getAccruedInterestAccount() == null))
            || (prepaidInsurance.signum() != 0
                && (loan.getPrepaidExpenseAccount() == null
                    || loan.getInsuranceExpenseAccount() == null));
    if (missing) {
      throw new AxelorException(
          Loan.class,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LOAN_CLOSURE_ACCOUNT_MISSING),
          loan.getReference() != null ? loan.getReference() : "");
    }
  }

  protected LoanLine getLastInstallmentBefore(Loan loan, LocalDate closingDate) {
    if (loan.getLineList() == null) {
      return null;
    }
    return loan.getLineList().stream()
        .filter(line -> line.getInstallmentDate() != null)
        .filter(line -> !line.getInstallmentDate().isAfter(closingDate))
        .max(Comparator.comparing(LoanLine::getInstallmentDate))
        .orElse(null);
  }

  protected LoanLine getFirstInstallmentAfter(Loan loan, LocalDate closingDate) {
    if (loan.getLineList() == null) {
      return null;
    }
    return loan.getLineList().stream()
        .filter(line -> line.getInstallmentDate() != null)
        .filter(line -> line.getInstallmentDate().isAfter(closingDate))
        .min(Comparator.comparing(LoanLine::getInstallmentDate))
        .orElse(null);
  }

  protected int getScale(Loan loan) {
    return loan.getCurrency() != null && loan.getCurrency().getNumberOfDecimals() != null
        ? loan.getCurrency().getNumberOfDecimals()
        : DEFAULT_SCALE;
  }
}
