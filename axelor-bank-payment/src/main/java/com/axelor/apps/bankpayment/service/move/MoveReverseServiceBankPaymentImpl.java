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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseServiceImpl;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveReverseServiceBankPaymentImpl extends MoveReverseServiceImpl {

  protected BankReconciliationService bankReconciliationService;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;

  @Inject
  public MoveReverseServiceBankPaymentImpl(
      MoveCreateService moveCreateService,
      ReconcileService reconcileService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineCreateService moveLineCreateService,
      ExtractContextMoveService extractContextMoveService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentCancelService invoicePaymentCancelService,
      BankReconciliationService bankReconciliationService,
      BankReconciliationLineRepository bankReconciliationLineRepository) {
    super(
        moveCreateService,
        reconcileService,
        moveValidateService,
        moveRepository,
        moveLineCreateService,
        extractContextMoveService,
        invoicePaymentRepository,
        invoicePaymentCancelService);
    this.bankReconciliationService = bankReconciliationService;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateReverse(Move move, Map<String, Object> assistantMap) throws AxelorException {

    boolean isHiddenMoveLinesInBankReconciliation =
        (boolean) assistantMap.get("isHiddenMoveLinesInBankReconciliation");
    List<BankReconciliationLine> bankReconciliationLineList = new ArrayList<>();
    if (isHiddenMoveLinesInBankReconciliation) {
      bankReconciliationLineList =
          bankReconciliationLineRepository
              .all()
              .filter("self.moveLine IN :moveLines")
              .bind("moveLines", move.getMoveLineList())
              .fetch();
      if (bankReconciliationLineList.stream()
          .anyMatch(
              bankReconciliationLine ->
                  bankReconciliationLine.getBankReconciliation().getStatusSelect()
                      == BankReconciliationRepository.STATUS_VALIDATED)) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.MOVE_LINKED_TO_VALIDATED_BANK_RECONCILIATION),
            move.getReference());
      }
    }
    Move newMove = super.generateReverse(move, assistantMap);
    if (isHiddenMoveLinesInBankReconciliation) {
      fillBankReconciledAmount(newMove);

      bankReconciliationService.unreconcileLines(
          bankReconciliationLineList.stream()
              .filter(
                  bankReconciliationLine ->
                      bankReconciliationLine.getBankReconciliation().getStatusSelect()
                          == BankReconciliationRepository.STATUS_UNDER_CORRECTION)
              .collect(Collectors.toList()));

      fillBankReconciledAmount(move);

    } else {
      this.updateBankAmountReconcile(newMove);
    }
    return newMove;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> massReverse(List<Move> moveList, Map<String, Object> assistantMap)
      throws AxelorException {
    boolean isHiddenMoveLinesInBankReconciliation =
        (boolean) assistantMap.get("isHiddenMoveLinesInBankReconciliation");
    List<Move> movesReconciled = new ArrayList<>();
    if (isHiddenMoveLinesInBankReconciliation) {
      for (Move move : moveList) {
        List<BankReconciliationLine> bankReconciliationValidatedLineList =
            bankReconciliationLineRepository
                .all()
                .filter(
                    "self.moveLine IN :moveLines AND self.bankReconciliation.statusSelect = :statusValidated")
                .bind("moveLines", move.getMoveLineList())
                .bind("statusValidated", BankReconciliationRepository.STATUS_VALIDATED)
                .fetch();
        if (CollectionUtils.isNotEmpty(bankReconciliationValidatedLineList)) {
          movesReconciled.add(move);
        }
      }
      moveList.removeAll(movesReconciled);
    }
    List<Move> newMoveList = super.massReverse(moveList, assistantMap);
    if (isHiddenMoveLinesInBankReconciliation) {
      for (Move move : moveList) {
        List<BankReconciliationLine> bankReconciliationUnderCorrectionLineList =
            bankReconciliationLineRepository
                .all()
                .filter(
                    "self.moveLine IN :moveLines AND self.bankReconciliation.statusSelect = :statusUnderCorrection")
                .bind("moveLines", move.getMoveLineList())
                .bind("statusUnderCorrection", BankReconciliationRepository.STATUS_UNDER_CORRECTION)
                .fetch();
        bankReconciliationService.unreconcileLines(bankReconciliationUnderCorrectionLineList);
      }
    }
    if (CollectionUtils.isNotEmpty(movesReconciled)) {
      if (movesReconciled.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.MOVE_LINKED_TO_VALIDATED_BANK_RECONCILIATION),
            movesReconciled.get(0).getReference());
      } else {
        String moveReferencesReconciled = getMoveReferencesReconciledString(movesReconciled);
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.MOVES_LINKED_TO_VALIDATED_BANK_RECONCILIATION),
            moveReferencesReconciled);
      }
    }
    return newMoveList;
  }

  protected String getMoveReferencesReconciledString(List<Move> movesReconciled) {
    StringBuilder moveReferencesReconciled = new StringBuilder();
    String separator = ", ";
    for (String moveReferenceReconciled :
        movesReconciled.stream().map(move -> move.getReference()).collect(Collectors.toList())) {
      moveReferencesReconciled.append(moveReferenceReconciled);
      moveReferencesReconciled.append(separator);
    }
    int lastIndexComma = moveReferencesReconciled.lastIndexOf(",");
    moveReferencesReconciled.deleteCharAt(lastIndexComma);
    lastIndexComma = moveReferencesReconciled.lastIndexOf(",");
    moveReferencesReconciled.replace(lastIndexComma, lastIndexComma + 1, " " + I18n.get("and"));
    return moveReferencesReconciled.toString();
  }

  @Override
  protected MoveLine generateReverseMoveLine(
      Move reverseMove, MoveLine orgineMoveLine, LocalDate dateOfReversion, boolean isDebit)
      throws AxelorException {
    MoveLine reverseMoveLine =
        super.generateReverseMoveLine(reverseMove, orgineMoveLine, dateOfReversion, isDebit);
    if (reverseMoveLine
        .getAccount()
        .getAccountType()
        .getTechnicalTypeSelect()
        .equals(AccountTypeRepository.TYPE_CASH)) {
      reverseMoveLine.setBankReconciledAmount(
          reverseMoveLine.getCurrencyAmount().subtract(orgineMoveLine.getBankReconciledAmount()));
    }
    return reverseMoveLine;
  }

  protected void updateBankAmountReconcile(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setBankReconciledAmount(BigDecimal.ZERO);
    }
  }

  protected void fillBankReconciledAmount(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (Optional.of(moveLine)
          .map(MoveLine::getAccount)
          .map(Account::getAccountType)
          .map(
              accountType ->
                  AccountTypeRepository.TYPE_CASH.equals(accountType.getTechnicalTypeSelect()))
          .orElse(false)) {
        moveLine.setBankReconciledAmount(moveLine.getCurrencyAmount().abs());
      }
    }
  }
}
