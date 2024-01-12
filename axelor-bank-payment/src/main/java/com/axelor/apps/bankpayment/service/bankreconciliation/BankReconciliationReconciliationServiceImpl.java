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
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class BankReconciliationReconciliationServiceImpl
    implements BankReconciliationReconciliationService {

  protected static final int RETURNED_SCALE = 2;
  protected BankStatementQueryRepository bankStatementQueryRepository;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationQueryService bankReconciliationQueryService;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected CurrencyService currencyService;
  protected DateService dateService;

  @Inject
  public BankReconciliationReconciliationServiceImpl(
      BankStatementQueryRepository bankStatementQueryRepository,
      MoveLineRepository moveLineRepository,
      BankReconciliationQueryService bankReconciliationQueryService,
      BankReconciliationLineService bankReconciliationLineService,
      CurrencyService currencyService,
      DateService dateService) {
    this.bankStatementQueryRepository = bankStatementQueryRepository;
    this.moveLineRepository = moveLineRepository;
    this.bankReconciliationQueryService = bankReconciliationQueryService;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.currencyService = currencyService;
    this.dateService = dateService;
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
            .filter(bankReconciliationQueryService.getRequestMoveLines(bankReconciliation))
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
    BankReconciliationLine bankReconciliationLine;
    String filter = bankReconciliationQueryService.getRequestMoveLines(bankReconciliation);
    filter = filter.concat(" AND self.isSelectedBankReconciliation = true");
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(filter)
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();
    checkReconciliation(moveLines, bankReconciliation);
    bankReconciliationLine =
        bankReconciliation.getBankReconciliationLineList().stream()
            .filter(line -> line.getIsSelectedBankReconciliation())
            .collect(Collectors.toList())
            .get(0);
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

  protected BigDecimal getAmountMarginLow(BankReconciliation bankReconciliation) {
    BigDecimal amountMargin =
        bankReconciliation
            .getCompany()
            .getBankPaymentConfig()
            .getBnkStmtAutoReconcileAmountMargin()
            .divide(BigDecimal.valueOf(100), RETURNED_SCALE, RoundingMode.HALF_UP);

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

    BigDecimal debit = bankReconciliationLine.getDebit();
    BigDecimal credit = bankReconciliationLine.getCredit();

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
