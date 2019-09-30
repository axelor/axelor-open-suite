/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public class BankReconciliationValidateService {

  protected MoveService moveService;
  protected MoveRepository moveRepository;
  protected MoveLineRepository moveLineRepository;
  protected MoveLineService moveLineService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankReconciliationLineService bankReconciliationLineService;

  @Inject
  public BankReconciliationValidateService(
      MoveService moveService,
      MoveRepository moveRepository,
      MoveLineRepository moveLineRepository,
      MoveLineService moveLineService,
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationLineService bankReconciliationLineService) {

    this.moveService = moveService;
    this.moveRepository = moveRepository;
    this.moveLineRepository = moveLineRepository;
    this.moveLineService = moveLineService;
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankReconciliationLineService = bankReconciliationLineService;
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void validate(BankReconciliation bankReconciliation) throws AxelorException {

    // TODO CHECK should be done on all, before generate any moves.
    // Also, line should be sort by date and sequence

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {

      if (!bankReconciliationLine.getIsPosted()) {

        if (bankReconciliationLine.getMoveLine() == null
            && bankReconciliationLine.getAccount() != null) {
          this.validate(bankReconciliationLine);
        } else if (bankReconciliationLine.getMoveLine() != null) {
          bankReconciliationLineService.checkAmount(bankReconciliationLine);
          updateBankReconciledAmounts(bankReconciliationLine);
        }
      }
    }

    bankReconciliation.setStatusSelect(BankReconciliationRepository.STATUS_VALIDATED);
    bankReconciliation.setValidatedByUser(AuthUtils.getUser());
    bankReconciliation.setValidatedDate(Beans.get(AppBaseService.class).getTodayDate());

    bankReconciliationRepository.save(bankReconciliation);
  }

  protected void validate(BankReconciliationLine bankReconciliationLine) throws AxelorException {

    BigDecimal debit = bankReconciliationLine.getDebit();
    BigDecimal credit = bankReconciliationLine.getCredit();

    if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    BankReconciliation bankReconciliation = bankReconciliationLine.getBankReconciliation();

    Company company = bankReconciliation.getCompany();
    LocalDate effectDate = bankReconciliationLine.getEffectDate();

    Partner partner = bankReconciliationLine.getPartner();

    String reference = bankReconciliationLine.getReference();
    String name = bankReconciliationLine.getName();
    if (name != null && name.length() > 255) {
      name = name.substring(0, 255);
    }

    BigDecimal amount = debit.add(credit);

    String origin = bankReconciliation.getName() + reference != null ? " - " + reference : "";

    boolean sign = credit.compareTo(BigDecimal.ZERO) > 0;

    Move move =
        moveService
            .getMoveCreateService()
            .createMove(
                bankReconciliation.getJournal(),
                company,
                null,
                partner,
                effectDate,
                null,
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    MoveLine partnerMoveLine =
        moveLineService.createMoveLine(
            move,
            partner,
            bankReconciliationLine.getAccount(),
            amount,
            sign,
            effectDate,
            effectDate,
            1,
            origin,
            name);
    move.addMoveLineListItem(partnerMoveLine);

    MoveLine cashMoveLine =
        moveLineService.createMoveLine(
            move,
            partner,
            bankReconciliation.getCashAccount(),
            amount,
            !sign,
            effectDate,
            effectDate,
            2,
            origin,
            name);
    cashMoveLine.setBankReconciledAmount(amount);

    move.addMoveLineListItem(cashMoveLine);

    moveRepository.save(move);

    moveService.getMoveValidateService().validate(move);

    bankReconciliationLine.setMoveLine(cashMoveLine);

    bankReconciliationLine.setIsPosted(true);

    updateBankReconciledAmounts(bankReconciliationLine);
  }

  protected void updateBankReconciledAmounts(BankReconciliationLine bankReconciliationLine) {

    bankReconciliationLine.setIsPosted(true);

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

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void validateMultipleBankReconciles(
      BankReconciliation bankReconciliation,
      BankReconciliationLine bankReconciliationLine,
      List<HashMap<String, Object>> moveLinesToReconcileContext)
      throws AxelorException {

    LocalDate effectDate = bankReconciliationLine.getEffectDate();
    String name = bankReconciliationLine.getName();
    String reference = bankReconciliationLine.getReference();
    BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
    BigDecimal bankStatementAmountRemaining = bankStatementLine.getAmountRemainToReconcile();
    boolean isDebit = bankReconciliationLine.getDebit().compareTo(BigDecimal.ZERO) == 1;

    boolean firstLine = true;

    if ((moveLinesToReconcileContext != null && !moveLinesToReconcileContext.isEmpty())) {
      for (HashMap<String, Object> moveLineToReconcile : moveLinesToReconcileContext) {

        if (bankStatementAmountRemaining.compareTo(BigDecimal.ZERO) != 1) {
          break;
        }

        MoveLine moveLine =
            moveLineRepository.find(((Integer) moveLineToReconcile.get("id")).longValue());
        BigDecimal debit;
        BigDecimal credit;
        if (isDebit) {
          debit =
              (moveLine.getCredit().subtract(moveLine.getBankReconciledAmount()))
                  .min(bankStatementAmountRemaining);
          credit = BigDecimal.ZERO;
        } else {
          debit = BigDecimal.ZERO;
          credit =
              (moveLine.getDebit().subtract(moveLine.getBankReconciledAmount()))
                  .min(bankStatementAmountRemaining);
        }

        if (firstLine) {
          bankReconciliationLine.setDebit(debit);
          bankReconciliationLine.setCredit(credit);
          bankReconciliationLine.setMoveLine(moveLine);
          firstLine = false;
        } else {
          bankReconciliation.addBankReconciliationLineListItem(
              bankReconciliationLineService.createBankReconciliationLine(
                  effectDate, debit, credit, name, reference, bankStatementLine, moveLine));
        }

        bankStatementAmountRemaining = bankStatementAmountRemaining.subtract(debit.add(credit));
      }

      if (bankStatementAmountRemaining.compareTo(BigDecimal.ZERO) == 1) {
        BigDecimal debit;
        BigDecimal credit;
        if (isDebit) {
          debit = bankStatementAmountRemaining;
          credit = BigDecimal.ZERO;
        } else {
          debit = BigDecimal.ZERO;
          credit = bankStatementAmountRemaining;
        }

        bankReconciliation.addBankReconciliationLineListItem(
            bankReconciliationLineService.createBankReconciliationLine(
                effectDate, debit, credit, name, reference, bankStatementLine, null));
      }

      bankReconciliationRepository.save(bankReconciliation);
    }
  }
}
