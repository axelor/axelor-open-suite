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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class MoveRecordServiceImpl implements MoveRecordService {

  protected MoveDefaultService moveDefaultService;
  protected MoveAttrsService moveAttrsService;
  protected PeriodServiceAccount periodAccountService;
  protected MoveCheckService moveCheckService;
  protected MoveComputeService moveComputeService;
  protected MoveRecordUpdateService moveRecordUpdateService;
  protected MoveRecordSetService moveRecordSetService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveRecordServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveComputeService moveComputeService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveRepository moveRepository) {
    this.moveDefaultService = moveDefaultService;
    this.moveAttrsService = moveAttrsService;
    this.periodAccountService = periodAccountService;
    this.moveCheckService = moveCheckService;
    this.moveComputeService = moveComputeService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveRepository = moveRepository;
    this.moveRecordSetService = moveRecordSetService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveBefore(Move move, boolean paymentConditionChange, boolean headerChange)
      throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    moveRecordUpdateService.updatePartner(move);
    result.merge(
        moveRecordUpdateService.updateInvoiceTerms(move, paymentConditionChange, headerChange));
    result.merge(moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate()));

    return result;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveCheck(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    moveCheckService.checkDates(move);
    moveCheckService.checkPeriodPermission(move);
    moveCheckService.checkRemovedLines(move);
    moveCheckService.checkAnalyticAccount(move);

    return result;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveAfter(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    move = moveRepository.find(move.getId());
    MoveContext result = new MoveContext();

    // No need to merge result
    moveRecordUpdateService.updateRoundInvoiceTermPercentages(move);

    // Move will be saved again in this method
    moveRecordUpdateService.updateInDayBookMode(move);

    return result;
  }

  @Override
  public MoveContext onNew(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveDefaultService.setDefaultMoveValues(move));
    result.putInValues(moveDefaultService.setDefaultCurrency(move));
    result.putInValues(moveRecordSetService.setJournal(move));
    moveRecordSetService.setPeriod(move);
    result.putInValues("period", move.getPeriod());
    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    result.putInAttrs(
        "$reconcileTags", "hidden", moveAttrsService.isHiddenMoveLineListViewer(move));
    result.putInValues(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    result.putInValues(moveCheckService.checkPeriodAndStatus(move));
    result.putInAttrs(moveAttrsService.getFunctionalOriginSelectDomain(move));
    result.putInValues(moveRecordSetService.setFunctionalOriginSelect(move));
    moveCheckService.checkPeriodPermission(move);
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));

    return result;
  }

  @Override
  public MoveContext onLoad(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    result.putInValues(moveComputeService.computeTotals(move));
    result.putInAttrs(
        "$reconcileTags", "hidden", moveAttrsService.isHiddenMoveLineListViewer(move));
    result.putInValues(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    result.putInValues(
        "$isThereRelatedCutOffMoves", moveCheckService.checkRelatedCutoffMoves(move));
    result.putInValues(moveCheckService.checkPeriodAndStatus(move));
    result.putInAttrs(moveAttrsService.getFunctionalOriginSelectDomain(move));
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));
    result.putInAttrs("dueDate", "hidden", moveAttrsService.isHiddenDueDate(move));

    return result;
  }

  @Override
  public MoveContext onChangeDate(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveRecordSetService.setPeriod(move));
    result.putInValues(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    moveCheckService.checkPeriodPermission(move);
    result.putInValues(moveRecordSetService.setMoveLineDates(move));
    result.merge(moveRecordUpdateService.updateMoveLinesCurrencyRate(move, move.getDueDate()));
    result.putInValues(moveComputeService.computeTotals(move));
    dateChange = true;
    result.putInAttrs("$dateChange", "value", true);
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));

    return result;
  }

  protected void updateDummiesDateConText(Move move) {
    move.setDueDate(null);
  }

  @Override
  public MoveContext onChangeJournal(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInAttrs(moveAttrsService.getFunctionalOriginSelectDomain(move));
    result.putInValues(moveRecordSetService.setFunctionalOriginSelect(move));
    checkPartnerCompatible(move, result);
    result.putInValues(moveRecordSetService.setPaymentMode(move));
    result.putInValues(moveRecordSetService.setPaymentCondition(move));
    result.putInValues(moveRecordSetService.setPartnerBankDetails(move));

    return result;
  }

  protected void checkPartnerCompatible(Move move, MoveContext result) {
    try {
      moveCheckService.checkPartnerCompatible(move);
    } catch (AxelorException e) {
      result.putInValues("partner", null);
      result.putInNotify(e.getMessage());
    }
  }

  @Override
  public MoveContext onChangePartner(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveRecordSetService.setCurrencyByPartner(move));
    result.putInValues(moveRecordSetService.setPaymentMode(move));
    result.putInValues(moveRecordSetService.setPaymentCondition(move));
    result.putInValues(moveRecordSetService.setPartnerBankDetails(move));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    result.putInValues(moveRecordSetService.setCompanyBankDetails(move));

    return result;
  }

  @Override
  public MoveContext onChangeMoveLineList(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveComputeService.computeTotals(move));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));

    return result;
  }

  @Override
  public MoveContext onChangeOriginDate(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    checkDuplicateOriginMove(move, result);
    result.putInValues(moveRecordSetService.setMoveLineOriginDates(move));
    updateDummiesDateConText(move);
    dateChange = true;
    result.putInAttrs("$dateChange", "value", true);
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs("$paymentConditionChange", "value", true);

    return result;
  }

  protected void checkDuplicateOriginMove(Move move, MoveContext result) {
    try {
      moveCheckService.checkDuplicatedMoveOrigin(move);
    } catch (AxelorException e) {
      result.putInAlert(e.getMessage());
    }
  }

  @Override
  public MoveContext onChangeOrigin(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    checkOrigin(move, result);
    checkDuplicateOriginMove(move, result);
    result.putInValues(moveRecordSetService.setOriginOnMoveLineList(move));

    return result;
  }

  protected void checkOrigin(Move move, MoveContext result) {
    try {
      moveCheckService.checkOrigin(move);
    } catch (AxelorException e) {
      result.putInAlert(e.getMessage());
    }
  }

  @Override
  public MoveContext onChangePaymentCondition(
      Move move, boolean paymentConditionChange, boolean dateChange, boolean headerChange)
      throws AxelorException {

    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.merge(moveCheckService.checkTermsInPayment(move));
    if (!result.getError().isEmpty()) {
      return result;
    }
    result.putInAttrs("$paymentConditionChange", "value", true);
    paymentConditionChange = true;
    result.merge(
        moveRecordUpdateService.updateInvoiceTerms(move, paymentConditionChange, headerChange));
    result.merge(moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate()));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));

    return result;
  }
}
