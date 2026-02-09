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
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
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
    if (selectedBankReconciliationLines.isEmpty() && moveLines.size() == 2) {
      reconcileTwoMoveLines(moveLines.get(0), moveLines.get(1));
      return bankReconciliation;
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
    // Get the debit and credit amounts for both moveLines
    BigDecimal amount1 =
        moveLine1.getDebit() != null && moveLine1.getDebit().compareTo(BigDecimal.ZERO) > 0
            ? moveLine1.getDebit()
            : moveLine1.getCredit();
    BigDecimal amount2 =
        moveLine2.getDebit() != null && moveLine2.getDebit().compareTo(BigDecimal.ZERO) > 0
            ? moveLine2.getDebit()
            : moveLine2.getCredit();

    // Calculate the smallest amount between the two moveLines
    BigDecimal reconciledAmount = amount1.min(amount2);

    // Determine which moveLine has the smallest amount (for postedNbr generation)
    MoveLine moveLineWithSmallestAmount = amount1.compareTo(amount2) <= 0 ? moveLine1 : moveLine2;

    // Generate postedNbr format: "ML {moveLineId}: {reconciledAmount}"
    String postedNbr =
        String.format("ML %d: %s", moveLineWithSmallestAmount.getId(), reconciledAmount);

    // Set the reconciled amount on both moveLines
    moveLine1.setBankReconciledAmount(reconciledAmount);
    moveLine2.setBankReconciledAmount(reconciledAmount);

    // Set the postedNbr on both moveLines
    moveLine1.setPostedNbr(postedNbr);
    moveLine2.setPostedNbr(postedNbr);

    // Unselect both moveLines
    moveLine1.setIsSelectedBankReconciliation(false);
    moveLine2.setIsSelectedBankReconciliation(false);

    // Save changes
    moveLineRepository.save(moveLine1);
    moveLineRepository.save(moveLine2);
  }

  protected BigDecimal getUnreconciledAmount(MoveLine moveLine) {
    BigDecimal totalAmount;

    // Use currencyAmount if the move has a different currency than company currency
    if (moveLine.getMove().getCurrency() != null
        && moveLine.getMove().getCompany() != null
        && moveLine.getMove().getCompany().getCurrency() != null
        && !moveLine.getMove().getCurrency().equals(moveLine.getMove().getCompany().getCurrency())) {
      totalAmount =
          moveLine.getCurrencyAmount() != null
              ? moveLine.getCurrencyAmount().abs()
              : BigDecimal.ZERO;
    } else {
      totalAmount = moveLine.getDebit().add(moveLine.getCredit());
    }

    BigDecimal alreadyReconciled =
        moveLine.getBankReconciledAmount() != null
            ? moveLine.getBankReconciledAmount()
            : BigDecimal.ZERO;

    return totalAmount.subtract(alreadyReconciled);
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
