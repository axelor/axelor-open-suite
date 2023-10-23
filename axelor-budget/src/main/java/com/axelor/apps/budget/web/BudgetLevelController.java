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
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.export.ExportGlobalBudgetLevelService;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BudgetLevelController {

  public void importBudgetLevel(ActionRequest request, ActionResponse response) {

    try {
      BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
      MetaFile errorLogMetaFile = null;
      Beans.get(BudgetLevelService.class).importBudgetLevel(budgetLevel);
      if (errorLogMetaFile != null) {
        response.setAttr("$errorLogMetaFile", "value", errorLogMetaFile);
      } else {
        response.setInfo(I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_IMPORT_DATA));
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void exportBudgetLevel(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      Long budgetLevelId = Long.valueOf(String.valueOf(context.get("_id")));
      Object advancedExportBudgetObj = context.get("advancedExportBudget");
      Object advancedExportPurchasedOrderLineObj = context.get("advancedExportPurchaseOrderLine");
      if (budgetLevelId != null
          && advancedExportBudgetObj != null
          && advancedExportPurchasedOrderLineObj != null) {
        BudgetLevel budgetLevel = Beans.get(BudgetLevelRepository.class).find(budgetLevelId);
        AdvancedExport advancedExportBudget =
            Beans.get(AdvancedExportRepository.class)
                .find(
                    Long.valueOf(
                        String.valueOf(((Map<String, Object>) advancedExportBudgetObj).get("id"))));
        AdvancedExport advancedExportPurchasedOrderLine =
            Beans.get(AdvancedExportRepository.class)
                .find(
                    Long.valueOf(
                        String.valueOf(
                            ((Map<String, Object>) advancedExportPurchasedOrderLineObj)
                                .get("id"))));
        String language = AuthUtils.getUser().getLanguage();
        if (language == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get("Please select a language on user form."));
        }
        downloadExportFile(
            response,
            Beans.get(ExportGlobalBudgetLevelService.class)
                .export(budgetLevel, advancedExportBudget, advancedExportPurchasedOrderLine));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void downloadExportFile(ActionResponse response, MetaFile exportFile) {
    if (exportFile != null) {
      response.setView(
          ActionView.define(I18n.get("Export file"))
              .model(AdvancedExport.class.getName())
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + exportFile.getId()
                      + "/content/download?v="
                      + exportFile.getVersion())
              .param("download", "true")
              .map());
    }
  }

  public void setDates(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    LocalDate fromDate = budgetLevel.getFromDate();
    LocalDate toDate = budgetLevel.getToDate();
    BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevelService.getUpdatedBudgetLevelList(
          budgetLevel.getBudgetLevelList(), fromDate, toDate);
    } else if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevelService.getUpdatedBudgetList(budgetLevel.getBudgetList(), fromDate, toDate);
    }
    response.setReload(true);
  }

  public void setProjectBudget(ActionRequest request, ActionResponse response) {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    budgetLevel = Beans.get(BudgetLevelRepository.class).find(budgetLevel.getId());
    if (budgetLevel != null && budgetLevel.getProject() != null) {
      Beans.get(BudgetLevelService.class).setProjectBudget(budgetLevel);
    }
  }

  public void computeChildrenKey(ActionRequest request, ActionResponse response) {
    try {
      BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
      Beans.get(BudgetLevelService.class).computeChildrenKey(budgetLevel);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    budgetLevel = Beans.get(BudgetLevelRepository.class).find(budgetLevel.getId());
    Beans.get(BudgetLevelService.class).validateChildren(budgetLevel);
    response.setReload(true);
  }

  @ErrorException
  public void draft(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    budgetLevel = Beans.get(BudgetLevelRepository.class).find(budgetLevel.getId());
    Beans.get(BudgetLevelService.class).draftChildren(budgetLevel);
    response.setReload(true);
  }

  public void showButtonFields(ActionRequest request, ActionResponse response) {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    GlobalBudget globalBudget =
        Beans.get(BudgetToolsService.class).getGlobalBudgetUsingBudgetLevel(budgetLevel);
    if (globalBudget != null
        && globalBudget.getStatusSelect()
            != GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID_STRUCTURE) {
      return;
    }
    response.setAttr("buttonsPanel", "hidden", false);
    if (BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT.equals(
        budgetLevel.getStatusSelect())) {
      response.setAttr("validateBtn", "hidden", false);
    } else {
      response.setAttr("draftBtn", "hidden", false);
    }
  }

  public void filterBudgetLevelList(ActionRequest request, ActionResponse response) {
      BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
      GlobalBudget globalBudget =
          Beans.get(BudgetToolsService.class).getGlobalBudgetUsingBudgetLevel(budgetLevel);
      if (globalBudget != null &&
              globalBudget.getStatusSelect() == GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT) {
        return;
      }

      List<BudgetLevel> filteredBudgetLevelList =
          Beans.get(BudgetLevelService.class).getFilteredBudgetLevelList(budgetLevel);

      response.setValue("budgetLevelList", filteredBudgetLevelList);
  }

  public void saveOnChange(ActionRequest request, ActionResponse response) {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    List<BudgetLevel> finalList = new ArrayList<>();
    BudgetLevelRepository budgetLevelRepository = Beans.get(BudgetLevelRepository.class);
    BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);
    for (BudgetLevel level : budgetLevel.getBudgetLevelList()) {
      List<BudgetLevel> myList = level.getBudgetLevelList();
      if (level.getId() != null) {
        BudgetLevel myBudgetLevel = budgetLevelRepository.find(level.getId());
        List<BudgetLevel> list = budgetLevelService.getOtherUsersBudgetLevelList(myBudgetLevel);
        Set<BudgetLevel> uniqueSet = new HashSet<>(myList);
        uniqueSet.addAll(list);
        List<BudgetLevel> combinedList = new ArrayList<>(uniqueSet);
        level.setBudgetLevelList(combinedList);
      }
      finalList.add(level);
    }
    budgetLevel.setBudgetLevelList(finalList);
    response.setValues(budgetLevel);
  }
}
