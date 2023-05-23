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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

  @Override
  public Map<String, Map<String, Object>> getHiddenAttributeValues(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();

    mapResult.put("moveLineList.counter", new HashMap<>());
    mapResult.put("moveLineList.amountRemaining", new HashMap<>());
    mapResult.put("moveLineList.reconcileGroup", new HashMap<>());
    mapResult.put("moveLineList.partner", new HashMap<>());

    mapResult.get("moveLineList.partner").put("hidden", move.getPartner() != null);
    mapResult
        .get("moveLineList.counter")
        .put(
            "hidden",
            move.getStatusSelect() == null || move.getStatusSelect() == MoveRepository.STATUS_NEW);
    mapResult
        .get("moveLineList.amountRemaining")
        .put(
            "hidden",
            move.getStatusSelect() == null
                || move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED
                || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED);
    mapResult
        .get("moveLineList.reconcileGroup")
        .put(
            "hidden",
            move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED);
    return mapResult;
  }

  @Override
  public boolean isHiddenMoveLineListViewer(Move move) {
    boolean isHidden = true;
    if (move.getMoveLineList() != null
        && move.getStatusSelect() < MoveRepository.STATUS_ACCOUNTED) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) > 0
            || moveLine.getReconcileGroup() != null) {
          isHidden = false;
        }
      }
    }
    return isHidden;
  }

  @Override
  public Map<String, Map<String, Object>> getFunctionalOriginSelectDomain(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();
    mapResult.put("functionalOriginSelect", new HashMap<>());

    String selectionValue = null;

    if (move.getJournal() != null) {
      selectionValue =
          Optional.ofNullable(move.getJournal().getAuthorizedFunctionalOriginSelect()).orElse("0");
    }
    mapResult.get("functionalOriginSelect").put("selection-in", selectionValue);

    return mapResult;
  }

  @Override
  public Map<String, Map<String, Object>> getMoveLineAnalyticAttrs(Move move)
      throws AxelorException {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> resultMap = new HashMap<>();

    if (move.getCompany() != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());
      if (accountConfig != null
          && appAccountService.getAppAccount().getManageAnalyticAccounting()
          && accountConfig.getManageAnalyticAccounting()) {
        AnalyticAxis analyticAxis = null;
        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = "moveLineList.axis" + i + "AnalyticAccount";
          resultMap.put(analyticAxisKey, new HashMap<>());
          resultMap
              .get(analyticAxisKey)
              .put("hidden", !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
          for (AnalyticAxisByCompany analyticAxisByCompany :
              accountConfig.getAnalyticAxisByCompanyList()) {
            if (analyticAxisByCompany.getSequence() + 1 == i) {
              analyticAxis = analyticAxisByCompany.getAnalyticAxis();
            }
          }
          if (analyticAxis != null) {
            resultMap.get(analyticAxisKey).put("title", analyticAxis.getName());
            analyticAxis = null;
          }
        }
      } else {
        resultMap.put("moveLineList.analyticDistributionTemplate", new HashMap<>());
        resultMap.get("moveLineList.analyticDistributionTemplate").put("hidden", true);
        resultMap.put("moveLineList.analyticMoveLineList", new HashMap<>());
        resultMap.get("moveLineList.analyticMoveLineList").put("hidden", true);
        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = "moveLineList.axis" + i + "AnalyticAccount";
          resultMap.put(analyticAxisKey, new HashMap<>());
          resultMap.get(analyticAxisKey).put("hidden", true);
        }
      }
    }
    return resultMap;
  }

  @Override
  public boolean isHiddenDueDate(Move move) {

    return !moveInvoiceTermService.displayDueDate(move);
  }
}
