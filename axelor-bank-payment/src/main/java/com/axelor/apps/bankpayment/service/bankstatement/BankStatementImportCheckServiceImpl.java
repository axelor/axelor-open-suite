/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class BankStatementImportCheckServiceImpl implements BankStatementImportCheckService {
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankStatementLineDeleteService bankStatementLineDeleteService;
  protected BankStatementRepository bankStatementRepository;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankStatementImportCheckServiceImpl(
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementRepository bankStatementRepository,
      BankStatementLineRepository bankStatementLineRepository,
      CurrencyScaleService currencyScaleService) {
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.currencyScaleService = currencyScaleService;
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
    boolean alreadyImported = false;

    for (BankDetails bd : bankDetails) {
      alreadyImported = isAlreadyImported(bankStatement, bd, alreadyImported);

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

    if (alreadyImported) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_ALREADY_IMPORTED));
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

  protected boolean isAlreadyImported(
      BankStatement bankStatement, BankDetails bd, boolean alreadyImported) {
    List<BankStatementLine> initialLines =
        orderBankStatementLineQuery(
                bankStatementLineFetchService.findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE),
                BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE)
            .fetch();

    List<BankStatementLine> finalLines =
        orderBankStatementLineQuery(
                bankStatementLineFetchService.findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE),
                BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
            .fetch();

    return bankStatementLineAlreadyExists(initialLines)
        || bankStatementLineAlreadyExists(finalLines)
        || alreadyImported;
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
            .order("sequence")
            .fetch();
    List<BankStatementLine> finalBankStatementLine =
        bankStatementLineFetchService
            .findByBankStatementBankDetailsAndLineType(
                bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
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

  protected boolean bankStatementLineAlreadyExists(List<BankStatementLine> initialLines) {
    boolean alreadyImported = false;
    BankStatementLine tempBankStatementLine;
    for (BankStatementLine bsl : initialLines) {
      tempBankStatementLine =
          bankStatementLineRepository
              .all()
              .filter(
                  "self.operationDate = :operationDate"
                      + " AND self.lineTypeSelect = :lineTypeSelect"
                      + " AND self.bankStatement != :bankStatement"
                      + " AND self.bankDetails = :bankDetails")
              .bind("operationDate", bsl.getOperationDate())
              .bind("lineTypeSelect", bsl.getLineTypeSelect())
              .bind("bankStatement", bsl.getBankStatement())
              .bind("bankDetails", bsl.getBankDetails())
              .fetchOne();
      if (ObjectUtils.notEmpty(tempBankStatementLine)) {
        alreadyImported = true;
        break;
      }
    }
    return alreadyImported;
  }

  protected void checkBankStatementBalanceIncoherence(
      BankStatement bankStatement,
      BankDetails bankDetails,
      BankStatementLine initialBankStatementLine,
      BankStatementLine finalBankStatementLine)
      throws AxelorException {
    BigDecimal initialBankStatementLineSum =
        initialBankStatementLine.getDebit().max(initialBankStatementLine.getCredit());
    BigDecimal finalBankStatementLineSum =
        finalBankStatementLine.getDebit().max(finalBankStatementLine.getCredit());

    BigDecimal movementLineSum =
        orderBankStatementLineQuery(
                bankStatementLineFetchService.findByBankStatementBankDetailsAndLineType(
                    bankStatement, bankDetails, BankStatementLineRepository.LINE_TYPE_MOVEMENT),
                BankStatementLineRepository.LINE_TYPE_MOVEMENT)
            .fetch().stream()
            .map(
                bankStatementLine ->
                    bankStatementLine.getCredit().subtract(bankStatementLine.getDebit()))
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
