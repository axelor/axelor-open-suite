package com.axelor.apps.budget.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetGeneratorRepository;
import com.axelor.apps.budget.db.repo.BudgetStructureRepository;
import com.axelor.apps.budget.service.BudgetScenarioLineService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Request;
import com.axelor.utils.db.Wizard;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BudgetGeneratorController {

  public void generateGlobalBudget(ActionRequest request, ActionResponse response) {
    try {
      BudgetGenerator budgetGenerator = getBudgetGenerator(request);

      Year year = getYear(request);

      if (budgetGenerator == null) {
        BudgetStructure budgetStructure = getBudgetStructure(request);
        if (budgetStructure != null && !budgetStructure.getIsScenario()) {
          budgetGenerator = new BudgetGenerator();
          budgetGenerator.setBudgetStructure(budgetStructure);
          budgetGenerator.setCompany(budgetStructure.getCompany());
          budgetGenerator.setCode(budgetStructure.getCode());
          budgetGenerator.setName(budgetStructure.getName());
        }
      }

      if (budgetGenerator != null && year != null) {
        GlobalBudget globalBudget =
            Beans.get(GlobalBudgetService.class).generateGlobalBudget(budgetGenerator, year);
        if (globalBudget != null) {
          response.setView(
              ActionView.define(I18n.get("Global budget"))
                  .model(GlobalBudget.class.getName())
                  .add("grid", "global-budget-grid")
                  .add("form", "global-budget-form")
                  .domain(String.format("self.id = %s", globalBudget.getId()))
                  .context("_showRecord", String.valueOf(globalBudget.getId()))
                  .context(
                      "_budgetTypeSelect",
                      budgetGenerator.getBudgetStructure().getBudgetTypeSelect())
                  .map());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void visualizeAmounts(ActionRequest request, ActionResponse response) {
    try {
      BudgetGenerator budgetGenerator = request.getContext().asType(BudgetGenerator.class);

      if (budgetGenerator == null
          || budgetGenerator.getBudgetStructure() == null
          || budgetGenerator.getBudgetScenario() == null) {
        return;
      }

      List<Map<String, Object>> budgetScenarioLineList =
          Beans.get(GlobalBudgetService.class).visualizeVariableAmounts(budgetGenerator);

      List<Integer> fiscalYears =
          Beans.get(BudgetScenarioLineService.class)
              .getFiscalYears(budgetGenerator.getBudgetScenario());

      response.setValue("$budgetScenarioLine", budgetScenarioLineList);

      Set<Year> targetYears = budgetGenerator.getYearSet();

      List<Integer> positions = new ArrayList<>();

      for (Year targetYear : targetYears) {

        int myYear = targetYear.getFromDate().getYear();

        int position = fiscalYears.indexOf(myYear);

        positions.add(position);
      }
      Collections.sort(positions);

      if (positions.size() > 0) {
        for (int i : positions) {
          if (i >= 0) {
            String fieldName = "$budgetScenarioLine.year" + (i + 1) + "Value";
            response.setAttr(fieldName, "hidden", false);
            response.setAttr(fieldName, "title", Integer.toString(fiscalYears.get(i)));
          }
        }
      }
      response.setAttr("linePanel", "hidden", false);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void popupView(ActionRequest request, ActionResponse response) {
    BudgetGenerator budgetGenerator = null;
    BudgetStructure budgetStructure = null;
    if (BudgetGenerator.class.equals(request.getContext().getContextClass())) {
      budgetGenerator = request.getContext().asType(BudgetGenerator.class);
    } else if (BudgetStructure.class.equals(request.getContext().getContextClass())) {
      budgetStructure = request.getContext().asType(BudgetStructure.class);
    }

    ActionViewBuilder generateView =
        ActionView.define("Budget Generator")
            .model(Wizard.class.getName())
            .add("form", "global-budget-generator-wizard-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .param("forceEdit", "true")
            .context("_budgetGenerator", budgetGenerator)
            .context("_budgetStructure", budgetStructure);

    response.setView(generateView.map());
  }

  public void getYearDomain(ActionRequest request, ActionResponse response) {
    BudgetGenerator budgetGenerator = getBudgetGenerator(request);
    BudgetStructure budgetStructure = getBudgetStructure(request);
    String domain = String.format("self.typeSelect = %d ", YearRepository.TYPE_FISCAL);
    if (budgetGenerator != null && !ObjectUtils.isEmpty(budgetGenerator.getYearSet())) {
      domain =
          String.format(
              "self.id IN (%s) ",
              Joiner.on(",")
                  .join(
                      budgetGenerator.getYearSet().stream()
                          .map(Year::getId)
                          .collect(Collectors.toList())));
    } else if (budgetStructure != null && budgetStructure.getCompany() != null) {
      domain =
          domain.concat(
              String.format(" AND self.company.id = %d", budgetStructure.getCompany().getId()));
    }

    response.setAttr("$year", "domain", domain);
  }

  public BudgetGenerator getBudgetGenerator(Request request) {
    Map<String, Object> partialBudgetGenerator =
        (Map<String, Object>) request.getContext().get("_budgetGenerator");
    if (partialBudgetGenerator != null && partialBudgetGenerator.get("id") != null) {
      return Beans.get(BudgetGeneratorRepository.class)
          .find(Long.valueOf(partialBudgetGenerator.get("id").toString()));
    }
    return null;
  }

  public BudgetStructure getBudgetStructure(Request request) {
    Map<String, Object> partialBudgetStructure =
        (Map<String, Object>) request.getContext().get("_budgetStructure");
    if (partialBudgetStructure != null && partialBudgetStructure.get("id") != null) {
      return Beans.get(BudgetStructureRepository.class)
          .find(Long.valueOf(partialBudgetStructure.get("id").toString()));
    }
    return null;
  }

  public Year getYear(Request request) {
    Map<String, Object> partialYear = (Map<String, Object>) request.getContext().get("year");
    if (partialYear != null && partialYear.get("id") != null) {
      return Beans.get(YearRepository.class).find(Long.valueOf(partialYear.get("id").toString()));
    }
    return null;
  }
}
