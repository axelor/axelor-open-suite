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

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.repo.LoanLineRepository;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LoanAdjustmentServiceImpl implements LoanAdjustmentService {

  protected static final MathContext MC = MathContext.DECIMAL64;
  protected static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  protected static final BigDecimal TWELVE = BigDecimal.valueOf(12);
  protected static final String SNAPSHOT_LINE_SEPARATOR = "\n";
  protected static final String SNAPSHOT_FIELD_SEPARATOR = ";";

  protected LoanLineComputationService loanLineComputationService;
  protected LoanRepository loanRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public LoanAdjustmentServiceImpl(
      LoanLineComputationService loanLineComputationService,
      LoanRepository loanRepository,
      CurrencyScaleService currencyScaleService) {
    this.loanLineComputationService = loanLineComputationService;
    this.loanRepository = loanRepository;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public LoanLine getNextUnpaidLine(Loan loan) {
    return orderedPlannedLines(loan).stream().findFirst().orElse(null);
  }

  @Override
  public void computeEditedLine(LoanLine loanLine) throws AxelorException {
    Loan loan = loanLine.getLoan();
    BigDecimal rdBefore = nz(loanLine.getRemainingDebtBefore());
    BigDecimal interest = nz(loanLine.getInterestAmount());
    BigDecimal capital = nz(loanLine.getCapitalAmount());
    BigDecimal insurance = nz(loanLine.getInsuranceAmount());
    BigDecimal total = nz(loanLine.getTotalAmount());

    if (interest.signum() < 0 || capital.signum() < 0 || insurance.signum() < 0) {
      throw new AxelorException(
          LoanLine.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_LINE_NEGATIVE_AMOUNT));
    }

    if (nz(loanLine.getEditedFieldSelect()) == LoanLineRepository.EDITED_FIELD_INTEREST) {
      // Keep the installment total and adjust the capital repayment.
      capital = scale(loan, total.subtract(interest).subtract(insurance));
      if (capital.signum() < 0) {
        capital = BigDecimal.ZERO;
      }
    }

    if (capital.compareTo(rdBefore) > 0) {
      throw new AxelorException(
          LoanLine.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_LINE_CAPITAL_EXCEEDS_REMAINING_DEBT));
    }

    loanLine.setCapitalAmount(capital);
    loanLine.setTotalAmount(scale(loan, interest.add(capital).add(insurance)));
    loanLine.setRemainingDebtAfter(scale(loan, rdBefore.subtract(capital)));
    loanLine.setIsManuallyModified(true);
  }

  @Override
  public List<LoanLine> recomputeSchedule(Loan loan) throws AxelorException {
    List<LoanLine> planned = orderedPlannedLines(loan);
    LoanLine anchor =
        planned.stream()
            .filter(line -> nz(line.getEditedFieldSelect()) != 0)
            .findFirst()
            .orElse(null);
    if (anchor == null) {
      return orderedLines(loan);
    }
    int field = nz(anchor.getEditedFieldSelect());
    int anchorIndex = planned.indexOf(anchor);
    List<LoanLine> after = planned.subList(anchorIndex + 1, planned.size());

    switch (field) {
      case LoanLineRepository.EDITED_FIELD_CAPITAL:
        List<LoanLine> tail =
            loanLineComputationService.computeLinesFrom(
                loan,
                anchor.getRemainingDebtAfter(),
                anchor.getInstallmentDate().plusMonths(1),
                after.size());
        replaceLines(loan, after, tail);
        break;
      case LoanLineRepository.EDITED_FIELD_INSURANCE:
        carryInsurance(loan, anchor.getInsuranceAmount(), after);
        break;
      case LoanLineRepository.EDITED_FIELD_INTEREST:
      default:
        regularizeOnLastLine(loan, planned.subList(anchorIndex, planned.size()));
        break;
    }

    planned.forEach(line -> line.setEditedFieldSelect(LoanLineRepository.EDITED_FIELD_NONE));
    return orderedLines(loan);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void defer(
      Loan loan,
      int installmentCount,
      boolean capitalizeInterest,
      boolean recomputePayment,
      boolean keepInsurance)
      throws AxelorException {
    List<LoanLine> planned = orderedPlannedLines(loan);
    if (planned.isEmpty() || installmentCount <= 0) {
      throw new AxelorException(
          Loan.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_NO_INSTALLMENT_TO_ADJUST));
    }
    takeSnapshot(loan);

    LoanLine first = planned.get(0);
    LocalDate startDate = first.getInstallmentDate();
    BigDecimal rd = first.getRemainingDebtBefore();
    BigDecimal t = monthlyRate(loan);
    BigDecimal insurance = keepInsurance ? insuranceOf(loan) : BigDecimal.ZERO;
    int resumeCount = planned.size();

    List<LoanLine> deferralLines = new ArrayList<>();
    for (int j = 0; j < installmentCount; j++) {
      BigDecimal interest = scale(loan, rd.multiply(t));
      LoanLine line = new LoanLine();
      line.setInstallmentDate(startDate.plusMonths(j));
      line.setRemainingDebtBefore(rd);
      line.setInsuranceAmount(insurance);
      line.setCapitalAmount(BigDecimal.ZERO);
      line.setIsManuallyModified(true);
      line.setIsDeferral(true);
      if (capitalizeInterest) {
        line.setInterestAmount(BigDecimal.ZERO);
        rd = scale(loan, rd.add(interest));
        line.setTotalAmount(insurance);
      } else {
        line.setInterestAmount(interest);
        line.setTotalAmount(scale(loan, interest.add(insurance)));
      }
      line.setRemainingDebtAfter(rd);
      deferralLines.add(line);
    }

    if (capitalizeInterest && recomputePayment) {
      // Recompute the following installments on the new (higher) remaining debt.
      LocalDate resumeDate = startDate.plusMonths(installmentCount);
      List<LoanLine> resumed =
          loanLineComputationService.computeLinesFrom(loan, rd, resumeDate, resumeCount);
      removePlannedLines(loan);
      resumed.forEach(loan::addLineListItem);
    } else {
      // Keep the existing installments (including manual edits): only shift their dates.
      for (LoanLine plannedLine : planned) {
        plannedLine.setInstallmentDate(
            plannedLine.getInstallmentDate().plusMonths(installmentCount));
      }
    }
    deferralLines.forEach(loan::addLineListItem);
    loanRepository.save(loan);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelDeferral(Loan loan) throws AxelorException {
    if (loan.getScheduleSnapshot() == null || loan.getScheduleSnapshot().isBlank()) {
      throw new AxelorException(
          Loan.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LOAN_NO_ADJUSTMENT_TO_CANCEL));
    }
    // Remove the (planned) deferral installments and shift the other installments back, without
    // resetting their amounts (manual edits are preserved). Booked installments are out of scope.
    long deferralCount =
        loan.getLineList().stream()
            .filter(
                line -> Boolean.TRUE.equals(line.getIsDeferral()) && line.getAccountMove() == null)
            .count();
    loan.getLineList()
        .removeIf(
            line -> Boolean.TRUE.equals(line.getIsDeferral()) && line.getAccountMove() == null);

    if (deferralCount > 0) {
      for (LoanLine line : orderedPlannedLines(loan)) {
        line.setInstallmentDate(line.getInstallmentDate().minusMonths(deferralCount));
      }
    }
    loan.setScheduleSnapshot(null);
    loanRepository.save(loan);
  }

  /** Re-amortizes keeping each installment capital fixed; the last installment reaches zero. */
  protected void regularizeOnLastLine(Loan loan, List<LoanLine> lines) {
    for (int i = 1; i < lines.size(); i++) {
      LoanLine line = lines.get(i);
      BigDecimal rdBefore = lines.get(i - 1).getRemainingDebtAfter();
      line.setRemainingDebtBefore(rdBefore);
      BigDecimal capital = (i == lines.size() - 1) ? rdBefore : nz(line.getCapitalAmount());
      line.setCapitalAmount(capital);
      line.setRemainingDebtAfter(scale(loan, rdBefore.subtract(capital)));
      line.setTotalAmount(
          scale(
              loan, nz(line.getInterestAmount()).add(capital).add(nz(line.getInsuranceAmount()))));
    }
  }

  protected void carryInsurance(Loan loan, BigDecimal insurance, List<LoanLine> lines) {
    for (LoanLine line : lines) {
      line.setInsuranceAmount(insurance);
      line.setTotalAmount(
          scale(
              loan,
              nz(line.getInterestAmount()).add(nz(line.getCapitalAmount())).add(nz(insurance))));
    }
  }

  protected void replaceLines(Loan loan, List<LoanLine> toRemove, List<LoanLine> toAdd) {
    List<LoanLine> removable = new ArrayList<>(toRemove);
    loan.getLineList().removeAll(removable);
    toAdd.forEach(loan::addLineListItem);
  }

  protected void removePlannedLines(Loan loan) {
    if (loan.getLineList() != null) {
      loan.getLineList().removeIf(line -> line.getAccountMove() == null);
    }
  }

  protected List<LoanLine> orderedLines(Loan loan) {
    if (loan.getLineList() == null) {
      return new ArrayList<>();
    }
    return loan.getLineList().stream()
        .sorted(Comparator.comparing(LoanLine::getInstallmentDate))
        .collect(Collectors.toList());
  }

  protected List<LoanLine> orderedPlannedLines(Loan loan) {
    if (loan.getLineList() == null) {
      return new ArrayList<>();
    }
    return loan.getLineList().stream()
        .filter(line -> line.getAccountMove() == null && line.getInstallmentDate() != null)
        .sorted(Comparator.comparing(LoanLine::getInstallmentDate))
        .collect(Collectors.toList());
  }

  protected BigDecimal monthlyRate(Loan loan) {
    if (loan.getAnnualInterestRate() == null) {
      return BigDecimal.ZERO;
    }
    return loan.getAnnualInterestRate().divide(HUNDRED, MC).divide(TWELVE, MC);
  }

  protected BigDecimal insuranceOf(Loan loan) {
    return loan.getMonthlyInsuranceAmount() == null
        ? BigDecimal.ZERO
        : loan.getMonthlyInsuranceAmount();
  }

  protected BigDecimal scale(Loan loan, BigDecimal value) {
    return currencyScaleService.getScaledValue(
        value, currencyScaleService.getCurrencyScale(loan.getCurrency()));
  }

  protected void takeSnapshot(Loan loan) {
    String snapshot =
        orderedPlannedLines(loan).stream()
            .map(this::serializeLine)
            .collect(Collectors.joining(SNAPSHOT_LINE_SEPARATOR));
    loan.setScheduleSnapshot(snapshot);
  }

  protected String serializeLine(LoanLine line) {
    return String.join(
        SNAPSHOT_FIELD_SEPARATOR,
        line.getInstallmentDate().toString(),
        plain(line.getRemainingDebtBefore()),
        plain(line.getInterestAmount()),
        plain(line.getCapitalAmount()),
        plain(line.getInsuranceAmount()),
        plain(line.getTotalAmount()),
        plain(line.getRemainingDebtAfter()),
        Boolean.toString(Boolean.TRUE.equals(line.getIsManuallyModified())));
  }

  protected String plain(BigDecimal value) {
    return nz(value).toPlainString();
  }

  protected BigDecimal nz(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  protected int nz(Integer value) {
    return value == null ? 0 : value;
  }
}
