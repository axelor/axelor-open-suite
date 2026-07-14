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
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.BankReconciliationToolService;
import com.axelor.apps.bankpayment.service.moveline.MoveLinePostedNbrService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationValidateService {

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected MoveLineRepository moveLineRepository;
  protected MoveLineCreateService moveLineCreateService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected BankReconciliationComputeService bankReconciliationComputeService;
  protected CurrencyScaleService currencyScaleService;
  protected MoveLinePostedNbrService moveLinePostedNbrService;
  protected BankReconciliationSelectedLineComputationService
      bankReconciliationSelectedLineComputationService;
  protected SequenceService sequenceService;

  @Inject
  public BankReconciliationValidateService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineRepository moveLineRepository,
      MoveLineCreateService moveLineCreateService,
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationLineService bankReconciliationLineService,
      BankReconciliationComputeService bankReconciliationComputeService,
      CurrencyScaleService currencyScaleService,
      MoveLinePostedNbrService moveLinePostedNbrService,
      BankReconciliationSelectedLineComputationService
          bankReconciliationSelectedLineComputationService,
      SequenceService sequenceService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
    this.moveLineRepository = moveLineRepository;
    this.moveLineCreateService = moveLineCreateService;
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.bankReconciliationComputeService = bankReconciliationComputeService;
    this.currencyScaleService = currencyScaleService;
    this.moveLinePostedNbrService = moveLinePostedNbrService;
    this.bankReconciliationSelectedLineComputationService =
        bankReconciliationSelectedLineComputationService;
    this.sequenceService = sequenceService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validate(BankReconciliation bankReconciliation) throws AxelorException {

    // TODO CHECK should be done on all, before generate any moves.
    // Also, line should be sort by date and sequence

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {

      if (!bankReconciliationLine.getIsPosted()) {

        if (bankReconciliationLine.getPostedNbr() != null
            && bankReconciliationLine.getMoveLine() == null) {
          bankReconciliationLine.setIsPosted(true);
          if (bankReconciliationLine.getBankStatementLine() != null) {
            BigDecimal amount =
                bankReconciliationLine
                    .getDebit()
                    .subtract(bankReconciliationLine.getCredit())
                    .abs();
            BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
            bankStatementLine.setAmountRemainToReconcile(
                currencyScaleService.getScaledValue(
                    bankReconciliationLine,
                    bankStatementLine.getAmountRemainToReconcile().subtract(amount)));
          }
        } else if (bankReconciliationLine.getMoveLine() == null
            && bankReconciliationLine.getAccount() != null) {
          this.validate(bankReconciliationLine);
        } else if (bankReconciliationLine.getMoveLine() != null) {
          bankReconciliationLine.setIsPosted(true);
          bankReconciliationLineService.checkAmount(bankReconciliationLine);
          bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
          if (bankReconciliationLine.getBankStatementLine() == null) {
            BigDecimal amount =
                bankReconciliationLine
                    .getDebit()
                    .subtract(bankReconciliationLine.getCredit())
                    .abs();
            bankReconciliationLine
                .getMoveLine()
                .setBankReconciledAmount(
                    currencyScaleService.getScaledValue(bankReconciliationLine, amount));
          }
        }
      }
    }

    bankReconciliation.setStatusSelect(BankReconciliationRepository.STATUS_VALIDATED);
    bankReconciliation.setValidatedByUser(AuthUtils.getUser());
    bankReconciliation.setValidateDateTime(
        Beans.get(AppBaseService.class)
            .getTodayDateTime(bankReconciliation.getCompany())
            .toLocalDateTime());
    bankReconciliation = bankReconciliationComputeService.computeEndingBalance(bankReconciliation);
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

    BigDecimal amount =
        currencyScaleService.getScaledValue(bankReconciliationLine, debit.add(credit));

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
    boolean isForeignCurrency = BankReconciliationToolService.isForeignCurrency(bankReconciliation);

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
          BigDecimal moveLineCredit = moveLine.getCredit();
          if (isForeignCurrency) {
            moveLineCredit = moveLine.getCurrencyAmount().abs();
          }
          debit =
              currencyScaleService.getScaledValue(
                  bankReconciliation,
                  (moveLineCredit.subtract(moveLine.getBankReconciledAmount()))
                      .min(bankStatementAmountRemaining));
          credit = BigDecimal.ZERO;
        } else {
          debit = BigDecimal.ZERO;
          BigDecimal moveLineDebit = moveLine.getDebit();
          if (isForeignCurrency) {
            moveLineDebit = moveLine.getCurrencyAmount().abs();
          }
          credit =
              currencyScaleService.getScaledValue(
                  bankReconciliation,
                  (moveLineDebit.subtract(moveLine.getBankReconciledAmount()))
                      .min(bankStatementAmountRemaining));
        }

        if (firstLine) {
          bankReconciliationLine.setDebit(debit);
          bankReconciliationLine.setCredit(credit);
          bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId().toString());
          moveLine =
              moveLinePostedNbrService.setMoveLinePostedNbr(
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
        bankStatementAmountRemaining =
            currencyScaleService.getScaledValue(
                bankReconciliation, bankStatementAmountRemaining.subtract(debit.add(credit)));
      }

      if (bankStatementAmountRemaining.compareTo(BigDecimal.ZERO) == 1) {
        BigDecimal debit;
        BigDecimal credit;
        if (isDebit) {
          debit =
              currencyScaleService.getScaledValue(bankReconciliation, bankStatementAmountRemaining);
          credit = BigDecimal.ZERO;
        } else {
          debit = BigDecimal.ZERO;
          credit =
              currencyScaleService.getScaledValue(bankReconciliation, bankStatementAmountRemaining);
        }

        bankReconciliationLine =
            bankReconciliationLineService.createBankReconciliationLine(
                effectDate, debit, credit, name, reference, bankStatementLine, null);
        bankReconciliation.addBankReconciliationLineListItem(bankReconciliationLine);
      }

      bankReconciliationRepository.save(bankReconciliation);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validateReconcileToMoveLine(
      BankReconciliation bankReconciliation,
      MoveLine moveLine,
      List<BankReconciliationLine> bankReconciliationLineList,
      boolean userConfirmedOverrun)
      throws AxelorException {

    checkReconcileToMoveLineEligibility(bankReconciliation, moveLine, bankReconciliationLineList);

    List<BankReconciliationLine> consumedLineList =
        capBankReconciliationLinesToMoveLineRemaining(
            bankReconciliation, moveLine, bankReconciliationLineList, userConfirmedOverrun);

    reconcileBankReconciliationLinesToMoveLine(bankReconciliation, moveLine, consumedLineList);
  }

  protected void checkReconcileToMoveLineEligibility(
      BankReconciliation bankReconciliation,
      MoveLine moveLine,
      List<BankReconciliationLine> bankReconciliationLineList)
      throws AxelorException {

    if (CollectionUtils.isEmpty(bankReconciliationLineList)) {
      throw new AxelorException(
          bankReconciliation,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              BankPaymentExceptionMessage
                  .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_NO_LINE_SELECTED));
    }

    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLineList) {
      String reference =
          bankReconciliationLine.getReference() != null
              ? bankReconciliationLine.getReference()
              : "";
      if (!bankReconciliation.equals(bankReconciliationLine.getBankReconciliation())) {
        throw new AxelorException(
            bankReconciliationLine,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_WRONG_BANK_RECONCILIATION),
            reference);
      }
      if (bankReconciliationLine.getMoveLine() != null) {
        throw new AxelorException(
            bankReconciliationLine,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_ALREADY_RECONCILED),
            reference);
      }
      if (bankReconciliationLine.getBankStatementLine() == null) {
        throw new AxelorException(
            bankReconciliationLine,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_MISSING_BANK_STATEMENT_LINE),
            reference);
      }
      if (bankReconciliationLine.getBankStatementLine().getLineTypeSelect()
          != BankStatementLineRepository.LINE_TYPE_MOVEMENT) {
        throw new AxelorException(
            bankReconciliationLine,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_NOT_MOVEMENT),
            reference);
      }
    }
    bankReconciliationLineService.checkReconcileToMoveLine(bankReconciliationLineList, moveLine);

    Company company = bankReconciliation.getCompany();
    if (company == null
        || !company.equals(moveLine.getMove().getCompany())
        || bankReconciliation.getCashAccount() == null
        || !bankReconciliation.getCashAccount().equals(moveLine.getAccount())
        || bankReconciliationSelectedLineComputationService
                .getMoveLineRemainingAmount(moveLine, bankReconciliation)
                .compareTo(BigDecimal.ZERO)
            <= 0) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              BankPaymentExceptionMessage.BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_NOT_ELIGIBLE));
    }

    if (!moveLine.getMove().getCurrency().equals(bankReconciliation.getCurrency())) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              BankPaymentExceptionMessage
                  .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_CURRENCY_MISMATCH));
    }
  }

  protected List<BankReconciliationLine> capBankReconciliationLinesToMoveLineRemaining(
      BankReconciliation bankReconciliation,
      MoveLine moveLine,
      List<BankReconciliationLine> bankReconciliationLineList,
      boolean userConfirmedOverrun)
      throws AxelorException {

    BigDecimal bankMovementsTotal =
        bankReconciliationSelectedLineComputationService.computeBankReconciliationLinesTotal(
            bankReconciliation, bankReconciliationLineList);

    BigDecimal moveLineRemaining =
        bankReconciliationSelectedLineComputationService.getMoveLineRemainingAmount(
            moveLine, bankReconciliation);

    if (bankMovementsTotal.compareTo(moveLineRemaining) <= 0) {
      return new ArrayList<>(bankReconciliationLineList);
    }

    if (!userConfirmedOverrun) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              BankPaymentExceptionMessage
                  .BANK_RECONCILIATION_RECONCILE_TO_MOVE_LINE_OVERRUN_NOT_CONFIRMED));
    }

    List<BankReconciliationLine> consumedLineList = new ArrayList<>();
    BigDecimal remainingCapacity = moveLineRemaining;

    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLineList) {
      if (remainingCapacity.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }

      BigDecimal lineAmount =
          currencyScaleService.getScaledValue(
              bankReconciliation,
              bankReconciliationLine.getDebit().add(bankReconciliationLine.getCredit()));

      if (lineAmount.compareTo(remainingCapacity) <= 0) {
        consumedLineList.add(bankReconciliationLine);
        remainingCapacity =
            currencyScaleService.getScaledValue(
                bankReconciliation, remainingCapacity.subtract(lineAmount));
        continue;
      }

      boolean isDebit = bankReconciliationLine.getDebit().compareTo(BigDecimal.ZERO) > 0;
      BigDecimal keepAmount = remainingCapacity;
      BigDecimal excess =
          currencyScaleService.getScaledValue(bankReconciliation, lineAmount.subtract(keepAmount));

      BankReconciliationLine leftoverLine =
          bankReconciliationLineService.createBankReconciliationLine(
              bankReconciliationLine.getEffectDate(),
              isDebit ? excess : BigDecimal.ZERO,
              isDebit ? BigDecimal.ZERO : excess,
              bankReconciliationLine.getName(),
              bankReconciliationLine.getReference(),
              bankReconciliationLine.getBankStatementLine(),
              null);
      bankReconciliation.addBankReconciliationLineListItem(leftoverLine);

      bankReconciliationLine.setDebit(isDebit ? keepAmount : BigDecimal.ZERO);
      bankReconciliationLine.setCredit(isDebit ? BigDecimal.ZERO : keepAmount);
      consumedLineList.add(bankReconciliationLine);
      remainingCapacity = BigDecimal.ZERO;
    }

    return consumedLineList;
  }

  protected void reconcileBankReconciliationLinesToMoveLine(
      BankReconciliation bankReconciliation,
      MoveLine moveLine,
      List<BankReconciliationLine> consumedLineList)
      throws AxelorException {

    String reconcileNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.BANK_RECONCILIATION,
            bankReconciliation.getCompany(),
            BankReconciliationLine.class,
            "reconcileNumber",
            consumedLineList.get(0));

    if (reconcileNumber == null) {
      throw new AxelorException(
          bankReconciliation,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_COMPANY_NO_SEQUENCE),
          bankReconciliation.getCompany().getName());
    }

    for (BankReconciliationLine bankReconciliationLine : consumedLineList) {
      bankReconciliationLineService.reconcileBRLToMoveLine(
          bankReconciliationLine, moveLine, reconcileNumber);
      bankReconciliationLine.setIsPosted(true);
      bankReconciliationLineService.checkAmount(bankReconciliationLine);
      bankReconciliationLineService.updateBankReconciledAmounts(bankReconciliationLine);
    }

    bankReconciliationRepository.save(bankReconciliation);
  }
}
