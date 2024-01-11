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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public class BankReconciliationValidateService {

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected MoveLineRepository moveLineRepository;
  protected MoveLineCreateService moveLineCreateService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected BankReconciliationService bankReconciliationService;

  @Inject
  public BankReconciliationValidateService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineRepository moveLineRepository,
      MoveLineCreateService moveLineCreateService,
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationLineService bankReconciliationLineService,
      BankReconciliationService bankReconciliationService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
    this.moveLineRepository = moveLineRepository;
    this.moveLineCreateService = moveLineCreateService;
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.bankReconciliationService = bankReconciliationService;
  }

  @Transactional(rollbackOn = {Exception.class})
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
          bankReconciliationLine.setIsPosted(true);
          bankReconciliationLineService.checkAmount(bankReconciliationLine);
          bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
        }
      }
    }

    bankReconciliation.setStatusSelect(BankReconciliationRepository.STATUS_VALIDATED);
    bankReconciliation.setValidatedByUser(AuthUtils.getUser());
    bankReconciliation.setValidateDateTime(
        Beans.get(AppBaseService.class)
            .getTodayDateTime(bankReconciliation.getCompany())
            .toLocalDateTime());
    bankReconciliation = bankReconciliationService.computeEndingBalance(bankReconciliation);
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
    String description = bankReconciliationLine.getName();
    if (description != null && description.length() > 255) {
      description = description.substring(0, 255);
    }

    BigDecimal amount = debit.add(credit);

    String origin = bankReconciliation.getName() + reference != null ? " - " + reference : "";

    boolean isDebit = debit.compareTo(BigDecimal.ZERO) > 0;

    Move move =
        moveCreateService.createMove(
            bankReconciliation.getJournal(),
            company,
            null,
            partner,
            effectDate,
            effectDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            description,
            bankReconciliation.getBankDetails());

    MoveLine partnerMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            bankReconciliationLine.getAccount(),
            amount,
            isDebit,
            effectDate,
            effectDate,
            1,
            origin,
            description);
    move.addMoveLineListItem(partnerMoveLine);

    MoveLine cashMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            bankReconciliation.getCashAccount(),
            amount,
            !isDebit,
            effectDate,
            effectDate,
            2,
            origin,
            description);
    cashMoveLine.setBankReconciledAmount(amount);

    move.addMoveLineListItem(cashMoveLine);

    moveRepository.save(move);

    moveValidateService.accounting(move);

    bankReconciliationLineService.reconcileBRLAndMoveLine(bankReconciliationLine, cashMoveLine);

    bankReconciliationLine.setIsPosted(true);

    bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
  }

  @Transactional
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
      boolean isUnderCorrection =
          bankReconciliation.getStatusSelect()
              == BankReconciliationRepository.STATUS_UNDER_CORRECTION;
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
          bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId().toString());
          moveLine =
              bankReconciliationLineService.setMoveLinePostedNbr(
                  moveLine, bankReconciliationLine.getPostedNbr());
          bankReconciliationLine.setMoveLine(moveLine);
          firstLine = false;
        } else {
          bankReconciliationLine =
              bankReconciliationLineService.createBankReconciliationLine(
                  effectDate, debit, credit, name, reference, bankStatementLine, moveLine);
          bankReconciliation.addBankReconciliationLineListItem(bankReconciliationLine);
        }
        if (isUnderCorrection) {
          bankReconciliationLine.setIsPosted(true);
          bankReconciliationLineService.checkAmount(bankReconciliationLine);
          bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
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

        bankReconciliationLine =
            bankReconciliationLineService.createBankReconciliationLine(
                effectDate, debit, credit, name, reference, bankStatementLine, null);
        bankReconciliation.addBankReconciliationLineListItem(bankReconciliationLine);
      }

      bankReconciliationRepository.save(bankReconciliation);
    }
  }
}
