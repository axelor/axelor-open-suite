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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class BankStatementImportCheckServiceImpl implements BankStatementImportCheckService {
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankStatementLineDeleteService bankStatementLineDeleteService;
  protected BankStatementRepository bankStatementRepository;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected CurrencyScaleService currencyScaleService;
  protected BankStatementDateService bankStatementDateService;
  protected TraceBackRepository traceBackRepository;

  @Inject
  public BankStatementImportCheckServiceImpl(
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementRepository bankStatementRepository,
      BankStatementLineRepository bankStatementLineRepository,
      CurrencyScaleService currencyScaleService,
      BankStatementDateService bankStatementDateService,
      TraceBackRepository traceBackRepository) {
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.currencyScaleService = currencyScaleService;
    this.bankStatementDateService = bankStatementDateService;
    this.traceBackRepository = traceBackRepository;
  }

  @Override
  public long countSkippedStatements(BankStatement bankStatement, ZonedDateTime fromDateTime) {
    return traceBackRepository
        .all()
        .filter("self.ref = :ref AND self.refId = :refId AND self.date >= :fromDateTime")
        .bind("ref", BankStatement.class.getName())
        .bind("refId", bankStatement.getId())
        .bind("fromDateTime", fromDateTime)
        .count();
  }

  @Override
  public void checkImport(BankStatement bankStatement) throws AxelorException {
    try {
      List<BankDetails> bankDetails =
          bankStatementLineFetchService.getBankDetailsFromStatementLines(bankStatement);

      this.checkImportPrecondition(bankStatement, bankDetails);
    } catch (Exception e) {
      bankStatementLineDeleteService.deleteBankStatementLines(
          bankStatementRepository.find(bankStatement.getId()));
      throw e;
    }
  }

  protected void checkImportPrecondition(BankStatement bankStatement, List<BankDetails> bankDetails)
      throws AxelorException {
    List<AxelorException> skippedStatements = new ArrayList<>();
    boolean hasStatementToImport = false;

    for (BankDetails bd : bankDetails) {
      skippedStatements.addAll(removeAlreadyImportedStatements(bankStatement, bd));

      if (findBankStatementLines(bankStatement, bd).count() == 0) {
        continue;
      }
      hasStatementToImport = true;

      BankStatementLine initialBankStatementLine =
          orderBankStatementLineQuery(
                  bankStatementLineFetchService.findByBankStatementBankDetailsAndLineType(
                      bankStatement, bd, BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE),
                  BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE)
              .fetchOne();
      BankStatementLine finalBankStatementLine =
          orderBankStatementLineQuery(
                  bankStatementLineFetchService.findByBankStatementBankDetailsAndLineType(
                      bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE),
                  BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
              .fetchOne();
      BankStatementLine lastFinalBankStatementLine =
          orderBankStatementLineQuery(
                  bankStatementLineFetchService.findByBankDetailsLineTypeExcludeBankStatement(
                      bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE),
                  BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
              .fetchOne();

      this.checkInitialAndFinalBankStatementLine(
          bankStatement, initialBankStatementLine, finalBankStatementLine);
      this.checkAmountWithPreviousBankStatement(
          bankStatement, initialBankStatementLine, lastFinalBankStatementLine);
      this.checkBankStatementBalanceIncoherence(
          bankStatement, bd, initialBankStatementLine, finalBankStatementLine);
      this.checkAmountWithinBankStatement(bankStatement, bd);
    }

    if (skippedStatements.isEmpty()) {
      return;
    }

    if (!hasStatementToImport) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_ALREADY_IMPORTED));
    }

    updateBankStatementDates(bankStatement);
    skippedStatements.forEach(TraceBackService::trace);
  }

  protected List<AxelorException> removeAlreadyImportedStatements(
      BankStatement bankStatement, BankDetails bankDetails) {
    List<AxelorException> skippedStatements = new ArrayList<>();

    for (List<BankStatementLine> statementLines :
        splitByStatement(findBankStatementLines(bankStatement, bankDetails).fetch())) {
      if (!isAlreadyImportedStatement(statementLines)) {
        continue;
      }

      skippedStatements.add(
          new AxelorException(
              bankStatement,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_ALREADY_IMPORTED_SKIPPED),
              bankDetails.getFullName(),
              statementLines.get(0).getOperationDate(),
              statementLines.get(statementLines.size() - 1).getOperationDate()));
      bankStatementLineDeleteService.deleteBankStatementLines(statementLines);
    }

    return skippedStatements;
  }

  protected List<List<BankStatementLine>> splitByStatement(List<BankStatementLine> lines) {
    List<List<BankStatementLine>> statements = new ArrayList<>();
    List<BankStatementLine> currentStatement = null;

    for (BankStatementLine line : lines) {
      if (currentStatement == null
          || line.getLineTypeSelect() == BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE) {
        currentStatement = new ArrayList<>();
        statements.add(currentStatement);
      }
      currentStatement.add(line);
    }

    return statements;
  }

  protected Query<BankStatementLine> findBankStatementLines(
      BankStatement bankStatement, BankDetails bankDetails) {
    return bankStatementLineRepository
        .all()
        .filter("self.bankStatement = :bankStatement AND self.bankDetails = :bankDetails")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .order("sequence")
        .order("id");
  }

  protected void updateBankStatementDates(BankStatement bankStatement) {
    bankStatement.setFromDate(null);
    bankStatement.setToDate(null);
    for (BankStatementLine line :
        bankStatementLineRepository
            .all()
            .filter("self.bankStatement = :bankStatement AND self.lineTypeSelect IN (:lineTypes)")
            .bind("bankStatement", bankStatement)
            .bind(
                "lineTypes",
                List.of(
                    BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE,
                    BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE))
            .fetch()) {
      bankStatementDateService.updateBankStatementDate(
          bankStatement, line.getOperationDate(), line.getLineTypeSelect());
    }
  }

  protected void checkInitialAndFinalBankStatementLine(
      BankStatement bankStatement,
      BankStatementLine initialBankStatementLine,
      BankStatementLine finalBankStatementLine)
      throws AxelorException {
    if (initialBankStatementLine == null) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_NO_INITIAL_LINE_ON_IMPORT));
    }

    if (finalBankStatementLine == null) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_NO_FINAL_LINE_ON_IMPORT));
    }
  }

  protected void checkAmountWithPreviousBankStatement(
      BankStatement bankStatement,
      BankStatementLine initialBankStatementLine,
      BankStatementLine finalBankStatementLine)
      throws AxelorException {
    if (isDeleteLines(finalBankStatementLine, initialBankStatementLine)) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_NOT_MATCHING));
    }
  }

  protected void checkAmountWithinBankStatement(BankStatement bankStatement, BankDetails bd)
      throws AxelorException {
    List<BankStatementLine> initialBankStatementLine =
        bankStatementLineFetchService
            .findByBankStatementBankDetailsAndLineType(
                bankStatement, bd, BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE)
            .order("operationDate")
            .order("sequence")
            .fetch();
    List<BankStatementLine> finalBankStatementLine =
        bankStatementLineFetchService
            .findByBankStatementBankDetailsAndLineType(
                bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
            .order("operationDate")
            .order("sequence")
            .fetch();
    initialBankStatementLine.remove(0);
    finalBankStatementLine.remove(finalBankStatementLine.size() - 1);

    if (initialBankStatementLine.size() != finalBankStatementLine.size()
        || isDeleteLinesFor(initialBankStatementLine, finalBankStatementLine)) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_INCOHERENT_BALANCE));
    }
  }

  protected boolean isDeleteLinesFor(
      List<BankStatementLine> initialBankStatementLine,
      List<BankStatementLine> finalBankStatementLine) {
    for (int i = 0; i < initialBankStatementLine.size(); i++) {
      if (isDeleteLines(initialBankStatementLine.get(i), finalBankStatementLine.get(i))) {
        return true;
      }
    }
    return false;
  }

  protected boolean isDeleteLines(
      BankStatementLine finalBankStatementLine, BankStatementLine initialBankStatementLine) {
    return ObjectUtils.notEmpty(finalBankStatementLine)
        && (!currencyScaleService.equals(
                initialBankStatementLine.getDebit(),
                finalBankStatementLine.getDebit(),
                initialBankStatementLine,
                false)
            || !currencyScaleService.equals(
                initialBankStatementLine.getCredit(),
                finalBankStatementLine.getCredit(),
                initialBankStatementLine,
                false));
  }

  protected boolean isAlreadyImportedStatement(List<BankStatementLine> statementLines) {
    BankStatementLine initialLine = statementLines.get(0);
    BankStatementLine finalLine = statementLines.get(statementLines.size() - 1);

    return initialLine.getLineTypeSelect() == BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE
        && finalLine.getLineTypeSelect() == BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE
        && balanceLineAlreadyExists(initialLine)
        && balanceLineAlreadyExists(finalLine);
  }

  protected boolean balanceLineAlreadyExists(BankStatementLine balanceLine) {
    return bankStatementLineRepository
            .all()
            .filter(
                "self.operationDate = :operationDate"
                    + " AND self.lineTypeSelect = :lineTypeSelect"
                    + " AND self.debit = :debit"
                    + " AND self.credit = :credit"
                    + " AND self.bankStatement != :bankStatement"
                    + " AND self.bankDetails = :bankDetails")
            .bind("operationDate", balanceLine.getOperationDate())
            .bind("lineTypeSelect", balanceLine.getLineTypeSelect())
            .bind("debit", balanceLine.getDebit())
            .bind("credit", balanceLine.getCredit())
            .bind("bankStatement", balanceLine.getBankStatement())
            .bind("bankDetails", balanceLine.getBankDetails())
            .count()
        > 0;
  }

  protected void checkBankStatementBalanceIncoherence(
      BankStatement bankStatement,
      BankDetails bankDetails,
      BankStatementLine initialBankStatementLine,
      BankStatementLine finalBankStatementLine)
      throws AxelorException {
    BigDecimal initialBankStatementLineSum =
        initialBankStatementLine.getDebit().subtract(initialBankStatementLine.getCredit());
    BigDecimal finalBankStatementLineSum =
        finalBankStatementLine.getDebit().subtract(finalBankStatementLine.getCredit());

    BigDecimal movementLineSum =
        orderBankStatementLineQuery(
                bankStatementLineFetchService.findByBankStatementBankDetailsAndLineType(
                    bankStatement, bankDetails, BankStatementLineRepository.LINE_TYPE_MOVEMENT),
                BankStatementLineRepository.LINE_TYPE_MOVEMENT)
            .fetch()
            .stream()
            .map(
                bankStatementLine ->
                    bankStatementLine.getDebit().subtract(bankStatementLine.getCredit()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    if (initialBankStatementLineSum.add(movementLineSum).compareTo(finalBankStatementLineSum)
        != 0) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_INCOHERENT_BALANCE));
    }
  }

  protected Query<BankStatementLine> orderBankStatementLineQuery(
      Query<BankStatementLine> query, int type) {
    String order = "operationDate";
    String sequence = "sequence";

    if (type == BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE) {
      order = "-".concat(order);
      sequence = "-".concat(sequence);
    }
    return query.order(order).order(sequence);
  }
}
