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

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticLine;
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
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

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
    AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
    Company company = null;
    if (analyticMoveLine.getAnalyticJournal() != null
        && analyticMoveLine.getAnalyticJournal().getCompany() != null) {
      company = analyticMoveLine.getAnalyticJournal().getCompany();
    } else {
      Context parent = request.getContext().getParent();
      if (parent.getParent() != null && parent.getParent().get("company") != null) {
        company = (Company) parent.get("company");
      } else if (parent.get("move") != null && ((Move) parent.get("move")).getCompany() != null) {
        company = ((Move) parent.get("move")).getCompany();
      }
    }
    if (company != null) {
      response.setAttr(
          "analyticAxis",
          "domain",
          Beans.get(AnalyticMoveLineService.class)
              .getAnalyticAxisDomain(analyticMoveLine, company));
    }
  }

  public void setAnalyticAccountDomain(ActionRequest request, ActionResponse response) {
    String domain = "null";
    List<Long> analyticAccountIdList = new ArrayList<>();
    AnalyticDistributionLine analyticDistributionLine =
        request.getContext().asType(AnalyticDistributionLine.class);
    Context parentContext = request.getContext().getParent();
    Context grandParentContext = null;

    if (parentContext != null) {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          parentContext.asType(AnalyticDistributionTemplate.class);

      if (analyticDistributionTemplate != null
          && analyticDistributionTemplate.getCompany() != null) {
        domain =
            "(self.company is null OR self.company.id = "
                + analyticDistributionTemplate.getCompany().getId()
                + ") AND self.analyticAxis.id ";
        if (analyticDistributionLine.getAnalyticAxis() != null) {
          domain += "= " + analyticDistributionLine.getAnalyticAxis().getId();
        } else {
          domain +=
              "in ("
                  + Beans.get(AccountConfigRepository.class)
                      .findByCompany(analyticDistributionTemplate.getCompany())
                      .getAnalyticAxisByCompanyList().stream()
                      .map(it -> it.getAnalyticAxis().getId().toString())
                      .collect(Collectors.toList())
                  + ")";
        }

        grandParentContext = request.getContext().getParent().getParent();
        if (grandParentContext != null) {
          Account account = grandParentContext.asType(Account.class);
          if (Beans.get(AccountAnalyticRulesRepository.class)
                  .all()
                  .filter(
                      "self.fromAccount < "
                          + account.getId()
                          + " AND self.toAccount > "
                          + account.getId())
                  .count()
              != 0) {
            List<AnalyticAccount> analyticAccountList =
                Beans.get(AccountAnalyticRulesRepository.class)
                    .findAnalyticAccountByAccounts(account);
            if (CollectionUtils.isNotEmpty(analyticAccountList)) {
              for (AnalyticAccount analyticAccount : analyticAccountList) {
                analyticAccountIdList.add(analyticAccount.getId());
              }

              domain += " AND self.id in (";
              String idList = Joiner.on(",").join(analyticAccountIdList);
              domain += idList + ")";
            } else {
              domain += " AND self.id in (0)";
            }
          }
        }
      }
    }

    response.setAttr("analyticAccount", "domain", domain);
    response.setAttr("analyticAccountSet", "domain", domain);
  }
}
