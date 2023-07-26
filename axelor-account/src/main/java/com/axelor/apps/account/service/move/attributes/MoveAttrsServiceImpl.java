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
package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MovePfpService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveAttrsServiceImpl implements MoveAttrsService {

  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveViewHelperService moveViewHelperService;
  protected MovePfpService movePfpService;

  @Inject
  public MoveAttrsServiceImpl(
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveViewHelperService moveViewHelperService,
      MovePfpService movePfpService) {
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveViewHelperService = moveViewHelperService;
    this.movePfpService = movePfpService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("moveLineList.partner", "hidden", move.getPartner() != null, attrsMap);

    this.addAttr(
        "moveLineList.counter",
        "hidden",
        move.getStatusSelect() == null || move.getStatusSelect() == MoveRepository.STATUS_NEW,
        attrsMap);

    this.addAttr(
        "moveLineList.amountRemaining",
        "hidden",
        move.getStatusSelect() == null
            || move.getStatusSelect() == MoveRepository.STATUS_NEW
            || move.getStatusSelect() == MoveRepository.STATUS_CANCELED
            || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED,
        attrsMap);

    this.addAttr(
        "moveLineList.reconcileGroup",
        "hidden",
        move.getStatusSelect() == MoveRepository.STATUS_NEW
            || move.getStatusSelect() == MoveRepository.STATUS_CANCELED,
        attrsMap);
  }

  @Override
  public void addMoveLineListViewerHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    boolean isNotHidden =
        move.getMoveLineList() != null
            && move.getStatusSelect() < MoveRepository.STATUS_ACCOUNTED
            && move.getMoveLineList().stream()
                .anyMatch(it -> it.getAmountPaid().signum() > 0 || it.getReconcileGroup() != null);

    this.addAttr("$reconcileTags", "hidden", !isNotHidden, attrsMap);
  }

  @Override
  public void addFunctionalOriginSelectDomain(
      Move move, Map<String, Map<String, Object>> attrsMap) {
    String selectionValue = null;

    if (move.getJournal() != null) {
      selectionValue =
          Optional.ofNullable(move.getJournal().getAuthorizedFunctionalOriginSelect()).orElse("0");
    }

    this.addAttr("functionalOriginSelect", "selection-in", selectionValue, attrsMap);
  }

  @Override
  public void addMoveLineAnalyticAttrs(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    String fieldNameToSet = "moveLineList";
    if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL) {
      fieldNameToSet = "moveLineMassEntryList";
    }

    if (move.getCompany() != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());

      if (accountConfig != null
          && appAccountService.getAppAccount().getManageAnalyticAccounting()
          && accountConfig.getManageAnalyticAccounting()) {
        AnalyticAxis analyticAxis = null;

        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = fieldNameToSet + ".axis" + i + "AnalyticAccount";
          this.addAttr(
              analyticAxisKey,
              "hidden",
              !(i <= accountConfig.getNbrOfAnalyticAxisSelect()),
              attrsMap);

          for (AnalyticAxisByCompany analyticAxisByCompany :
              accountConfig.getAnalyticAxisByCompanyList()) {
            if (analyticAxisByCompany.getSequence() + 1 == i) {
              analyticAxis = analyticAxisByCompany.getAnalyticAxis();
            }
          }

          if (analyticAxis != null) {
            this.addAttr(analyticAxisKey, "title", analyticAxis.getName(), attrsMap);
            analyticAxis = null;
          }
        }
      } else {
        this.addAttr(fieldNameToSet + ".analyticDistributionTemplate", "hidden", true, attrsMap);
        this.addAttr(fieldNameToSet + ".analyticMoveLineList", "hidden", true, attrsMap);

        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = fieldNameToSet + ".axis" + i + "AnalyticAccount";
          this.addAttr(analyticAxisKey, "hidden", true, attrsMap);
        }
      }
    }
  }

  @Override
  public void addPartnerDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    if (move == null) {
      return;
    }

    String domain = moveViewHelperService.filterPartner(move.getCompany(), move.getJournal());

    this.addAttr("partner", "domain", domain, attrsMap);
  }

  @Override
  public void addPaymentModeDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    if (move == null || move.getCompany() == null) {
      return;
    }

    String domain =
        String.format(
            "self.id IN (SELECT am.paymentMode FROM AccountManagement am WHERE am.company.id = %d)",
            move.getCompany().getId());

    this.addAttr("paymentMode", "domain", domain, attrsMap);
  }

  @Override
  public void addPartnerBankDetailsDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    Partner partner = move.getPartner();
    String domain;

    if (partner == null || CollectionUtils.isEmpty(partner.getBankDetailsList())) {
      domain = "self = NULL";
    } else {
      String bankDetailsIds =
          partner.getBankDetailsList().stream()
              .map(BankDetails::getId)
              .map(Object::toString)
              .collect(Collectors.joining(","));

      domain = String.format("self.id IN (%s) AND self.active IS TRUE", bankDetailsIds);
    }

    this.addAttr("partnerBankDetails", "domain", domain, attrsMap);
  }

  @Override
  public void addTradingNameDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    if (move == null || move.getCompany() == null) {
      return;
    }

    String tradingNameIds =
        CollectionUtils.isEmpty(move.getCompany().getTradingNameSet())
            ? "0"
            : move.getCompany().getTradingNameSet().stream()
                .map(TradingName::getId)
                .map(Objects::toString)
                .collect(Collectors.joining(","));

    String domain = String.format("self.id IN (%s)", tradingNameIds);

    this.addAttr("tradingName", "domain", domain, attrsMap);
  }

  @Override
  public void addWizardDefault(LocalDate moveDate, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("isAutomaticReconcile", "value", true, attrsMap);
    this.addAttr("isAutomaticAccounting", "value", true, attrsMap);
    this.addAttr("isUnreconcileOriginalMove", "value", true, attrsMap);
    this.addAttr("isHiddenMoveLinesInBankReconciliation", "value", true, attrsMap);
    this.addAttr("dateOfReversion", "value", moveDate, attrsMap);
    this.addAttr(
        "dateOfReversionSelect",
        "value",
        MoveRepository.DATE_OF_REVERSION_ORIGINAL_MOVE_DATE,
        attrsMap);
  }

  @Override
  public void addDueDateHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("dueDate", "hidden", !moveInvoiceTermService.displayDueDate(move), attrsMap);
  }

  @Override
  public void addDateChangeTrueValue(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$dateChange", "value", true, attrsMap);
  }

  @Override
  public void addDateChangeFalseValue(
      Move move, boolean paymentConditionChange, Map<String, Map<String, Object>> attrsMap) {
    if (moveInvoiceTermService.displayDueDate(move)
        && (move.getDueDate() == null || paymentConditionChange)) {
      this.addAttr("$dateChange", "value", false, attrsMap);
    }
  }

  @Override
  public void addPaymentConditionChangeChangeValue(
      boolean value, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$paymentConditionChange", "value", value, attrsMap);
  }

  @Override
  public void addHeaderChangeValue(boolean value, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$headerChange", "value", value, attrsMap);
  }

  @Override
  public void getPfpAttrs(Move move, User user, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    Objects.requireNonNull(move);

    this.addAttr(
        "passedForPaymentValidationBtn",
        "hidden",
        !movePfpService.isPfpButtonVisible(move, user, true),
        attrsMap);
    this.addAttr(
        "refusalToPayBtn",
        "hidden",
        !movePfpService.isPfpButtonVisible(move, user, false),
        attrsMap);
    this.addAttr(
        "pfpValidatorUser", "hidden", !movePfpService.isValidatorUserVisible(move), attrsMap);
  }

  @Override
  public void addMassEntryHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    Objects.requireNonNull(move);

    if (move.getJournal() != null) {
      boolean technicalTypeSelectIsNotNull =
          move.getJournal().getJournalType() != null
              && move.getJournal().getJournalType().getTechnicalTypeSelect() != null;
      boolean isSameCurrency =
          move.getCompany() != null && move.getCompany().getCurrency() == move.getCurrency();

      this.addAttr(
          "moveLineMassEntryList.originDate",
          "hidden",
          technicalTypeSelectIsNotNull
              && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                  || move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER),
          attrsMap);
      this.addAttr(
          "moveLineMassEntryList.origin",
          "hidden",
          technicalTypeSelectIsNotNull
              && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                  || move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER),
          attrsMap);
      this.addAttr(
          "moveLineMassEntryList.movePaymentMode",
          "hidden",
          technicalTypeSelectIsNotNull
              && move.getJournal().getJournalType().getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER,
          attrsMap);
      this.addAttr("moveLineMassEntryList.currencyRate", "hidden", isSameCurrency, attrsMap);
      this.addAttr("moveLineMassEntryList.currencyAmount", "hidden", isSameCurrency, attrsMap);
      this.addAttr(
          "moveLineMassEntryList.movePfpValidatorUser",
          "hidden",
          technicalTypeSelectIsNotNull
              && move.getJournal().getJournalType().getTechnicalTypeSelect()
                  != JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
          attrsMap);
      this.addAttr(
          "moveLineMassEntryList.cutOffStartDate",
          "hidden",
          !move.getMassEntryManageCutOff(),
          attrsMap);
      this.addAttr(
          "moveLineMassEntryList.cutOffEndDate",
          "hidden",
          !move.getMassEntryManageCutOff(),
          attrsMap);
      this.addAttr(
          "moveLineMassEntryList.deliveryDate",
          "hidden",
          !move.getMassEntryManageCutOff()
              && technicalTypeSelectIsNotNull
              && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                  || move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER),
          attrsMap);
    }
  }

  @Override
  public void addMassEntryPaymentConditionRequired(
      Move move, Map<String, Map<String, Object>> attrsMap) {
    Objects.requireNonNull(move);

    this.addAttr(
        "moveLineMassEntryList.movePaymentCondition",
        "required",
        move.getJournal() != null
            && move.getJournal().getJournalType() != null
            && move.getJournal().getJournalType().getTechnicalTypeSelect() != null
            && move.getJournal().getJournalType().getTechnicalTypeSelect()
                < JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER,
        attrsMap);
  }

  @Override
  public void addMassEntryBtnHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    Objects.requireNonNull(move);

    this.addAttr(
        "controlMassEntryMoves",
        "hidden",
        move.getMassEntryStatusSelect() == MoveRepository.MASS_ENTRY_STATUS_VALIDATED,
        attrsMap);
    this.addAttr("validateMassEntryMoves", "hidden", true, attrsMap);
  }

  @Override
  public void addPartnerRequired(Move move, Map<String, Map<String, Object>> attrsMap) {
    Objects.requireNonNull(move);
    this.addAttr("partner", "required", isPartnerRequired(move.getJournal()), attrsMap);
  }

  @Override
  public void addMainPanelTabHiddenValue(Move move, Map<String, Map<String, Object>> attrsMap) {
    Objects.requireNonNull(move);

    this.addAttr(
        "$mainPanelTabHidden",
        "value",
        move.getJournal() == null
            || (isPartnerRequired(move.getJournal()) && move.getPartner() == null),
        attrsMap);
  }

  protected boolean isPartnerRequired(Journal journal) {
    return journal != null
        && journal.getJournalType() != null
        && (journal.getJournalType().getTechnicalTypeSelect()
                == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
            || journal.getJournalType().getTechnicalTypeSelect()
                == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE);
  }
}
