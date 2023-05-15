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
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MoveAttrsServiceImpl implements MoveAttrsService {

  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveAttrsServiceImpl(
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveRepository moveRepository) {
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.moveInvoiceTermService = moveInvoiceTermService;
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
  public void addPaymentConditionChangeChangeTrueValue(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$paymentConditionChange", "value", true, attrsMap);
  }

  @Override
  public void addPaymentConditionChangeChangeFalseValue(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$paymentConditionChange", "value", false, attrsMap);
  }

  @Override
  public void addHeaderChangeTrueValue(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$dateChange", "value", true, attrsMap);
  }

  @Override
  public void addHeaderChangeFalseValue(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("$headerChange", "value", false, attrsMap);
  }
}
