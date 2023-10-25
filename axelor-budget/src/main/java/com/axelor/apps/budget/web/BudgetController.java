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
package com.axelor.apps.budget.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.export.ExportGlobalBudgetLevelService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class BudgetController {

  public void computeTotalAmount(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      response.setValue(
          "totalAmountExpected", Beans.get(BudgetService.class).computeTotalAmount(budget));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void exportBudgetLevel(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      if (context.get("_id") == null || Long.valueOf(String.valueOf(context.get("_id"))) == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(BudgetExceptionMessage.BUDGET_IS_MISSING));
      }
      Long budgetId = Long.valueOf(String.valueOf(context.get("_id")));
      Budget budget = Beans.get(BudgetRepository.class).find(budgetId);
      Object advancedExportBudgetObj = context.get("advancedExportBudget");
      Object advancedExportPurchasedOrderLineObj = context.get("advancedExportPurchaseOrderLine");
      if (budget != null
          && advancedExportBudgetObj != null
          && advancedExportPurchasedOrderLineObj != null) {
        BudgetLevel budgetLevel =
            Beans.get(BudgetLevelRepository.class).find(budget.getBudgetLevel().getId());
        AdvancedExportRepository advancedExportRepository =
            Beans.get(AdvancedExportRepository.class);
        AdvancedExport advancedExportBudget =
            advancedExportRepository.find(
                Long.valueOf(
                    String.valueOf(((Map<String, Object>) advancedExportBudgetObj).get("id"))));
        AdvancedExport advancedExportPurchasedOrderLine =
            advancedExportRepository.find(
                Long.valueOf(
                    String.valueOf(
                        ((Map<String, Object>) advancedExportPurchasedOrderLineObj).get("id"))));

        Beans.get(BudgetLevelController.class)
            .downloadExportFile(
                response,
                Beans.get(ExportGlobalBudgetLevelService.class)
                    .export(budgetLevel, advancedExportBudget, advancedExportPurchasedOrderLine));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void generatePeriods(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      response.setValue("budgetLineList", Beans.get(BudgetService.class).generatePeriods(budget));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeToBeCommittedAndFirmGap(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      BudgetService budgetService = Beans.get(BudgetService.class);
      response.setValue("totalFirmGap", budgetService.computeFirmGap(budget));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageBudgetKey(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext() == null) {
        return;
      }

      Budget budget = request.getContext().asType(Budget.class);
      Company company = getGlobalCompany(request, response);

      if (company != null && company.getId() != null) {
        company = Beans.get(CompanyRepository.class).find(company.getId());
        boolean checkBudgetKey = Beans.get(BudgetService.class).checkBudgetKeyInConfig(company);
        response.setAttr("accountSet", "hidden", !checkBudgetKey);
        response.setAttr("analyticAxis", "hidden", !checkBudgetKey);
        response.setAttr("analyticAccount", "hidden", !checkBudgetKey);
        response.setAttr("budgetKey", "hidden", !checkBudgetKey);

        response.setAttr("analyticAxis", "required", checkBudgetKey);
        response.setAttr("analyticAccount", "required", checkBudgetKey);

        response.setAttr(
            "accountSet",
            "required",
            checkBudgetKey
                && budget.getTypeSelect() != null
                && BudgetRepository.BUDGET_TYPE_SELECT_BUDGET.equals(budget.getTypeSelect()));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      Long companyId = 0L;
      int budgetType = 0;

      GlobalBudget globalBudget =
          Optional.ofNullable(budget.getBudgetLevel())
              .map(BudgetLevel::getParentBudgetLevel)
              .map(BudgetLevel::getGlobalBudget)
              .orElse(null);
      if (globalBudget != null && globalBudget.getCompany() != null) {
        companyId = globalBudget.getCompany().getId();

        budgetType = globalBudget.getBudgetTypeSelect();
      } else {
        globalBudget =
            Optional.ofNullable(request.getContext())
                .map(Context::getParent)
                .map(Context::getParent)
                .map(Context::getParent)
                .map(context -> context.asType(GlobalBudget.class))
                .orElse(null);
        if (globalBudget != null && globalBudget.getCompany() != null) {
          companyId = globalBudget.getCompany().getId();

          budgetType = globalBudget.getBudgetTypeSelect();
        }
      }

      response.setAttr(
          "accountSet",
          "domain",
          "self.id IN ("
              + Beans.get(BudgetService.class).getAccountIdList(companyId, budgetType)
              + ")");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void managePeriodFields(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      boolean periodNotHidden = budget != null && budget.getId() != null;

      response.setAttr(
          "periodsGenerationAssistantPanel",
          "hidden",
          !periodNotHidden || budget.getStatusSelect() == BudgetRepository.STATUS_VALIDATED);
      response.setAttr("budgetLineListPanel", "hidden", !periodNotHidden);
      response.setAttr("budgetLineList", "hidden", !periodNotHidden);
      response.setAttr("$periodNotAvailable", "hidden", periodNotHidden);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createBudgetKey(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      Beans.get(BudgetService.class).createBudgetKey(budget);
      response.setValues(budget);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkDatesOnBudget(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      Beans.get(BudgetService.class).checkDatesOnBudget(budget);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setAnalyticAxisDomain(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      if (budget.getAnalyticAccount() != null) {
        response.setValue("analyticAccount", null);
      } else {
        Company company = getGlobalCompany(request, response);

        List<Long> idList = Beans.get(BudgetService.class).getAnalyticAxisInConfig(company);

        if (CollectionUtils.isNotEmpty(idList)) {
          response.setAttr(
              "analyticAxis", "domain", "self.id in (" + Joiner.on(",").join(idList) + ")");
        } else {
          response.setAttr("analyticAxis", "domain", "self.id in (0)");
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      if (budget.getAnalyticAxis() != null) {
        response.setAttr(
            "analyticAccount",
            "domain",
            "self.analyticAxis.id = " + budget.getAnalyticAxis().getId());
      } else {
        Company company = getGlobalCompany(request, response);

        List<Long> idList = Beans.get(BudgetService.class).getAnalyticAxisInConfig(company);

        if (CollectionUtils.isNotEmpty(idList)) {
          response.setAttr(
              "analyticAccount",
              "domain",
              "self.analyticAxis.id in (" + Joiner.on(",").join(idList) + ")");
        } else {
          response.setAttr("analyticAccount", "domain", "self.id in (0)");
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public Company getGlobalCompany(ActionRequest request, ActionResponse response) {
    Budget budget = request.getContext().asType(Budget.class);
    GlobalBudget globalBudget =
        Optional.ofNullable(budget.getBudgetLevel())
            .map(BudgetLevel::getParentBudgetLevel)
            .map(BudgetLevel::getGlobalBudget)
            .orElse(null);
    if (globalBudget != null && globalBudget.getCompany() != null) {
      return globalBudget.getCompany();
    } else if (budget.getId() != null) {
      globalBudget =
          Optional.ofNullable(request.getContext())
              .map(Context::getParent)
              .map(Context::getParent)
              .map(Context::getParent)
              .map(context -> context.asType(GlobalBudget.class))
              .orElse(null);
      if (globalBudget != null && globalBudget.getCompany() != null) {
        return globalBudget.getCompany();
      }
    }
    return null;
  }
}
