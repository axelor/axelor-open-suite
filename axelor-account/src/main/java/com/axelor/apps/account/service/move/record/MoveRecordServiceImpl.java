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
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryVerificationService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
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
  protected AppAccountService appAccountService;
  protected MassEntryService massEntryService;
  protected MoveLineMassEntryToolService moveLineMassEntryToolService;

  @Inject
  public MoveRecordServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveComputeService moveComputeService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveRepository moveRepository,
      AppAccountService appAccountService,
      MassEntryService massEntryService,
      MoveLineMassEntryToolService moveLineMassEntryToolService) {
    this.moveDefaultService = moveDefaultService;
    this.moveAttrsService = moveAttrsService;
    this.periodAccountService = periodAccountService;
    this.moveCheckService = moveCheckService;
    this.moveComputeService = moveComputeService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveRepository = moveRepository;
    this.moveRecordSetService = moveRecordSetService;
    this.appAccountService = appAccountService;
    this.massEntryService = massEntryService;
    this.moveLineMassEntryToolService = moveLineMassEntryToolService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveBefore(Move move, boolean paymentConditionChange, boolean headerChange)
      throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    moveRecordUpdateService.updatePartner(move);

    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      result.putInValues("massEntryErrors", "");
      result.putInAttrs(moveAttrsService.getMassEntryBtnHiddenAttributeValues(move));
    } else if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_ON_GOING) {
      moveLineMassEntryToolService.setNewMoveStatusSelectMassEntryLines(
          move.getMoveLineMassEntryList(), MoveRepository.STATUS_NEW);
      result.putInValues("moveLineMassEntryList", move.getMoveLineMassEntryList());
    }

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
  public MoveContext onNew(Move move, User user, boolean isMassEntryMove) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveDefaultService.setDefaultMoveValues(move));
    result.putInValues(moveDefaultService.setDefaultCurrency(move));
    result.putInValues(moveRecordSetService.setJournal(move));
    if (isMassEntryMove) {
      result.putInValues("massEntryStatusSelect", MoveRepository.MASS_ENTRY_STATUS_ON_GOING);
      result.putInAttrs(moveAttrsService.getMassEntryHiddenAttributeValues(move));
      result.putInAttrs(moveAttrsService.getMassEntryRequiredAttributeValues(move));
    }

    moveRecordSetService.setPeriod(move);
    result.putInValues("period", move.getPeriod());
    if (move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      result.putInValues(moveRecordSetService.setOriginDate(move));
    }
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

    if (appAccountService.getAppAccount().getActivatePassedForPayment()) {
      result.putInAttrs(moveAttrsService.getPfpAttrs(move, user));
      result.putInValues(moveRecordSetService.setPfpStatus(move));
    }
    result.putInValues(moveRecordSetService.setCompanyBankDetails(move));
    result.putInValues(
        "companyBankDetails",
        Beans.get(MassEntryVerificationService.class)
            .verifyCompanyBankDetails(
                move.getCompany(), move.getCompanyBankDetails(), move.getJournal()));

    return result;
  }

  @Override
  public MoveContext onLoad(Move move, User user) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      result.putInAttrs(moveAttrsService.getMassEntryHiddenAttributeValues(move));
      result.putInAttrs(moveAttrsService.getMassEntryRequiredAttributeValues(move));
      result.putInAttrs(moveAttrsService.getMassEntryBtnHiddenAttributeValues(move));
    }

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

    if (appAccountService.getAppAccount().getActivatePassedForPayment()) {
      result.putInAttrs(moveAttrsService.getPfpAttrs(move, user));
    }

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
    if (move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      result.putInValues(moveRecordSetService.setOriginDate(move));
      onChangeOriginDate(move, paymentConditionChange, dateChange);
    }
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
    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      result.putInAttrs(moveAttrsService.getMassEntryHiddenAttributeValues(move));
      result.putInAttrs(moveAttrsService.getMassEntryRequiredAttributeValues(move));
      result.putInAttrs(moveAttrsService.getMassEntryBtnHiddenAttributeValues(move));
    }

    if (move.getJournal() != null) {
      result.putInValues(
          "companyBankDetails",
          Beans.get(MassEntryVerificationService.class)
              .verifyCompanyBankDetails(
                  move.getCompany(), move.getCompanyBankDetails(), move.getJournal()));
    }
    checkPartnerCompatible(move, result);
    result.putInValues(moveRecordSetService.setPaymentMode(move));
    result.putInValues(moveRecordSetService.setPaymentCondition(move));
    result.putInValues(moveRecordSetService.setPartnerBankDetails(move));

    if (move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      result.putInValues(moveRecordSetService.setOriginDate(move));
    }

    if (appAccountService.getAppAccount().getActivatePassedForPayment()
        && move.getJournal() != null) {
      result.putInValues(moveRecordSetService.setPfpStatus(move));
    }

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

    if (appAccountService.getAppAccount().getActivatePassedForPayment()
        && move.getPfpValidateStatusSelect() > MoveRepository.PFP_NONE) {
      result.putInValues(moveRecordSetService.setPfpValidatorUser(move));
    }

    return result;
  }

  @Override
  public MoveContext onChangeMoveLineList(
      Move move, LocalDate dueDate, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveComputeService.computeTotals(move));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));
    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      result.putInValues(
          massEntryService.verifyFieldsAndGenerateTaxLineAndCounterpart(move, dueDate));
      result.putInAttrs(moveAttrsService.getMassEntryBtnHiddenAttributeValues(move));
    }

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

  @Override
  public MoveContext onChangeCurrency(Move move, Context context) {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    result.putInValues(moveDefaultService.setDefaultCurrency(move));
    result.putInAttrs(moveAttrsService.getMassEntryHiddenAttributeValues(move));
    result.putInAttrs(moveAttrsService.getMassEntryRequiredAttributeValues(move));
    result.putInAttrs(moveAttrsService.getMassEntryBtnHiddenAttributeValues(move));

    return result;
  }
}
