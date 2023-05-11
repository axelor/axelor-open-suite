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
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MoveGroupServiceImpl implements MoveGroupService {

  protected MoveDefaultService moveDefaultService;
  protected MoveAttrsService moveAttrsService;
  protected PeriodServiceAccount periodAccountService;
  protected MoveCheckService moveCheckService;
  protected MoveComputeService moveComputeService;
  protected MoveRecordUpdateService moveRecordUpdateService;
  protected MoveRecordSetService moveRecordSetService;
  protected MoveToolService moveToolService;
  protected MoveLineControlService moveLineControlService;
  protected PeriodService periodService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveGroupServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveComputeService moveComputeService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveToolService moveToolService,
      MoveLineControlService moveLineControlService,
      PeriodService periodService,
      MoveRepository moveRepository) {
    this.moveDefaultService = moveDefaultService;
    this.moveAttrsService = moveAttrsService;
    this.periodAccountService = periodAccountService;
    this.moveCheckService = moveCheckService;
    this.moveComputeService = moveComputeService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveRecordSetService = moveRecordSetService;
    this.moveToolService = moveToolService;
    this.moveLineControlService = moveLineControlService;
    this.periodService = periodService;
    this.moveRepository = moveRepository;
  }

  protected void addPeriodDummyFields(Move move, Map<String, Object> valuesMap)
      throws AxelorException {
    valuesMap.put("$simulatedPeriodClosed", moveToolService.isSimulatedMovePeriodClosed(move));
    valuesMap.put("$periodClosed", periodService.isClosedPeriod(move.getPeriod()));
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
  public Map<String, Object> getOnNewValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveCheckService.checkPeriodPermission(move);
    moveDefaultService.setDefaultValues(move);
    moveRecordSetService.setJournal(move);
    moveRecordSetService.setPeriod(move);
    moveRecordSetService.setFunctionalOriginSelect(move);

    valuesMap.put("company", move.getCompany());
    valuesMap.put("date", move.getDate());
    valuesMap.put("currency", move.getCurrency());
    valuesMap.put("companyCurrency", move.getCompanyCurrency());
    valuesMap.put("currencyCode", move.getCurrencyCode());
    valuesMap.put("companyCurrencyCode", move.getCompanyCurrencyCode());
    valuesMap.put("technicalOriginSelect", move.getTechnicalOriginSelect());
    valuesMap.put("functionalOriginSelect", move.getFunctionalOriginSelect());
    valuesMap.put("tradingName", move.getTradingName());
    valuesMap.put("journal", move.getJournal());
    valuesMap.put("period", move.getPeriod());

    valuesMap.put(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));

    this.addPeriodDummyFields(move, valuesMap);

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addHidden(move, attrsMap);
    moveAttrsService.addMoveLineListViewerHidden(move, attrsMap);
    moveAttrsService.addFunctionalOriginSelectDomain(move, attrsMap);
    moveAttrsService.addMoveLineAnalyticAttrs(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOnLoadValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = moveComputeService.computeTotals(move);

    valuesMap.put(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    valuesMap.put("$isThereRelatedCutOffMoves", moveCheckService.isRelatedCutoffMoves(move));

    this.addPeriodDummyFields(move, valuesMap);

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = this.getOnNewAttrsMap(move);

    moveAttrsService.addDueDateHidden(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDateOnChangeValuesMap(Move move, boolean paymentConditionChange)
      throws AxelorException {
    moveCheckService.checkPeriodPermission(move);
    moveRecordSetService.setPeriod(move);
    moveLineControlService.setMoveLineDates(move);
    moveRecordUpdateService.updateMoveLinesCurrencyRate(move);

    Map<String, Object> valuesMap = moveComputeService.computeTotals(move);

    moveRecordUpdateService.updateDueDate(move, paymentConditionChange, true);

    valuesMap.put("period", move.getPeriod());
    valuesMap.put("dueDate", move.getDueDate());
    valuesMap.put("moveLineList", move.getMoveLineList());

    valuesMap.put(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getDateOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addDueDateHidden(move, attrsMap);
    moveAttrsService.addDateChangeTrueValue(attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, paymentConditionChange, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getJournalOnChangeValuesMap(Move move) {
    Map<String, Object> valuesMap = new HashMap<>();

    moveRecordSetService.setFunctionalOriginSelect(move);
    moveRecordSetService.setPaymentMode(move);
    moveRecordSetService.setPaymentCondition(move);
    moveRecordSetService.setPartnerBankDetails(move);

    valuesMap.put("functionalOriginSelect", move.getFunctionalOriginSelect());
    valuesMap.put("paymentMode", move.getPaymentMode());
    valuesMap.put("paymentCondition", move.getPaymentCondition());
    valuesMap.put("partnerBankDetails", move.getPartnerBankDetails());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getJournalOnChangeAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addFunctionalOriginSelectDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getPartnerOnChangeValuesMap(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveRecordSetService.setCurrencyByPartner(move);
    moveRecordSetService.setPaymentMode(move);
    moveRecordSetService.setPaymentCondition(move);
    moveRecordSetService.setPartnerBankDetails(move);
    moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange);

    valuesMap.put("currency", move.getCurrency());
    valuesMap.put("currencyCode", move.getCurrencyCode());
    valuesMap.put("fiscalPosition", move.getFiscalPosition());
    valuesMap.put("paymentMode", move.getPaymentMode());
    valuesMap.put("paymentCondition", move.getPaymentCondition());
    valuesMap.put("partnerBankDetails", move.getPartnerBankDetails());
    valuesMap.put("dueDate", move.getDueDate());
    valuesMap.put("companyBankDetails", move.getCompanyBankDetails());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addHidden(move, attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, paymentConditionChange, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getMoveLineListOnChangeValuesMap(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException {
    Map<String, Object> valuesMap = moveComputeService.computeTotals(move);

    moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange);

    valuesMap.put("dueDate", move.getDueDate());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getMoveLineListOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addMoveLineAnalyticAttrs(move, attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, paymentConditionChange, attrsMap);

    return attrsMap;
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

  protected void updateDummiesDateConText(Move move) {
    move.setDueDate(null);
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
