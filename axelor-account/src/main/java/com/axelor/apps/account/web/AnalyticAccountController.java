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
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.analytic.AnalyticAccountService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class AnalyticAccountController {

  public void setParentDomain(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAccount analyticAccount = request.getContext().asType(AnalyticAccount.class);

      if (analyticAccount != null
          && analyticAccount.getAnalyticAxis() != null
          && analyticAccount.getAnalyticLevel() != null) {
        response.setAttr(
            "parent",
            "domain",
            Beans.get(AnalyticAccountService.class).getParentDomain(analyticAccount));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void toggleStatus(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAccount analyticAccount = request.getContext().asType(AnalyticAccount.class);
      analyticAccount = Beans.get(AnalyticAccountRepository.class).find(analyticAccount.getId());

      Beans.get(AnalyticAccountService.class).toggleStatusSelect(analyticAccount);

      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setCompanyOnAxisChange(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAccount analyticAccount = request.getContext().asType(AnalyticAccount.class);
      if (analyticAccount.getAnalyticAxis() != null
          && analyticAccount.getAnalyticAxis().getCompany() != null) {
        response.setAttr("company", "readonly", true);
        response.setValue("company", analyticAccount.getAnalyticAxis().getCompany());
      } else {
        response.setAttr("company", "readonly", false);
        response.setValue("company", null);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setCompanyOnParentChange(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAccount analyticAccount = request.getContext().asType(AnalyticAccount.class);
      if (analyticAccount.getParent() != null && analyticAccount.getParent().getCompany() != null) {
        response.setAttr("company", "readonly", true);
        response.setValue("company", analyticAccount.getParent().getCompany());
      } else {
        response.setAttr("company", "readonly", false);
        response.setValue("company", null);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkChildrenCompany(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAccount analyticAccount = request.getContext().asType(AnalyticAccount.class);
      AnalyticAccountRepository analyticAccountRepository =
          Beans.get(AnalyticAccountRepository.class);

      if (analyticAccount.getCompany() != null
          && analyticAccount.getId() != null
          && !analyticAccount
              .getCompany()
              .equals(analyticAccountRepository.find(analyticAccount.getId()).getCompany())) {
        List<AnalyticAccount> childrenList =
            analyticAccountRepository.findByParent(analyticAccount).fetch();

        if (Beans.get(AnalyticAccountService.class)
            .checkChildrenAccount(analyticAccount.getCompany(), childrenList)) {
          response.setError(I18n.get(AccountExceptionMessage.ANALYTIC_ACCOUNT_ERROR_ON_COMPANY));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
