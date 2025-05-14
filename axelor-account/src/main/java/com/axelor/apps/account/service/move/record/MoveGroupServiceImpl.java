/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryVerificationService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.account.service.period.PeriodCheckService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoveGroupServiceImpl implements MoveGroupService {

  protected MoveDefaultService moveDefaultService;
  protected MoveAttrsService moveAttrsService;
  protected PeriodCheckService periodCheckService;
  protected MoveCheckService moveCheckService;
  protected MoveCutOffService moveCutOffService;
  protected MoveRecordUpdateService moveRecordUpdateService;
  protected MoveRecordSetService moveRecordSetService;
  protected MoveToolService moveToolService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveCounterPartService moveCounterPartService;
  protected MoveLineTaxService moveLineTaxService;
  protected PeriodService periodService;
  protected MoveRepository moveRepository;
  protected MassEntryService massEntryService;
  protected MassEntryVerificationService massEntryVerificationService;
  protected MoveLineMassEntryRecordService moveLineMassEntryRecordService;
  protected PfpService pfpService;
  protected AnalyticAttrsService analyticAttrsService;
  protected MoveLineRecordService moveLineRecordService;

  @Inject
  public MoveGroupServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodCheckService periodCheckService,
      MoveCheckService moveCheckService,
      MoveCutOffService moveCutOffService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveCounterPartService moveCounterPartService,
      MoveLineTaxService moveLineTaxService,
      PeriodService periodService,
      MoveRepository moveRepository,
      MassEntryService massEntryService,
      MassEntryVerificationService massEntryVerificationService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      PfpService pfpService,
      AnalyticAttrsService analyticAttrsService,
      MoveLineRecordService moveLineRecordService) {
    this.moveDefaultService = moveDefaultService;
    this.moveAttrsService = moveAttrsService;
    this.periodCheckService = periodCheckService;
    this.moveCheckService = moveCheckService;
    this.moveCutOffService = moveCutOffService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveRecordSetService = moveRecordSetService;
    this.moveToolService = moveToolService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveCounterPartService = moveCounterPartService;
    this.moveLineTaxService = moveLineTaxService;
    this.periodService = periodService;
    this.moveRepository = moveRepository;
    this.massEntryService = massEntryService;
    this.massEntryVerificationService = massEntryVerificationService;
    this.moveLineMassEntryRecordService = moveLineMassEntryRecordService;
    this.pfpService = pfpService;
    this.analyticAttrsService = analyticAttrsService;
    this.moveLineRecordService = moveLineRecordService;
  }

  protected void addPeriodDummyFields(Move move, Map<String, Object> valuesMap)
      throws AxelorException {
    valuesMap.put("$simulatedPeriodClosed", moveToolService.isSimulatedMovePeriodClosed(move));
    valuesMap.put("$periodClosed", periodService.isClosedPeriod(move.getPeriod()));
  }

  protected void addValidatePeriod(Move move, Map<String, Object> valuesMap)
      throws AxelorException {
    valuesMap.put(
        "$validatePeriod",
        !periodCheckService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
  }

  protected void addValidateJournalRole(Move move, Map<String, Object> valuesMap) {
    if (move.getJournal() != null) {
      valuesMap.put(
          "$validateJournalRole",
          !UserRoleToolService.checkUserRolesPermissionIncludingEmpty(
              AuthUtils.getUser(), move.getJournal().getAuthorizedRoleSet()));
    }
  }

  public void checkBeforeSave(Move move) throws AxelorException {
    moveCheckService.checkDates(move);
    moveCheckService.checkPeriodPermission(move);
    moveCheckService.checkRemovedLines(move);
    moveCheckService.checkAnalyticAccount(move);
    moveCheckService.checkCurrencyAmountSum(move);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void onSave(
      Move move, boolean paymentConditionChange, boolean dateChange, boolean headerChange)
      throws AxelorException {
    moveRecordUpdateService.updatePartner(move);

    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      move.setMassEntryErrors(null);
    } else if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_ON_GOING) {
      moveLineMassEntryRecordService.setMoveStatusSelect(
          move.getMoveLineMassEntryList(), MoveRepository.STATUS_NEW);
    }

    moveRecordUpdateService.updateInvoiceTerms(
        move, paymentConditionChange || dateChange, headerChange);
    moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate());
    moveRecordUpdateService.updateThirdPartyPayerPartner(move);

    moveRepository.save(move);

    moveInvoiceTermService.roundInvoiceTermPercentages(move);
    moveRecordUpdateService.updateInDayBookMode(move);
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(Move move, boolean isMassEntry)
      throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveCheckService.checkPeriodPermission(move);
    moveDefaultService.setDefaultValues(move);
    if (move.getJournal() == null) {
      moveRecordSetService.setJournal(move);
    }
    moveRecordSetService.setPeriod(move);

    moveRecordSetService.setFunctionalOriginSelect(move);
    moveRecordSetService.setOriginDate(move);

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
    valuesMap.put("originDate", move.getOriginDate());

    if (pfpService.isManagePassedForPayment(move.getCompany())) {
      moveRecordSetService.setPfpStatus(move);
      valuesMap.put("pfpValidateStatusSelect", move.getOriginDate());
    }

    if (isMassEntry) {
      move.setMassEntryStatusSelect(MoveRepository.MASS_ENTRY_STATUS_ON_GOING);
      valuesMap.put("massEntryStatusSelect", move.getMassEntryStatusSelect());
    } else {
      this.addValidatePeriod(move, valuesMap);
      this.addPeriodDummyFields(move, valuesMap);
      this.addValidateJournalRole(move, valuesMap);
    }

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(Move move, User user)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        moveAttrsService.addFunctionalOriginSelectDomain(move.getJournal());

    moveAttrsService.addHidden(move, attrsMap);
    moveAttrsService.addMoveLineListViewerHidden(move, attrsMap);
    analyticAttrsService.addAnalyticAxisAttrs(
        move.getCompany(), move.getMassEntryStatusSelect(), attrsMap);
    moveAttrsService.addPartnerRequired(move, attrsMap);
    moveAttrsService.addMainPanelTabHiddenValue(move, attrsMap);

    if (pfpService.isManagePassedForPayment(move.getCompany())) {
      moveAttrsService.getPfpAttrs(move, user, attrsMap);
    }

    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveAttrsService.addMassEntryHidden(move, attrsMap);
      moveAttrsService.addMassEntryPaymentConditionRequired(move, attrsMap);
      moveAttrsService.addMassEntryBtnHidden(move, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOnLoadValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = moveRecordSetService.computeTotals(move);

    this.addValidatePeriod(move, valuesMap);
    valuesMap.put("$isThereRelatedCutOffMoves", moveCheckService.isRelatedCutoffMoves(move));
    valuesMap.put("$isThereRelatedFixedAsset", moveCheckService.isRelatedToFixedAsset(move));

    this.addPeriodDummyFields(move, valuesMap);
    this.addValidateJournalRole(move, valuesMap);

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(Move move, User user)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = this.getOnNewAttrsMap(move, user);

    moveAttrsService.addDueDateHidden(move, attrsMap);
    moveAttrsService.addThirdPartyPayerPartnerReadonly(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDateOnChangeValuesMap(Move move) throws AxelorException {
    if (move.getMassEntryStatusSelect() == MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveRecordSetService.setPeriod(move);
    }

    moveLineRecordService.setMoveLineDates(move);
    moveRecordUpdateService.updateMoveLinesCurrencyRate(move);
    moveRecordSetService.setOriginDate(move);

    Map<String, Object> valuesMap = moveRecordSetService.computeTotals(move);

    moveRecordUpdateService.updateDueDate(move, true, true);
    moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate());

    this.addPeriodDummyFields(move, valuesMap);

    valuesMap.put("period", move.getPeriod());
    valuesMap.put("dueDate", move.getDueDate());
    valuesMap.put(
        "moveLineList",
        move.getMoveLineList() != null
            ? move.getMoveLineList().stream().map(Mapper::toMap).collect(Collectors.toList())
            : new ArrayList<>());
    valuesMap.put("originDate", move.getOriginDate());

    if (move.getMassEntryStatusSelect() == MoveRepository.MASS_ENTRY_STATUS_NULL) {
      this.addValidatePeriod(move, valuesMap);
    }
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
  public Map<String, Object> getJournalOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveRecordSetService.setFunctionalOriginSelect(move);
    moveRecordSetService.setPaymentMode(move);
    moveRecordSetService.setPaymentCondition(move);
    moveRecordSetService.setPartnerBankDetails(move);
    moveRecordSetService.setOriginDate(move);
    moveRecordSetService.setThirdPartyPayerPartner(move);
    this.addValidateJournalRole(move, valuesMap);

    if (move.getJournal() != null
        && move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      massEntryVerificationService.verifyCompanyBankDetails(
          move, move.getCompany(), move.getCompanyBankDetails(), move.getJournal());
      valuesMap.put("companyBankDetails", move.getCompanyBankDetails());
    }

    if (pfpService.isManagePassedForPayment(move.getCompany())) {
      moveRecordSetService.setPfpStatus(move);
      valuesMap.put("pfpValidateStatusSelect", move.getPfpValidateStatusSelect());
    }

    valuesMap.put("functionalOriginSelect", move.getFunctionalOriginSelect());
    valuesMap.put("paymentMode", move.getPaymentMode());
    valuesMap.put("paymentCondition", move.getPaymentCondition());
    valuesMap.put("partnerBankDetails", move.getPartnerBankDetails());
    valuesMap.put("originDate", move.getOriginDate());
    valuesMap.put("thirdPartyPayerPartner", move.getThirdPartyPayerPartner());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getJournalOnChangeAttrsMap(Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        moveAttrsService.addFunctionalOriginSelectDomain(move.getJournal());

    moveAttrsService.addHidden(move, attrsMap);
    moveAttrsService.addPartnerRequired(move, attrsMap);
    moveAttrsService.addMainPanelTabHiddenValue(move, attrsMap);
    analyticAttrsService.addAnalyticAxisAttrs(
        move.getCompany(), move.getMassEntryStatusSelect(), attrsMap);

    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveAttrsService.addMassEntryHidden(move, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getPartnerOnChangeValuesMap(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveRecordSetService.setCurrencyByPartner(move);
    moveRecordSetService.setPaymentMode(move);
    moveRecordSetService.setCompanyBankDetails(move);
    moveRecordSetService.setPaymentCondition(move);
    moveRecordSetService.setPartnerBankDetails(move);
    moveRecordSetService.setThirdPartyPayerPartner(move);
    moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange);

    if (pfpService.isManagePassedForPayment(move.getCompany())
        && move.getPfpValidateStatusSelect() > MoveRepository.PFP_NONE) {
      moveRecordSetService.setPfpValidatorUser(move);
      valuesMap.put("pfpValidatorUser", move.getPfpValidatorUser());
    }

    valuesMap.put("currency", move.getCurrency());
    valuesMap.put("currencyCode", move.getCurrencyCode());
    valuesMap.put("fiscalPosition", move.getFiscalPosition());
    valuesMap.put("paymentMode", move.getPaymentMode());
    valuesMap.put("paymentCondition", move.getPaymentCondition());
    valuesMap.put("partnerBankDetails", move.getPartnerBankDetails());
    valuesMap.put("dueDate", move.getDueDate());
    valuesMap.put("companyBankDetails", move.getCompanyBankDetails());
    valuesMap.put("thirdPartyPayerPartner", move.getThirdPartyPayerPartner());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addHidden(move, attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, paymentConditionChange, attrsMap);
    moveAttrsService.addMainPanelTabHiddenValue(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getMoveLineListOnChangeValuesMap(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException {
    Map<String, Object> valuesMap = moveRecordSetService.computeTotals(move);

    moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange);
    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      massEntryService.verifyFieldsAndGenerateTaxLineAndCounterpart(move, move.getDate());
      valuesMap.put("massEntryErrors", move.getMassEntryErrors());
      valuesMap.put("moveLineMassEntryList", move.getMoveLineMassEntryList());
    }

    if (List.of(
                MoveRepository.STATUS_DAYBOOK,
                MoveRepository.STATUS_ACCOUNTED,
                MoveRepository.STATUS_SIMULATED)
            .contains(move.getStatusSelect())
        && pfpService.isManagePassedForPayment(move.getCompany())) {
      Integer pfpStatus = moveInvoiceTermService.checkOtherInvoiceTerms(move);
      if (pfpStatus != null) {
        valuesMap.put("pfpValidateStatusSelect", move.getPfpValidateStatusSelect());
      }
    }

    valuesMap.put("dueDate", move.getDueDate());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getMoveLineListOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    analyticAttrsService.addAnalyticAxisAttrs(
        move.getCompany(), move.getMassEntryStatusSelect(), attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, paymentConditionChange, attrsMap);
    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveAttrsService.addMassEntryBtnHidden(move, attrsMap);
    }
    if (move.getStatusSelect() != MoveRepository.STATUS_NEW) {
      moveAttrsService.getPfpAttrs(move, AuthUtils.getUser(), attrsMap);
    }
    moveAttrsService.addThirdPartyPayerPartnerReadonly(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOriginDateOnChangeValuesMap(
      Move move, boolean paymentConditionChange) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveLineRecordService.setMoveLineOriginDates(move);
    move.setDueDate(null);
    moveRecordUpdateService.updateDueDate(move, paymentConditionChange, true);

    valuesMap.put("moveLineList", move.getMoveLineList());
    valuesMap.put("dueDate", move.getDueDate());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOriginDateOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addDateChangeTrueValue(attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, paymentConditionChange, attrsMap);
    moveAttrsService.addPaymentConditionChangeChangeValue(true, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOriginOnChangeValuesMap(Move move) {
    Map<String, Object> valuesMap = new HashMap<>();

    moveToolService.setOriginOnMoveLineList(move);

    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getPaymentConditionOnChangeValuesMap(
      Move move, boolean dateChange, boolean headerChange) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    try {
      moveCheckService.checkTermsInPayment(move);
    } catch (AxelorException e) {
      valuesMap.put("paymentCondition", move.getPaymentCondition());
      valuesMap.put("info", e.getLocalizedMessage());

      return valuesMap;
    }

    valuesMap.put("flash", moveRecordUpdateService.updateInvoiceTerms(move, true, headerChange));
    moveRecordUpdateService.resetDueDate(move);
    moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate());
    moveRecordUpdateService.updateDueDate(move, true, dateChange);

    valuesMap.put("dueDate", move.getDueDate());
    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPaymentConditionOnChangeAttrsMap(Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addPaymentConditionChangeChangeValue(false, attrsMap);
    moveAttrsService.addHeaderChangeValue(false, attrsMap);
    moveAttrsService.addDateChangeFalseValue(move, true, attrsMap);
    moveAttrsService.addDueDateHidden(move, attrsMap);
    moveAttrsService.getPfpAttrs(move, AuthUtils.getUser(), attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDescriptionOnChangeValuesMap(Move move) {
    Map<String, Object> valuesMap = new HashMap<>();

    moveToolService.setDescriptionOnMoveLineList(move);

    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getCompanyOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = this.getDateOnChangeValuesMap(move);

    moveRecordSetService.setJournal(move);
    moveRecordSetService.setCompanyBankDetails(move);
    moveDefaultService.setDefaultCurrency(move);
    moveRecordSetService.setPeriod(move);

    if (move.getJournal() != null
        && move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      massEntryVerificationService.verifyCompanyBankDetails(
          move, move.getCompany(), move.getCompanyBankDetails(), move.getJournal());
    }

    valuesMap.put("journal", move.getJournal());
    valuesMap.put("companyBankDetails", move.getCompanyBankDetails());
    valuesMap.put("currency", move.getCurrency());
    valuesMap.put("companyCurrency", move.getCompanyCurrency());
    valuesMap.put("currencyCode", move.getCurrencyCode());
    valuesMap.put("companyCurrencyCode", move.getCompanyCurrencyCode());
    valuesMap.put("period", move.getPeriod());

    if (move.getPartner() != null
        && move.getCompany() != null
        && move.getPartner().getCompanySet() != null
        && !move.getPartner().getCompanySet().contains(move.getCompany())) {
      valuesMap.put("partner", null);
      valuesMap.put("currency", move.getCompany().getCurrency());
      valuesMap.put("companyBankDetails", null);
    }

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getCompanyOnChangeAttrsMap(Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    analyticAttrsService.addAnalyticAxisAttrs(
        move.getCompany(), move.getMassEntryStatusSelect(), attrsMap);
    moveAttrsService.addPartnerRequired(move, attrsMap);
    moveAttrsService.addMainPanelTabHiddenValue(move, attrsMap);

    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveAttrsService.addMassEntryBtnHidden(move, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getPaymentModeOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveRecordSetService.setCompanyBankDetails(move);

    valuesMap.put("companyBankDetails", move.getCompanyBankDetails());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getHeaderChangeAttrsMap() {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addHeaderChangeValue(true, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getCurrencyOnChangeValuesMap(Move move) {
    Map<String, Object> valuesMap = new HashMap<>();

    moveDefaultService.setDefaultCurrencyOnChange(move);

    valuesMap.put("currency", move.getCurrency());
    valuesMap.put("companyCurrency", move.getCompanyCurrency());
    valuesMap.put("currencyCode", move.getCurrencyCode());
    valuesMap.put("companyCurrencyCode", move.getCompanyCurrencyCode());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getFiscalPositionOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveDefaultService.setDefaultFiscalPositionOnChange(move);

    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getCurrencyOnChangeAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveAttrsService.addMassEntryHidden(move, attrsMap);
      moveAttrsService.addMassEntryPaymentConditionRequired(move, attrsMap);
      moveAttrsService.addMassEntryBtnHidden(move, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDateOfReversionSelectOnChangeValuesMap(
      LocalDate moveDate, int dateOfReversionSelect) {
    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put(
        "dateOfReversion",
        moveRecordUpdateService.getDateOfReversion(moveDate, dateOfReversionSelect));

    return valuesMap;
  }

  @Override
  public Map<String, Object> getGenerateCounterpartOnClickValuesMap(Move move, LocalDate dueDate)
      throws AxelorException {
    moveToolService.exceptionOnGenerateCounterpart(move);
    moveLineTaxService.autoTaxLineGenerateNoSave(move);
    moveCounterPartService.generateCounterpartMoveLine(move, dueDate);

    Map<String, Object> valuesMap = moveRecordSetService.computeTotals(move);

    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getGenerateTaxLinesOnClickValuesMap(Move move) throws AxelorException {
    moveLineTaxService.autoTaxLineGenerateNoSave(move);

    Map<String, Object> valuesMap = moveRecordSetService.computeTotals(move);

    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getApplyCutOffDatesOnClickValuesMap(
      Move move, LocalDate cutOffStartDate, LocalDate cutOffEndDate) throws AxelorException {
    moveCheckService.checkManageCutOffDates(move);
    moveCutOffService.applyCutOffDates(move, cutOffStartDate, cutOffEndDate);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("moveLineList", move.getMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addPartnerDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPaymentModeOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addPaymentModeDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerBankDetailsOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addPartnerBankDetailsDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getTradingNameOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addTradingNameDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getJournalOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addJournalDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getThirdPartyPayerPartnerOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addThirdPartyPayerPartnerReadonly(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getWizardDefaultAttrsMap(LocalDate moveDate) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addWizardDefault(moveDate, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getMassEntryAttrsMap(Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    analyticAttrsService.addAnalyticAxisAttrs(
        move.getCompany(), move.getMassEntryStatusSelect(), attrsMap);
    moveAttrsService.addMassEntryHidden(move, attrsMap);
    moveAttrsService.addMassEntryPaymentConditionRequired(move, attrsMap);
    moveAttrsService.addMassEntryBtnHidden(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getCompanyOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addCompanyDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getCompanyBankDetailsOnSelectAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addCompanyBankDetailsDomain(move, attrsMap);

    return attrsMap;
  }
}
