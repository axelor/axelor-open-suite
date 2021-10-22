/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class AnalyticDistributionLineController {

  public void computeAmount(ActionRequest request, ActionResponse response) {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
      response.setValue(
          "amount", Beans.get(AnalyticMoveLineService.class).computeAmount(analyticMoveLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateLines(ActionRequest request, ActionResponse response) {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      if (!Beans.get(AnalyticMoveLineService.class)
          .validateLines(analyticDistributionTemplate.getAnalyticDistributionLineList())) {
        response.setError(
            "The distribution is wrong, some axes percentage values are higher than 100%");
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageNewAnalyticDistributionLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      MoveLine moveLine = request.getContext().getParent().asType(MoveLine.class);
      if (moveLine != null)
        response.setValue(
            "analyticJournal",
            Beans.get(AccountConfigService.class)
                .getAccountConfig(moveLine.getAccount().getCompany())
                .getAnalyticJournal());
      if (moveLine.getDate() != null) {
        response.setValue("date", moveLine.getDate());
      } else {
        response.setValue(
            "date",
            Beans.get(AppBaseService.class).getTodayDate(moveLine.getAccount().getCompany()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void calculateAmountWithPercentage(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
      MoveLine moveLine = request.getContext().getParent().asType(MoveLine.class);
      if (analyticMoveLine != null && moveLine != null) {
        response.setValue(
            "amount",
            Beans.get(MoveLineService.class).getAnalyticAmount(moveLine, analyticMoveLine));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onNewFromAccount(ActionRequest request, ActionResponse response) {

    try {
      Context parentContext = request.getContext().getParent();
      if (parentContext.getContextClass().toString().equals(Account.class.toString())) {
        Account account = parentContext.asType(Account.class);
        AnalyticDistributionTemplate analyticDistributionTemplate =
            Beans.get(AnalyticDistributionTemplateService.class)
                .createDistributionTemplateFromAccount(account);
        account.setAnalyticDistributionTemplate(analyticDistributionTemplate);
        response.setValue("analyticDistributionTemplate", analyticDistributionTemplate);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
