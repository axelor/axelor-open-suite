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
package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.journal.JournalCheckPartnerTypeService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveCheckServiceImpl implements MoveCheckService {

  protected MoveRepository moveRepository;
  protected MoveToolService moveToolService;
  protected PeriodService periodService;
  protected AppAccountService appAccountService;
  protected MoveLineCheckService moveLineCheckService;
  protected MoveLineService moveLineService;
  protected JournalCheckPartnerTypeService journalCheckPartnerTypeService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected PaymentConditionService paymentConditionService;

  @Inject
  public MoveCheckServiceImpl(
      MoveRepository moveRepository,
      MoveToolService moveToolService,
      PeriodService periodService,
      AppAccountService appAccountService,
      MoveLineCheckService moveLineCheckService,
      MoveLineService moveLineService,
      JournalCheckPartnerTypeService journalCheckPartnerTypeService,
      MoveInvoiceTermService moveInvoiceTermService,
      PaymentConditionService paymentConditionService) {
    this.moveRepository = moveRepository;
    this.moveToolService = moveToolService;
    this.periodService = periodService;
    this.appAccountService = appAccountService;
    this.moveLineCheckService = moveLineCheckService;
    this.moveLineService = moveLineService;
    this.journalCheckPartnerTypeService = journalCheckPartnerTypeService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.paymentConditionService = paymentConditionService;
  }

  @Override
  public boolean isRelatedCutoffMoves(Move move) {
    return appAccountService.getAppAccount().getManageCutOffPeriod()
        && move.getId() != null
        && moveRepository
                .all()
                .filter("self.cutOffOriginMove = :id")
                .bind("id", move.getId())
                .count()
            > 0;
  }

  @Override
  public void checkPeriodPermission(Move move) throws AxelorException {
    if (periodService.isClosedPeriod(move.getPeriod())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PERIOD_CLOSED_AND_NO_PERMISSIONS));
    }
  }

  @Override
  public void checkDates(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    moveLineCheckService.checkDates(move);
  }

  @Override
  public void checkRemovedLines(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    if (move.getId() == null) {
      return;
    }

    Move moveDB = moveRepository.find(move.getId());

    List<String> moveLineReconciledAndRemovedNameList = new ArrayList<>();
    for (MoveLine moveLineBD : moveDB.getMoveLineList()) {
      if (!move.getMoveLineList().contains(moveLineBD)) {
        if (moveLineBD.getReconcileGroup() != null) {
          moveLineReconciledAndRemovedNameList.add(moveLineBD.getName());
        }
      }
    }
    if (moveLineReconciledAndRemovedNameList != null
        && !moveLineReconciledAndRemovedNameList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(AccountExceptionMessage.MOVE_LINE_RECONCILE_LINE_CANNOT_BE_REMOVED),
              moveLineReconciledAndRemovedNameList.toString()));
    }
  }

  @Override
  public void checkAnalyticAccount(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      moveLineCheckService.checkAnalyticAccount(move.getMoveLineList());
    }
  }

  @Override
  public boolean isPartnerCompatible(Move move) {
    return move.getPartner() == null
        || journalCheckPartnerTypeService.isPartnerCompatible(move.getJournal(), move.getPartner());
  }

  @Override
  public String getDuplicatedMoveOriginAlert(Move move) {
    if (move.getJournal() != null
        && move.getPartner() != null
        && move.getJournal().getHasDuplicateDetectionOnOrigin()) {
      List<Move> moveList = moveToolService.getMovesWithDuplicatedOrigin(move);

      if (ObjectUtils.notEmpty(moveList)) {
        return String.format(
            I18n.get(AccountExceptionMessage.MOVE_DUPLICATE_ORIGIN_NON_BLOCKING_MESSAGE),
            moveList.stream().map(Move::getReference).collect(Collectors.joining(",")),
            move.getPartner().getFullName(),
            move.getPeriod().getYear().getName());
      }
    }

    return null;
  }

  @Override
  public String getOriginAlert(Move move) {
    if (move.getOrigin() == null) {
      return I18n.get(AccountExceptionMessage.MOVE_CHECK_ORIGIN);
    }

    return null;
  }

  @Override
  public void checkTermsInPayment(Move move) throws AxelorException {
    String errorMessage = moveInvoiceTermService.checkIfInvoiceTermInPayment(move);

    if (StringUtils.notEmpty(errorMessage)) {
      if (move.getId() != null) {
        PaymentCondition formerPaymentCondition =
            moveRepository.find(move.getId()).getPaymentCondition();
        paymentConditionService.checkPaymentCondition(formerPaymentCondition);
        move.setPaymentCondition(formerPaymentCondition);
      }

      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, errorMessage);
    }
  }

  @Override
  public String getDescriptionAlert(Move move) {
    if (move.getDescription() == null) {
      return I18n.get(AccountExceptionMessage.MOVE_CHECK_DESCRIPTION);
    }

    return null;
  }

  @Override
  public String getAccountingAlert(Move move) {
    if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
        || ((!move.getCompany().getAccountConfig().getAccountingDaybook()
                || !move.getJournal().getAllowAccountingDaybook())
            && (move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED))) {
      return I18n.get(AccountExceptionMessage.MOVE_CHECK_ACCOUNTING);
    }

    return null;
  }

  @Override
  public void checkManageCutOffDates(Move move) throws AxelorException {
    if (!(CollectionUtils.isNotEmpty(move.getMoveLineList())
        && move.getMoveLineList().stream().anyMatch(moveLineService::checkManageCutOffDates))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.NO_CUT_OFF_TO_APPLY));
    }
  }

  @Override
  public String getPeriodAlert(Move move) {
    try {
      if (move.getDate() != null && move.getCompany() != null) {
        periodService.getActivePeriod(
            move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
      }
    } catch (AxelorException axelorException) {
      return axelorException.getMessage();
    }

    return null;
  }
}
