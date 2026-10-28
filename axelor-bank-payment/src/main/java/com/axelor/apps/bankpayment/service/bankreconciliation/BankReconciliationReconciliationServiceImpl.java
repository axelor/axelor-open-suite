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
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementQueryRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.moveline.MoveLinePostedNbrService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.cache.AxelorCache;
import com.axelor.cache.CacheBuilder;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankReconciliationReconciliationServiceImpl
    implements BankReconciliationReconciliationService {

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BankStatementQueryRepository bankStatementQueryRepository;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationQueryService bankReconciliationQueryService;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected CurrencyService currencyService;
  protected DateService dateService;
  protected CurrencyScaleService currencyScaleService;
  protected SequenceService sequenceService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected MoveLinePostedNbrService moveLinePostedNbrService;
  protected BankReconciliationService bankReconciliationService;

  private final AxelorCache<String, BigDecimal> exchangeRateCache =
      CacheBuilder.newBuilder("exchangeRateCache")
          .expireAfterWrite(Duration.ofMinutes(10))
          .maximumSize(100)
          .build();

  @Inject
  public BankReconciliationReconciliationServiceImpl(
      BankStatementQueryRepository bankStatementQueryRepository,
      MoveLineRepository moveLineRepository,
      BankReconciliationQueryService bankReconciliationQueryService,
      BankReconciliationLineService bankReconciliationLineService,
      CurrencyService currencyService,
      DateService dateService,
      CurrencyScaleService currencyScaleService,
      SequenceService sequenceService,
      BankReconciliationRepository bankReconciliationRepository,
      MoveLinePostedNbrService moveLinePostedNbrService,
      BankReconciliationService bankReconciliationService) {
    this.bankStatementQueryRepository = bankStatementQueryRepository;
    this.moveLineRepository = moveLineRepository;
    this.bankReconciliationQueryService = bankReconciliationQueryService;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.currencyService = currencyService;
    this.dateService = dateService;
    this.currencyScaleService = currencyScaleService;
    this.sequenceService = sequenceService;
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.moveLinePostedNbrService = moveLinePostedNbrService;
    this.bankReconciliationService = bankReconciliationService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BankReconciliation reconciliateAccordingToQueries(BankReconciliation bankReconciliation)
      throws AxelorException {
    long startTime = System.nanoTime();
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
    int initialQueryCount = bankStatementQueries.size();
    int initialMoveLineCount = moveLines.size();
    int initialReconciliationLineCount = bankReconciliationLines.size();
    int initialEligibleLineCount =
        (int)
            bankReconciliationLines.stream()
                .filter(line -> line.getBankStatementLine() != null)
                .count();
    int remainingEligibleLineCount = initialEligibleLineCount;
    int totalMatchedLineCount = 0;

    BigInteger dateMargin =
        BigInteger.valueOf(
            bankReconciliation
                .getCompany()
                .getBankPaymentConfig()
                .getBnkStmtAutoReconcileDateMargin());

    BigDecimal amountMarginLow = this.getAmountMarginLow(bankReconciliation);
    BigDecimal amountMarginHigh = BigDecimal.ONE;

    int queryIndex = 0;
    Set<Long> matchedMoveLineIds = new HashSet<>();

    LOG.info(
        "Starting automatic reconciliation for bank reconciliation {}: {} queries, {} candidate move lines, {} unreconciled lines ({} eligible)",
        bankReconciliation.getId(),
        initialQueryCount,
        initialMoveLineCount,
        initialReconciliationLineCount,
        initialEligibleLineCount);

    for (BankStatementQuery bankStatementQuery : bankStatementQueries) {
      queryIndex++;
      int matchedLineCount = 0;
      String query =
          computeQuery(bankStatementQuery, dateMargin, amountMarginLow, amountMarginHigh);

      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
        if (bankReconciliationLine.getMoveLine() != null || bankStatementLine == null) {
          continue;
        }

        bankStatementLine.setMoveLine(null);
        ScriptBindings sb =
            this.getScriptBinding(bankReconciliation, bankStatementLine, bankReconciliationLine);
        BigDecimal debit = (BigDecimal) sb.get("debit");
        BigDecimal credit = (BigDecimal) sb.get("credit");
        BigDecimal baseAmount = debit.compareTo(BigDecimal.ZERO) == 0 ? credit : debit;

        for (MoveLine moveLine : moveLines) {
          if (matchedMoveLineIds.contains(moveLine.getId())
              || !bankReconciliationLineService.isDebitCreditDirectionValid(
                  bankReconciliationLine, moveLine)) {
            continue;
          }

          sb.put("moveLine", moveLine);
          sb.put(
              "currencyAmount",
              getConvertedCurrencyAmount(bankReconciliation, moveLine, baseAmount));

          Boolean result = (Boolean) new GroovyScriptHelper(sb).eval(query);

          if (result) {
            bankStatementLine.setMoveLine(moveLine);
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
            moveLinePostedNbrService.updateBankReconcileNbr(moveLine);
            matchedMoveLineIds.add(moveLine.getId());
            matchedLineCount++;
            totalMatchedLineCount++;
            remainingEligibleLineCount--;
            break;
          }
        }
      }

      LOG.info(
          "Automatic reconciliation query {}/{} for bank reconciliation {} (query id {}): matched {} lines, {} eligible lines remaining, {} candidate move lines remaining",
          queryIndex,
          initialQueryCount,
          bankReconciliation.getId(),
          bankStatementQuery.getId(),
          matchedLineCount,
          remainingEligibleLineCount,
          initialMoveLineCount - matchedMoveLineIds.size());
    }

    LOG.info(
        "Completed automatic reconciliation for bank reconciliation {} in {} ms: {} queries, {} initial candidate move lines, {} initial unreconciled lines ({} eligible), {} matched lines, {} eligible lines remaining, {} candidate move lines remaining",
        bankReconciliation.getId(),
        (System.nanoTime() - startTime) / 1_000_000,
        initialQueryCount,
        initialMoveLineCount,
        initialReconciliationLineCount,
        initialEligibleLineCount,
        totalMatchedLineCount,
        remainingEligibleLineCount,
        initialMoveLineCount - matchedMoveLineIds.size());
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
        reconcileTwoMoveLines(bankReconciliation, moveLines.get(0), moveLines.get(1));
        return bankReconciliation;
      }
    }

    // Case 2: Reconcile bankReconciliationLine with moveLine (existing behavior)
    checkReconciliation(moveLines, bankReconciliation);
    BankReconciliationLine bankReconciliationLine = selectedBankReconciliationLines.get(0);
    bankReconciliationLineService.checkDebitCreditDirection(
        bankReconciliationLine, moveLines.get(0));
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
    bankReconciliationService.checkMultipleMoveLineReconciliation(bankReconciliation);
    return bankReconciliation;
  }

  protected void reconcileTwoMoveLines(
      BankReconciliation bankReconciliation, MoveLine moveLine1, MoveLine moveLine2)
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

    // The reconciliation number uses the bank reconciliation sequence (same as
    // bankReconciliationLine.reconcileNumber), shared by both lines, with the reconciled amount
    // appended: "{sequence}:{reconciledAmount}" (e.g. BNKREC2:2000.00). No spaces, to ease parsing.
    String reconcileNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.BANK_RECONCILIATION,
            bankReconciliation.getCompany(),
            BankReconciliationLine.class,
            "reconcileNumber",
            moveLineWithSmallestAmount);
    if (reconcileNumber == null) {
      throw new AxelorException(
          bankReconciliation,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_COMPANY_NO_SEQUENCE),
          bankReconciliation.getCompany().getName());
    }
    String moveLineBankReconcileNbr = String.format("%s:%s", reconcileNumber, reconciledAmount);

    // Accumulate the reconciled amount and the reconciliation number on both move lines
    applyMoveLineReconciliation(moveLine1, reconciledAmount, moveLineBankReconcileNbr);
    applyMoveLineReconciliation(moveLine2, reconciledAmount, moveLineBankReconcileNbr);
  }

  protected void applyMoveLineReconciliation(
      MoveLine moveLine, BigDecimal reconciledAmount, String moveLineBankReconcileNbr) {
    BigDecimal alreadyReconciled =
        moveLine.getBankReconciledAmount() != null
            ? moveLine.getBankReconciledAmount()
            : BigDecimal.ZERO;
    moveLine.setBankReconciledAmount(
        currencyScaleService.getScaledValue(alreadyReconciled.add(reconciledAmount)));
    moveLine.setMoveLineBankReconcileNbr(
        addMoveLineBankReconcileNbr(
            moveLine.getMoveLineBankReconcileNbr(), moveLineBankReconcileNbr));
    moveLine.setIsSelectedBankReconciliation(false);
    moveLinePostedNbrService.updateBankReconcileNbr(moveLine);
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

  protected String addMoveLineBankReconcileNbr(String existing, String moveLineBankReconcileNbr) {
    if (StringUtils.isEmpty(existing)) {
      return moveLineBankReconcileNbr;
    }
    List<String> reconciledNbrs = new ArrayList<>(Arrays.asList(existing.split(",")));
    reconciledNbrs.add(moveLineBankReconcileNbr);
    return String.join(",", reconciledNbrs);
  }

  protected String removeMoveLineBankReconcileNbr(
      String existing, String moveLineBankReconcileNbr) {
    if (StringUtils.isEmpty(existing)) {
      return existing;
    }
    List<String> reconciledNbrs = new ArrayList<>(Arrays.asList(existing.split(",")));
    reconciledNbrs.remove(moveLineBankReconcileNbr);
    return reconciledNbrs.isEmpty() ? null : String.join(",", reconciledNbrs);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unreconcileMoveLines(List<MoveLine> moveLines) throws AxelorException {
    // Only the move line to move line reconciliations (moveLineBankReconcileNbr) are undone here:
    // reconciliations against a bank reconciliation line are undone by selecting that line.
    Set<String> processedReconciledNbrs = new HashSet<>();
    for (MoveLine moveLine : moveLines) {
      String reconciledNbrList = moveLine.getMoveLineBankReconcileNbr();
      if (StringUtils.isEmpty(reconciledNbrList)) {
        continue;
      }
      for (String moveLineBankReconcileNbr : reconciledNbrList.split(",")) {
        // Each reconciliation number is shared by the two reconciled lines: process it once
        if (processedReconciledNbrs.add(moveLineBankReconcileNbr)) {
          unreconcileByReconciledNbr(moveLineBankReconcileNbr);
        }
      }
    }
  }

  protected void unreconcileByReconciledNbr(String moveLineBankReconcileNbr)
      throws AxelorException {
    // The reconciled amount is stored in the number: "{sequence}:{amount}" (e.g. BNKREC2:2000.00)
    BigDecimal reconciledAmount =
        new BigDecimal(
            moveLineBankReconcileNbr.substring(moveLineBankReconcileNbr.indexOf(":") + 1).trim());
    List<MoveLine> reconciledMoveLines =
        moveLineRepository
            .all()
            .filter("self.moveLineBankReconcileNbr LIKE :moveLineBankReconcileNbr")
            .bind("moveLineBankReconcileNbr", "%" + moveLineBankReconcileNbr + "%")
            .fetch()
            .stream()
            // Guard against LIKE substring false positives (e.g. "BNKREC2:10" inside "BNKREC2:100")
            .filter(
                moveLine ->
                    Arrays.asList(moveLine.getMoveLineBankReconcileNbr().split(","))
                        .contains(moveLineBankReconcileNbr))
            .collect(Collectors.toList());

    // Check every line before changing any of them
    for (MoveLine moveLine : reconciledMoveLines) {
      checkNotInValidatedBankReconciliation(moveLine);
    }

    for (MoveLine moveLine : reconciledMoveLines) {
      BigDecimal alreadyReconciled =
          moveLine.getBankReconciledAmount() != null
              ? moveLine.getBankReconciledAmount()
              : BigDecimal.ZERO;
      moveLine.setBankReconciledAmount(
          currencyScaleService.getScaledValue(
              alreadyReconciled.subtract(reconciledAmount).max(BigDecimal.ZERO)));
      moveLine.setMoveLineBankReconcileNbr(
          removeMoveLineBankReconcileNbr(
              moveLine.getMoveLineBankReconcileNbr(), moveLineBankReconcileNbr));
      moveLine.setIsSelectedBankReconciliation(false);
      moveLinePostedNbrService.updateBankReconcileNbr(moveLine);
      moveLineRepository.save(moveLine);
    }
  }

  // A move line to move line reconciliation is not linked to a bank reconciliation, so a move
  // line dated in a period already covered by a validated bank reconciliation of its account is
  // considered closed and cannot be unreconciled.
  protected void checkNotInValidatedBankReconciliation(MoveLine moveLine) throws AxelorException {
    BankReconciliation validatedBankReconciliation =
        bankReconciliationRepository
            .all()
            .filter(
                "self.company = :company"
                    + " AND self.statusSelect = :validated"
                    + " AND self.toDate >= :date"
                    + " AND (self.cashAccount IS NULL OR self.cashAccount = :account)")
            .bind("company", moveLine.getMove().getCompany())
            .bind("validated", BankReconciliationRepository.STATUS_VALIDATED)
            .bind("date", moveLine.getDate())
            .bind("account", moveLine.getAccount())
            .order("-toDate")
            .fetchOne();
    if (validatedBankReconciliation != null) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              BankPaymentExceptionMessage
                  .BANK_RECONCILIATION_MOVE_LINE_IN_VALIDATED_RECONCILIATION),
          moveLine.getName(),
          validatedBankReconciliation.getName());
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

  protected ScriptBindings getScriptBinding(
      BankReconciliation bankReconciliation,
      BankStatementLine bankStatementLine,
      BankReconciliationLine bankReconciliationLine)
      throws AxelorException {
    ScriptBindings sb = new ScriptBindings(Mapper.toMap(bankStatementLine));

    BigDecimal debit =
        currencyScaleService.getScaledValue(bankReconciliation, bankReconciliationLine.getDebit());
    BigDecimal credit =
        currencyScaleService.getScaledValue(bankReconciliation, bankReconciliationLine.getCredit());

    sb.put("debit", debit);
    sb.put("credit", credit);

    return sb;
  }

  protected BigDecimal getExchangeRate(Currency startCurrency, Currency endCurrency)
      throws AxelorException {
    if (startCurrency.equals(endCurrency)) {
      return BigDecimal.ONE;
    }
    String key = startCurrency.getId() + "_" + endCurrency.getId();
    BigDecimal rate = exchangeRateCache.get(key);
    if (rate != null) {
      return rate;
    }
    rate =
        currencyService.getCurrencyConversionRate(startCurrency, endCurrency, dateService.date());
    exchangeRateCache.put(key, rate);
    return rate;
  }

  protected BigDecimal getConvertedCurrencyAmount(
      BankReconciliation bankReconciliation, MoveLine moveLine, BigDecimal baseAmount)
      throws AxelorException {
    BigDecimal exchangeRate =
        getExchangeRate(bankReconciliation.getCurrency(), moveLine.getMove().getCurrency());
    return currencyService.getAmountCurrencyConvertedUsingExchangeRate(
        baseAmount, exchangeRate, moveLine.getMove().getCurrency());
  }

  protected BankReconciliationLine updateBankReconciliationLine(
      BankReconciliationLine bankReconciliationLine,
      MoveLine moveLine,
      BankStatementQuery bankStatementQuery) {
    bankReconciliationLine.setMoveLine(moveLine);
    bankReconciliationLineService.fillPartnerAndAccountFromMoveLine(
        bankReconciliationLine, moveLine);
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
