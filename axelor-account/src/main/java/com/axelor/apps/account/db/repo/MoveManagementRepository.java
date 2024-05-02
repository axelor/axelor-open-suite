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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.util.InvoiceTermUtilsService;
import com.axelor.apps.account.util.MoveLineUtilsService;
import com.axelor.apps.account.util.MoveUtilsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.utils.PeriodUtilsService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class MoveManagementRepository extends MoveRepository {

  protected PeriodUtilsService periodUtilsService;
  protected AppBaseService appBaseService;
  protected MoveUtilsService moveUtilsService;
  protected MoveSequenceService moveSequenceService;
  protected InvoiceTermUtilsService invoiceTermUtilsService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected MoveLineUtilsService moveLineUtilsService;

  @Inject
  public MoveManagementRepository(
      PeriodUtilsService periodUtilsService,
      AppBaseService appBaseService,
      MoveUtilsService moveUtilsService,
      MoveSequenceService moveSequenceService,
      InvoiceTermUtilsService invoiceTermUtilsService,
      InvoiceTermToolService invoiceTermToolService,
      MoveLineUtilsService moveLineUtilsService) {
    this.periodUtilsService = periodUtilsService;
    this.appBaseService = appBaseService;
    this.moveUtilsService = moveUtilsService;
    this.moveSequenceService = moveSequenceService;
    this.invoiceTermUtilsService = invoiceTermUtilsService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.moveLineUtilsService = moveLineUtilsService;
  }

  @Override
  public Move copy(Move entity, boolean deep) {
    Move copy = super.copy(entity, deep);

    try {
      copy.setDate(appBaseService.getTodayDate(copy.getCompany()));

      Period period =
          periodUtilsService.getActivePeriod(
              copy.getDate(), entity.getCompany(), YearRepository.TYPE_FISCAL);
      String origin = entity.getOrigin();
      if (entity.getJournal().getHasDuplicateDetectionOnOrigin()
          && entity.getJournal().getPrefixOrigin() != null) {
        origin = entity.getJournal().getPrefixOrigin() + origin;
      }

      copy.setStatusSelect(STATUS_NEW);
      copy.setTechnicalOriginSelect(MoveRepository.TECHNICAL_ORIGIN_ENTRY);
      copy.setReference(null);
      copy.setExportNumber(null);
      copy.setExportDate(null);
      copy.setAccountingReport(null);
      copy.setAccountingDate(null);
      copy.setPeriod(period);
      copy.setAccountingOk(false);
      copy.setIgnoreInDebtRecoveryOk(false);
      copy.setPaymentVoucher(null);
      copy.setRejectOk(false);
      copy.setInvoice(null);
      copy.setPaymentSession(null);
      copy.setOrigin(origin);
      copy.setReasonOfRefusalToPay(null);
      copy.setReasonOfRefusalToPayStr(null);
      moveUtilsService.setPfpStatus(copy);

      List<MoveLine> moveLineList = copy.getMoveLineList();

      if (moveLineList != null) {

        for (MoveLine moveLine : moveLineList) {
          resetMoveLine(moveLine, copy.getDate(), copy);
          moveLineUtilsService.updateInvoiceTermsParentFields(moveLine);
        }
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return copy;
  }

  public void resetMoveLine(MoveLine moveLine, LocalDate date, Move move) throws AxelorException {
    moveLine.setInvoiceReject(null);
    moveLine.setDate(date);
    moveLine.setExportedDirectDebitOk(false);
    moveLine.setReimbursementStatusSelect(MoveLineRepository.REIMBURSEMENT_STATUS_NULL);
    moveLine.setReconcileGroup(null);
    moveLine.setDebitReconcileList(null);
    moveLine.setCreditReconcileList(null);
    moveLine.setAmountPaid(BigDecimal.ZERO);
    moveLine.setTaxPaymentMoveLineList(null);
    moveLine.setTaxAmount(BigDecimal.ZERO);
    moveLine.setPostedNbr(null);
    moveLine.setOrigin(move.getOrigin());
    moveLine.setOriginDate(move.getOriginDate());

    List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();

    if (analyticMoveLineList != null) {
      moveLine.getAnalyticMoveLineList().forEach(line -> line.setDate(moveLine.getDate()));
    }

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : moveLine.getInvoiceTermList()) {
        this.resetInvoiceTerm(invoiceTerm);
      }
    }
  }

  public void resetInvoiceTerm(InvoiceTerm invoiceTerm) throws AxelorException {
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setIsSelectedOnPaymentSession(false);
    invoiceTerm.setDebtRecoveryBlockingOk(false);
    invoiceTerm.setAmountRemaining(invoiceTerm.getAmount());
    invoiceTerm.setCompanyAmountRemaining(invoiceTerm.getCompanyAmount());
    invoiceTerm.setAmountRemainingAfterFinDiscount(
        invoiceTerm.getRemainingAmountAfterFinDiscount());
    invoiceTerm.setInitialPfpAmount(BigDecimal.ZERO);
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);
    invoiceTerm.setRemainingPfpAmount(BigDecimal.ZERO);
    invoiceTerm.setAmountPaid(BigDecimal.ZERO);
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    invoiceTerm.setImportId(null);
    invoiceTerm.setPaymentSession(null);
    invoiceTerm.setPfpPartialReason(null);
    invoiceTerm.setReasonOfRefusalToPay(null);
    invoiceTerm.setReasonOfRefusalToPayStr(null);
    invoiceTerm.setDecisionPfpTakenDateTime(null);
    invoiceTerm.setInvoice(null);

    invoiceTermUtilsService.setPfpStatus(invoiceTerm, null);
  }

  @Override
  public Move save(Move move) {
    try {
      MoveValidateService moveValidateService = Beans.get(MoveValidateService.class);

      moveValidateService.checkMoveLinesPartner(move);
      moveValidateService.checkJournalPermissions(move);

      if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
          || move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
          || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED) {
        moveValidateService.checkPreconditions(move);
      }
      if (move.getCurrency() != null) {
        move.setCurrencyCode(move.getCurrency().getCodeISO());
      }

      moveSequenceService.setDraftSequence(move);
      MoveLineControlService moveLineControlService = Beans.get(MoveLineControlService.class);

      List<MoveLine> moveLineList = move.getMoveLineList();
      if (moveLineList != null) {
        for (MoveLine moveLine : moveLineList) {
          moveLineControlService.validateMoveLine(moveLine);
          List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();
          if (analyticMoveLineList != null) {
            for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
              analyticMoveLine.setAccount(moveLine.getAccount());
              analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
            }
          }
          moveLineControlService.controlAccountingAccount(moveLine);

          if (!moveLine.getAccount().getUseForPartnerBalance()
              && CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
            if (moveLine.getInvoiceTermList().stream()
                    .allMatch(invoiceTermToolService::isNotReadonly)
                && moveLine.getInvoiceTermList().stream().noneMatch(InvoiceTerm::getIsHoldBack)) {
              moveLine.clearInvoiceTermList();
            } else {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_INCONSISTENCY,
                  I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_ACCOUNT_CHANGE));
            }
          }
        }
      }

      return super.save(move);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(Move entity) {
    if (!entity.getStatusSelect().equals(MoveRepository.STATUS_NEW)
        && !entity.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_REMOVE_NOT_OK),
            entity.getReference());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    } else {
      try {
        if (entity.getStatusSelect().equals(MoveRepository.STATUS_NEW)) {
          moveUtilsService.checkMoveBeforeRemove(entity);
        }
      } catch (Exception e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
        throw new PersistenceException(e.getMessage(), e);
      }
      super.remove(entity);
    }
  }
}
