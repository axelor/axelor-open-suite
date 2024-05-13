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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AccountingSituationSupplychainController {

  public void computeUsedCredit(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    try {
      accountingSituation =
          Beans.get(AccountingSituationSupplychainServiceImpl.class)
              .computeUsedCredit(accountingSituation);
      response.setValue("usedCredit", accountingSituation.getUsedCredit());
    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getMessage());
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      AccountingSituation accountingSituation =
          request.getContext().asType(AccountingSituation.class);

      if (accountingSituation == null || accountingSituation.getCompany() == null) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(AnalyticAttrsService.class)
          .addAnalyticAxisAttrs(accountingSituation.getCompany(), null, attrsMap);

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      AccountingSituation accountingSituation =
          request.getContext().asType(AccountingSituation.class);

      if (accountingSituation == null || accountingSituation.getCompany() == null) {
        return;
      }

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(accountingSituation);
      response.setValues(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAccountValueMap(analyticLineModel, accountingSituation.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      AccountingSituation accountingSituation =
          request.getContext().asType(AccountingSituation.class);

      if (accountingSituation == null) {
        return;
      }

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(accountingSituation);
      response.setAttrs(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAxisDomainAttrsMap(analyticLineModel, accountingSituation.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      AccountingSituation accountingSituation =
          request.getContext().asType(AccountingSituation.class);

      if (accountingSituation == null) {
        return;
      }

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(accountingSituation);

      if (Beans.get(AnalyticLineModelService.class)
          .computeAnalyticMoveLineList(analyticLineModel, accountingSituation.getCompany())) {
        response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      AccountingSituation accountingSituation =
          request.getContext().asType(AccountingSituation.class);
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(accountingSituation);

      Beans.get(AnalyticLineModelService.class)
          .createAnalyticDistributionWithTemplate(analyticLineModel);

      response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
