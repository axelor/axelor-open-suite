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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.AnalyticMoveLineQuery;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.analytic.AnalyticAccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
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
            I18n.get(
                "The configured distribution is incorrect, the sum of percentages for at least an axis is different than 100%"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageNewAnalyticDistributionLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);

      AnalyticLine parent =
          Beans.get(AnalyticControllerUtils.class)
              .getParentWithContext(request, response, analyticMoveLine);

      if (parent != null) {
        AnalyticLineService analyticMoveLineService = Beans.get(AnalyticLineService.class);
        response.setValue("analyticJournal", analyticMoveLineService.getAnalyticJournal(parent));
        response.setValue("date", analyticMoveLineService.getDate(parent));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void calculateAmountWithPercentage(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);

      AnalyticLine parent =
          Beans.get(AnalyticControllerUtils.class)
              .getParentWithContext(request, response, analyticMoveLine);
      response.setValue(
          "amount",
          Beans.get(AnalyticLineService.class)
              .getAnalyticAmountFromParent(parent, analyticMoveLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticAxisDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AnalyticDistributionLine analyticDistributionLine =
        request.getContext().asType(AnalyticDistributionLine.class);
    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticDistributionLine.getAnalyticDistributionTemplate();
    Company company = null;
    if (analyticDistributionTemplate != null && analyticDistributionTemplate.getCompany() != null) {
      company = analyticDistributionTemplate.getCompany();
    } else {
      company =
          Beans.get(AnalyticToolService.class)
              .getFieldFromContextParent(request.getContext(), "company", Company.class);
    }
    if (company != null) {
      response.setAttr(
          "analyticAxis",
          "domain",
          Beans.get(AnalyticMoveLineService.class).getAnalyticAxisDomain(company));
    }
  }

  public void setAnalyticAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAxis analyticAxis = null;
      Company company = null;
      Context parentContext = request.getContext().getParent();
      Context grandParentContext = null;
      Account account = null;
      String domain = "";
      if (AnalyticDistributionLine.class.equals(request.getContext().getContextClass())) {
        analyticAxis =
            request.getContext().asType(AnalyticDistributionLine.class).getAnalyticAxis();

        if (parentContext != null
            && AnalyticDistributionTemplate.class.equals(parentContext.getContextClass())) {
          company = parentContext.asType(AnalyticDistributionTemplate.class).getCompany();

          grandParentContext = parentContext.getParent();
          if (grandParentContext != null
              && Account.class.equals(grandParentContext.getContextClass())) {
            account = grandParentContext.asType(Account.class);
          }
        }
      } else if (AnalyticMoveLineQueryParameter.class.equals(
          request.getContext().getContextClass())) {
        analyticAxis =
            request.getContext().asType(AnalyticMoveLineQueryParameter.class).getAnalyticAxis();

        if (parentContext != null
            && AnalyticMoveLineQuery.class.equals(parentContext.getContextClass())) {
          company = parentContext.asType(AnalyticMoveLineQuery.class).getCompany();
        }
      }
      domain =
          Beans.get(AnalyticAccountService.class)
              .getAnalyticAccountDomain(company, analyticAxis, account);

      response.setAttr("analyticAccount", "domain", domain);
      response.setAttr("analyticAccountSet", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
