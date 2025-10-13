/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetGroupService;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BudgetController {

  public void computeTotalAmount(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      BigDecimal totalAmount = Beans.get(BudgetService.class).computeTotalAmount(budget);
      response.setValue("totalAmountExpected", totalAmount);
      response.setValue("availableAmount", totalAmount);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
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
      GlobalBudget globalBudget = getGlobalBudget(request);
      Company company = null;
      if (globalBudget != null) {
        company = globalBudget.getCompany();
      }

      if (company != null) {

        boolean checkBudgetKey =
            Beans.get(BudgetToolsService.class).checkBudgetKeyInConfig(company);
        response.setAttr("budgetKeyPanel", "hidden", !checkBudgetKey);
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

      GlobalBudget globalBudget = getGlobalBudget(request);
      if (globalBudget != null && globalBudget.getCompany() != null) {
        companyId = globalBudget.getCompany().getId();

        budgetType = globalBudget.getBudgetTypeSelect();
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
      GlobalBudget globalBudget = getGlobalBudget(request);
      Company company = null;
      if (globalBudget != null) {
        company = globalBudget.getCompany();
      }

      Beans.get(BudgetService.class).createBudgetKey(budget, company);
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
        GlobalBudget globalBudget = getGlobalBudget(request);
        BudgetStructure budgetStructure = getBudgetStructure(request);
        Company company = null;
        if (globalBudget != null) {
          company = globalBudget.getCompany();
        } else if (budgetStructure != null) {
          company = budgetStructure.getCompany();
        }

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
        GlobalBudget globalBudget = getGlobalBudget(request);
        BudgetStructure budgetStructure = getBudgetStructure(request);
        Company company = null;
        if (globalBudget != null) {
          company = globalBudget.getCompany();
        } else if (budgetStructure != null) {
          company = budgetStructure.getCompany();
        }

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

  public void getBudgetLevelDomain(ActionRequest request, ActionResponse response) {
    Budget budget = request.getContext().asType(Budget.class);
    GlobalBudget globalBudget = getGlobalBudget(request);

    if (globalBudget != null) {
      List<BudgetLevel> budgetLevelList =
          Beans.get(BudgetLevelService.class).getLastSections(globalBudget);
      List<Long> idList = Arrays.asList(0L);
      if (!ObjectUtils.isEmpty(budgetLevelList)) {
        idList = budgetLevelList.stream().map(BudgetLevel::getId).collect(Collectors.toList());
      }
      response.setAttr(
          "budgetLevel", "domain", String.format("self.id in (%s)", Joiner.on(",").join(idList)));
    }
  }

  public void initializeValues(ActionRequest request, ActionResponse response) {
    Budget budget = request.getContext().asType(Budget.class);
    Context parentContext = request.getContext().getParent();
    GlobalBudget globalBudget = getGlobalBudget(request);
    BudgetStructure budgetStructure = getBudgetStructure(request);
    BudgetLevel parent = null;
    if (parentContext != null) {
      if (BudgetLevel.class.equals(parentContext.getContextClass())) {
        parent = parentContext.asType(BudgetLevel.class);
      } else if (globalBudget == null
          && GlobalBudget.class.equals(parentContext.getContextClass())) {
        globalBudget = parentContext.asType(GlobalBudget.class);
      } else if (budgetStructure == null
          && BudgetStructure.class.equals(parentContext.getContextClass())) {
        budgetStructure = parentContext.asType(BudgetStructure.class);
      }
    }

    if (globalBudget == null) {
      if (request.getContext().get("_globalId") != null
          && !ObjectUtils.isEmpty(request.getContext().get("_globalId").toString())) {
        String globalId = request.getContext().get("_globalId").toString();
        globalBudget = Beans.get(GlobalBudgetRepository.class).find(Long.valueOf(globalId));
      }
    }

    parent = EntityHelper.getEntity(parent);
    globalBudget = EntityHelper.getEntity(globalBudget);
    budgetStructure = EntityHelper.getEntity(budgetStructure);

    response.setValues(
        Beans.get(BudgetGroupService.class)
            .getOnNewValuesMap(
                budget,
                parent,
                globalBudget,
                budgetStructure,
                Optional.of(request.getContext())
                    .map(context -> context.get("_typeSelect"))
                    .map(Object::toString)
                    .orElse("")));
  }

  public GlobalBudget getGlobalBudget(ActionRequest request) {
    Context context = request.getContext();
    Budget budget = context.asType(Budget.class);
    GlobalBudget globalBudget =
        Beans.get(BudgetToolsService.class).getGlobalBudgetUsingBudget(budget);
    if (globalBudget != null) {
      return globalBudget;
    }
    if (context == null) {
      return null;
    }

    if (context.getOrDefault("parent", null) != null
        && GlobalBudget.class.isAssignableFrom(context.getParent().getContextClass())) {
      return context.getParent().asType(GlobalBudget.class);
    }
    if (context.getOrDefault("parent", null) != null
        && BudgetLevel.class.isAssignableFrom(context.getParent().getContextClass())) {
      return getGlobalBudgetUsingBudgetLevel(context.getParent());
    }

    return null;
  }

  protected GlobalBudget getGlobalBudgetUsingBudgetLevel(Context context) {
    if (context == null) {
      return null;
    }

    if (context.getOrDefault("parent", null) != null
        && GlobalBudget.class.isAssignableFrom(context.getParent().getContextClass())) {
      return context.getParent().asType(GlobalBudget.class);
    }

    return getGlobalBudgetUsingBudgetLevel(context.getParent());
  }

  public BudgetStructure getBudgetStructure(ActionRequest request) {
    Context context = request.getContext();
    Budget budget = context.asType(Budget.class);
    BudgetStructure budgetStructure =
        Beans.get(BudgetToolsService.class).getBudgetStructureUsingBudget(budget);
    if (budgetStructure != null) {
      return budgetStructure;
    }
    if (context.getParent() != null
        && BudgetStructure.class.isAssignableFrom(context.getParent().getContextClass())) {
      return context.getParent().asType(BudgetStructure.class);
    }
    if (context.getParent() != null
        && BudgetLevel.class.isAssignableFrom(context.getParent().getContextClass())) {
      return getBudgetStructureUsingBudgetLevel(context.getParent());
    }

    return null;
  }

  protected BudgetStructure getBudgetStructureUsingBudgetLevel(Context context) {
    if (context == null) {
      return null;
    }

    if (context.getParent() != null
        && BudgetStructure.class.isAssignableFrom(context.getParent().getContextClass())) {
      return context.getParent().asType(BudgetStructure.class);
    }

    return getBudgetStructureUsingBudgetLevel(context.getParent());
  }
}
