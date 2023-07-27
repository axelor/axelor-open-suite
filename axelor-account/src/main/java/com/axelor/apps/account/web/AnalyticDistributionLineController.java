/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.AnalyticMoveLineQuery;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.analytic.AnalyticAccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.tool.ContextTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
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
      Class<?> parentClass = request.getContext().getParent().getContextClass();
      if (AnalyticLine.class.isAssignableFrom(parentClass)) {
        AnalyticLine parent = request.getContext().getParent().asType(AnalyticLine.class);
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
      Class<?> parentClass = request.getContext().getParent().getContextClass();
      if (AnalyticLine.class.isAssignableFrom(parentClass)) {
        AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
        AnalyticLine parent = request.getContext().getParent().asType(AnalyticLine.class);
        response.setValue(
            "amount",
            Beans.get(AnalyticLineService.class)
                .getAnalyticAmountFromParent(parent, analyticMoveLine));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticAxisDomain(ActionRequest request, ActionResponse response) {
    if (!AnalyticDistributionLine.class.isAssignableFrom(request.getContext().getContextClass())) {
      return;
    }
    AnalyticDistributionLine analyticDistributionLine =
        request.getContext().asType(AnalyticDistributionLine.class);
    Company company = null;
    Context parent = request.getContext().getParent();
    if (parent != null
        && AnalyticDistributionTemplate.class.isAssignableFrom(parent.getContextClass())) {
      company = parent.asType(AnalyticDistributionTemplate.class).getCompany();
    }
    if (company == null) {
      InvoiceLine invoiceLine =
          ContextTool.getContextParent(request.getContext(), InvoiceLine.class, 1);
      MoveLine moveLine = ContextTool.getContextParent(request.getContext(), MoveLine.class, 1);

      company =
          Beans.get(AnalyticToolService.class)
              .getParentCompany(
                  analyticDistributionLine.getAnalyticJournal(), invoiceLine, moveLine);
    }

    response.setAttr(
        "analyticAxis",
        "domain",
        Beans.get(AnalyticMoveLineService.class).getAnalyticAxisDomain(company));
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
