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
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.CurrencyScaleService;
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

public class BankReconciliationLineServiceImpl implements BankReconciliationLineService {

  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected MoveLineRepository moveLineRepository;
  protected MoveLineService moveLineService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationLineServiceImpl(
      BankReconciliationLineRepository bankReconciliationLineRepository,
      MoveLineRepository moveLineRepository,
      MoveLineService moveLineService,
      CurrencyScaleService currencyScaleService) {
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.moveLineRepository = moveLineRepository;
    this.moveLineService = moveLineService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
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

  @Override
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

  @Override
  public void checkAmount(BankReconciliationLine bankReconciliationLine) throws AxelorException {
    MoveLine moveLine = bankReconciliationLine.getMoveLine();

    BigDecimal bankDebit =
        currencyScaleService.getScaledValue(
            bankReconciliationLine, bankReconciliationLine.getDebit());
    BigDecimal bankCredit =
        currencyScaleService.getScaledValue(
            bankReconciliationLine, bankReconciliationLine.getCredit());
    boolean isDebit = bankDebit.compareTo(bankCredit) > 0;

    BigDecimal moveLineDebit;
    BigDecimal moveLineCredit;

    if (isDebit) {
      moveLineCredit =
          currencyScaleService.getScaledValue(moveLine, moveLine.getCurrencyAmount().abs());
      moveLineDebit = currencyScaleService.getCompanyScaledValue(moveLine, moveLine.getDebit());
    } else {
      moveLineDebit =
          currencyScaleService.getScaledValue(moveLine, moveLine.getCurrencyAmount().abs());
      moveLineCredit = currencyScaleService.getCompanyScaledValue(moveLine, moveLine.getCredit());
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
            && bankDebit.compareTo(
                    currencyScaleService.getScaledValue(
                        bankReconciliationLine,
                        moveLineCredit.subtract(moveLine.getBankReconciledAmount())))
                == 0)
        && !(bankCredit.compareTo(BigDecimal.ZERO) > 0
            && moveLineDebit.compareTo(BigDecimal.ZERO) > 0
            && bankCredit.compareTo(
                    currencyScaleService.getScaledValue(
                        bankReconciliationLine,
                        moveLineDebit.subtract(moveLine.getBankReconciledAmount())))
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

  @Override
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

  @Override
  public MoveLine setMoveLinePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    if (StringUtils.notEmpty(posted)) {
      List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
      postedNbrs.add(postedNbr);
      posted = String.join(",", postedNbrs);
    } else posted = postedNbr;
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  @Override
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

  @Override
  public void updateBankReconciledAmounts(BankReconciliationLine bankReconciliationLine) {
    BigDecimal bankReconciledAmount =
        currencyScaleService.getScaledValue(
            bankReconciliationLine,
            bankReconciliationLine.getDebit().add(bankReconciliationLine.getCredit()));

    BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
    if (bankStatementLine != null) {
      bankStatementLine.setAmountRemainToReconcile(
          currencyScaleService.getScaledValue(
              bankReconciliationLine,
              bankStatementLine.getAmountRemainToReconcile().subtract(bankReconciledAmount)));
    }

    MoveLine moveLine = bankReconciliationLine.getMoveLine();

    moveLine.setBankReconciledAmount(bankReconciledAmount);
  }

  @Override
  public void unreconcileLines(List<BankReconciliationLine> bankReconciliationLines) {
    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
      if (StringUtils.notEmpty((bankReconciliationLine.getPostedNbr()))) {
        unreconcileLine(bankReconciliationLine);
      }
    }
  }

  @Override
  @Transactional
  public void unreconcileLine(BankReconciliationLine bankReconciliationLine) {
    bankReconciliationLine.setBankStatementQuery(null);
    bankReconciliationLine.setIsSelectedBankReconciliation(false);

    String query = "self.postedNbr LIKE '%%s%'";
    query = query.replace("%s", bankReconciliationLine.getPostedNbr());
    List<MoveLine> moveLines = moveLineRepository.all().filter(query).fetch();
    for (MoveLine moveLine : moveLines) {
      moveLineService.removePostedNbr(moveLine, bankReconciliationLine.getPostedNbr());
      moveLine.setIsSelectedBankReconciliation(false);
    }
    boolean isUnderCorrection =
        bankReconciliationLine.getBankReconciliation().getStatusSelect()
            == BankReconciliationRepository.STATUS_UNDER_CORRECTION;
    if (isUnderCorrection) {
      MoveLine moveLine = bankReconciliationLine.getMoveLine();
      BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
      if (bankStatementLine != null) {
        bankStatementLine.setAmountRemainToReconcile(
            currencyScaleService.getScaledValue(
                bankStatementLine,
                bankStatementLine
                    .getAmountRemainToReconcile()
                    .add(moveLine.getBankReconciledAmount())));
      }
      moveLine.setBankReconciledAmount(BigDecimal.ZERO);
      moveLineRepository.save(moveLine);
      bankReconciliationLine.setIsPosted(false);
    }
    bankReconciliationLine.setMoveLine(null);
    bankReconciliationLine.setConfidenceIndex(0);
    bankReconciliationLine.setPostedNbr(null);
  }

  @Override
  @Transactional
  public BankReconciliationLine setSelected(BankReconciliationLine bankReconciliationLineContext) {
    BankReconciliationLine bankReconciliationLine =
        bankReconciliationLineRepository.find(bankReconciliationLineContext.getId());
    if (bankReconciliationLine.getIsSelectedBankReconciliation() != null) {
      bankReconciliationLine.setIsSelectedBankReconciliation(
          !bankReconciliationLineContext.getIsSelectedBankReconciliation());
    } else {
      bankReconciliationLine.setIsSelectedBankReconciliation(true);
    }
    return bankReconciliationLineRepository.save(bankReconciliationLine);
  }
}
