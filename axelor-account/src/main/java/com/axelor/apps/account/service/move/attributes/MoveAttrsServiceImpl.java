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
package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MovePfpService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
  protected AnalyticToolService analyticToolService;
  protected AnalyticAttrsService analyticAttrsService;
  protected CompanyRepository companyRepository;
  protected JournalRepository journalRepository;

  @Inject
  public MoveAttrsServiceImpl(
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveViewHelperService moveViewHelperService,
      MovePfpService movePfpService,
      AnalyticToolService analyticToolService,
      AnalyticAttrsService analyticAttrsService,
      CompanyRepository companyRepository,
      JournalRepository journalRepository) {
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveViewHelperService = moveViewHelperService;
    this.movePfpService = movePfpService;
    this.analyticToolService = analyticToolService;
    this.analyticAttrsService = analyticAttrsService;
    this.companyRepository = companyRepository;
    this.journalRepository = journalRepository;
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

    this.addAttr(
        "moveLineList.vatSystemSelect",
        "hidden",
        move.getJournal() != null
            && move.getJournal().getJournalType().getTechnicalTypeSelect()
                != JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
            && move.getJournal().getJournalType().getTechnicalTypeSelect()
                != JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE,
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
  public Map<String, Map<String, Object>> addFunctionalOriginSelectDomain(Journal journal) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    String selectionValue = null;

    if (journal != null) {
      selectionValue =
          Optional.ofNullable(journal.getAuthorizedFunctionalOriginSelect()).orElse("0");
    }

    this.addAttr("functionalOriginSelect", "selection-in", selectionValue, attrsMap);
    return attrsMap;
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
        CollectionUtils.isEmpty(move.getCompany().getTradingNameList())
            ? "0"
            : move.getCompany().getTradingNameList().stream()
                .map(TradingName::getId)
                .map(Objects::toString)
                .collect(Collectors.joining(","));

    String domain = String.format("self.id IN (%s)", tradingNameIds);

    this.addAttr("tradingName", "domain", domain, attrsMap);
  }

  @Override
  public void addJournalDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    if (move == null || move.getCompany() == null) {
      return;
    }

    Query<Journal> journalQuery = journalRepository.all();
    String query = "self.company.id = :company AND self.statusSelect = :statusSelect";
    BankDetails companyBankDetails = move.getCompanyBankDetails();
    if (companyBankDetails != null && companyBankDetails.getJournal() != null) {
      query += " AND (self.id = :journal OR self.journalType.technicalTypeSelect != :journalType)";
      journalQuery.bind("journal", companyBankDetails.getJournal());
      journalQuery.bind("journalType", JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY);
    }
    List<Journal> journalList =
        journalQuery
            .filter(query)
            .bind("company", move.getCompany().getId())
            .bind("statusSelect", JournalRepository.STATUS_ACTIVE)
            .fetch();
    String journalIdList =
        journalList.stream()
            .filter(
                journal ->
                    UserRoleToolService.checkUserRolesPermissionIncludingEmpty(
                        AuthUtils.getUser(), journal.getAuthorizedRoleSet()))
            .map(Journal::getId)
            .map(Objects::toString)
            .collect(Collectors.joining(","));

    this.addAttr("journal", "domain", String.format("self.id IN (%s)", journalIdList), attrsMap);
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

  @Override
  public void addThirdPartyPayerPartnerReadonly(
      Move move, Map<String, Map<String, Object>> attrsMap) {
    boolean isReadonly =
        CollectionUtils.isNotEmpty(move.getMoveLineList())
            && move.getMoveLineList().stream()
                .map(MoveLine::getInvoiceTermList)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .allMatch(InvoiceTerm::getIsPaid);

    this.addAttr("thirdPartyPayerPartner", "readonly", isReadonly, attrsMap);
  }

  @Override
  public void addCompanyDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    String companyIds =
        companyRepository.all().filter("self.accountConfig IS NOT NULL").fetch().stream()
            .map(Company::getId)
            .map(Objects::toString)
            .collect(Collectors.joining(","));

    String domain = String.format("self.id IN (%s)", companyIds.isEmpty() ? "0" : companyIds);

    this.addAttr("company", "domain", domain, attrsMap);
  }

  @Override
  public void addCompanyBankDetailsDomain(Move move, Map<String, Map<String, Object>> attrsMap) {

    String domain = getCompanyBankDetailsDomain(move);
    this.addAttr("companyBankDetails", "domain", domain, attrsMap);
  }

  protected String getCompanyBankDetailsDomain(Move move) {
    Partner partner = move.getPartner();
    Company company = move.getCompany();

    if (!appAccountService.isApp("account")
        || !appAccountService.getAppBase().getManageMultiBanks()) {
      return getBankDetailsDomain(company);
    } else if (Boolean.TRUE.equals(appAccountService.getAppAccount().getManageFactors())
        && partner != null
        && Boolean.TRUE.equals(partner.getFactorizedCustomer())) {
      return "self.partner.isFactor = true AND self.active = true";
    } else {
      PaymentMode paymentMode = move.getPaymentMode();
      if (paymentMode == null) {
        return getBankDetailsDomain(company);
      }
      List<BankDetails> authorizedBankDetails = new ArrayList<>();
      paymentMode.getAccountManagementList().stream()
          .filter(am -> am.getCompany().equals(move.getCompany()))
          .map(AccountManagement::getBankDetails)
          .filter(Objects::nonNull)
          .forEach(authorizedBankDetails::add);

      return authorizedBankDetails.isEmpty()
          ? "self.id IN (0)"
          : getBankDetailsListDomain(authorizedBankDetails);
    }
  }

  protected String getBankDetailsDomain(Company company) {
    return company == null
        ? "self.id IN (0)"
        : getBankDetailsListDomain(company.getBankDetailsList());
  }

  protected String getBankDetailsListDomain(List<BankDetails> bankDetailsList) {
    return "self.id IN ("
        + StringHelper.getIdListString(bankDetailsList)
        + ") AND self.active = true";
  }
}
