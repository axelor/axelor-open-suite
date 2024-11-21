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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticAxisRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AnalyticAxisControlService;
import com.axelor.apps.account.service.analytic.AnalyticAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class AnalyticAxisController {

  public void checkCompanyOnMoveLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {

      AnalyticAxis analyticAxis = request.getContext().asType(AnalyticAxis.class);

      if (analyticAxis != null && analyticAxis.getCompany() != null) {
        if (Beans.get(AnalyticAxisService.class).checkCompanyOnMoveLine(analyticAxis)) {
          response.setError(
              I18n.get(
                  "This axis already contains Analytic Move Lines attached to several companies. Please make sure to correctly reassign the analytic move lines currently attached to this axis to another axis before being able to assign other."));
          response.setValue("company", null);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void controlUnicity(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAxis analyticAxis = request.getContext().asType(AnalyticAxis.class);
      Beans.get(AnalyticAxisControlService.class).controlUnicity(analyticAxis);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setReadOnly(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAxis analyticAxis =
          Beans.get(AnalyticAxisRepository.class)
              .find(request.getContext().asType(AnalyticAxis.class).getId());
      if (analyticAxis != null) {
        Boolean isInMove =
            Beans.get(AnalyticAxisControlService.class).isInAnalyticMoveLine(analyticAxis);
        response.setAttr("name", "readonly", isInMove);
        response.setAttr("code", "readonly", isInMove);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void removeSameAnalyticGrouping(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAxis analyticAxis = request.getContext().asType(AnalyticAxis.class);
      String analyticGroupingChanged = request.getContext().get("_source").toString();
      List<Integer> analyticGroupingToRemoveList =
          Beans.get(AnalyticAxisService.class)
              .getSameAnalyticGroupingValues(analyticAxis, analyticGroupingChanged);
      for (Integer value : analyticGroupingToRemoveList) {
        response.setValue("analyticGrouping" + value, null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticAccountCompany(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAxis analyticAxis = request.getContext().asType(AnalyticAxis.class);
      if (analyticAxis.getId() != null
          && analyticAxis.getCompany() != null
          && !analyticAxis
              .getCompany()
              .equals(
                  Beans.get(AnalyticAxisRepository.class)
                      .find(analyticAxis.getId())
                      .getCompany())) {
        List<AnalyticAccount> childrenList =
            Beans.get(AnalyticAccountRepository.class).findByAnalyticAxis(analyticAxis).fetch();

        if (Beans.get(AnalyticAccountService.class)
            .checkChildrenAccount(analyticAxis.getCompany(), childrenList)) {
          response.setError(
              I18n.get(AccountExceptionMessage.ANALYTIC_AXIS_ACCOUNT_ERROR_ON_COMPANY));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
