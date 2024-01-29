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
package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveAttrsServiceImpl implements MoveAttrsService {

  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveViewHelperService moveViewHelperService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveAttrsServiceImpl(
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveViewHelperService moveViewHelperService,
      MoveRepository moveRepository) {
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveViewHelperService = moveViewHelperService;
    this.moveRepository = moveRepository;
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
    if (move.getCompany() != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());

      if (accountConfig != null
          && appAccountService.getAppAccount().getManageAnalyticAccounting()
          && accountConfig.getManageAnalyticAccounting()) {
        AnalyticAxis analyticAxis = null;

        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = "moveLineList.axis" + i + "AnalyticAccount";
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
        this.addAttr("moveLineList.analyticDistributionTemplate", "hidden", true, attrsMap);
        this.addAttr("moveLineList.analyticMoveLineList", "hidden", true, attrsMap);

        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = "moveLineList.axis" + i + "AnalyticAccount";
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
}
