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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementQueryRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankReconciliationReconciliationServiceImpl
    implements BankReconciliationReconciliationService {

  protected BankStatementQueryRepository bankStatementQueryRepository;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationQueryService bankReconciliationQueryService;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected CurrencyService currencyService;
  protected DateService dateService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationReconciliationServiceImpl(
      BankStatementQueryRepository bankStatementQueryRepository,
      MoveLineRepository moveLineRepository,
      BankReconciliationQueryService bankReconciliationQueryService,
      BankReconciliationLineService bankReconciliationLineService,
      CurrencyService currencyService,
      DateService dateService,
      CurrencyScaleService currencyScaleService) {
    this.bankStatementQueryRepository = bankStatementQueryRepository;
    this.moveLineRepository = moveLineRepository;
    this.bankReconciliationQueryService = bankReconciliationQueryService;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.currencyService = currencyService;
    this.dateService = dateService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BankReconciliation reconciliateAccordingToQueries(BankReconciliation bankReconciliation)
      throws AxelorException {
    List<BankStatementQuery> bankStatementQueries =
        bankStatementQueryRepository
            .findByRuleTypeSelect(BankStatementRuleRepository.RULE_TYPE_RECONCILIATION_AUTO)
            .fetch();
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(bankReconciliationQueryService.getRequestMoveLines())
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();

    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliation.getBankReconciliationLineList().stream()
            .filter(line -> line.getMoveLine() == null)
            .collect(Collectors.toList());

    BigInteger dateMargin =
        BigInteger.valueOf(
            bankReconciliation
                .getCompany()
                .getBankPaymentConfig()
                .getBnkStmtAutoReconcileDateMargin());

    BigDecimal amountMarginLow = this.getAmountMarginLow(bankReconciliation);
    BigDecimal amountMarginHigh = BigDecimal.ONE;

    Context scriptContext;

    for (BankStatementQuery bankStatementQuery : bankStatementQueries) {
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
        if (bankReconciliationLine.getMoveLine() != null || bankStatementLine == null) {
          continue;
        }
        for (MoveLine moveLine : moveLines) {
          bankStatementLine.setMoveLine(moveLine);

          scriptContext =
              this.getScriptContext(
                  bankReconciliation, bankStatementLine, bankReconciliationLine, moveLine);
          String query =
              computeQuery(bankStatementQuery, dateMargin, amountMarginLow, amountMarginHigh);
          Boolean result = (Boolean) new GroovyScriptHelper(scriptContext).eval(query);

          if (result) {
            bankReconciliationLine =
                updateBankReconciliationLine(bankReconciliationLine, moveLine, bankStatementQuery);
            boolean isUnderCorrection =
                bankReconciliation.getStatusSelect()
                    == BankReconciliationRepository.STATUS_UNDER_CORRECTION;

            if (isUnderCorrection) {
              bankReconciliationLine.setIsPosted(true);
              bankReconciliationLineService.checkAmount(bankReconciliationLine);
              bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
            }

            moveLine.setPostedNbr(bankReconciliationLine.getPostedNbr());
            moveLines.remove(moveLine);
            break;
          }

          bankStatementLine.setMoveLine(null);
        }
      }
    }
    return bankReconciliation;
  }

  @Override
  public void checkReconciliation(List<MoveLine> moveLines, BankReconciliation br)
      throws AxelorException {

    if (br.getBankReconciliationLineList().stream()
                .filter(line -> line.getIsSelectedBankReconciliation())
                .count()
            == 0
        || moveLines.size() == 0) {
      if (br.getBankReconciliationLineList().stream()
                  .filter(line -> line.getIsSelectedBankReconciliation())
                  .count()
              == 0
          && moveLines.size() == 0) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_SELECT_MOVE_LINE_AND_BANK_RECONCILIATION_LINE));
      } else if (br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .count()
          == 0) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage.BANK_RECONCILIATION_SELECT_BANK_RECONCILIATION_LINE));
      } else if (moveLines.size() == 0) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_SELECT_MOVE_LINE));
      }
    } else if (br.getBankReconciliationLineList().stream()
                .filter(line -> line.getIsSelectedBankReconciliation())
                .count()
            > 1
        || moveLines.size() > 1) {
      if (br.getBankReconciliationLineList().stream()
                  .filter(line -> line.getIsSelectedBankReconciliation())
                  .count()
              > 1
          && moveLines.size() > 1) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_SELECT_MOVE_LINE_AND_BANK_RECONCILIATION_LINE));
      } else if (br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .count()
          > 1) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage.BANK_RECONCILIATION_SELECT_BANK_RECONCILIATION_LINE));
      } else if (moveLines.size() > 1) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_SELECT_MOVE_LINE));
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BankReconciliation reconcileSelected(BankReconciliation bankReconciliation)
      throws AxelorException {
    String filter = bankReconciliationQueryService.getRequestMoveLines();
    filter = filter.concat(" AND self.isSelectedBankReconciliation = true");
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(filter)
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();

    // Check if bankReconciliationLines are selected
    List<BankReconciliationLine> selectedBankReconciliationLines =
        bankReconciliation.getBankReconciliationLineList().stream()
            .filter(line -> line.getIsSelectedBankReconciliation())
            .collect(Collectors.toList());

    // Case 1: Reconcile two moveLines together (no bankReconciliationLine selected)
    if (selectedBankReconciliationLines.isEmpty()) {
      // Validation: Check maximum 2 moveLines selection for moveLine-to-moveLine reconciliation
      if (moveLines.size() > 2) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_MAX_TWO_MOVE_LINES));
      }
      if (moveLines.size() == 2) {
        reconcileTwoMoveLines(moveLines.get(0), moveLines.get(1));
        return bankReconciliation;
      }
    }

    // Case 2: Reconcile bankReconciliationLine with moveLine (existing behavior)
    checkReconciliation(moveLines, bankReconciliation);
    BankReconciliationLine bankReconciliationLine = selectedBankReconciliationLines.get(0);
    bankReconciliationLine.setMoveLine(moveLines.get(0));
    bankReconciliationLine =
        bankReconciliationLineService.reconcileBRLAndMoveLine(
            bankReconciliationLine, moveLines.get(0));
    boolean isUnderCorrection =
        bankReconciliation.getStatusSelect()
            == BankReconciliationRepository.STATUS_UNDER_CORRECTION;
    if (isUnderCorrection) {
      bankReconciliationLine.setIsPosted(true);
      bankReconciliationLineService.checkAmount(bankReconciliationLine);
      bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
    }
    return bankReconciliation;
  }

  protected void reconcileTwoMoveLines(MoveLine moveLine1, MoveLine moveLine2)
      throws AxelorException {
    // Amount still to reconcile on each move line (supports partial reconciliation)
    BigDecimal remainingAmount1 = getUnreconciledAmount(moveLine1);
    BigDecimal remainingAmount2 = getUnreconciledAmount(moveLine2);

    // Validation 1: block only if a move line is already fully reconciled (nothing remaining)
    if (remainingAmount1.signum() <= 0) {
      throw new AxelorException(
          moveLine1,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_MOVE_LINE_ALREADY_RECONCILED),
          moveLine1.getName());
    }
    if (remainingAmount2.signum() <= 0) {
      throw new AxelorException(
          moveLine2,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_MOVE_LINE_ALREADY_RECONCILED),
          moveLine2.getName());
    }

    // Validation 2: Check debit VS credit - one must be debit and one must be credit
    boolean moveLine1IsDebit =
        moveLine1.getDebit() != null && moveLine1.getDebit().compareTo(BigDecimal.ZERO) > 0;
    boolean moveLine2IsDebit =
        moveLine2.getDebit() != null && moveLine2.getDebit().compareTo(BigDecimal.ZERO) > 0;

    if (moveLine1IsDebit == moveLine2IsDebit) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              BankPaymentExceptionMessage.BANK_RECONCILIATION_MOVE_LINES_MUST_BE_DEBIT_VS_CREDIT));
    }

    // Reconcile the smallest remaining amount, scaled to the currency scale (2 decimals)
    BigDecimal reconciledAmount =
        currencyScaleService.getScaledValue(remainingAmount1.min(remainingAmount2));

    // The reconciliation number references the move line with the smallest remaining amount
    MoveLine moveLineWithSmallestAmount =
        remainingAmount1.compareTo(remainingAmount2) <= 0 ? moveLine1 : moveLine2;

    // Generate moveLineReconciledNbr format: "ML {moveLineId}: {reconciledAmount}"
    String moveLineReconciledNbr =
        String.format("ML %d: %s", moveLineWithSmallestAmount.getId(), reconciledAmount);

    // Accumulate the reconciled amount and the reconciliation number on both move lines
    applyMoveLineReconciliation(moveLine1, reconciledAmount, moveLineReconciledNbr);
    applyMoveLineReconciliation(moveLine2, reconciledAmount, moveLineReconciledNbr);
  }

  protected void applyMoveLineReconciliation(
      MoveLine moveLine, BigDecimal reconciledAmount, String moveLineReconciledNbr) {
    BigDecimal alreadyReconciled =
        moveLine.getBankReconciledAmount() != null
            ? moveLine.getBankReconciledAmount()
            : BigDecimal.ZERO;
    moveLine.setBankReconciledAmount(
        currencyScaleService.getScaledValue(alreadyReconciled.add(reconciledAmount)));
    moveLine.setMoveLineReconciledNbr(
        addMoveLineReconciledNbr(moveLine.getMoveLineReconciledNbr(), moveLineReconciledNbr));
    moveLine.setIsSelectedBankReconciliation(false);
    moveLineRepository.save(moveLine);
  }

  protected BigDecimal getUnreconciledAmount(MoveLine moveLine) {
    BigDecimal debit = moveLine.getDebit() != null ? moveLine.getDebit() : BigDecimal.ZERO;
    BigDecimal credit = moveLine.getCredit() != null ? moveLine.getCredit() : BigDecimal.ZERO;
    BigDecimal alreadyReconciled =
        moveLine.getBankReconciledAmount() != null
            ? moveLine.getBankReconciledAmount()
            : BigDecimal.ZERO;
    return debit.add(credit).subtract(alreadyReconciled);
  }

  protected String addMoveLineReconciledNbr(String existing, String moveLineReconciledNbr) {
    if (StringUtils.isEmpty(existing)) {
      return moveLineReconciledNbr;
    }
    List<String> reconciledNbrs = new ArrayList<>(Arrays.asList(existing.split(",")));
    reconciledNbrs.add(moveLineReconciledNbr);
    return String.join(",", reconciledNbrs);
  }

  protected String removeMoveLineReconciledNbr(String existing, String moveLineReconciledNbr) {
    if (StringUtils.isEmpty(existing)) {
      return existing;
    }
    List<String> reconciledNbrs = new ArrayList<>(Arrays.asList(existing.split(",")));
    reconciledNbrs.remove(moveLineReconciledNbr);
    return reconciledNbrs.isEmpty() ? null : String.join(",", reconciledNbrs);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unreconcileMoveLines(List<MoveLine> moveLines) {
    Set<String> processedReconciledNbrs = new HashSet<>();
    for (MoveLine moveLine : moveLines) {
      String reconciledNbrList = moveLine.getMoveLineReconciledNbr();
      if (StringUtils.isEmpty(reconciledNbrList)) {
        continue;
      }
      for (String moveLineReconciledNbr : reconciledNbrList.split(",")) {
        // Each reconciliation number is shared by the two reconciled lines: process it once
        if (processedReconciledNbrs.add(moveLineReconciledNbr)) {
          unreconcileByReconciledNbr(moveLineReconciledNbr);
        }
      }
    }
  }

  protected void unreconcileByReconciledNbr(String moveLineReconciledNbr) {
    // The reconciled amount is stored in the number: "ML {id}: {amount}"
    BigDecimal reconciledAmount =
        new BigDecimal(
            moveLineReconciledNbr.substring(moveLineReconciledNbr.indexOf(":") + 1).trim());
    List<MoveLine> reconciledMoveLines =
        moveLineRepository
            .all()
            .filter("self.moveLineReconciledNbr LIKE :moveLineReconciledNbr")
            .bind("moveLineReconciledNbr", "%" + moveLineReconciledNbr + "%")
            .fetch();
    for (MoveLine moveLine : reconciledMoveLines) {
      // Guard against LIKE substring false positives (e.g. "ML 3: 10" inside "ML 3: 100")
      List<String> reconciledNbrs =
          Arrays.asList(
              moveLine.getMoveLineReconciledNbr() != null
                  ? moveLine.getMoveLineReconciledNbr().split(",")
                  : new String[0]);
      if (!reconciledNbrs.contains(moveLineReconciledNbr)) {
        continue;
      }
      BigDecimal alreadyReconciled =
          moveLine.getBankReconciledAmount() != null
              ? moveLine.getBankReconciledAmount()
              : BigDecimal.ZERO;
      moveLine.setBankReconciledAmount(
          currencyScaleService.getScaledValue(
              alreadyReconciled.subtract(reconciledAmount).max(BigDecimal.ZERO)));
      moveLine.setMoveLineReconciledNbr(
          removeMoveLineReconciledNbr(moveLine.getMoveLineReconciledNbr(), moveLineReconciledNbr));
      moveLine.setIsSelectedBankReconciliation(false);
      moveLineRepository.save(moveLine);
    }
  }

  protected BigDecimal getAmountMarginLow(BankReconciliation bankReconciliation) {
    BigDecimal amountMargin =
        bankReconciliation
            .getCompany()
            .getBankPaymentConfig()
            .getBnkStmtAutoReconcileAmountMargin()
            .divide(
                BigDecimal.valueOf(100),
                currencyScaleService.getScale(bankReconciliation),
                RoundingMode.HALF_UP);

    return BigDecimal.ONE.subtract(amountMargin);
  }

  protected Context getScriptContext(
      BankReconciliation bankReconciliation,
      BankStatementLine bankStatementLine,
      BankReconciliationLine bankReconciliationLine,
      MoveLine moveLine)
      throws AxelorException {
    Context scriptContext =
        new Context(Mapper.toMap(bankStatementLine), BankStatementLineAFB120.class);

    BigDecimal debit =
        currencyScaleService.getScaledValue(bankReconciliation, bankReconciliationLine.getDebit());
    BigDecimal credit =
        currencyScaleService.getScaledValue(bankReconciliation, bankReconciliationLine.getCredit());

    BigDecimal currencyAmount = debit.compareTo(BigDecimal.ZERO) == 0 ? credit : debit;
    currencyAmount =
        currencyService.getAmountCurrencyConvertedAtDate(
            bankReconciliation.getCurrency(),
            moveLine.getMove().getCurrency(),
            currencyAmount,
            dateService.date());

    scriptContext.put("debit", debit);
    scriptContext.put("credit", credit);
    scriptContext.put("currencyAmount", currencyAmount);

    return scriptContext;
  }

  protected BankReconciliationLine updateBankReconciliationLine(
      BankReconciliationLine bankReconciliationLine,
      MoveLine moveLine,
      BankStatementQuery bankStatementQuery) {
    bankReconciliationLine.setMoveLine(moveLine);
    bankReconciliationLine.setBankStatementQuery(bankStatementQuery);
    bankReconciliationLine.setConfidenceIndex(bankStatementQuery.getConfidenceIndex());
    bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId().toString());
    return bankReconciliationLine;
  }

  protected String computeQuery(
      BankStatementQuery bankStatementQuery,
      BigInteger dateMargin,
      BigDecimal amountMarginLow,
      BigDecimal amountMarginHigh) {
    String query = bankStatementQuery.getQuery();
    query = query.replace("%amt+", amountMarginHigh.toString());
    query = query.replace("%amt-", amountMarginLow.toString());
    query = query.replace("%date", dateMargin.toString());
    return query;
  }
}
