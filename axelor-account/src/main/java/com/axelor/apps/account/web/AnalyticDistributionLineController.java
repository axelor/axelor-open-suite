/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.analytic.AnalyticDistributionLineService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.base.db.Company;
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

  public void setAnalyticAxisDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Context context = request.getContext();
      AnalyticDistributionLine analyticDistributionLine =
          context.asType(AnalyticDistributionLine.class);
      Company company = null;
      if (context.getParent() != null && context.getParent().get("company") != null) {
        company = (Company) context.getParent().get("company");
      }
      response.setAttr(
          "analyticAxis",
          "domain",
          Beans.get(AnalyticDistributionLineService.class)
              .getAxisDomain(analyticDistributionLine, company));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
