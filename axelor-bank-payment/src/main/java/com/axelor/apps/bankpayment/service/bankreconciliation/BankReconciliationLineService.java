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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BankReconciliationLineService {

  public static final int BANK_RECONCILIATION_LINE_COMPLETE = 0;
  public static final int BANK_RECONCILIATION_LINE_COMPLETABLE = 1;
  public static final int BANK_RECONCILIATION_LINE_INCOMPLETE = 2;

  protected BankReconciliationLineRepository bankReconciliationLineRepository;

  @Inject
  public BankReconciliationLineService(
      BankReconciliationLineRepository bankReconciliationLineRepository) {
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
  }

  public BankReconciliationLine createBankReconciliationLine(
      LocalDate effectDate,
      BigDecimal debit,
      BigDecimal credit,
      String name,
      String reference,
      BankStatementLine bankStatementLine,
      MoveLine moveLine) {

    BankReconciliationLine bankReconciliationLine = new BankReconciliationLine();
    bankReconciliationLine.setEffectDate(effectDate);
    bankReconciliationLine.setDebit(debit);
    bankReconciliationLine.setCredit(credit);
    bankReconciliationLine.setName(name);
    bankReconciliationLine.setReference(reference);
    bankReconciliationLine.setBankStatementLine(bankStatementLine);
    bankReconciliationLine.setIsPosted(false);
    if (ObjectUtils.notEmpty(moveLine)) {
      if (ObjectUtils.isEmpty(bankReconciliationLine.getId())) {
        bankReconciliationLine = bankReconciliationLineRepository.save(bankReconciliationLine);
      }
      bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId().toString());
      moveLine = setMoveLinePostedNbr(moveLine, bankReconciliationLine.getPostedNbr());
    }
    if (debit.compareTo(BigDecimal.ZERO) == 0) {
      bankReconciliationLine.setTypeSelect(BankReconciliationLineRepository.TYPE_SELECT_CUSTOMER);
    } else {
      bankReconciliationLine.setTypeSelect(BankReconciliationLineRepository.TYPE_SELECT_SUPPLIER);
    }
    if (ObjectUtils.notEmpty(moveLine)) {
      bankReconciliationLine.setMoveLine(moveLine);
      bankReconciliationLine.setPartner(moveLine.getPartner());
      bankReconciliationLine.setAccount(moveLine.getAccount());
    }
    return bankReconciliationLine;
  }

  public BankReconciliationLine createBankReconciliationLine(BankStatementLine bankStatementLine) {
    BigDecimal debit =
        bankStatementLine.getDebit().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : bankStatementLine.getAmountRemainToReconcile();
    BigDecimal credit =
        bankStatementLine.getDebit().compareTo(BigDecimal.ZERO) != 0
            ? BigDecimal.ZERO
            : bankStatementLine.getAmountRemainToReconcile();
    return this.createBankReconciliationLine(
        bankStatementLine.getValueDate(),
        debit,
        credit,
        bankStatementLine.getDescription(),
        bankStatementLine.getReference(),
        bankStatementLine,
        null);
  }

  public void checkAmount(BankReconciliationLine bankReconciliationLine) throws AxelorException {

    MoveLine moveLine = bankReconciliationLine.getMoveLine();

    BigDecimal bankDebit = bankReconciliationLine.getDebit();
    BigDecimal bankCredit = bankReconciliationLine.getCredit();
    boolean isDebit = bankDebit.compareTo(bankCredit) > 0;

    BigDecimal moveLineDebit = moveLine.getDebit();
    BigDecimal moveLineCredit = moveLine.getCredit();

    if (moveLine
        .getMove()
        .getCurrency()
        .equals(bankReconciliationLine.getBankReconciliation().getCurrency())) {
      if (isDebit) {
        moveLineCredit = moveLine.getCurrencyAmount().abs();
      } else {
        moveLineDebit = moveLine.getCurrencyAmount().abs();
      }
    }

    if (bankDebit.add(bankCredit).compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          bankReconciliationLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BANK_STATEMENT_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          bankReconciliationLine.getReference() != null
              ? bankReconciliationLine.getReference()
              : "");
    }

    if (!(bankDebit.compareTo(BigDecimal.ZERO) > 0
            && moveLineCredit.compareTo(BigDecimal.ZERO) > 0
            && bankDebit.compareTo(moveLineCredit.subtract(moveLine.getBankReconciledAmount()))
                == 0)
        && !(bankCredit.compareTo(BigDecimal.ZERO) > 0
            && moveLineDebit.compareTo(BigDecimal.ZERO) > 0
            && bankCredit.compareTo(moveLineDebit.subtract(moveLine.getBankReconciledAmount()))
                == 0)) {
      throw new AxelorException(
          bankReconciliationLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BANK_STATEMENT_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          bankReconciliationLine.getReference() != null
              ? bankReconciliationLine.getReference()
              : "");
    }
  }

  @Transactional
  public BankReconciliationLine reconcileBRLAndMoveLine(
      BankReconciliationLine bankReconciliationLine, MoveLine moveLine) {
    bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId().toString());
    bankReconciliationLine.setConfidenceIndex(
        BankReconciliationLineRepository.CONFIDENCE_INDEX_GREEN);
    moveLine = setMoveLinePostedNbr(moveLine, bankReconciliationLine.getPostedNbr());
    moveLine.setIsSelectedBankReconciliation(false);
    bankReconciliationLine.setIsSelectedBankReconciliation(false);
    bankReconciliationLine.setMoveLine(moveLine);
    BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
    if (!Objects.isNull(bankStatementLine)) {
      bankStatementLine.setMoveLine(bankReconciliationLine.getMoveLine());
    }
    return bankReconciliationLine;
  }

  protected MoveLine setMoveLinePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    if (StringUtils.notEmpty(posted)) {
      List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
      postedNbrs.add(postedNbr);
      posted = String.join(",", postedNbrs);
    } else posted = postedNbr;
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  public void checkIncompleteLine(BankReconciliationLine bankReconciliationLine)
      throws AxelorException {
    if (ObjectUtils.isEmpty(bankReconciliationLine.getMoveLine())) {
      if (ObjectUtils.notEmpty(bankReconciliationLine.getAccount())) {
        if (bankReconciliationLine.getBankReconciliation().getJournal() == null) {
          throw new AxelorException(
              bankReconciliationLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_MISSING_JOURNAL));
        }
        if (bankReconciliationLine.getBankReconciliation().getCashAccount() == null) {
          throw new AxelorException(
              bankReconciliationLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_MISSING_CASH_ACCOUNT));
        }
      }
    }
  }

  public void updateBankReconciledAmounts(BankReconciliationLine bankReconciliationLine) {

    BigDecimal bankReconciledAmount =
        bankReconciliationLine.getDebit().add(bankReconciliationLine.getCredit());

    BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
    if (bankStatementLine != null) {
      bankStatementLine.setAmountRemainToReconcile(
          bankStatementLine.getAmountRemainToReconcile().subtract(bankReconciledAmount));
    }

    MoveLine moveLine = bankReconciliationLine.getMoveLine();

    moveLine.setBankReconciledAmount(bankReconciledAmount);
  }
}
