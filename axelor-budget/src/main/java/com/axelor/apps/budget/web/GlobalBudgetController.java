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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.export.ExportBudgetCallableService;
import com.axelor.apps.budget.service.BudgetComputeHiddenDateService;
import com.axelor.apps.budget.service.BudgetVersionService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetGroupService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetResetToolService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetWorkflowService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Map;

public class GlobalBudgetController {

  @ErrorException
  public void checkBudgetDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    if (globalBudget != null) {
      Beans.get(GlobalBudgetService.class).validateDates(globalBudget);
    }
  }

  @ErrorException
  public void setDates(ActionRequest request, ActionResponse response) throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

    Beans.get(GlobalBudgetService.class).updateGlobalBudgetDates(globalBudget);
    response.setReload(true);
  }

  @ErrorException
  public void validateChildren(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetWorkflowService.class)
        .validateChildren(globalBudget, GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID);
    response.setValues(globalBudget);
  }

  public void archiveChildren(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetWorkflowService.class).archiveChildren(globalBudget);
    response.setValues(globalBudget);
  }

  public void draftChildren(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetWorkflowService.class).draftChildren(globalBudget);
    response.setReload(true);
  }

  @ErrorException
  public void validateStructure(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

    Beans.get(GlobalBudgetGroupService.class).validateStructure(globalBudget);
    response.setValues(globalBudget);
  }

  public void createNewBudgetVersion(ActionRequest request, ActionResponse response) {
    Map<String, Object> partialGlobalBudget =
        (Map<String, Object>) request.getContext().get("_globalBudget");
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(Long.valueOf(partialGlobalBudget.get("id").toString()));
    if (globalBudget != null) {
      String versionName = (String) request.getContext().get("name");
      BudgetVersion budgetVersion =
          Beans.get(BudgetVersionService.class).createNewVersion(globalBudget, versionName);

      response.setView(
          ActionView.define(I18n.get("Budget version"))
              .model(BudgetVersion.class.getName())
              .add("grid", "budget-version-grid")
              .add("form", "budget-version-form")
              .context("_showRecord", String.valueOf(budgetVersion.getId()))
              .map());
      response.setCanClose(true);
    }
  }

  public void createDefaultBudgetVersion(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

      if (globalBudget != null
          && globalBudget.getId() != null
          && globalBudget.getActiveVersion() == null) {
        globalBudget = Beans.get(GlobalBudgetRepository.class).find(globalBudget.getId());
        BudgetVersion budgetVersion =
            Beans.get(BudgetVersionService.class)
                .createNewVersion(globalBudget, globalBudget.getName());
        globalBudget =
            Beans.get(GlobalBudgetService.class)
                .changeBudgetVersion(globalBudget, budgetVersion, false);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void changeBudgetVersion(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> partialGlobalBudget =
        (Map<String, Object>) request.getContext().get("_globalBudget");
    Map<String, Object> partialBudgetVersion =
        (Map<String, Object>) request.getContext().get("budgetVersion");
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(Long.valueOf(partialGlobalBudget.get("id").toString()));
    BudgetVersion selectedVersion =
        Beans.get(BudgetVersionRepository.class)
            .find(Long.valueOf(partialBudgetVersion.get("id").toString()));
    globalBudget =
        Beans.get(GlobalBudgetService.class)
            .changeBudgetVersion(globalBudget, selectedVersion, true);
  }

  public void setBudgetVersionDomain(ActionRequest request, ActionResponse response) {
    Map<String, Object> partialGlobalBudget =
        (Map<String, Object>) request.getContext().get("_globalBudget");
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(Long.valueOf(partialGlobalBudget.get("id").toString()));
    String domain = "self.id = 0";
    if (globalBudget != null) {
      domain =
          String.format(
              "self.isActive = false  AND self.globalBudget.id = %s", globalBudget.getId());
    }
    response.setAttr("$budgetVersion", "domain", domain);
  }

  public void clearBudgetList(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetResetToolService.class).clearBudgetList(globalBudget);
    globalBudget.setBudgetList(globalBudget.getBudgetList());
    Beans.get(GlobalBudgetService.class).computeTotals(globalBudget);
    response.setValues(globalBudget);
  }

  @ErrorException
  public void exportBudgetLevel(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    Long globalBudgetId = Long.valueOf(String.valueOf(context.get("_id")));
    Object advancedExportGlobalBudgetObj = context.get("advancedExportGlobalBudget");
    Object advancedExportBudgetLevelObj = context.get("advancedExportBudgetLevel");
    Object advancedExportBudgetObj = context.get("advancedExportBudget");
    Object advancedExportBudgetLineObj = context.get("advancedExportBudgetLine");

    if (globalBudgetId != null
        && advancedExportGlobalBudgetObj != null
        && advancedExportBudgetLevelObj != null
        && advancedExportBudgetObj != null
        && advancedExportBudgetLineObj != null) {
      AdvancedExportRepository advancedExportRepository = Beans.get(AdvancedExportRepository.class);
      GlobalBudget globalBudget = Beans.get(GlobalBudgetRepository.class).find(globalBudgetId);
      AdvancedExport advancedExportGlobalBudget =
          advancedExportRepository.find(
              Long.valueOf(
                  String.valueOf(((Map<String, Object>) advancedExportGlobalBudgetObj).get("id"))));
      AdvancedExport advancedExportBudgetLevel =
          advancedExportRepository.find(
              Long.valueOf(
                  String.valueOf(((Map<String, Object>) advancedExportBudgetLevelObj).get("id"))));
      AdvancedExport advancedExportBudget =
          advancedExportRepository.find(
              Long.valueOf(
                  String.valueOf(((Map<String, Object>) advancedExportBudgetObj).get("id"))));
      AdvancedExport advancedExportBudgetLine =
          advancedExportRepository.find(
              Long.valueOf(
                  String.valueOf(((Map<String, Object>) advancedExportBudgetLineObj).get("id"))));

      String language = AuthUtils.getUser().getLanguage();
      if (language == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get("Please select a language on user form."));
      }

      ExportBudgetCallableService exportGlobalBudgetLevelService =
          Beans.get(ExportBudgetCallableService.class);
      exportGlobalBudgetLevelService.initialize(
          globalBudget,
          advancedExportGlobalBudget,
          advancedExportBudgetLevel,
          advancedExportBudget,
          advancedExportBudgetLine);

      ControllerCallableTool<MetaFile> controllerCallableTool = new ControllerCallableTool<>();

      downloadExportFile(
          response,
          controllerCallableTool.runInSeparateThread(exportGlobalBudgetLevelService, response));
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

  public void initializeValues(ActionRequest request, ActionResponse response) {
    response.setValues(Beans.get(GlobalBudgetGroupService.class).getOnNewValuesMap());
  }

  public void hideAmounts(ActionRequest request, ActionResponse response) {
    response.setAttrs(Beans.get(GlobalBudgetToolsService.class).manageHiddenAmounts(true));
  }

  public void showAmounts(ActionRequest request, ActionResponse response) {
    response.setAttrs(Beans.get(GlobalBudgetToolsService.class).manageHiddenAmounts(false));
  }

  @ErrorException
  public void showUpdateDatesBtn(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    boolean isHidden = Beans.get(BudgetComputeHiddenDateService.class).isHidden(globalBudget);
    response.setAttr("updateDatesBtn", "hidden", isHidden);
  }

  public void computeAmounts(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetService.class).computeTotals(globalBudget);
    response.setValue("totalAmountExpected", globalBudget.getTotalAmountExpected());
    response.setValue("totalAmountAvailable", globalBudget.getTotalAmountAvailable());
  }
}
