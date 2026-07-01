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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LoanAdjustmentServiceImpl implements LoanAdjustmentService {

  protected static final MathContext MC = MathContext.DECIMAL64;
  protected static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  protected static final BigDecimal TWELVE = BigDecimal.valueOf(12);
  protected static final int MAX_INSTALLMENTS = 1200;
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
  public void computeEditedLine(LoanLine loanLine) {
    Loan loan = loanLine.getLoan();
    BigDecimal rdBefore = nz(loanLine.getRemainingDebtBefore());
    BigDecimal interest = nz(loanLine.getInterestAmount());
    BigDecimal capital = nz(loanLine.getCapitalAmount());
    BigDecimal insurance = nz(loanLine.getInsuranceAmount());
    BigDecimal total = nz(loanLine.getTotalAmount());

    if (nz(loanLine.getEditedFieldSelect()) == LoanLineRepository.EDITED_FIELD_INTEREST) {
      // Keep the installment total and adjust the capital repayment.
      capital = scale(loan, total.subtract(interest).subtract(insurance));
      if (capital.signum() < 0) {
        capital = BigDecimal.ZERO;
      }
      loanLine.setCapitalAmount(capital);
    } else {
      loanLine.setTotalAmount(scale(loan, interest.add(capital).add(insurance)));
    }
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
    BigDecimal originalPayment =
        scale(loan, nz(first.getInterestAmount()).add(nz(first.getCapitalAmount())));

    List<LoanLine> deferralLines = new ArrayList<>();
    for (int j = 0; j < installmentCount; j++) {
      BigDecimal interest = scale(loan, rd.multiply(t));
      LoanLine line = new LoanLine();
      line.setInstallmentDate(startDate.plusMonths(j));
      line.setRemainingDebtBefore(rd);
      line.setInsuranceAmount(insurance);
      line.setCapitalAmount(BigDecimal.ZERO);
      line.setIsManuallyModified(true);
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

    LocalDate resumeDate = startDate.plusMonths(installmentCount);
    List<LoanLine> resumed;
    if (capitalizeInterest && !recomputePayment) {
      resumed = amortizeWithFixedPayment(loan, rd, resumeDate, originalPayment);
    } else {
      resumed = loanLineComputationService.computeLinesFrom(loan, rd, resumeDate, resumeCount);
    }

    removePlannedLines(loan);
    deferralLines.forEach(loan::addLineListItem);
    resumed.forEach(loan::addLineListItem);
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
    List<LoanLine> restored = deserializeSnapshot(loan);
    removePlannedLines(loan);
    restored.forEach(loan::addLineListItem);
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

  protected List<LoanLine> amortizeWithFixedPayment(
      Loan loan, BigDecimal startRd, LocalDate startDate, BigDecimal payment) {
    List<LoanLine> lines = new ArrayList<>();
    BigDecimal t = monthlyRate(loan);
    BigDecimal insurance = insuranceOf(loan);
    BigDecimal rd = startRd;
    int i = 0;
    while (rd.signum() > 0 && i < MAX_INSTALLMENTS) {
      BigDecimal interest = scale(loan, rd.multiply(t));
      BigDecimal capital = scale(loan, payment.subtract(interest));
      if (capital.signum() <= 0) {
        capital = rd; // payment cannot cover interest: close the schedule
      }
      if (capital.compareTo(rd) >= 0) {
        capital = rd;
      }
      BigDecimal rdAfter = scale(loan, rd.subtract(capital));
      LoanLine line = new LoanLine();
      line.setInstallmentDate(startDate.plusMonths(i));
      line.setRemainingDebtBefore(rd);
      line.setInterestAmount(interest);
      line.setCapitalAmount(capital);
      line.setInsuranceAmount(insurance);
      line.setTotalAmount(scale(loan, interest.add(capital).add(insurance)));
      line.setRemainingDebtAfter(rdAfter);
      lines.add(line);
      rd = rdAfter;
      i++;
    }
    return lines;
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

  protected List<LoanLine> deserializeSnapshot(Loan loan) {
    return Arrays.stream(loan.getScheduleSnapshot().split(SNAPSHOT_LINE_SEPARATOR))
        .filter(row -> !row.isBlank())
        .map(this::deserializeLine)
        .collect(Collectors.toList());
  }

  protected LoanLine deserializeLine(String row) {
    String[] parts = row.split(SNAPSHOT_FIELD_SEPARATOR, -1);
    LoanLine line = new LoanLine();
    line.setInstallmentDate(LocalDate.parse(parts[0]));
    line.setRemainingDebtBefore(new BigDecimal(parts[1]));
    line.setInterestAmount(new BigDecimal(parts[2]));
    line.setCapitalAmount(new BigDecimal(parts[3]));
    line.setInsuranceAmount(new BigDecimal(parts[4]));
    line.setTotalAmount(new BigDecimal(parts[5]));
    line.setRemainingDebtAfter(new BigDecimal(parts[6]));
    line.setIsManuallyModified(Boolean.parseBoolean(parts[7]));
    return line;
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
