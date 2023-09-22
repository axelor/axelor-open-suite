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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MoveLineAttrsServiceImpl implements MoveLineAttrsService {
  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  protected AccountConfigService accountConfigService;
  protected MoveLineControlService moveLineControlService;
  protected AnalyticLineService analyticLineService;
  protected PeriodServiceAccount periodServiceAccount;
  protected JournalService journalService;

  @Inject
  public MoveLineAttrsServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineControlService moveLineControlService,
      AnalyticLineService analyticLineService,
      PeriodServiceAccount periodServiceAccount,
      JournalService journalService) {
    this.accountConfigService = accountConfigService;
    this.moveLineControlService = moveLineControlService;
    this.analyticLineService = analyticLineService;
    this.periodServiceAccount = periodServiceAccount;
    this.journalService = journalService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticAccountRequired(
      MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    for (int i = startAxisPosition; i <= endAxisPosition; i++) {
      this.addAttr(
          "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
          "required",
          analyticLineService.isAxisRequired(moveLine, move != null ? move.getCompany() : null, i),
          attrsMap);
    }
  }

  @Override
  public void addDescriptionRequired(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    this.addAttr(
        "$isDescriptionRequired",
        "value",
        accountConfigService.getAccountConfig(move.getCompany()).getIsDescriptionRequired(),
        attrsMap);
  }

  @Override
  public void addAnalyticDistributionTypeSelect(
      Move move, Map<String, Map<String, Object>> attrsMap) throws AxelorException {
    this.addAttr(
        "$analyticDistributionTypeSelect",
        "value",
        accountConfigService
            .getAccountConfig(move.getCompany())
            .getAnalyticDistributionTypeSelect(),
        attrsMap);
  }

  @Override
  public void addInvoiceTermListPercentageWarningText(
      MoveLine moveLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "$invoiceTermListPercentageWarningText",
        "value",
        !moveLineControlService.displayInvoiceTermWarningMessage(moveLine),
        attrsMap);
  }

  @Override
  public void addReadonly(MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap) {
    boolean statusCondition =
        move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
            || move.getStatusSelect() == MoveRepository.STATUS_CANCELED;
    boolean singleStatusCondition = move.getStatusSelect() == MoveRepository.STATUS_CANCELED;

    this.addAttr("informationsPanel", "readonly", statusCondition, attrsMap);
    this.addAttr("analyticDistributionPanel", "readonly", statusCondition, attrsMap);
    this.addAttr("irrecoverableDetailsPanel", "readonly", singleStatusCondition, attrsMap);
    this.addAttr("currency", "readonly", singleStatusCondition, attrsMap);
    this.addAttr("otherPanel", "readonly", singleStatusCondition, attrsMap);
    this.addAttr(
        "partner",
        "readonly",
        moveLine.getAmountPaid().signum() > 0 || move.getPartner() != null,
        attrsMap);

    if (move.getPaymentCondition() != null) {
      this.addAttr("dueDate", "readonly", !move.getPaymentCondition().getIsFree(), attrsMap);
    }
  }

  @Override
  public void addShowTaxAmount(MoveLine moveLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "taxAmount",
        "hidden",
        !moveLine.getAccount().getReconcileOk() || !moveLine.getAccount().getUseForPartnerBalance(),
        attrsMap);
  }

  @Override
  public void addShowAnalyticDistributionPanel(
      Move move, MoveLine moveLine, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    boolean condition =
        accountConfigService.getAccountConfig(move.getCompany()).getManageAnalyticAccounting()
            && moveLine.getAccount().getAnalyticDistributionAuthorized();

    this.addAttr("analyticDistributionPanel", "hidden", !condition, attrsMap);
  }

  @Override
  public void addValidatePeriod(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    this.addAttr(
        "$validatePeriod",
        "value",
        !periodServiceAccount.isAuthorizedToAccountOnPeriod(move.getPeriod(), AuthUtils.getUser()),
        attrsMap);
  }

  @Override
  public void addPartnerReadonly(
      MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap) {
    boolean readonly =
        moveLine.getAmountPaid().signum() != 0
            || (moveLine.getAccount() != null
                && move.getPartner() != null
                && moveLine.getAccount().getUseForPartnerBalance()
                && move.getMassEntryStatusSelect() == MoveRepository.MASS_ENTRY_STATUS_NULL);

    this.addAttr("partner", "readonly", readonly, attrsMap);
  }

  @Override
  public void addAccountDomain(
      Journal journal, Company company, Map<String, Map<String, Object>> attrsMap) {
    String validAccountTypes =
        journal.getValidAccountTypeSet().stream()
            .map(AccountType::getId)
            .map(Objects::toString)
            .collect(Collectors.joining(","));

    if (StringUtils.isEmpty(validAccountTypes)) {
      validAccountTypes = "0";
    }

    String validAccounts =
        journal.getValidAccountSet().stream()
            .map(Account::getId)
            .map(Objects::toString)
            .collect(Collectors.joining(","));

    if (StringUtils.isEmpty(validAccounts)) {
      validAccounts = "0";
    }

    String domain =
        String.format(
            "self.statusSelect = %s AND self.company.id = %d AND (self.accountType.id IN (%s) OR self.id IN (%s))",
            AccountRepository.STATUS_ACTIVE, company.getId(), validAccountTypes, validAccounts);

    this.addAttr("account", "domain", domain, attrsMap);
  }

  @Override
  public void addPartnerDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    if (move == null) {
      return;
    }

    String domain =
        String.format(
            "self.isContact IS FALSE AND %d MEMBER OF self.companySet", move.getCompany().getId());

    this.addAttr("partner", "domain", domain, attrsMap);
  }

  @Override
  public void addAnalyticDistributionTemplateDomain(
      Move move, Map<String, Map<String, Object>> attrsMap) {
    if (move == null) {
      return;
    }

    String domain =
        String.format(
            "self.isSpecific IS FALSE AND self.company.id = %d", move.getCompany().getId());

    this.addAttr("analyticDistributionTemplate", "domain", domain, attrsMap);
  }

  @Override
  public void changeFocus(Move move, MoveLine moveLine, Map<String, Map<String, Object>> attrsMap) {
    Account account = moveLine.getAccount();
    Company company = move.getCompany();

    if (account != null) {
      if (account.getCommonPosition() == AccountRepository.COMMON_POSITION_CREDIT) {
        this.addAttr("credit", "focus", true, attrsMap);
      }

      if (account.getCommonPosition() == AccountRepository.COMMON_POSITION_DEBIT) {
        this.addAttr("debit", "focus", true, attrsMap);
      }
    }

    if (company.getCurrency() != move.getCurrency()) {
      this.addAttr("currencyAmount", "focus", true, attrsMap);
    }
  }

  @Override
  public void addSubrogationPartnerHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "invoiceTermList.subrogationPartner",
        "hidden",
        !journalService.isSubrogationOk(move.getJournal()),
        attrsMap);
  }
}
