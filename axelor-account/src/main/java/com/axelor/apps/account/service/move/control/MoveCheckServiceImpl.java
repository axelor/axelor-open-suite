/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.journal.JournalCheckPartnerTypeService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveCheckServiceImpl implements MoveCheckService {

  protected MoveRepository moveRepository;
  protected MoveToolService moveToolService;
  protected PeriodService periodService;
  protected AppAccountService appAccountService;
  protected MoveLineCheckService moveLineCheckService;
  protected JournalCheckPartnerTypeService journalCheckPartnerTypeService;
  protected MoveInvoiceTermService moveInvoiceTermService;

  @Inject
  public MoveCheckServiceImpl(
      MoveRepository moveRepository,
      MoveToolService moveToolService,
      PeriodService periodService,
      AppAccountService appAccountService,
      MoveLineCheckService moveLineCheckService,
      JournalCheckPartnerTypeService journalCheckPartnerTypeService,
      MoveInvoiceTermService moveInvoiceTermService) {
    this.moveRepository = moveRepository;
    this.moveToolService = moveToolService;
    this.periodService = periodService;
    this.appAccountService = appAccountService;
    this.moveLineCheckService = moveLineCheckService;
    this.journalCheckPartnerTypeService = journalCheckPartnerTypeService;
    this.moveInvoiceTermService = moveInvoiceTermService;
  }

  @Override
  public boolean checkRelatedCutoffMoves(Move move) {
    Objects.requireNonNull(move);

    if (appAccountService.getAppAccount().getManageCutOffPeriod()) {
      if (move.getId() != null) {
        return moveRepository
                .all()
                .filter("self.cutOffOriginMove = :id")
                .bind("id", move.getId())
                .count()
            > 0;
      }
    }

    return false;
  }

  @Override
  public Map<String, Object> checkPeriodAndStatus(Move move) throws AxelorException {

    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    resultMap.put("$simulatedPeriodClosed", moveToolService.isSimulatedMovePeriodClosed(move));
    resultMap.put("$periodClosed", periodService.isClosedPeriod(move.getPeriod()));
    return resultMap;
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
  public void checkPartnerCompatible(Move move) throws AxelorException {
    if (move.getPartner() != null) {
      boolean isPartnerCompatible =
          journalCheckPartnerTypeService.isPartnerCompatible(move.getJournal(), move.getPartner());
      if (!isPartnerCompatible) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_PARTNER_IS_NOT_COMPATIBLE_WITH_SELECTED_JOURNAL));
      }
    }
  }

  @Override
  public void checkDuplicatedMoveOrigin(Move move) throws AxelorException {
    if (move.getJournal() != null
        && move.getPartner() != null
        && move.getJournal().getHasDuplicateDetectionOnOrigin()) {
      List<Move> moveList = moveToolService.getMovesWithDuplicatedOrigin(move);
      if (ObjectUtils.notEmpty(moveList)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(AccountExceptionMessage.MOVE_DUPLICATE_ORIGIN_NON_BLOCKING_MESSAGE),
                moveList.stream().map(Move::getReference).collect(Collectors.joining(",")),
                move.getPartner().getFullName(),
                move.getPeriod().getYear().getName()));
      }
    }
  }

  @Override
  public void checkOrigin(Move move) throws AxelorException {
    if (move.getOrigin() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_CHECK_ORIGIN));
    }
  }

  @Override
  public MoveContext checkTermsInPayment(Move move) throws AxelorException {

    MoveContext moveContext = new MoveContext();
    String errorMessage = moveInvoiceTermService.checkIfInvoiceTermInPayment(move);

    if (StringUtils.notEmpty(errorMessage)) {
      if (move.getId() != null) {
        PaymentCondition formerPaymentCondition =
            moveRepository.find(move.getId()).getPaymentCondition();
        move.setPaymentCondition(formerPaymentCondition);
        moveContext.putInValues("paymentCondition", formerPaymentCondition);
      }

      moveContext.putInError(errorMessage);
    }

    return moveContext;
  }
}
